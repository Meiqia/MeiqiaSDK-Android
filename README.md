# 美洽移动应用 SDK 3.0 for Android 开发文档

## ScreenShot
![美恰SDKDemo](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/155c91da06ea9bfd)

## 集成美洽 SDK

### Environment Required
- JDK7+

### AndroidStudio  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk)

```
// -------------------- 以下三个库是必须依赖的 okhttp 必须 3.5.0 或者更高版本 ----------------------------
implementation 'com.meiqia:meiqiasdk:+'
implementation 'com.android.support:support-v4:23.1.1'
implementation 'com.squareup.okhttp3:okhttp:3.5.0'
// -------------------- 以上三个库是必须依赖的 okhttp 必须 3.5.0 或者更高版本 ----------------------------

// 目前支持常见的 4 种图片加载库，必须在下面四个图片加载库中选择一个添加依赖
implementation 'com.nostra13.universalimageloader:universal-image-loader:1.9.5'
//    implementation 'com.squareup.picasso:picasso:2.5.2'
//    implementation 'org.xutils:xutils:3.3.36'
//    implementation 'com.github.bumptech.glide:glide:4.9.0'
//    annotationProcessor 'com.github.bumptech.glide:compiler:4.9.0'
```
> 如果使用的是 glide 3.x ，可以参考 [自定义 ImageLoader][10]

### Eclipse
 [查看详情][9]

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

### 5.Android O 权限处理

如果你的 App 需要兼容 Android O，需要在 App 后台的时候确保关闭美洽服务。
``` java
MQManager.getInstance(context).closeMeiqiaService();
```


## 常见使用场景

> 开发者的 App 有自己的账号系统，希望每个账号对应不同的顾客，有不同的聊天记录。那就需要开发者在启动对话的时候，绑定账号：
``` java
Intent intent = new MQIntentBuilder(this)
        .setCustomizedId("开发者的 id") // 相同的 id 会被识别为同一个顾客
        .build();
startActivity(intent);
```

> 开发者希望顾客上线的时候，能够上传（或者更新）一些用户的自定义信息：

``` java
HashMap<String, String> clientInfo = new HashMap<>();
clientInfo.put("name", "富坚义博");
clientInfo.put("avatar", "https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/1dee88eabfbd7bd4");
clientInfo.put("gender", "男");
clientInfo.put("tel", "1300000000");
clientInfo.put("技能1", "休刊");

HashMap<String, String> updateInfo = new HashMap<>();
updateInfo.put("name", "update name");

Intent intent = new MQIntentBuilder(this)
        .setClientInfo(clientInfo) // 设置顾客信息 PS: 这个接口只会生效一次,如果需要更新顾客信息,需要调用更新接口
//      .updateClientInfo(updateInfo) // 更新顾客信息 PS: 如果客服在工作台更改了顾客信息，更新接口会覆盖之前的内容
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

> 设置预发送消息

``` java
Intent intent = new MQIntentBuilder(this)
        .setPreSendTextMessage("我是预发送文字消息")
        .setPreSendImageMessage(new File("预发送图片的路径"))
        .build();
startActivity(intent);
```

> 设置监听 MQConversationActivity 生命周期的回调接口

``` java
MQConfig.setActivityLifecycleCallback(new MQSimpleActivityLifecycleCallback() {
});
```


## 常见问题列表

- **java.lang.NoClassDefFoundError: com.meiqia.core.xx**

   没有依赖 okhttp3.5.0 或者 以上版本，检查依赖设置

- **code == 400 track_id 错误**

   如果需要绑定用户 id，请使用 setCustomizedId 接口；如果还是有问题，就换一个 id 绑定再试试

- **客服名字显示 null**

   更新最新版 SDK

- **java.lang.NoSuchMethodError: No Virtual method displayImage xxxx**

   如果使用的是 glide 4.x ，可以参考 https://github.com/Meiqia/MeiqiaSDK-Android/blob/master/imageloader/MQGlideImageLoader4.java

- **后台改了配置，SDK 不生效**

   SDK 的配置不是立即生效，会至少间隔 15 分钟刷新一次，刷新后下次生效。如果想要立即看到配置改变的效果，可以卸载应用重新安装。

## 全部文档
[查看详情][1]

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

 [1]: http://meiqia.com/docs/meiqia-android-sdk/
 [2]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-3
 [3]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-2
 [4]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-4
 [5]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-10
 [6]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-11-19
 [7]: http://meiqia.com/docs/meiqia-android-sdk/#tocAnchor-1-32
 [8]: https://github.com/Meiqia/MeiqiaSDK-Android/blob/master/demo%2Fsrc%2Fmain%2Fjava%2Fcom%2Fmeiqia%2Fmeiqiasdk%2Fdemo%2FMainActivity.java
 [9]: https://github.com/Meiqia/MeiqiaSDK-Android/blob/master/Eclipse_README.md
 [10]: https://github.com/Meiqia/MeiqiaSDK-Android/blob/master/imageloader/MQGlideImageLoader.java
