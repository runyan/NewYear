package com.imooc.run.newyear.Util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.imooc.run.newyear.MainActivity;
import com.imooc.run.newyear.R;
import com.imooc.run.newyear.constants.Constants;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import pl.droidsonroids.gif.GifImageView;

public class Util {

    private final Context context;
    private final Activity activity;

    public Util(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    /**
     * 获得Drawable对象
     *
     * @param resId Drawable对象的id
     * @return 所D获得的Drawable对象
     */
    public Drawable getDrawable(int resId) {
        return ContextCompat.getDrawable(context, resId);
    }

    /**
     * 关闭屏幕键盘
     *
     * @param textView 获得焦点的TextView
     */
    public void closeInputMethod(TextView textView) {
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
    private boolean versionCheck() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * 退出APP
     */
    public void exit() {
        activity.finish();
        System.exit(0);
    }

    /**
     * 当系统版本小于4.0时退出程序
     */
    public void verifyVersion() {
        if (!versionCheck()) {
            showMessage(context.getString(R.string.version_error), Toast.LENGTH_LONG);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit();
                }
            }, 1000);
        }
    }

    /**
     * 实现手机震动
     *
     * @param mode 震动模式
     */
    public void vibrate(int mode) {
        //通过系统服务获得手机震动服务
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        //得到震动服务后检测vibrator是否存在
        if (vibrator.hasVibrator()) {
            switch (mode) {
                case Constants.VIBRATE_SINGLE: {
                    vibrator.vibrate(200); //开始启动vibrator持续milliseconds毫秒。
                    break;
                }
                case Constants.VIBRATE_DOUBLE: {
                    /**
                     * 以pattern方式重复repeat次启动vibrator。
                     * pattern的形式为new long[]{arg1,arg2,arg3,arg4......},
                     * 其中以两个一组的如arg1和arg2为一组、arg3和arg4为一组，
                     * 每一组的前一个代表等待多少毫秒启动vibrator，后一个代表vibrator持续多少毫秒停止之后往复即可。
                     * Repeat表示重复次数，当其为-1时，表示不重复只以pattern的方式运行一次）。
                     */
                    long[] pattern = {100, 400, 100, 400};
                    int repeat = -1;
                    vibrator.vibrate(pattern, repeat);
                    break;
                }
                default: {
                    vibrator.cancel(); //停止
                }
            }
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
            final String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            if (!checkStoragePermission(activity)) {
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
     * 确认读写权限是否以被授予应用
     *
     * @param activity 要检查的activity
     * @return true 有读写权限，false 有读写权限
     */
    public static boolean checkStoragePermission(Activity activity) {
        // 检查是否拥有写权限
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查设备是否有相机
     *
     * @return true 设备有相机，false 设备没有相机
     */
    public boolean hasCamera() {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 根据系统版本改变AlertDialog Builder样式
     *
     * @return 不同样式的AlertDialog Builder
     */
    public AlertDialog.Builder getAlertDialog() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle) : new AlertDialog.Builder(context);
    }

    /**
     * 删除文件
     *
     * @param path 要删除的文件路径
     * @return true 删除成功， false 删除失败
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    /**
     * 显示异常信息
     *
     * @param throwable 程序抛出的异常
     */
    public void showExceptionMsg(Throwable throwable) {
        String errMsg = context.getString(R.string.error) + ":" + throwable.getMessage();
        showMessage(errMsg, Toast.LENGTH_SHORT);
        throwable.printStackTrace();
    }


    /**
     * 显示提示信息
     *
     * @param message 要显示的信息
     * @param length  显示时长
     */
    public void showMessage(String message, int length) {
        Toast.makeText(context, message, length).show();
    }

    /**
     * 解决全屏与非全屏activity切换时界面不流畅问题
     */
    public void smoothSwitchScreen() {
        // 5.0以上修复了此bug
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup rootView = ((ViewGroup) activity.findViewById(android.R.id.content));
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            int statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            rootView.setPadding(0, statusBarHeight, 0, 0);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 制作贺卡
     *
     * @param activity 生成贺卡的Activity
     * @return 生成的贺卡
     */
    public static Bitmap generateSpringCard(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache(); //截屏并返回
    }

    /**
     * 通过媒体文件获得Uri
     *
     * @param mediaStoragePath 文件夹位置
     * @param mediaPath        文件位置
     * @return 媒体文件的Uri
     */
    public Uri getOutputMediaFileUri(String mediaStoragePath, String mediaPath) {
        return Uri.fromFile(getOutputFile(mediaStoragePath, mediaPath));
    }

    /**
     * 获得要分享的文件
     *
     * @param fileStoragePath 文件夹位置
     * @param filePath        文件位置
     * @return 要分享的文件
     */
    @Nullable
    private File getOutputFile(String fileStoragePath, String filePath) {
        File file;
        try {
            File mediaStorageDir = new File(fileStoragePath);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            file = new File(filePath);
        } catch (Exception e) {
            showExceptionMsg(e);
            return null;
        }
        return file;
    }

    /**
     * 产生随机数
     *
     * @param seed 随机数的seed
     * @return 产生的随机数
     */
    public int getRandomNumber(int seed) {
        return new Random().nextInt(seed);
    }

    /**
     * 检查是否有可用的网络
     *
     * @return true 有可用的网络, false 没有可用的网络
     */
    public boolean checkNetworkAvailability() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return null != networkInfo && networkInfo.isAvailable();
    }

    /**
     * 检查wifi连接状态
     *
     * @return true wifi已连接， false wifi未连接
     */
    public boolean checkWifiAvailability() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return checkNetworkAvailability() && wifiManager.isWifiEnabled();
    }

    /**
     * 检查设备是否为手机，不是手机则退出
     */
    public void verifyDeviceType() {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            showMessage(context.getString(R.string.device_not_supported), Toast.LENGTH_SHORT);
            exit();
        }
    }

    /**
     * 使用手势识别
     *
     * @param resId GestureOverlayView的id
     */
    public void useGesture(int resId) {
        final GestureOverlayView v = (GestureOverlayView) activity.findViewById(resId);
        final GestureLibrary library = GestureLibraries.fromRawResource(context, R.raw.gestures);
        library.load();
        v.addOnGesturePerformedListener(new OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
                ArrayList<Prediction> predictions = library.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = predictions.get(0);
                    if (prediction.score >= 3.0) {
                        switch (prediction.name) {
                            case "previous": {
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                                break;
                            }
                            case "wish": {
                                if (activity.getClass() == MainActivity.class) {
                                    int[] gifIds = {R.drawable.kumamon1, R.drawable.kumamon2,
                                            R.drawable.kumamon3, R.drawable.kumamon4, R.drawable.kumamon5};
                                    String[] wishTexts = context.getResources().getStringArray(R.array.WishTextItemArray);

                                    int gifId = gifIds[getRandomNumber(gifIds.length)];
                                    String wishText = wishTexts[getRandomNumber(wishTexts.length)];

                                    final PopupWindowUtil popupWindow = new PopupWindowUtil(context, activity, R.layout.popupwindow_show_wish,
                                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    popupWindow.closeOnOutside(true);
                                    Button mClose = (Button) popupWindow.getView(R.id.close);

                                    mClose.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            popupWindow.closePopuoWindow();
                                        }
                                    });
                                    GifImageView gifImage = (GifImageView) popupWindow.getView(R.id.kumamon);
                                    gifImage.setImageResource(gifId);

                                    TextView mWishText = (TextView) popupWindow.getView(R.id.wish_text);
                                    mWishText.setText(wishText);
                                    mWishText.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/test.ttf"));

                                    popupWindow.setAnimation(R.style.anim_menu_bottomBar);
                                    popupWindow.showAtCenter(v);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * 获得应用版本
     *
     * @return 应用版本
     */
    public String getAppVersion() {
        String version = "0";
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            showExceptionMsg(e);
        }
        return version;
    }

    /**
     * 获取屏幕的宽度，以像素为单位
     *
     * @return 屏幕的显示宽度
     */
    public int getDisplayWidth() {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

}
