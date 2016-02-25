package com.imooc.run.newyear.wxapi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.imooc.run.newyear.R;
import com.imooc.run.newyear.constants.WeChatConstants;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IWXAPI api;
        mContext = WXEntryActivity.this;
        api = WXAPIFactory.createWXAPI(mContext, WeChatConstants.APP_ID, false);
        api.handleIntent(getIntent(), this);
        super.onCreate(savedInstanceState);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {

    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        String result;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                //分享成功
                result = getString(R.string.share_success);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                //分享取消
                result = getString(R.string.share_cancel);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                //分享失败
                result = getString(R.string.share_deny);
                break;
            default:
                result = getString(R.string.share_return);
                break;
        }
        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
        this.finish();
    }
}
