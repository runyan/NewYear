package com.imooc.run.newyear.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.imooc.run.newyear.R;
import com.imooc.run.newyear.constants.WeiBoConstants;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MusicObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoObject;
import com.sina.weibo.sdk.api.VoiceObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.exception.WeiboException;

public class WeiBoShareUtil {

    private final IWeiboShareAPI mWeiBoShareAPI;//微博微博分享接口实例
    private final Context mContext;
    private final Activity mActivity;
    private final Util util;

    private final TextObject textObj;
    private final ImageObject imageObj;

    public WeiBoShareUtil(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
        util = new Util(mContext, mActivity);

        textObj = new TextObject();
        imageObj = new ImageObject();

        mWeiBoShareAPI = WeiboShareSDK.createWeiboAPI(context, WeiBoConstants.APP_KEY); // 创建微博分享接口实例
        mWeiBoShareAPI.registerApp(); //注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
    }

    public IWeiboShareAPI getMWeiBoShareAPI() {
        return this.mWeiBoShareAPI;
    }

    /**
     * 检查微博客户端是否安装
     *
     * @return true 已安装微博客户端 false 未安装微博客户端
     */
    private boolean checkWBInstalled() {
        return mWeiBoShareAPI.isWeiboAppInstalled();
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     *
     * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
     */
    private void sendMessage(boolean hasText, boolean hasImage,
                             boolean hasWebPage, boolean hasMusic, boolean hasVideo, boolean hasVoice) {


        if (mWeiBoShareAPI.isWeiboAppSupportAPI()) {
            int supportApi = mWeiBoShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351) {
                sendMultiMessage(hasText, hasImage, hasWebPage, hasMusic, hasVideo, hasVoice);
            } else {
                sendSingleMessage(hasText, hasImage, hasWebPage, hasMusic, hasVideo);
            }
        } else {
            util.showMessage(mContext.getString(R.string.weiBo_not_supported), Toast.LENGTH_SHORT);
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
        mWeiBoShareAPI.sendRequest(mActivity, request);
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

        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        mWeiBoShareAPI.sendRequest(mActivity, request);
    }

    /**
     * 设置要分享的文本消息内容
     *
     * @param text 要分享的文本消息内容
     */
    public void setWeiBoShareText(String text) {
        textObj.text = text;
    }

    /**
     * 获得要分享的文本消息内容
     */
    private TextObject getTextObj() {
        return textObj;
    }

    /**
     * 设置要分享图片消息对象。
     */
    public void setWeiBoShareImage(Bitmap picture) {
        imageObj.setImageObject(picture);
    }

    /**
     * 获得要分享的图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject getImageObj() {
        return imageObj;
    }


    /**
     * 处理微博分享动作
     *
     * @param hasText    分享的内容是否有文本
     * @param hasImage   分享的内容是否有图片
     * @param hasWebPage 分享的内容是否有网页
     * @param hasMusic   分享的内容是否有音乐
     * @param hasVideo   分享的内容是否有视频
     */
    public void weiBoAction(boolean hasText, boolean hasImage, boolean hasWebPage,
                            boolean hasMusic, boolean hasVideo, boolean hasVoice) {
        try {
            if (!checkWBInstalled()) {
                util.showMessage(mContext.getString(R.string.weiBo_not_installed), Toast.LENGTH_SHORT);
            } else {
                sendMessage(hasText, hasImage, hasWebPage, hasMusic, hasVideo, hasVoice);
            }
        } catch (WeiboException e) {
            util.showExceptionMsg(e);
        }
    }
}
