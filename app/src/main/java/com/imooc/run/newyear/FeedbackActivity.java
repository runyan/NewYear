package com.imooc.run.newyear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private String name;
    private String email;
    private String content;

    private final Util util = new Util(FeedbackActivity.this, FeedbackActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Button mFeedBack = (Button) findViewById(R.id.send_feedback);
        Button mCancel = (Button) findViewById(R.id.cancel_feedback);

        mName = (EditText) findViewById(R.id.feedback_name);
        mEmail = (EditText) findViewById(R.id.feedback_email);
        mContent = (EditText) findViewById(R.id.feedback_content);

        mFeedBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0 == Util.getTextLength(mContent)) {
                    util.showMessage(getString(R.string.enter_feedback), Toast.LENGTH_SHORT);
                } else {
                    name = 0 == Util.getTextLength(mName) ? getString(R.string.anonymous) : mName.getText().toString();
                    email = 0 == Util.getTextLength(mEmail) ? getString(R.string.anonymous) : mEmail.getText().toString();
                    content = mContent.getText().toString();

                    if (util.checkNetworkAvailability()) {
                        SendTask sTask = new SendTask();
                        sTask.execute();
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
            MailUtil mail = new MailUtil("email address@126.com", "email password");
            String[] toArr = {"recepient1", "recepient2"};
            mail.setHost("smtp.126.com");
            mail.setPort("25");
            mail.setTo(toArr);
            mail.setFrom("email address@126.com");
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
}

