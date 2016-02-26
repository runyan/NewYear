package com.imooc.run.newyear.Util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;

public class Util {

    /**
     * 获得Drawable对象
     *
     * @param context 需要获得Drawable对象的上下文
     * @param resId   Drawable对象的id
     * @return 所D获得的Drawable对象
     */
    public static Drawable getDrawable(Context context, int resId) {
        return ContextCompat.getDrawable(context, resId);
    }

    /**
     * 关闭屏幕键盘
     *
     * @param context  上下文对象
     * @param textView 获得焦点的TextView
     */
    public static void closeInputMethod(Context context, TextView textView) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive(); //判断键盘是否显示
        if (isOpen) {
            imm.hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 判断安卓版本是否小于4.0
     *
     * @return true 如果安卓版本大于4.0, false 如果安卓版本小于4.0
     */
    @Contract(pure = true)
    public static boolean versionCheck() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * 彻底退出
     */
    public static void exit(Activity activity) {
        activity.finish();
        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 返回指定TextView内的文本长度
     *
     * @param textView 指定的TextView
     * @return 指定TextView内的文本长度
     */
    public static int getTextLength(TextView textView) {
        return textView.getText().length();
    }

    /**
     * 实现手机震动
     *
     * @param activity 实现手机震动的activity
     */
    public static void vibrate(Activity activity) {
        //通过系统服务获得手机震动服务
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) { //得到震动服务后检测vibrator是否存在
            vibrator.vibrate(200); //开始启动vibrator持续milliseconds毫秒。
            // 以pattern方式重复repeat次启动vibrator。（
            // pattern的形式为new long[]{arg1,arg2,arg3,arg4......},
            // 其中以两个一组的如arg1和arg2为一组、arg3和arg4为一组，
            // 每一组的前一个代表等待多少毫秒启动vibrator，后一个代表vibrator持续多少毫秒停止之后往复即可。
            // Repeat表示重复次数，当其为-1时，表示不重复只以pattern的方 式运行一次）。
            //long[] pattern = {100, 400, 100, 400};
            //int repeat = -1;
            // vibrator.vibrate(pattern, repeat);
            //vibrator.cancel(); //停止
        }
    }

    /**
     * 检查应用是否有写权限，如果没有则向用户申请
     *
     * @param activity 要检查权限的activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //存储权限
            final int REQUEST_EXTERNAL_STORAGE = 1;
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            // 检查是否拥有写权限
            int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 如果没有权限则向用户申请
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }

    /**
     * 确认应用是否拥有读写权限
     *
     * @param activity 要检查的activity
     * @return true 有读写权限，false 有读写权限
     */
    public static boolean checkStoragePermission(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

}
