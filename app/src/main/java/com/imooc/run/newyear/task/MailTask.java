package com.imooc.run.newyear.task;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.imooc.run.newyear.R;
import com.imooc.run.newyear.Util.MailUtil;
import com.imooc.run.newyear.Util.Util;

public class MailTask extends AsyncTask<Integer, Integer, String> {

    private boolean needProgressBar;
    private boolean needToShowResult;

    private String sender;
    private String[] receivers;
    private String password;
    private String host;
    private String port;
    private String subject;
    private String body;

    private Activity activity;
    private Context context;

    private MaterialDialog progress;

    public MailTask(Context context, Activity activity) {
        this.needProgressBar = false;
        this.needToShowResult = false;
        this.activity = activity;
        this.context = context;
    }

    public void setNeedProgressBar(boolean needProgressBar) {
        this.needProgressBar = needProgressBar;
    }

    public void setNeedToShowResult(boolean needToShowResult) {
        this.needToShowResult = needToShowResult;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceivers(String[] receivers) {
        this.receivers = receivers;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean getNeedToShowResult() {
        return this.needToShowResult;
    }

    public boolean getNeedProgressBar() {
        return this.needProgressBar;
    }

    @Override
    protected void onPreExecute() {
        if (this.getNeedProgressBar()) {
            MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(context);
            mBuilder.theme(Theme.LIGHT)
                    .title(R.string.info)
                    .content(R.string.sending)
                    .progress(true, 0)
                    .cancelable(false);
            progress = mBuilder.build();
            progress.show();
        }
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Integer... params) {
        MailUtil mail = new MailUtil(sender, password);
        mail.setHost(host);
        mail.setPort(port);
        mail.setFrom(sender);
        mail.setTo(receivers);
        mail.setSubject(subject);
        mail.setBody(body);
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
        Util util = new Util(context, activity);
        if (this.getNeedProgressBar())
            progress.dismiss();
        if (this.getNeedToShowResult()) {
            switch (r) {
                case "success": {
                    util.showMessage(context.getString(R.string.feedback_success), Toast.LENGTH_SHORT);
                    break;
                }
                case "fail": {
                    util.showMessage(context.getString(R.string.feedback_fail), Toast.LENGTH_SHORT);
                }
                case "exception": {
                    util.showMessage(context.getString(R.string.error), Toast.LENGTH_SHORT);
                    break;
                }
            }
        }
        activity.finish();
    }

}
