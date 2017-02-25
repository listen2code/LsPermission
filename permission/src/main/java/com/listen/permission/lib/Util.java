package com.listen.permission.lib;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * @author listen
 * @desc
 */
public class Util {

    public static void checkContext(Context context) {
        if (isContextDestroyed(context)) {
            throw new RuntimeException("context is destoryed or disabled");
        }
    }

    /**
     * @desc 判断Activity是否已经销毁
     * @author listen
     * @date 2017/2/23 14:21
     */
    public static boolean isContextDestroyed(Context context) {
        if (null == context) {
            return true;
        }
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return activity.isDestroyed() && activity.isFinishing();
            } else {
                return activity.isFinishing();
            }
        } else {
            log("Context must be an AppCompatActivity");
            return true;
        }
    }

    public static <T> void checkNull(T object, String message) {
        if (null == object) {
            throw new RuntimeException(message);
        }
    }

    public static <T> void checkList(T[] list, String message) {
        if (isListEmpty(list)) {
            throw new RuntimeException(message);
        }
    }

    public static <T> void checkList(List<T> list, String message) {
        if (isListEmpty(list)) {
            throw new RuntimeException(message);
        }
    }

    public static <T> boolean isListEmpty(List<T> list) {
        return list == null || list.size() <= 0;
    }

    public static <T> boolean isListEmpty(T[] list) {
        return list == null || list.length <= 0;
    }

    public static void log(String message, Object... args) {
        if (!PermissionUtil.DEBUG) {
            return;
        }
        Log.d(PermissionUtil.TAG, String.format(Locale.US, message, args));
    }
}
