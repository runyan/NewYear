package com.imooc.run.newyear;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imooc.run.newyear.Util.Util;
import com.imooc.run.newyear.constants.Constants;
import com.imooc.run.newyear.constants.WeChatConstants;
import com.imooc.run.newyear.constants.WeiBoConstants;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MusicObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoObject;
import com.sina.weibo.sdk.api.VoiceObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;

/**
 * 微博分享： https://open.weibo.com https://github.com/sinaweibosdk/weibo_android_sdk
 * <p/>
 * 微信分享：https://open.weixin.qq.com/
 */
public class MainActivity extends Activity implements IWeiboHandler.Response {

    private ImageView mPhoto;
    private Button mWeChatShareTimeLine;
    private Button mWeChatShareFriend;
    private Button mWeiBoShare;
    private EditText mWishText;
    private TextView mTextLength;

    private IWXAPI iwxapi; //微信分享接口实例
    private IWeiboShareAPI iWeiBoShareAPI; //微博分享接口实例

    private Context mContext;
    private AlertDialog.Builder mAlertBuilder;
    private AlertDialog.Builder mSelectBuilder;

    private Sensor mSensor;
    private SensorManager mSensorManager;

    private boolean hasShaken = false; //判断是否已经摇晃的标志位

    private String mFilePath; //相机拍照后图片的保存位置

    private boolean hasClicked = false; //判断是否已被点击

    private final int maxTextLength = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mAlertBuilder = new AlertDialog.Builder(mContext);
        mSelectBuilder = new AlertDialog.Builder(mContext);

        if (!Util.versionCheck()) {
            Toast.makeText(mContext, getString(R.string.version_error), Toast.LENGTH_LONG).show();
            Util.exit(this);
        }

        initViews(); //初始化显示控件;
        Util.verifyStoragePermissions(this); //确认应用拥有读写存储权限

        //初始化保存位置
        mFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                + "/Camera" + "/" + "img.png";

        //注册app到微博，微信
        regToWX();
        regToWB();

