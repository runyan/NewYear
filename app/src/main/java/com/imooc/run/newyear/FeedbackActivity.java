package com.imooc.run.newyear;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.imooc.run.newyear.Util.Util;

public class FeedbackActivity extends Activity {

    private EditText mName;
    private EditText mEmail;
    private EditText mContent;

    private final String developerEmail = "developer@126.com";
    private final String developerEmailPassword = "password";
    private final String developerEmailHost = "smtp.126.com";
    private final String developerEmailPort = "25";

    private String name;
    private String email;
    private String content;

    private final Util util = new Util(FeedbackActivity.this, FeedbackActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        util.smoothSwitchScreen();
        setContentView(R.layout.activity_feedback);

        Button mFeedBack = (Button) findViewById(R.id.send_feedback);
        Button mCancel = (Button) findViewById(R.id.cancel_feedback);

        mName = (EditText) findViewById(R.id.feedback_name);
        mEmail = (EditText) findViewById(R.id.feedback_email);
        mContent = (EditText) findViewById(R.id.feedback_content);

        mFeedBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                name = TextUtils.isEmpty(mName.getText().toString()) ? getString(R.string.anonymous) : mName.getText().toString();
                email = TextUtils.isEmpty(mEmail.getText().toString()) ? getString(R.string.anonymous) : mEmail.getText().toString();
                content = mContent.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    util.showMessage(getString(R.string.enter_feedback), Toast.LENGTH_SHORT);
                } else {
                    if (util.checkNetworkAvailability()) {
                        String emailContent = "姓名：" + name + "\n" + "Email:" + email + "\n" + "反馈内容：" + content;
                        String[] toArr = {developerEmail};
                        MailTask feedbackTask = new MailTask(FeedbackActivity.this, FeedbackActivity.this);
                        feedbackTask.setSender(developerEmail);
                        feedbackTask.setPassword(developerEmailPassword);
                        feedbackTask.setReceivers(toArr);
                        feedbackTask.setHost(developerEmailHost);
                        feedbackTask.setPort(developerEmailPort);
                        feedbackTask.setSubject("反馈");
                        feedbackTask.setBody(emailContent);
                        feedbackTask.setNeedToShowResult(true);
                        feedbackTask.setNeedProgressBar(true);
                        feedbackTask.execute();
                        if (!"匿名".equals(email)) {
                            MailTask autoReplyTask = new MailTask(FeedbackActivity.this, FeedbackActivity.this);
                            String[] receivers = {email};
                            autoReplyTask.setSender(developerEmail);
                            autoReplyTask.setPassword(developerEmailPassword);
                            autoReplyTask.setReceivers(receivers);
                            autoReplyTask.setHost(developerEmailHost);
                            autoReplyTask.setPort(developerEmailPort);
                            autoReplyTask.setSubject("反馈成功");
                            autoReplyTask.setBody("感谢反馈\n此邮件为自动回复邮件请不要直接回复本邮件");
                            autoReplyTask.execute();
                        }
                    } else {
                        util.showMessage(getString(R.string.network_unavailable), Toast.LENGTH_SHORT);
                    }
                }
            }
        });

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackActivity.this.finish();
            }
        });
    }

}

