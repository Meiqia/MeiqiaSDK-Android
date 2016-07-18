# 美洽移动应用 SDK 3.0 for Android 开发文档

## ScreenShot
![美恰SDKDemo](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/155c91da06ea9bfd)

## 集成美洽 SDK

### Environment Required
- JDK7+

### AndroidStudio  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk)

```
// -------------------- 以下三个库是必须依赖的 ----------------------------
compile 'com.meiqia:meiqiasdk:3.2.4@aar'
compile 'com.android.support:support-v4:23.1.1'
compile 'com.squareup.okhttp3:okhttp:3.3.1'
// -------------------- 以上三个库是必须依赖的 ----------------------------

// 目前支持常见的 4 种图片加载库，必须在下面四个图片加载库中选择一个添加依赖
compile 'com.nostra13.universalimageloader:universal-image-loader:1.9.5'
//    compile 'com.github.bumptech.glide:glide:3.7.0'
//    compile 'com.squareup.picasso:picasso:2.5.2'
//    compile 'org.xutils:xutils:3.3.36'
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

<!--留言表单界面-->
<activity
    android:name="com.meiqia.meiqiasdk.activity.MQMessageFormActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateHidden|adjustResize" />

<!--WebView 界面-->
<activity
    android:name=".activity.MQWebViewActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateHidden|adjustResize" />

<service android:name="com.meiqia.core.MeiQiaService" />
```

5.如果你自己的工程中已经添加了 **/eclipse/MeiqiaSdk/libs** 中的 jar 包，拷贝你自己的工程中对应的 jar 包替换 **/eclipse/MeiqiaSdk/libs** 中的 jar 包

**注意：**
报 Cannot find the class file for java.nio.file.OpenOption 错的解决方法：Project -> Properties -> Java Build Path -> Libraries -> Add Library -> JRE System Library -> Select Workspace Default (jdk 1.7*)

如果编译失败, Project Build Target 必须指定 Android 6.0

## 使用美洽

### 1.初始化
``` java
MQConfig.init(this, "Your Appkey", new OnInitCallback() {
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

### 3.启动留言表单界面

目前是两种模式：
(1) 完全对话模式
无机器人时：如果当前客服不在线，直接聊天界面输入就是留言，客服上线后能够直接回复，如果客服在线，则进入正常客服对话模式。
有机器人时：如果当前客服不在线时，直接聊天界面输入的话，还是由机器人回答，顾客点击留言就会跳转到表单。
(2) 单一表单模式
不管客服是否在线都会进入表单，顾客提交后，不会有聊天的界面。这种主要用于一些 App 只需要用户反馈，不需要直接回复的形式。

``` java
startActivity(new Intent(this, MQMessageFormActivity.class));
```

### 4.Android M 权限处理

如果你的 App 需要兼容 Android M，需要处理权限问题。 [参考 Demo][8]

## 可选设置

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

> 自定义留言表单引导文案

配置了该引导文案后将不会读取工作台配置的引导文案。
最佳实践：劲量不要在 SDK 中配置引导文案，而是通过工作台配置引导文案，方便在节假日的时候统一配置各终端的引导文案，避免重新打包发布 App。

``` java
MQConfig.leaveMessageIntro = "自定义留言表单引导文案";
```

> 自定义留言表单输入项

如果不配置该选项则留言表单界面默认有「留言」「邮箱」「手机」这三个输入项

``` java
MQConfig.messageFormInputModels = new ArrayList<>();
MessageFormInputModel phoneMfim = new MessageFormInputModel();
phoneMfim.tip = "手机";
phoneMfim.key = "tel";
phoneMfim.required = true;
phoneMfim.hint = "请输入你的手机号";
phoneMfim.inputType = InputType.TYPE_CLASS_PHONE;

MessageFormInputModel emailMfim = new MessageFormInputModel();
emailMfim.tip = "邮箱";
emailMfim.key = "email";
emailMfim.required = true;
emailMfim.hint = "请输入你的邮箱";
emailMfim.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

MessageFormInputModel nameMfim = new MessageFormInputModel();
nameMfim.tip = "姓名";
nameMfim.key = "name";
nameMfim.hint = "请输入你的姓名";
nameMfim.inputType = InputType.TYPE_CLASS_TEXT;

MQConfig.messageFormInputModels.add(phoneMfim);
MQConfig.messageFormInputModels.add(emailMfim);
MQConfig.messageFormInputModels.add(nameMfim);
```

### 3.可选设置
* [绑定自定义 id 并设置上线][2]
* [绑定美洽 id 并设置上线][3]
* [指定客服或者分组][4]
* [开发者自定义当前顾客的信息][5]
* [接收即时消息][6]
* [消息推送][7]

## Proguard

```
## ----------------------------------
##      OkHttp相关
## ----------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn com.squareup.okhttp3.**

## ----------------------------------
##      Okio相关
## ----------------------------------
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

## ----------------------------------
##      UIL相关
## ----------------------------------
-keep class com.nostra13.universalimageloader.** { *; }
-keepclassmembers class com.nostra13.universalimageloader.** {*;}
-dontwarn com.nostra13.universalimageloader.**

## ----------------------------------
##      Glide相关
## ----------------------------------
-keep class com.bumptech.glide.Glide { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.**

## ----------------------------------
##      Picasso相关
## ----------------------------------
-keep class com.squareup.picasso.Picasso { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.picasso.**

## ----------------------------------
##      xUtils3相关
## ----------------------------------
-keepattributes Signature,*Annotation*
-keep public class org.xutils.** {
    public protected *;
}
-keep public interface org.xutils.** {
    public protected *;
}
-keepclassmembers class * extends org.xutils.** {
    public protected *;
}
-keepclassmembers @org.xutils.db.annotation.* class * {*;}
-keepclassmembers @org.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @org.xutils.view.annotation.Event <methods>;
}
-dontwarn org.xutils.**
```

## 文档详情
 [文档详情][1]

 [1]: http://meiqia.com/docs/meiqia-android-sdk/
 [2]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-3
 [3]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-2
 [4]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-4
 [5]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-10
 [6]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-19
 [7]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-32
 [8]: https://github.com/Meiqia/MeiqiaSDK-Android/blob/master/demo%2Fsrc%2Fmain%2Fjava%2Fcom%2Fmeiqia%2Fmeiqiasdk%2Fdemo%2FMainActivity.java