        /**
         * 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
         * 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
         * 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
         * 失败返回 false，不调用上述回调
         */
        if (savedInstanceState != null) {
            iWeiBoShareAPI.handleWeiboResponse(getIntent(), this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.exit(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册监听事件
        if (mSensorManager != null) {
            mSensorManager.registerListener(mShakeWatcher, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //取消监听
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mShakeWatcher);
        }
    }

    /**
     *  通用部分
     */

    /**
     * 初始化显示界面
     */
    private void initViews() {
        mWishText = (EditText) findViewById(R.id.text);
        setTextStyle();
        mWishText.addTextChangedListener(mTextWatcher); //为文本添加监听事件
        mWishText.setOnClickListener(new View.OnClickListener() { //输入框的点击事件
            @Override
            public void onClick(View v) {
                if (!hasClicked) {
                    Toast.makeText(mContext, getString(R.string.text_enter_hint), Toast.LENGTH_SHORT)
                            .show();
                    hasClicked = true;
                }
                hasShaken = false;
                if (0 == Util.getTextLength(mWishText)) { //当输入框中没有文本时，弹出选择框
                    enterText(true);
                }
            }
        });

        mWishText.setOnLongClickListener(new View.OnLongClickListener() { //长按输入框清空文本
            @Override
            public boolean onLongClick(View v) {
                if (0 != Util.getTextLength(mWishText)) { //如果输入框内有内容则可以重新选择祝福语
                    enterText(false);
                }
                return true;
            }
        });

        mTextLength = (TextView) findViewById(R.id.text_length);
        mTextLength.setText(R.string.text_length_hint);

        mPhoto = (ImageView) findViewById(R.id.photo);
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertBuilder.setItems(getResources().getStringArray(R.array.GalleryItemSelectionArray), new DialogInterface.OnClickListener() { //选择图片位置
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: { //选择系统图库
                                Intent intent = new Intent(Intent.ACTION_PICK, null);
                                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(intent, Constants.REQ_ALBUM);
                                break;
                            }
                            case 1: {//选择相机
                                if (Util.checkStoragePermission(MainActivity.this)) {
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    Uri photoUri = Uri.fromFile(new File(mFilePath));
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                    startActivityForResult(intent, Constants.REQ_CAMERA); //拍照并保存照片
                                } else {
                                    Toast.makeText(mContext, "no permission", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case 2: {//选择默认图片
                                Intent intent = new Intent(MainActivity.this, PicSelectActivity.class);
                                startActivityForResult(intent, Constants.REQ_DEFAULT);
                            }
                        }
                    }
                });
                mAlertBuilder.show();
            }
        });

        mWeChatShareTimeLine = (Button) findViewById(R.id.wechat_share_timeline);
        mWeChatShareTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weChatAction(0);
            }
        });

        mWeChatShareFriend = (Button) findViewById(R.id.wechat_share_friend);
        mWeChatShareFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weChatAction(1);
            }
        });

        mWeiBoShare = (Button) findViewById(R.id.weibo_share);
        mWeiBoShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weiBoAction();
            }
        });
    }

    /**
     * 弹出选择祝福语对话框
     *
     * @param flag 是否可以输入祝福语
     */
    private void enterText(final boolean flag) {
        final String[] wishTexts = getResources().getStringArray(R.array.WishTextItemArray); //默认祝福语
        mAlertBuilder.setItems(getResources().getStringArray(R.array.WishTextItemSelectionArray),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {  //选择默认祝福语
                                //默认祝福语选择对话框
                                mSelectBuilder.setTitle(getString(R.string.wish_text_selection))
                                        .setSingleChoiceItems(wishTexts, 0, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mWishText.setText(wishTexts[which]); //设置默认祝福语到输入框
                                                dialog.dismiss(); //关闭对话框
                                                Util.closeInputMethod(mContext, mWishText); //选择祝福语后关闭屏幕键盘
                                            }
                                        });
                                mSelectBuilder.show();
                                break;
                            }
                            case 1: { //选择输入祝福语
                                if (flag) {
                                    //输入框获得焦点
                                    mWishText.setFocusable(true);
                                    mWishText.requestFocus();
                                } else {
                                    //输入祝福语不可用
                                    Toast.makeText(mContext, getString(R.string.text_enter_not_available), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        }
                    }
                });
        mAlertBuilder.show();
    }

    //摇晃设备监听类
    private final SensorEventListener mShakeWatcher = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float values[] = event.values;
            float x = values[0]; // x轴方向的重力加速度
            float y = values[1]; // y轴方向的重力加速度
            float z = values[2]; // z轴方向的重力加速度

            int threshold = 18; //这里设置的一个阈值为18，经测试比较满足一般的摇晃，也可以自己按需定义修改
            if ((Math.abs(x) > threshold || Math.abs(y) > threshold || Math
                    .abs(z) > threshold) && !hasShaken) {
                if (0 != Util.getTextLength(mWishText) && !hasShaken) {
                    mWishText.setText("");
                    Util.vibrate(MainActivity.this);
                    hasShaken = true;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //文本事件监听类
    private final TextWatcher mTextWatcher = new TextWatcher() {

        private CharSequence temp; //文本内容
        private int remainTextLength = maxTextLength; //可以输入的字符长度

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            temp = s;
            if (remainTextLength >= 0) {
                remainTextLength = maxTextLength - temp.length();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            String str = "还能输入" + remainTextLength + "个字";
            mTextLength.setText(str);
            if (temp.length() >= 10) {
                Toast.makeText(mContext, getString(R.string.text_too_long), Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 注册微信分享api
     */
    private void regToWX() {
        //通过WXAPIFactory工厂，获取IWXAPI的实例
        iwxapi = WXAPIFactory.createWXAPI(mContext, WeChatConstants.APP_ID, false);
        //将应用的appId注册到微信
        iwxapi.registerApp(WeChatConstants.APP_ID);
    }

    /**
     * 注册微博分享api
     */
    private void regToWB() {
        //实例化微博分享接口实例
        iWeiBoShareAPI = WeiboShareSDK.createWeiboAPI(mContext, WeiBoConstants.APP_KEY);
        /**
         * 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
         * NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
         */
        iWeiBoShareAPI.registerApp();
    }

    /**
     * 设置分享文本的字体，隐藏光标
     */
    private void setTextStyle() {
        mWishText.setCursorVisible(false);
        mWishText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/test.ttf"));
    }

    /**
     * 设置分享按钮为可见
     */
    private void setVisible() {
        mWeChatShareFriend.setVisibility(View.VISIBLE);
        mWeChatShareTimeLine.setVisibility(View.VISIBLE);
        mWeiBoShare.setVisibility(View.VISIBLE);
        mTextLength.setVisibility(View.VISIBLE);
    }

    /**
     * 设置分享按钮为不可见
     */
    private void setInvisible() {
        mWeChatShareFriend.setVisibility(View.INVISIBLE);
        mWeChatShareTimeLine.setVisibility(View.INVISIBLE);
        mWeiBoShare.setVisibility(View.INVISIBLE);
        mTextLength.setVisibility(View.INVISIBLE);
    }

    /**
     * 处理Activity执行结果
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) { //如果使用相机或图库返回成功
            if (requestCode == Constants.REQ_ALBUM) { //如果使用系统图库
                if (null != data) {
                    mPhoto.setImageURI(data.getData());
                }
            } else if (requestCode == Constants.REQ_CAMERA) { //如果使用相机拍照
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mFilePath); //获取照片的输入流
                    Bitmap bitmap = BitmapFactory.decodeStream(fis); //从照片的输入流中将图片解码为Bitmap
                    mPhoto.setImageBitmap(bitmap); //设置显示图片
                    //询问是否将图片保存在图库
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage(R.string.save_photo);
                    builder.setPositiveButton(R.string.yes, null);
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() { //如果不保存
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File f = new File(mFilePath);
                            if (!f.delete()) { //将图片从图库中删除
                                Toast.makeText(mContext, getString(R.string.delete_fail), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != fis) {
                            fis.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (requestCode == Constants.REQ_DEFAULT) { //选择默认图片返回成功
            if (resultCode == Constants.DEFAULT_RESULT) {
                int picId = Integer.parseInt(data.getStringExtra("picId")); //获得所选图片
                mPhoto.setImageDrawable(Util.getDrawable(mContext, picId));
            }
        }
    }

    /**
     * 制作贺卡
     *
     * @return 生成的贺卡
     */
    private Bitmap generateSpringCard() {
        setInvisible(); //将分享按钮设为不可见
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache(); //截屏并返回
    }

//    @Deprecated
//    private boolean savePhoto(Bitmap photo) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//
//        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera").toString());
//        if (!dir.exists()) {
//            if (!dir.mkdirs()) {
//                Toast.makeText(MainActivity.this, R.string.photo_taken_fail, Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        }
//        File file = new File(dir.getAbsolutePath(), System.currentTimeMillis() + ".png");
//
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(byteArray, 0, byteArray.length);
//            fos.flush();
//            fos.close();
//
//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            Uri uri = Uri.fromFile(file);
//            intent.setData(uri);
//            MainActivity.this.sendBroadcast(intent);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            return false;
//        }
//    }

    /**
     * 微信分享部分
     */

    /**
     * 判断微信是否安装
     *
     * @return true 微信已安装 false 微信未安装
     */
    private boolean checkWXInstalled() {
        return iwxapi.isWXAppInstalled();
    }

    /**
     * 处理微信分享动作
     *
     * @param flag 0为分享到朋友圈 1为分享给微信好友
     */
    private void weChatAction(int flag) {
        if (!checkWXInstalled()) {
            Toast.makeText(mContext, getString(R.string.weiChat_not_installed), Toast.LENGTH_SHORT).show();
        } else {
            weChatShare(flag);
            setVisible();
        }
    }

    /**
     * 微信分享
     *
     * @param flag 分享位置（朋友圈或微信好友）
     */
    private void weChatShare(int flag) {
        //初始化一个WXWebpageObject对象
        WXWebpageObject webPage = new WXWebpageObject();
        //用WXWebpageObject对象初始化一个WXMediaMessage 对象，填写信息
        WXMediaMessage msg = new WXMediaMessage(webPage);
        msg.mediaObject = new WXImageObject(generateSpringCard());

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis()); //transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession; //根据flag判断发送位置
        //调用api接口发送数据到微信
        iwxapi.sendReq(req);
    }

    /**
     * 微博分享部分
     */

    /**
     * @see {@link Activity#onNewIntent}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /**
         * 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
         * 来接收微博客户端返回的数据；执行成功，返回 true，并调用
         * {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
         */
        iWeiBoShareAPI.handleWeiboResponse(intent, this);
    }

    /**
     * 检查微博客户端是否安装
     *
     * @return true 已安装微博客户端 false 未安装微博客户端
     */
    private boolean checkWBInstalled() {
        return iWeiBoShareAPI.isWeiboAppInstalled();
    }

    /**
     * 处理微博分享动作
     */
    private void weiBoAction() {
        try {
            if (!checkWBInstalled()) {
                Toast.makeText(mContext, getString(R.string.weiBo_not_installed), Toast.LENGTH_SHORT).show();
            } else {
                sendMessage(true, true, false, false, false, false);
                setVisible();
            }
        } catch (WeiboException e) {
            e.printStackTrace();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 接收微客户端博请求的数据。
     * 当微博客户端唤起当前应用并进行分享时，该方法被调用。
     *
     * @param baseResp 微博请求数据对象
     * @see {@link IWeiboShareAPI#handleWeiboRequest}
     */
    @Override
    public void onResponse(BaseResponse baseResp) {
        if (null != baseResp) {
            switch (baseResp.errCode) {
                case WBConstants.ErrorCode.ERR_OK:
                    Toast.makeText(mContext, getString(R.string.share_success), Toast.LENGTH_LONG).show();
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    Toast.makeText(mContext, getString(R.string.share_cancel), Toast.LENGTH_LONG).show();
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    String errMsg = getString(R.string.share_fail) + " Error message:" + baseResp.errMsg;
                    Toast.makeText(mContext, errMsg, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     *
     * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
     */
    private void sendMessage(boolean hasText, boolean hasImage,
                             boolean hasWebPage, boolean hasMusic, boolean hasVideo, boolean hasVoice) {
        if (iWeiBoShareAPI.isWeiboAppSupportAPI()) {
            int supportAPI = iWeiBoShareAPI.getWeiboAppSupportAPI();
            if (supportAPI >= 10351) {
                sendMultiMessage(hasText, hasImage, hasWebPage, hasMusic, hasVideo, hasVoice);
            } else {
                sendSingleMessage(hasText, hasImage, hasWebPage, hasMusic, hasVideo);
            }
        } else {
            Toast.makeText(mContext, getString(R.string.weiBo_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 注意：当 {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息，
     * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
     *
     * @param hasText    分享的内容是否有文本
     * @param hasImage   分享的内容是否有图片
     * @param hasWebPage 分享的内容是否有网页
     * @param hasMusic   分享的内容是否有音乐
     * @param hasVideo   分享的内容是否有视频
     * @param hasVoice   分享的内容是否有声音
     */
    private void sendMultiMessage(boolean hasText, boolean hasImage, boolean hasWebPage,
                                  boolean hasMusic, boolean hasVideo, boolean hasVoice) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiBoMessage = new WeiboMultiMessage();
        if (hasText) {
            weiBoMessage.textObject = getTextObj();
        }
        if (hasImage) {
            weiBoMessage.imageObject = getImageObj();
        }
        // 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
        if (hasWebPage) {
            weiBoMessage.mediaObject = new WebpageObject();
        }
        if (hasMusic) {
            weiBoMessage.mediaObject = new MusicObject();
        }
        if (hasVideo) {
            weiBoMessage.mediaObject = new VideoObject();
        }
        if (hasVoice) {
            weiBoMessage.mediaObject = new VoiceObject();
        }

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiBoMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        iWeiBoShareAPI.sendRequest(MainActivity.this, request);
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 当{@link IWeiboShareAPI#getWeiboAppSupportAPI()} < 10351 时，只支持分享单条消息，即
     * 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
     *
     * @param hasText    分享的内容是否有文本
     * @param hasImage   分享的内容是否有图片
     * @param hasWebPage 分享的内容是否有网页
     * @param hasMusic   分享的内容是否有音乐
     * @param hasVideo   分享的内容是否有视频
     */
    private void sendSingleMessage(boolean hasText, boolean hasImage, boolean hasWebPage,
                                   boolean hasMusic, boolean hasVideo) {
        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种
        WeiboMessage weiboMessage = new WeiboMessage();
        if (hasText) {
            weiboMessage.mediaObject = getTextObj();
        }
        if (hasImage) {
            weiboMessage.mediaObject = getImageObj();
        }
        if (hasWebPage) {
            weiboMessage.mediaObject = new WebpageObject();
        }
        if (hasMusic) {
            weiboMessage.mediaObject = new MusicObject();
        }
        if (hasVideo) {
            weiboMessage.mediaObject = new VideoObject();
        }

        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        iWeiBoShareAPI.sendRequest(MainActivity.this, request);
    }

    /**
     * 获取分享的文本模板。
     *
     * @return 分享的文本模板
     */
    @NotNull
    private String getSharedText() {
        return getString(R.string.wish_text);
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = getSharedText();
        return textObject;
    }

    /**
     * 创建图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(generateSpringCard());
        return imageObject;
    }

}
