package com.listen.permission.lib;

import java.util.List;

public interface OnPermissionListener {

    /**
     * @desc 申请的权限全被授权
     * @author listen
     * @date 2017/2/23 11:23
     */
    void onGrant();

    /**
     * @desc 有权限被拒绝
     * @author listen
     * @date 2017/2/23 11:23
     */
    void onDeny(List<String> permissions);

    /**
     * @desc 拒绝授权, 并勾选 never ask again
     * @author listen
     * @date 2017/2/23 11:22
     */
    void onNeverAsk(List<String> permissions);

    /**
     * @desc 不管授权成功还是失败, 都执行
     * @author listen
     * @date 2017/2/23 11:22
     */
    void always(List<String> grantPermissions, List<String> denyPermissions, List<String> foreverDenyPermissions);
}
