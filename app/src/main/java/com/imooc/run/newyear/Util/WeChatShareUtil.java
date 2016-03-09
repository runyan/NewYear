package com.imooc.run.newyear.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.imooc.run.newyear.R;
import com.imooc.run.newyear.constants.WeChatConstants;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WeChatShareUtil {

    private final IWXAPI iwxapi; //微信分享接口实例
    private final Context mContext;
    private final Util util;

    public WeChatShareUtil(Context context, Activity activity) {
        mContext = context;
        util = new Util(context, activity);

        iwxapi = WXAPIFactory.createWXAPI(mContext, WeChatConstants.APP_ID, false); //通过WXAPIFactory工厂，获取实例
        iwxapi.registerApp(WeChatConstants.APP_ID);//将应用的appId注册到微信
    }

    /**
     * 判断微信是否安装
     *
     * @return true 微信已安装 false 微信未安装
     */
    private boolean checkWXInstalled() {
        return iwxapi.isWXAppInstalled();
    }

    /**
     * 微信分享
     *
     * @param flag    分享位置 0为分享到朋友圈 1为分享给微信好友
     * @param picture 要分享的图片
     */
    private void weChatShare(int flag, Bitmap picture) {
        WXWebpageObject webPage = new WXWebpageObject();//初始化一个WXWebPageObject对象
        WXMediaMessage msg = new WXMediaMessage(webPage);//用WXWebPageObject对象初始化一个WXMediaMessage 对象，填写信息
        msg.mediaObject = new WXImageObject(picture);

        SendMessageToWX.Req req = new SendMessageToWX.Req();//构造一个Req
        req.transaction = String.valueOf(System.currentTimeMillis()); //transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession; //根据flag判断发送位置

        iwxapi.sendReq(req); //调用api接口发送数据到微信
    }

    /**
     * 处理微信分享动作
     *
     * @param flag    0为分享到朋友圈 1为分享给微信好友
     * @param picture 要分享的图片
     */
    public void weChatAction(int flag, Bitmap picture) {
        if (!checkWXInstalled()) {
            util.showMessage(mContext.getString(R.string.weiChat_not_installed), Toast.LENGTH_SHORT);
        } else {
            weChatShare(flag, picture);
        }
    }
}
