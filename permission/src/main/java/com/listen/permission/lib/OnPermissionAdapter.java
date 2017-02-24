package com.listen.permission.lib;

import java.util.List;

/**
 * @author listen
 * @desc 权限回调适配器, 对于对每种回调初始化空实现
 * @date 2017/2/23 14:26
 */
public class OnPermissionAdapter implements OnPermissionListener {

    @Override
    public void onGrant() {

    }

    @Override
    public void onDeny(List<String> permissions) {

    }

    @Override
    public void onNeverAsk(List<String> permissions) {

    }

    @Override
    public void always(List<String> grantPermissions, List<String> denyPermissions, List<String> foreverDenyPermissions) {

    }
}
