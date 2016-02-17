package com.imooc.run.newyear;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.imooc.run.newyear.constants.Constants;
import com.imooc.run.newyear.constants.WeChatConstants;
import com.imooc.run.newyear.constants.WeiboConstants;
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

public class MainActivity extends Activity implements IWeiboHandler.Response {

    private ImageView mPhoto;
    private Button mWeChatShareTimeLine;
    private Button mWeChatShareFriend;
    private Button mWeiboShare;
    private EditText mWishText;

    private IWXAPI iwxapi;
    private IWeiboShareAPI mWeiboShareAPI;

    private int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iwxapi = WXAPIFactory.createWXAPI(this, WeChatConstants.APP_ID, false);
        iwxapi.registerApp(WeChatConstants.APP_ID);

        initViews();

        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, WeiboConstants.APP_KEY);
        mWeiboShareAPI.registerApp();

        if(savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                Toast.makeText(this, R.string.share_success, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                Toast.makeText(this, R.string.share_cancel, Toast.LENGTH_LONG).show();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                String errMsg = R.string.share_fail + " Error message:" + baseResp.errMsg;
                Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void sendMessage(boolean hasText, boolean hasImage,
                             boolean hasWebPage, boolean hasMusic, boolean hasVideo, boolean hasVoice) {
        if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
            int supportAPI = mWeiboShareAPI.getWeiboAppSupportAPI();
            if(supportAPI >= 10351) {
                sendMultiMessage(hasText, hasImage, hasWebPage, hasMusic, hasVideo, hasVoice);
            } else {
                sendSingleMessage(hasText, hasImage, hasWebPage, hasMusic, hasVideo);
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.weibo_not_supported, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMultiMessage(boolean hasText, boolean hasImage, boolean hasWebPage,
                                  boolean hasMusic, boolean hasVideo, boolean hasVoice) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if(hasText) {
            weiboMessage.textObject = getTextObj();
        }
        if(hasImage) {
            weiboMessage.imageObject = getImageObj();
        }
        if(hasWebPage) {
            weiboMessage.mediaObject = new WebpageObject();
        }
        if(hasMusic) {
            weiboMessage.mediaObject = new MusicObject();
        }
        if(hasVideo) {
            weiboMessage.mediaObject = new VideoObject();
        }
        if(hasVoice) {
            weiboMessage.mediaObject = new VoiceObject();
        }
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        mWeiboShareAPI.sendRequest(MainActivity.this, request);
    }

    private void sendSingleMessage(boolean hasText, boolean hasImage, boolean hasWebpage,
                                  boolean hasMusic, boolean hasVideo) {
        WeiboMessage weiboMessage = new WeiboMessage();
        if(hasText) {
            weiboMessage.mediaObject = getTextObj();
        }
        if(hasImage) {
            weiboMessage.mediaObject = getImageObj();
        }
        if(hasWebpage) {
            weiboMessage.mediaObject = new WebpageObject();
        }
        if(hasMusic) {
            weiboMessage.mediaObject = new MusicObject();
        }
        if(hasVideo) {
            weiboMessage.mediaObject = new VideoObject();
        }
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        mWeiboShareAPI.sendRequest(MainActivity.this, request);
    }

    private String getSharedText() {
        return getString(R.string.wish_text);
    }

    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = getSharedText();
        return textObject;
    }

    private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(generateSpringCard());
        return imageObject;
    }

    private void setVisible() {
        mWeChatShareFriend.setVisibility(View.VISIBLE);
        mWeChatShareTimeLine.setVisibility(View.VISIBLE);
        mWeiboShare.setVisibility(View.VISIBLE);
    }

    private void setInvisible() {
        mWeChatShareFriend.setVisibility(View.INVISIBLE);
        mWeChatShareTimeLine.setVisibility(View.INVISIBLE);
        mWeiboShare.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == Constants.REQ_ALBUM) {
                if(null != data) {
                    mPhoto.setImageURI(data.getData());
                }
            } else if(requestCode == Constants.REQ_CAMERA) {
                if(null != data) {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    mPhoto.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void weChatShare(int flag) {
        WXWebpageObject webPage = new WXWebpageObject();
        WXMediaMessage msg = new WXMediaMessage(webPage);
        msg.mediaObject = new WXImageObject(generateSpringCard());

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        iwxapi.sendReq(req);
    }

    private Bitmap generateSpringCard() {
        setInvisible();
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return  view.getDrawingCache();
    }

    private void setTextStyle() {
        mWishText.setCursorVisible(false);
        mWishText.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/test.ttf"));
    }

    private void initViews() {
        mWishText = (EditText) findViewById(R.id.text);
        setTextStyle();

        mPhoto = (ImageView) findViewById(R.id.photo);
        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(getResources().getStringArray(R.array.ItemArray), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(0 == which) {
                            Intent intent = new Intent(Intent.ACTION_PICK, null);
                            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(intent, Constants.REQ_ALBUM);
                        } else {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, Constants.REQ_CAMERA);
                        }
                    }
                });
                builder.show();
            }
        });

        mWeChatShareTimeLine = (Button) findViewById(R.id.wechat_share_timeline);
        mWeChatShareTimeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 0;
                weChatShare(flag);
                setVisible();
            }
        });

        mWeChatShareFriend = (Button) findViewById(R.id.wechat_share_friend);
        mWeChatShareFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = 1;
                weChatShare(flag);
                setVisible();
            }
        });

        mWeiboShare = (Button) findViewById(R.id.weibo_share);
        mWeiboShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessage(true, true, false, false, false, false);
                    setVisible();
                } catch (WeiboException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
