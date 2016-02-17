package com.imooc.run.newyear.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.imooc.run.newyear.constants.WeChatConstants;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IWXAPI api;
        api = WXAPIFactory.createWXAPI(this, WeChatConstants.APP_ID, false);
        api.handleIntent(getIntent(), this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        String result;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                //分享成功
                result = "分享成功";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                //分享取消
                result = "分享取消";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                //分享失败
                result = "分享被拒绝";
                break;
            default:
                result = "分享返回";
                break;
        }
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        this.finish();
    }
}
