package com.imooc.run.newyear.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class PopupWindowUtil {

    private final PopupWindow popupWindow;
    private final Context context;

    public PopupWindowUtil(Context context, Activity activity, int resId, int width, int height) {
        View popupView = activity.getLayoutInflater().inflate(resId, new RelativeLayout(context), false);
        popupWindow = new PopupWindow(popupView, width, height, true);
        this.context = context;
    }

    /**
     * 点击空白处时是否关闭弹窗
     *
     * @param close 是否关闭弹窗
     */
    public void closeOnOutside(boolean close) {
        if (close) {
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        }
    }

    /**
     * 根据资源id获取弹窗上的控件
     *
     * @param resId 资源id
     * @return 弹窗上的控件
     */
    public View getView(int resId) {
        return popupWindow.getContentView().findViewById(resId);
    }

    /**
     * 设置弹窗的动画
     *
     * @param resId 弹窗动画的资源id
     */
    public void setAnimation(int resId) {
        popupWindow.setAnimationStyle(resId);
    }

    /**
     * 设置弹窗在父视图中央显示
     *
     * @param parent 父视图
     */
    public void showAtCenter(View parent) {
        popupWindow.showAtLocation(parent.getRootView(), Gravity.CENTER, 0, 0); //设置popupWindow的弹出位置
        popupWindow.update(0, 0, popupWindow.getWidth(), popupWindow.getHeight());
        popupWindow.showAsDropDown(parent);
    }

}
