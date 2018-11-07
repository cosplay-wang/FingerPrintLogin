package com.cosplay.wang.fingerprintlogin.util;

import android.content.Context;

/**
 * Author:wangzhiwei on 2018/11/6.
 * Email :wangzhiwei@moyi365.com
 * Description:sp帮助类
 */
public class SPUtils {
    public static final String SP_FINGER = "FINGER";
    public static String getString(Context context, String id) {
        return context.getSharedPreferences(SP_FINGER, 0).getString(id, "");
    }


    public static void setString(Context context, String id, String json) {
        context.getSharedPreferences(SP_FINGER, 0).edit().putString(id, json).commit();
    }
}
