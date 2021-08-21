### LsPermission
6.0运行时权限辅助工具类

### Getting started
In your build.gradle:

```
compile 'con.listen.library:LsPermission:1.0.0'
```

### Usage

Step1. 在BaseActivity的onRequestPermissionsResult()中接收统一的权限回调，并转交给PermissionUtil处理。

```
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
   PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
   super.onRequestPermissionsResult(requestCode, permissions, grantResults);
}
```

Step2. 使用PermissionUtil申请权限

```
final String[] permissions =
            new String[] {Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION};
            
PermissionUtil.request(this, permissions, new OnPermissionAdapter() {
       /**
        * @desc 申请的权限全被授权
        */
       @Override
       public void onGrant() {
       }

       /**
        * @desc 权限被拒绝
        */
       @Override
       public void onDeny(List<String> permissions) {
       }

       /**
        * @desc 权限被拒绝,且勾选"never ask again"
        */
       @Override
       public void onNeverAsk(List<String> permissions) {
           // show dialog to user, show some message to ask for granted
           PermissionUtil.showNeverAskDialog(MainActivity.this, "这个权限很重要");
       }
       
       /**
        * @desc 无论权限授权成功还是失败，都会回调
        */
       @Override
       public void always(List<String> grantPermissions, List<String> denyPermissions,
           List<String> foreverDenyPermissions) {
       }
   });
```


### Blog
[6.0运行时权限的总结与实践](https://www.jianshu.com/p/149dd57d175c)

License
-------

```
Copyright 2017 listen.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

