package com.imooc.run.newyear.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
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

    private IWeiboShareAPI mWeiboShareAPI;//微博微博分享接口实例
    private Context mContext;
    private Activity mActivity;
    private Util util;

    public WeiBoShareUtil(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
        util = new Util(mContext, mActivity);

        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(context, WeiBoConstants.APP_KEY); // 创建微博分享接口实例
        mWeiboShareAPI.registerApp(); //注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
    }

    public IWeiboShareAPI getmWeiboShareAPI() {
        return this.mWeiboShareAPI;
    }

    /**
     * 检查微博客户端是否安装
     *
     * @return true 已安装微博客户端 false 未安装微博客户端
     */
    private boolean checkWBInstalled() {
        return mWeiboShareAPI.isWeiboAppInstalled();
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     *
     * @see {@link #sendMultiMessage} 或者 {@link #sendSingleMessage}
     */
    private void sendMessage(Bitmap picture, boolean hasText, boolean hasImage,
                             boolean hasWebpage, boolean hasMusic, boolean hasVideo, boolean hasVoice) {


        if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351) {
                sendMultiMessage(picture, hasText, hasImage, hasWebpage, hasMusic, hasVideo, hasVoice);
            } else {
                sendSingleMessage(picture, hasText, hasImage, hasWebpage, hasMusic, hasVideo);
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
     * @param picture    要分享的照片
     * @param hasText    分享的内容是否有文本
     * @param hasImage   分享的内容是否有图片
     * @param hasWebpage 分享的内容是否有网页
     * @param hasMusic   分享的内容是否有音乐
     * @param hasVideo   分享的内容是否有视频
     * @param hasVoice   分享的内容是否有声音
     */
    private void sendMultiMessage(Bitmap picture, boolean hasText, boolean hasImage, boolean hasWebpage,
                                  boolean hasMusic, boolean hasVideo, boolean hasVoice) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if (hasText) {
            weiboMessage.textObject = getTextObj();
        }

        if (hasImage) {
            weiboMessage.imageObject = getImageObj(picture);
        }

        // 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
        if (hasWebpage) {
            weiboMessage.mediaObject = new WebpageObject();
        }
        if (hasMusic) {
            weiboMessage.mediaObject = new MusicObject();
        }
        if (hasVideo) {
            weiboMessage.mediaObject = new VideoObject();
        }
        if (hasVoice) {
            weiboMessage.mediaObject = new VoiceObject();
        }

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(mActivity, request);
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 当{@link IWeiboShareAPI#getWeiboAppSupportAPI()} < 10351 时，只支持分享单条消息，即
     * 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
     *
     * @param picture    要分享的照片
     * @param hasText    分享的内容是否有文本
     * @param hasImage   分享的内容是否有图片
     * @param hasWebpage 分享的内容是否有网页
     * @param hasMusic   分享的内容是否有音乐
     * @param hasVideo   分享的内容是否有视频
     */
    private void sendSingleMessage(Bitmap picture, boolean hasText, boolean hasImage, boolean hasWebpage,
                                   boolean hasMusic, boolean hasVideo) {
        WeiboMessage weiboMessage = new WeiboMessage();
        if (hasText) {
            weiboMessage.mediaObject = getTextObj();
        }
        if (hasImage) {
            weiboMessage.mediaObject = getImageObj(picture);
        }
        if (hasWebpage) {
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

        mWeiboShareAPI.sendRequest(mActivity, request);
    }

    /**
     * 获取分享的文本模板。
     *
     * @return 分享的文本模板
     */
    @NonNull
    private String getSharedText() {
        return mContext.getString(R.string.app_name);
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
    private ImageObject getImageObj(Bitmap picture) {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(picture);
        return imageObject;
    }

    /**
     * 处理微博分享动作
     *
     * @param picture    要分享的照片
     * @param hasText    分享的内容是否有文本
     * @param hasImage   分享的内容是否有图片
     * @param hasWebPage 分享的内容是否有网页
     * @param hasMusic   分享的内容是否有音乐
     * @param hasVideo   分享的内容是否有视频
     */
    public void weiBoAction(Bitmap picture, boolean hasText, boolean hasImage, boolean hasWebPage,
                            boolean hasMusic, boolean hasVideo, boolean hasVoice) {
        try {
            if (!checkWBInstalled()) {
                util.showMessage(mContext.getString(R.string.weiBo_not_installed), Toast.LENGTH_SHORT);
            } else {
                sendMessage(picture, hasText, hasImage, hasWebPage, hasMusic, hasVideo, hasVoice);
            }
        } catch (WeiboException e) {
            util.showErrorMsg(e);
        }
    }
}
