# 美洽移动应用 SDK 3.0 for Android 开发文档

## ScreenShot
![美恰SDKDemo](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/155c91da06ea9bfd)

## 集成美洽 SDK

### Environment Required
- JDK7+

### AndroidStudio  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk)

```
// required     
// 「3.1.5」改成 maven central 徽章后面对应的版本号，例如3.1.6
compile 'com.meiqia:meiqiasdk:3.1.6@aar'

// 在下面的依赖中，如果你的项目已经依赖过其中的组件，则不需要重复依赖
compile 'com.android.support:support-v4:23.1.1'
compile 'com.squareup.okhttp:okhttp:2.7.0'
compile 'com.commit451:PhotoView:1.2.4'

// 目前支持常见的三种图片加载库，选择其中一种作为 MQConfig.init 方法的第三个参数进行初始化
compile 'com.meiqia:uilimageloader:1.0.0@aar'
compile 'com.nostra13.universalimageloader:universal-image-loader:1.9.5'

//    compile 'com.meiqia:glideimageloader:1.0.0@aar'
//    compile 'com.github.bumptech.glide:glide:3.7.0'

//    compile 'com.meiqia:picassoimageloader:1.0.0@aar'
//    compile 'com.squareup.picasso:picasso:2.5.2'

```

### Eclipse

1.拷贝 **/eclipse/MeiqiaSdk** 到工作空间并导入 Eclipse 中

2.选中你自己的工程的根目录 -> 右键 -> 选择 Properties -> 选中 Android -> 点击 Library 右边的的 Add 按钮 -> 选中 MeiqiaSdk -> 点击 OK

3.在你自己的工程的 AndroidManifest.xml 文件中添加以下权限

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

4.在你自己的工程的 AndroidManifest.xml 文件的 application 结点下加入以下代码

```
<!--聊天界面-->
<activity
    android:name="com.meiqia.meiqiasdk.activity.MQConversationActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateHidden|adjustResize" />

<!--图片查看界面-->
<activity
    android:name="com.meiqia.meiqiasdk.activity.MQPhotoPreviewActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateAlwaysHidden" />

<!--图片选择界面-->
<activity
    android:name="com.meiqia.meiqiasdk.activity.MQPhotoPickerActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateAlwaysHidden" />
<!--图片选择预览界面-->
<activity
    android:name="com.meiqia.meiqiasdk.activity.MQPhotoPickerPreviewActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateAlwaysHidden" />

<service android:name="com.meiqia.core.MeiQiaService" />
```

5.如果你自己的工程中已经添加了 **/eclipse/MeiqiaSdk/libs** 中的 jar 包，拷贝你自己的工程中对应的 jar 包替换 **/eclipse/MeiqiaSdk/libs** 中的 jar 包

**注意：**报 Cannot find the class file for java.nio.file.OpenOption 错的解决方法：Project -> Properties -> Java Build Path -> Libraries -> Add Library -> JRE System Library -> Select Workspace Default (jdk 1.7*)

## 使用美洽

### 1.初始化
``` java

// MQImageLoader 的实现类目前有 GlideImageloader、PicassoImageLoader、UILImageLoader，根据你自己项目中已使用的图片加载库来选择
MQConfig.init(this, "Your Appkey", new MQImageLoader的实现类(), new OnInitCallback() {
    @Override
    public void onSuccess(String clientId) {
        Toast.makeText(MainActivity.this, "init success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(int code, String message) {
        Toast.makeText(MainActivity.this, "int failure", Toast.LENGTH_SHORT).show();
    }
});
```
如果您不知道 Appkey ，请使用美洽管理员帐号登录 美洽，在「设置」 -> 「SDK」 菜单中查看。如下图：

![获取 Appkey](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/5a999b67233e77dc)

### 2.启动对话界面

初始化成功后，就可以直接启动对话界面

> 使用当前 id 分配上线

``` java
Intent intent = new MQIntentBuilder(this).build();
startActivity(intent);
```

============== 下面是可选设置 ==============

> 绑定开发者用户 id 上线

``` java
Intent intent = new MQIntentBuilder(this)
        .setCustomizedId("开发者的 id") // 相同的 id 会被识别为同一个顾客
        .build();
startActivity(intent);
```

> 设置顾客信息

``` java
HashMap<String, String> clientInfo = new HashMap<>();
clientInfo.put("name", "富坚义博");
clientInfo.put("avatar", "https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/1dee88eabfbd7bd4");
clientInfo.put("gender", "男");
clientInfo.put("tel", "1300000000");
clientInfo.put("技能1", "休刊");
Intent intent = new MQIntentBuilder(this)
        .setClientInfo(clientInfo)
        .build();
startActivity(intent);
```

> 指定客服分配

``` java
Intent intent = new MQIntentBuilder(this)
        .setScheduledAgent(agentId) // agentId 可以从工作台查询
        .build();
startActivity(intent);
```

> 指定客服分组分配

``` java
Intent intent = new MQIntentBuilder(this)
        .setScheduledGroup(groupId) // groupId 可以从工作台查询
        .build();
startActivity(intent);
```

### 3.可选设置
* [绑定自定义 id 并设置上线][2]
* [绑定美洽 id 并设置上线][3]
* [指定客服或者分组][4]
* [开发者自定义当前顾客的信息][5]
* [接收即时消息][6]
* [消息推送][7]

## 文档详情
 [文档详情][1]

 [1]: http://meiqia.com/docs/meiqia-android-sdk/
 [2]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-3
 [3]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-2
 [4]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-4
 [5]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-10
 [6]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-19
 [7]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-32
