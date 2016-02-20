package com.imooc.run.newyear.Util;

public class Util {

    private static long lastClickTime; //上次点击时间

    /**
     * 判断是否是连续点击
     *
     * @return true 是连续点击， false 不是连续点击
     */
    public static boolean checkFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

}
