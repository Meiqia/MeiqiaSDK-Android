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

<!--询前表单-->
<activity
    android:name=".activity.MQInquiryFormActivity"
    android:configChanges="keyboardHidden|orientation"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/MQTheme"
    android:windowSoftInputMode="stateHidden|adjustResize" />

<activity
    android:name=".activity.MQCollectInfoActivity"
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