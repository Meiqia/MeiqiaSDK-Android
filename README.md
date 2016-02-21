# 美洽移动应用 SDK 3.0 for Android 开发文档

## ScreenShot
![美恰SDKDemo](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/155c91da06ea9bfd)

## 集成美洽 SDK

### Environment Required
- JDK7+

### AndroidStudio  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.meiqia/meiqiasdk)

```
// required     
// 「latestVersion」改成 maven central 徽章后面对应的版本号，例如3.0.3
compile 'com.meiqia:meiqiasdk:latestVersion@aar'

// 在下面的依赖中，如果你的项目已经依赖过其中的组件，则不需要重复依赖
compile 'com.android.support:support-v4:23.1.1'
compile 'com.squareup.okhttp:okhttp:2.7.0'
compile 'org.java-websocket:Java-WebSocket:1.3.0'
compile 'com.commit451:PhotoView:1.2.4'
compile 'com.nostra13.universalimageloader:universal-image-loader:1.9.5'
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
<activity
    android:name="com.meiqia.meiqiasdk.activity.MQConversationActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateHidden|adjustResize" />

<service android:name="com.meiqia.core.MeiQiaService" />
```

5.如果你自己的工程中已经添加了 **/eclipse/MeiqiaSdk/libs** 中的 jar 包，拷贝你自己的工程中对应的 jar 包替换 **/eclipse/MeiqiaSdk/libs** 中的 jar 包

**注意：**报 Cannot find the class file for java.nio.file.OpenOption 错的解决方法：Project -> Properties -> Java Build Path -> Libraries -> Add Library -> JRE System Library -> Select Workspace Default (jdk 1.7*)

## 使用美洽

### 1.初始化
``` java
MQManager.init(context, "Your Appkey", new OnInitCallback() {
	@Override
	public void onSuccess(String clientId) {
		// 初始化成功
	    Toast.makeText(MainActivity.this, "init success", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onFailure(int code, String message) {
		// 初始化失败
	    Toast.makeText(MainActivity.this, "init failure", Toast.LENGTH_SHORT).show();
	}
});
```
如果您不知道 Appkey ，请使用美洽管理员帐号登录 美洽，在「设置」 -> 「SDK」 菜单中查看。如下图：

![获取 Appkey](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/5a999b67233e77dc)

### 2.启动对话界面

初始化成功后，就可以直接启动对话界面

``` java
Intent intent = new Intent(MainActivity.this, MQConversationActivity.class);
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
