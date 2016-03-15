package com.imooc.run.newyear;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.imooc.run.newyear.Util.Util;

/**
 * 启动页面
 */
public class SplashActivity extends Activity {

    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initView();

        verify();
    }

    /**
     * 界面初始化
     */
    private void initView() {
        util = new Util(SplashActivity.this, SplashActivity.this);
        SharedPreferences pref = getSharedPreferences("appear", MODE_PRIVATE);
        final Editor editor = pref.edit();

        CheckBox mShowOnStartUp = (CheckBox) findViewById(R.id.show_on_start_up);
        mShowOnStartUp.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("showOnStartUp", false);
                    editor.apply();
                }
            }
        });

        boolean hasWelcomeScreen = pref.getBoolean("showOnStartUp", true);
        proceedToMain(hasWelcomeScreen);

    }

    /**
     * 验证
     */
    private void verify() {
        util.verifyVersion();
        util.verifyDeviceType();
    }

    /**
     * 跳转至主界面
     *
     * @param hasWelcomeScreen 是否显示欢迎页面
     */
    private void proceedToMain(boolean hasWelcomeScreen) {
        if (hasWelcomeScreen) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out); //设置切换动画
                    SplashActivity.this.finish();
                }
            }, 2000);
        } else {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

}
