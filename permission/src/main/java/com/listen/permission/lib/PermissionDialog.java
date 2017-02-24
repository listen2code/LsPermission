package com.listen.permission.lib;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * @author listen
 * @desc 权限申请dialog辅助类
 */
public class PermissionDialog {
    /**
     * @desc 显示跳转到权限设置页面的提示弹框
     * @author listen
     * @date 2017/2/23 11:53
     */
    public static void showNeverAskDialog(final Context context, String message) {
        if (PermissionUtil.isContextDestroyed(context)) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("权限申请").setMessage(message).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PermissionUtil.intentToPermissionSetting(context);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setCancelable(false).show();
    }

    public static void showNeverAskDialog(Context context) {
        showNeverAskDialog(context, "必要的权限被拒绝");
    }

    /**
     * @desc 显示跳转到权限设置页面的提示弹框
     * @author listen
     * @date 2017/2/23 11:53
     */
    public static void showAskBeforeRequestDialog(final Context context, String message,
                                                  DialogInterface.OnClickListener confirmListener) {
        if (PermissionUtil.isContextDestroyed(context)) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("权限申请")
            .setMessage(message)
            .setPositiveButton("申请", confirmListener)
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .setCancelable(false)
            .show();
    }

}
