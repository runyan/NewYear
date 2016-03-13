package com.imooc.run.newyear;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.imooc.run.newyear.Util.Util;
import com.imooc.run.newyear.Util.WeChatShareUtil;
import com.imooc.run.newyear.Util.WeiBoShareUtil;
import com.imooc.run.newyear.constants.Constants;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.constant.WBConstants;

import java.io.FileInputStream;

import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends Activity implements IWeiboHandler.Response {

    private ImageView mPhoto;
    private ImageView mAbout;
    private ImageView mHelp;

    private Button mWeChatShareTimeLine;
    private Button mWeChatShareFriend;
    private Button mWeiBoShare;

    private EditText mWishText;
    private TextView mTextLength;

    private IWeiboShareAPI mWeiBoShareAPI;//微博微博分享接口实例
    private WeiBoShareUtil weiBoShareUtil;
    private WeChatShareUtil weChatShareUtil;

    private final Context mContext = MainActivity.this;

    private boolean hasClicked = false;

    private Sensor mSensor;
    private SensorManager mSensorManager;

    private final String photoDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            + "/Camera"; //相机拍照后图片的保存位置
    private final String photoPath = photoDirPath + "/img.png";

    private final int maxTextLength = 10; //可以输入的最大文本长度

    private boolean hasShaken = false; //判断是否已经摇晃的标志位

    private Util util;

    private String[] wishTexts;//默认祝福语

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out); //设置切换动画
        setContentView(R.layout.activity_main);

        initViews(); //初始化

        verification();

        if (savedInstanceState != null) {
            mWeiBoShareAPI.handleWeiboResponse(getIntent(), this);
        }

    }

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
        mWeiBoShareAPI.handleWeiboResponse(intent, this);
    }

    @Override
    public void onBackPressed() {
        //当APP没有被kill时只显示1次启动界面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        util.exit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册监听事件
        if (null != mSensorManager) {
            mSensorManager.registerListener(mShakeWatcher, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //取消监听
        if (null != mSensorManager) {
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
        weiBoShareUtil = new WeiBoShareUtil(mContext, MainActivity.this);
        mWeiBoShareAPI = weiBoShareUtil.getMWeiBoShareAPI();
        weChatShareUtil = new WeChatShareUtil(mContext, MainActivity.this);
        util = new Util(mContext, MainActivity.this);
        util.useGesture(R.id.gestureViewMain);
        wishTexts = getResources().getStringArray(R.array.WishTextItemArray);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mWishText = (EditText) findViewById(R.id.text);
        setTextStyle();
        mWishText.addTextChangedListener(mTextWatcher); //为文本添加监听事件
        mWishText.setOnClickListener(new OnClickListener() { //输入框的点击事件
            @Override
            public void onClick(View v) {
                if (!hasClicked) {
                    util.showMessage(getString(R.string.text_enter_hint), Toast.LENGTH_SHORT);
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
        mTextLength.setText(getString(R.string.text_length_hint));

        mPhoto = (ImageView) findViewById(R.id.photo);

        mPhoto.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final ProgressDialog progressDialog = ProgressDialog.show(mContext,
                        getString(R.string.info), getString(R.string.generating));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        autoGenerate();
                    }
                }, 3000);
                return true;
            }
        });

        mPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                util.showMessage(getString(R.string.long_press_hint2), Toast.LENGTH_SHORT);
                AlertDialog.Builder builder = util.getAlertDialog();
                builder.setItems(getResources().getStringArray(R.array.GalleryItemSelectionArray), new DialogInterface.OnClickListener() { //选择图片位置
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
                                if (util.hasCamera()) {
                                    if (Util.checkStoragePermission(MainActivity.this)) {
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        Uri photoUri = util.getOutputMediaFileUri(photoDirPath, photoPath);
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                        startActivityForResult(intent, Constants.REQ_CAMERA); //拍照并保存照片
                                    } else {
                                        util.showMessage(getString(R.string.need_permission), Toast.LENGTH_SHORT);
                                    }
                                } else {
                                    util.showMessage(getString(R.string.camera_not_available), Toast.LENGTH_SHORT);
                                }
                                break;
                            }
                            case 2: {//选择默认图片
                                Intent intent = new Intent(MainActivity.this, PicSelectActivity.class);
                                startActivityForResult(intent, Constants.REQ_DEFAULT);
                                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out); //设置切换动画
                                break;
                            }
                        }
                    }
                });
                builder.show();
            }
        });

        mWeChatShareTimeLine = (Button) findViewById(R.id.wechat_share_timeline);
        mWeChatShareTimeLine.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWifiAvailabilityForWeChatShare(0);
            }
        });

        mWeChatShareFriend = (Button) findViewById(R.id.wechat_share_friend);
        mWeChatShareFriend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWifiAvailabilityForWeChatShare(1);
            }
        });

        mWeiBoShare = (Button) findViewById(R.id.weibo_share);
        mWeiBoShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWifiAvailabilityForWeiBoShare();

            }
        });

        mHelp = (ImageView) findViewById(R.id.help);
        mHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog materialDialog = new MaterialDialog(mContext);
                materialDialog.setTitle(getString(R.string.info))
                        .setMessage(getString(R.string.gesture_instruction))
                        .setCanceledOnTouchOutside(true)
                        .show();
            }
        });

        mAbout = (ImageView) findViewById(R.id.about);
        mAbout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaPlayer player = MediaPlayer.create(mContext, R.raw.happynewyear);
                player.start();
                final MaterialDialog mMaterialDialog = new MaterialDialog(mContext);
                String appVersion = util.getAppVersion();
                String aboutText = getString(R.string.version) + appVersion + "\n" + getString(R.string.reserved_right);
                mMaterialDialog.setTitle(getString(R.string.about)).setMessage(aboutText)
                        .setNegativeButton(getString(R.string.confirm), new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (player.isPlaying()) {
                                    player.stop();
                                }
                            }
                        }).show();
            }
        });
    }

    /**
     * 必要的验证
     */
    private void verification() {
        util.verifyVersion();//系统版本验证
        util.verifyDeviceType();//检查设备是否为手机
        Util.verifyStoragePermissions(MainActivity.this); //查询应用是否拥有读写存储权限，没有则询问用户是否授权
    }

    /**
     * 获得图片id，以自动生成贺卡
     *
     * @return 随机产生的图片id
     */
    private int getAutoGeneratePicId() {
        int basePicId = R.drawable.largep1;
        int random = util.getRandomNumber(Constants.DEFAULT_PIC_NUMBER);
        return random + basePicId;
    }

    /**
     * 获得默认祝福语id，以自动生成贺卡
     *
     * @return 随机产生的默认祝福语id
     */
    private int getAutoGenerateWishTextId() {
        return util.getRandomNumber(wishTexts.length);
    }

    /**
     * 随机生成贺卡
     */
    private void autoGenerate() {
        int autoPicId = getAutoGeneratePicId();
        int autoWishTextId = getAutoGenerateWishTextId();
        mPhoto.setImageDrawable(util.getDrawable(autoPicId));
        mWishText.setText(wishTexts[autoWishTextId]);
    }

    /**
     * 弹出选择祝福语对话框
     *
     * @param flag 是否可以输入祝福语
     */
    private void enterText(final boolean flag) {
        AlertDialog.Builder builder = util.getAlertDialog();
        builder.setItems(getResources().getStringArray(R.array.WishTextItemSelectionArray),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {  //选择默认祝福语
                                //默认祝福语选择对话框
                                AlertDialog.Builder selector = util.getAlertDialog();
                                selector.setTitle(getString(R.string.wish_text_selection))
                                        .setSingleChoiceItems(wishTexts, 0, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mWishText.setText(wishTexts[which]); //设置默认祝福语到输入框
                                                dialog.dismiss(); //关闭对话框
                                                util.closeInputMethod(mWishText); //选择祝福语后关闭屏幕键盘
                                            }
                                        });
                                selector.show();
                                break;
                            }
                            case 1: { //选择输入祝福语
                                if (flag) {
                                    //输入框获得焦点
                                    mWishText.setFocusable(true);
                                    mWishText.requestFocus();
                                } else {
                                    //输入祝福语不可用
                                    util.showMessage(getString(R.string.text_enter_not_available), Toast.LENGTH_SHORT);
                                }
                                break;
                            }
                        }
                    }
                });
        builder.show();
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
            if ((Math.abs(x) > threshold || Math.abs(y) > threshold
                    || Math.abs(z) > threshold) && !hasShaken) {
                if (0 != Util.getTextLength(mWishText) && !hasShaken) {
                    mWishText.setText("");
                    util.vibrate(Constants.VIBRATE_SINGLE);
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
            String str = getString(R.string.you_can_enter) + remainTextLength + getString(R.string.words);
            mTextLength.setText(str);
            if (temp.length() >= 10) {
                util.showMessage(getString(R.string.text_too_long), Toast.LENGTH_SHORT);
            }
        }
    };

    /**
     * 设置分享文本的字体，隐藏光标
     */
    private void setTextStyle() {
        mWishText.setCursorVisible(false);
        mWishText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/test.ttf"));
    }

    /**
     * 设置分享按钮是否可见
     *
     * @param visibility 可见程度
     */
    private void setVisibility(int visibility) {
        mWeChatShareFriend.setVisibility(visibility);
        mWeChatShareTimeLine.setVisibility(visibility);
        mWeiBoShare.setVisibility(visibility);
        mTextLength.setVisibility(visibility);
        mHelp.setVisibility(visibility);
        mAbout.setVisibility(visibility);
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
                    fis = new FileInputStream(photoPath); //获取照片的输入流
                    Bitmap bitmap = BitmapFactory.decodeStream(fis); //从照片的输入流中将图片解码为Bitmap
                    mPhoto.setImageBitmap(bitmap); //设置显示图片
                    //询问是否将图片保存在图库
                    final MaterialDialog mMaterialDialog = new MaterialDialog(mContext);
                    mMaterialDialog.setMessage(R.string.save_photo);
                    mMaterialDialog.setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            util.showMessage(getString(R.string.save_hint), Toast.LENGTH_SHORT);
                            mMaterialDialog.dismiss();
                        }
                    });
                    mMaterialDialog.setNegativeButton(R.string.no, new OnClickListener() {//如果不保存
                        @Override
                        public void onClick(View v) {
                            if (!Util.deleteMedia(photoPath)) {
                                util.showMessage(getString(R.string.delete_fail), Toast.LENGTH_SHORT);
                                mMaterialDialog.dismiss();
                            }
                            mMaterialDialog.dismiss();
                        }
                    });
                    mMaterialDialog.setCanceledOnTouchOutside(false);
                    mMaterialDialog.show();
                } catch (Exception e) {
                    util.showErrorMsg(e);
                } finally {
                    try {
                        if (null != fis) {
                            fis.close();
                        }
                    } catch (Exception e) {
                        util.showErrorMsg(e);
                    }
                }
            }
        }

        if (requestCode == Constants.REQ_DEFAULT) { //选择默认图片返回成功
            if (resultCode == Constants.RES_DEFAULT) {
                int picId = Integer.parseInt(data.getStringExtra("picId")); //获得所选图片id
                mPhoto.setImageDrawable(util.getDrawable(picId));
            }
        }
    }

    /**
     * 制作贺卡
     *
     * @return 生成的贺卡
     */
    private Bitmap generateSpringCard() {
        setVisibility(View.INVISIBLE); //将分享按钮设为不可见
        return Util.generateSpringCard(MainActivity.this);
    }

    /**
     * 微信分享部分
     */

    /**
     * 微信分享前检测是否使用wifi
     *
     * @param flag 分享位置 0为分享到朋友圈 1为分享给微信好友
     */
    private void checkWifiAvailabilityForWeChatShare(final int flag) {
        if (!util.checkWifiAvailability()) {
            final MaterialDialog materialDialog = new MaterialDialog(mContext);
            materialDialog.setMessage(getString(R.string.wifi_not_enabled))
                    .setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            weChatShare(flag);
                            materialDialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            materialDialog.dismiss();
                        }
                    })
                    .show();
        } else {
            weChatShare(flag);
        }
    }

    /**
     * 微信分享
     *
     * @param flag 分享位置 0为分享到朋友圈 1为分享给微信好友
     */
    private void weChatShare(int flag) {
        weChatShareUtil.weChatAction(flag, generateSpringCard());
        setVisibility(View.VISIBLE);
    }


    /**
     * 微博分享部分
     */

    /**
     * 微博分享前检测是否使用wifi
     */
    private void checkWifiAvailabilityForWeiBoShare() {
        if (!util.checkWifiAvailability()) {
            final MaterialDialog materialDialog = new MaterialDialog(mContext);
            materialDialog.setMessage(getString(R.string.wifi_not_enabled))
                    .setPositiveButton(R.string.yes, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            weiBoShare();
                            materialDialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            materialDialog.dismiss();
                        }
                    }).show();
        } else {
            weiBoShare();
        }
    }

    /**
     * 微博分享
     */
    private void weiBoShare() {
        weiBoShareUtil.setWeiBoShareText(getString(R.string.wish_text));
        weiBoShareUtil.setWeiBoShareImage(generateSpringCard());
        weiBoShareUtil.weiBoAction(true, true, false, false, false, false);
        setVisibility(View.VISIBLE);
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
        if (baseResp != null) {
            switch (baseResp.errCode) {
                case WBConstants.ErrorCode.ERR_OK:
                    util.showMessage(getString(R.string.share_success), Toast.LENGTH_LONG);
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    util.showMessage(getString(R.string.share_cancel), Toast.LENGTH_LONG);
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    String msg = getString(R.string.share_fail) + "," + getString(R.string.error) + ":" + baseResp.errMsg;
                    util.showMessage(msg, Toast.LENGTH_LONG);
                    break;
            }
        }
    }
}
