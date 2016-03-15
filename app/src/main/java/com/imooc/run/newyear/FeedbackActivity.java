package com.imooc.run.newyear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.imooc.run.newyear.Util.MailUtil;
import com.imooc.run.newyear.Util.Util;

public class FeedbackActivity extends Activity {

    private EditText mName;
    private EditText mEmail;
    private EditText mContent;

    private final String developerEmail = "developerEmail@126.com";
    private final String developerEmailPassword = "password";
    private final String developerEmailHost = "smtp.126.com";
    private final String developerEmailPort = "25";
    private String userEmail;

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
                        SendTask sTask = new SendTask();
                        sTask.execute();
                        if (!"匿名".equals(email)) {
                            userEmail = email;
                            AutoReplyTask autoReplyTask = new AutoReplyTask();
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

    private class SendTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            //第一个执行方法
            progress = ProgressDialog.show(FeedbackActivity.this, getString(R.string.info), getString(R.string.sending));
            progress.setCancelable(false);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            String emailContent = "姓名：" + name + "\n" + "Email:" + email + "\n" + "反馈内容：" + content;
            String[] toArr = {"yanrun2007@gmail.com", developerEmail};
            MailUtil mail = new MailUtil(developerEmail, developerEmailPassword);
            mail.setHost(developerEmailHost);
            mail.setPort(developerEmailPort);
            mail.setTo(toArr);
            mail.setFrom(developerEmail);
            mail.setSubject("反馈");
            mail.setBody(emailContent);
            try {
                if (mail.send()) {
                    return "success";
                } else {
                    return "fail";
                }
            } catch (Exception e) {
                return "exception";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            /**这个函数在doInBackground调用publishProgress时触发，虽然调用时只有一个参数
             *但是这里取到的是一个数组,所以要用progress[0]来取值
             *第n个参数就用progress[n]来取值
             */
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String r) {
            /**
             * doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
             * 这里的result就是上面doInBackground执行后的返回值，所以这里是"执行完毕"
             */
            super.onPostExecute(r);
            progress.dismiss();
            switch (r) {
                case "success": {
                    util.showMessage(getString(R.string.feedback_success), Toast.LENGTH_SHORT);
                    break;
                }
                case "fail": {
                    util.showMessage(getString(R.string.feedback_fail), Toast.LENGTH_SHORT);
                }
                case "exception": {
                    util.showMessage(getString(R.string.error), Toast.LENGTH_SHORT);
                    break;
                }
            }
            FeedbackActivity.this.finish();
        }
    }

    private class AutoReplyTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {
            String[] receiver = {userEmail};
            MailUtil mail = new MailUtil(developerEmail, developerEmailPassword);
            mail.setHost(developerEmailHost);
            mail.setPort(developerEmailPort);
            mail.setTo(receiver);
            mail.setFrom(developerEmail);
            mail.setSubject("反馈成功");
            mail.setBody("感谢反馈\n此邮件为自动回复邮件请不要直接回复本邮件");
            try {
                if (mail.send()) {
                    return "success";
                } else {
                    return "fail";
                }
            } catch (Exception e) {
                return "exception";
            }
        }
    }
}

