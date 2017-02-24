package com.listen.permission.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

/**
 * @author listen
 * @desc 6.0权限工具类
 */
public class PermissionUtil {

    private static final String TAG = "PermissionUtil";

    private static final int DEFAULT_PERMISSION_REQUEST_CODE = 10001;// 在没有设置requestCode情况下的默认值

    /**
     * 如果一个页面有多个请求权限的行为, 需要根据不同的requestCode把不同权限的listener区分并回调
     */
    private static SparseArray<OnPermissionListener> mPermissionRequestList;

    /**
     * @desc 带确认弹框的权限申请, 先弹框提示告知权限用途; 如果申请的权限被勾选"never ask", 则弹框引导去权限设置页面
     * @author listen
     * @date 2017/2/24 09:44
     */
    public static void requestWithDialog(final Context context, String message, final List<String> permissions,
        final OnPermissionListener listener) {
        if (isContextDestroyed(context)) {
            return;
        }

        if (null == listener) {
            Log.e(TAG, "OnPermissionListener can not be null");
            return;
        }

        ArrayList<String> denyPermissions = getDenyPermissions(context, permissions);

        if (!isListEmpty(denyPermissions)) {
            ArrayList<String> foreverDenyPermissions = getForeverDenyPermissions(context, permissions);
            if (!isListEmpty(foreverDenyPermissions)) {
                /** 如果当前申请的权限中, 有foeverNever的权限, 则弹框提示去权限设置页面 */
                PermissionDialog.showNeverAskDialog(context, message);
            } else {
                PermissionDialog.showAskBeforeRequestDialog(context, message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request(context, DEFAULT_PERMISSION_REQUEST_CODE, permissions, listener);
                    }
                });
            }
        } else {
            /** 如果申请的权限都已经授权了, 直接成功回调 */
            listener.onGrant();
            listener.always(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        }
    }

    public static void requestWithDialog(final Context context, String message, final String[] permissions,
        final OnPermissionListener listener) {
        requestWithDialog(context, message, Arrays.asList(permissions), listener);
    }

    public static void request(Context context, List<String> permissions, OnPermissionListener listener) {
        request(context, DEFAULT_PERMISSION_REQUEST_CODE, permissions, listener);
    }

    public static void request(Context context, String[] permissions, OnPermissionListener listener) {
        request(context, DEFAULT_PERMISSION_REQUEST_CODE, Arrays.asList(permissions), listener);
    }

    /**
     * @desc 申请权限
     * @author listen
     * @date 2017/2/24 09:44
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void request(final Context context, int requestCode, List<String> permissions,
        OnPermissionListener listener) {

        if (isContextDestroyed(context)) {
            return;
        }

        if (null == listener) {
            Log.e(TAG, "OnPermissionListener can not be null");
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            listener.onGrant();
            listener.always(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
            return;
        }

        if (null == mPermissionRequestList) {
            mPermissionRequestList = new SparseArray<>();
        }

        if (context instanceof AppCompatActivity) {

            /** 获取未授权成功的权限 */
            ArrayList<String> denyPermissions = getDenyPermissions(context, permissions);

            if (!isListEmpty(denyPermissions)) {

                final int code = requestCode > -1 ? requestCode : DEFAULT_PERMISSION_REQUEST_CODE;
                synchronized (PermissionUtil.class) {
                    if (null != mPermissionRequestList.get(code)) {
                        /** 当前权限请求已经存在,不重复添加 */
                        Log.e(TAG, "the same permission is requesting");
                    } else {
                        /** 将当前权限请求加入队列 */
                        /** 如果一个页面中存在分别触发A, B多个权限的情况, 则最好将不同权限申请对应不同的requestCode, 存入SparseArray分别处理 */
                        mPermissionRequestList.put(code, listener);
                        Log.e(TAG, "add permission=" + mPermissionRequestList.toString());

                        /** 执行权限申请 */
                        ((AppCompatActivity) context).requestPermissions(
                            denyPermissions.toArray(new String[denyPermissions.size()]), requestCode);
                    }
                }
            } else {
                /** 如果申请的权限都已经授权了,直接成功回调 */
                /** 小米系统, 或是targetSDK<23的情况, 只要在manifest注册了, 就默认授权, 小米手机会自己的运行时权限流程 */
                listener.onGrant();
                listener.always(Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
            }
        } else {
            Log.e(TAG, "Context must be an AppCompatActivity");
        }
    }

    /**
     * @desc 获取未授权成功的权限
     * @author listen
     * @date 2017/2/23 10:45
     */
    private static ArrayList<String> getDenyPermissions(Context context, List<String> permissions) {
        if (isContextDestroyed(context)) {
            return null;
        }

        if (isListEmpty(permissions)) {
            return null;
        }

        ArrayList<String> denyPermissions = new ArrayList<>();
        for (int i = 0, len = permissions.size(); i < len; i++) {
            String p = permissions.get(i);
            if (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_DENIED) {
                denyPermissions.add(p);
            }
        }
        return denyPermissions;
    }

    /**
     * @desc 获取未授权, 且用户勾选 " never ask again"的按钮
     * @author listen
     * @date 2017/2/23 20:55
     */
    private static ArrayList<String> getForeverDenyPermissions(Context context, List<String> permissions) {
        if (isContextDestroyed(context)) {
            return null;
        }

        if (isListEmpty(permissions)) {
            return null;
        }

        ArrayList<String> denyPermissions = new ArrayList<>();
        for (int i = 0, len = permissions.size(); i < len; i++) {
            String p = permissions.get(i);
            if (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context, permissions.get(i))) {
                    /** 当用户拒绝过某个权限时,shouldShowRequestPermissionRationale返回true */
                } else {
                    /** 如果当前权限为deny, 且shouldShowRequestPermissionRationale返回false,说明当用户勾选"never ask again" */
                    denyPermissions.add(p);
                }
            }
        }
        return denyPermissions;
    }

    /**
     * @desc 申请权限后的回调
     * @author listen
     * @date 2017/2/23 10:51
     */
    public static void onRequestPermissionsResult(Context context, int requestCode, String[] permissions,
        int[] grantResults) {
        if (isContextDestroyed(context)) {
            return;
        }

        if (null == permissions || permissions.length <= 0) {
            return;
        }

        final OnPermissionListener listener;
        synchronized (PermissionUtil.class) {
            listener = mPermissionRequestList.get(requestCode);
            mPermissionRequestList.remove(requestCode);
        }
        Log.e(TAG, "remove permission=" + mPermissionRequestList.toString());

        if (null != listener) {
            if (context instanceof AppCompatActivity) {

                /** 授权成功的权限列表 */
                ArrayList<String> grantPermissions = new ArrayList<>();
                /** 获取未授权的权限列表 */
                ArrayList<String> denyPermissions = new ArrayList<>();
                /** 获取未授权的,并且勾选"never ask again"的权限列表 */
                ArrayList<String> foreverDenyPermissions = new ArrayList<>();

                for (int i = 0, len = grantResults.length; i < len; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        grantPermissions.add(permissions[i]);

                    } else if (ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity) context, permissions[i])) {
                        /** 当用户拒绝过某个权限时,shouldShowRequestPermissionRationale返回true */
                        denyPermissions.add(permissions[i]);
                    } else {
                        /** 如果当前权限为deny, 且shouldShowRequestPermissionRationale返回false,说明当用户勾选"never ask again" */
                        foreverDenyPermissions.add(permissions[i]);
                    }
                }

                if (isListEmpty(denyPermissions) && isListEmpty(foreverDenyPermissions) && !isContextDestroyed(context)) {
                    listener.onGrant();
                } else {
                    if (!isListEmpty(denyPermissions) && !isContextDestroyed(context)) {
                        listener.onDeny(denyPermissions);
                    }

                    if (!isListEmpty(foreverDenyPermissions) && !isContextDestroyed(context)) {
                        listener.onNeverAsk(foreverDenyPermissions);
                    }
                }

                listener.always(grantPermissions, denyPermissions, foreverDenyPermissions);

            } else {
                Log.e(TAG, "Context must be an AppCompatActivity");
            }
        } else {
            Log.e(TAG, "request is not exists");
        }
    }

    /**
     * @desc 判断Activity是否已经销毁
     * @author listen
     * @date 2017/2/23 14:21
     */
    public static boolean isContextDestroyed(Context context) {
        if (null == context) {
            Log.e(TAG, "Context can not be null");
            return true;
        }

        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return activity.isDestroyed() && activity.isFinishing();
            } else {
                return ((AppCompatActivity) context).isFinishing();
            }
        } else {
            Log.e(TAG, "Context must be an AppCompatActivity");
            return true;
        }
    }

    /**
     * @desc 跳转到权限设置页面
     * @author listen
     * @date 2017/2/23 11:47
     */
    public static void intentToPermissionSetting(Context context) {
        if (null == context) {
            Log.e(TAG, "Context can not be null");
            return;
        }

        Intent intentAppDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intentAppDetail.setData(Uri.parse("package:" + context.getPackageName()));
        intentAppDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (context.getPackageManager().resolveActivity(intentAppDetail, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            try {
                /** 跳转到应用详情设置页面 */
                context.startActivity(intentAppDetail);
            } catch (Exception e) {
                /** 跳转到应用列表页 */
                Intent intentSetting = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
                intentSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (context.getPackageManager().resolveActivity(intentSetting, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    context.startActivity(intentSetting);
                }
            }
        } else {
            /** 跳转到应用列表页 */
            Intent intentSetting = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
            intentSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (context.getPackageManager().resolveActivity(intentSetting, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                context.startActivity(intentSetting);
            }
        }
    }

    public static <T> boolean isListEmpty(List<T> list) {
        return list == null || list.size() <= 0;
    }

}
