package com.listen.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.listen.permission.lib.OnPermissionAdapter;
import com.listen.permission.lib.PermissionDialog;
import com.listen.permission.lib.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author listen
 * @desc 6.0权限demo
 * @date 2017/2/24 15:17
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                normal();
                break;
            case R.id.button2:
                weixinPermission();
                break;
            case R.id.button3:
                alipayPermission();
                break;
            case R.id.button4:
                baiduMapPermission();
                break;
        }
    }

    /**
     * @desc 正常模式申请多个权限
     * @author listen
     * @date 2017/2/24 15:17
     */
    private void normal() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        PermissionUtil.request(MainActivity.this, permissions, new OnPermissionAdapter() {
            @Override
            public void onGrant() {
                showToast("权限被同意");
                callPhone();
            }

            @Override
            public void onDeny(List<String> permissions) {
                showToast("用户拒绝授权" + permissions.toString());
            }

            @Override
            public void onNeverAsk(List<String> permissions) {
                showToast("用户拒绝授权, 并勾选 never ask again " + permissions.toString());
                PermissionDialog.showNeverAskDialog(MainActivity.this, "这个权限很重要");
            }

            @Override
            public void always(List<String> grantPermissions, List<String> denyPermissions, List<String> foreverDenyPermissions) {
                showToast("授权: " + grantPermissions.toString() + "\n 拒绝: " + denyPermissions.toString() + "\n never ask: " + foreverDenyPermissions.toString());
            }
        });
    }

    /**
     * @desc 微信模式: 逐步申请多个权限, 并对每个权限的拒绝进行弹框提示
     * @author listen
     * @date 2017/2/24 15:17
     */
    private void weixinPermission() {
        final ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        for (int i = 0; i < permissions.size(); i++) {
            final int index = i;
            PermissionUtil.request(MainActivity.this, new String[]{permissions.get(index)}, new OnPermissionAdapter() {
                @Override
                public void onGrant() {
                    showToast("权限被同意");
                    if ((index + 1) <= permissions.size() - 1) {
                        PermissionUtil.request(MainActivity.this, new String[]{permissions.get(index + 1)}, this);
                    }
                }

                @Override
                public void onDeny(List<String> permissions) {
                    PermissionDialog.showNeverAskDialog(MainActivity.this, permissions.get(index) + "这个权限很重要");
                }

                @Override
                public void onNeverAsk(List<String> permissions) {
                    PermissionDialog.showNeverAskDialog(MainActivity.this, permissions.get(index) + "这个权限很重要");
                }
            });
        }
    }

    /**
     * @desc 支付宝模式: 申请单个权限, 拒绝则弹框并继续申请, 拒绝且"never ask"则弹框并引导去设置页面
     * @author listen
     * @date 2017/2/24 15:18
     */
    private void alipayPermission() {
        final ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CALL_PHONE);

        PermissionUtil.requestWithDialog(MainActivity.this, "需要电话权限", permissions, new OnPermissionAdapter() {
            @Override
            public void onGrant() {
                showToast("权限被同意");
                callPhone();
            }

            @Override
            public void onDeny(List<String> permissions) {
                PermissionUtil.requestWithDialog(MainActivity.this, "需要电话权限", permissions, this);
            }

            @Override
            public void onNeverAsk(List<String> permissions) {
                PermissionDialog.showNeverAskDialog(MainActivity.this, "需要电话权限");
            }
        });
    }

    /**
     * @desc 百度地图模式: 申请多个权限, 有必要, 非必要权限
     * @author listen
     * @date 2017/2/24 15:18
     */
    private void baiduMapPermission() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        PermissionUtil.requestWithDialog(MainActivity.this, "需要电话(必要), 位置权限(非必要)", permissions, new OnPermissionAdapter() {
            @Override
            public void onGrant() {
                showToast("权限被同意");
            }

            @Override
            public void onDeny(List<String> permissions) {
                if (permissions.contains(Manifest.permission.CALL_PHONE)) {
                    // 必须权限
                    PermissionUtil.requestWithDialog(MainActivity.this, "需要电话权限",
                            new String[]{Manifest.permission.CALL_PHONE}, this);
                }
            }

            @Override
            public void onNeverAsk(List<String> permissions) {
                if (permissions.contains(Manifest.permission.CALL_PHONE)) {
                    // 必须权限
                    PermissionDialog.showNeverAskDialog(MainActivity.this, "需要电话权限");
                }
            }
        });
    }

    /**
     * @desc 将权限回调转发给PermissionUtil处理
     * @author listen
     * @date 2017/2/24 13:47
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void callPhone() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:10086"));
        startActivity(intent);
    }

    // ///////////////////////////
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
