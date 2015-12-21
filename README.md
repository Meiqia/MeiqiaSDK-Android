# 美洽移动应用 SDK 3.0 for Android 开发文档

## 目录
* [集成美洽 SDK](#集成美洽-sdk)
* [使用美洽](#使用美洽)
* [API 接口介绍](#api-接口介绍)
* [消息推送](#消息推送)

## ScreenShot
![美恰SDKDemo](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/7b75f3de0b949201)

## 集成美洽 SDK
### AndroidStudio

```groovy
dependencies {
    compile project(':meiqiasdk')
}
```

### Eclipse
>TODO

## 使用美洽

### 初始化
``` java
MQManager.init(context, "Your Appkey", new OnInitCallBackOn() {
	@Override
	public void onSuccess() {
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

![获取 Appkey](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/8fbdaa6076d0b9d0)

### 启动对话界面

初始化成功后，就可以直接启动对话界面

``` java
Intent intent = new Intent(MainActivity.this, MQConversationActivity.class);
startActivity(intent);
``` 
## API 接口介绍
获取 MQManager 实例后，
``` java
MQManager mqManager = MQManager.getInstacne(context);
```
就可以调用下面的接口：

### 设置当前 Client 上线
初始化 SDK 成功后，会默认生成一个顾客，如果没有更改过顾客 id，将以默认生成的顾客上线。
``` java
/**
* 设置当前 Client 上线
*
* @param onlineCallback 回调
*/
setCurrentClientOnline(final OnClientOnlineCallback onlineCallback)
``` 

### 绑定美洽 id，并设置上线
开发者可通过 [获取当前顾客的 id](#获取当前顾客的-id) 接口，取得顾客 id ，保存到开发者的服务端，以此来绑定美洽顾客和开发者用户系统。 如果开发者保存了美洽的顾客 id，可调用如下接口让其上线。调用此接口后，当前可用的顾客即为开发者传的顾客 id。
``` java
/**
 * 绑定美洽 id，并设置上线
 *
 * @param mqClientId     美洽 id
 * @param onlineCallback 回调接口
*/
setClientOnlineWithClientId(String mqClientId, final OnClientOnlineCallback onlineCallback)
``` 
MQConversationActivity.class 内部调用了此接口，所以可以直接简单的在 intent 中添加 CLIENT_ID，启动对话。

**Example:**
``` java
// 启动界面
Intent intent = new Intent(DeveloperActivity.this, MQConversationActivity.class);
// 假设 meiqia_id 是美洽生成的顾客 id
intent.putExtra(MQConversationActivity.CLIENT_ID,"meiqia_id");
startActivity(intent);
``` 
### 绑定自定义 id，并设置上线
如果开发者不愿保存「美洽顾客 id」来绑定自己的用户系统，也可以将自己的用户 id当做参数，进行顾客的上线，美洽将会为开发者绑定一个顾客，下次开发者直接调用如下接口，就能让这个绑定的顾客上线。

调用此接口后，当前可用的顾客即为该自定义 id 对应的顾客 id。

**特别注意：**传给美洽的自定义 id 不能为自增长的，否则非常容易受到中间人攻击，此情况的开发者建议保存美洽顾客 id。
``` java
/**
* 绑定自定义 id，并设置上线
*
* @param customizedId   自定义 id
* @param onlineCallback 回调接口
*/
setClientOnlineWithCustomizedId(String customizedId, final OnClientOnlineCallback onlineCallback)
``` 
MQConversationActivity.class 内部调用了此接口，所以可以直接简单的在 intent 中添加 CUSTOMIZED_ID，启动对话。

**Example:**
``` java
// 启动界面
Intent intent = new Intent(DeveloperActivity.this, MQConversationActivity.class);
// 假设 developer@dev.com 是开发者的用户 id
intent.putExtra(MQConversationActivity.CUSTOMIZED_ID,"developer@dev.com");
startActivity(intent);
``` 

### 指定客服或者分组
美洽默认会按照管理员设置的分配方式智能分配客服，但如果需要让来自 App 的顾客指定分配给某个客服或者某组客服。
``` java
/**
 * 指定客服或者分组
 *
 * @param agentId 指定客服的 id，不指定传 null
 * @param groupId 指定分组的 id，不指定传 null
*/
setScheduledAgentOrGroupWithId(String agentId, String groupId)
``` 

**Example:**
``` java
// 启动界面之前设置
MQManager.getInstance(DeveloperActivity.this).setScheduledAgentOrGroupWithId(agentId, groupId);
// 启动界面
Intent intent = new Intent(DeveloperActivity.this, MQConversationActivity.class);
startActivity(intent);
``` 

**注意：**
 - 该选项需要在用户上线前设置。
 - 客服组 ID 和客服 ID 可以通过管理员帐号在后台的「设置」中查看。 
  ![获取 客服 / 分组 ID](https://camo.githubusercontent.com/63eb2383e2dda083c17eeb16b360777c0e1b0ee9/68747470733a2f2f73332e636e2d6e6f7274682d312e616d617a6f6e6177732e636f6d2e636e2f706963732e6d65697169612e6275636b65742f38636465386235343439316332303365)


### 设置顾客离线
如果没有设置顾客离线，开发者将可以监听广播收到即时消息，用于显示小红点未读标记。

如果设置了顾客离线，则客服发送的消息将会发送给开发者的服务端。

美洽建议：顾客退出聊天界面时，不设置顾客离线，这样开发者仍能监听到收到消息的广播，以便提醒顾客有新消息。
``` java
/**
* 设置顾客离线
* 需要初始化成功后才能调用
* 如果设置了顾客离线，则客服发送的消息将会发送给开发者的推送服务器
*/
setClientOffline()
``` 

### 发送文字消息 / 图片消息 / 语音消息
``` java
/**
 * 发送文字消息
 *
 * @param content               消息内容
 * @param onMessageSendCallback 消息状态回调
*/
sendMQTextMessage(String content, final OnMessageSendCallback onMessageSendCallback)
/**
 * 发送图片消息
 *
 * @param localPath             图片的本地路径
 * @param onMessageSendCallback 消息状态回调
 */
sendMQPhotoMessage(String localPath, final OnMessageSendCallback onMessageSendCallback)
/**
 * 发送语音消息
 *
 * @param localPath             语音的本地路径
 * @param onMessageSendCallback 消息状态回调
 */
sendMQVoiceMessage(String localPath, final OnMessageSendCallback onMessageSendCallback)
``` 
### 从服务器获取历史消息
``` java
/**
 * 从服务器获取历史消息
 *
 * @param lastMessageCreateOn  获取该日期之前的消息
 * @param length               获取的消息长度
 * @param onGetMessageListCallback 回调
 */
getMQMessageFromService(final long lastMessageCreateOn, final int length, final OnGetMessageListCallback onGetMessageListCallback)
``` 
### 从本地获取历史消息
``` java
/**
 * 从服务器获取历史消息
 *
 * @param lastMessageCreateOn  获取该日期之前的消息
 * @param length               获取的消息长度
 * @param onGetMessageListCallback 回调
 */
getMQMessageFromDatabase(final long lastMessageCreateOn, final int length, final OnGetMessageListCallback onGetMessageListCallback)
``` 
### 设置用户的设备唯一标识
``` java
/**
 * 设置用户的设备唯一标识
 *
 * @param token 唯一标识
 */
registerDeviceToken(String token, OkHttpUtils.OnRegisterDeviceTokenCallback onRegisterDeviceTokenCallback)
``` 
App 进入后台后，美洽推送给开发者服务端的消息数据格式中，会有 deviceToken 的字段。

美洽推送消息给开发者服务端的数据格式，可参考 [推送消息数据结构](#推送消息数据结构)。

### 开发者自定义当前顾客的信息，用于展示给客服
``` java
/**
 * 开发者自定义当前顾客的信息，用于展示给客服
 *
 * @param clientInfo           顾客信息
 * @param onClientInfoCallback 回调
 */
setClientInfo(Map<String, String> clientInfo, OnClientInfoCallback onClientInfoCallback)
``` 
功能效果展示：

![设置顾客信息效果图](https://camo.githubusercontent.com/97de68c05a61ac3e2465bb320d669baffa21cc75/68747470733a2f2f73332e636e2d6e6f7274682d312e616d617a6f6e6177732e636f6d2e636e2f706963732e6d65697169612e6275636b65742f36353565373233343334323363386637)

为了让客服能更准确帮助用户，开发者可上传不同用户的属性信息。示例如下：
``` java 
Map<String, String> info = new HashMap<>();
info.put("name", "富坚义博");
info.put("avatar", "https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/1dee88eabfbd7bd4");
info.put("sex", "男");
info.put("tel", "111111");
info.put("技能1", "休刊");
info.put("技能2", "外出取材");
info.put("技能3", "打麻将");
MQManager.getInstance(context).setClientInfo(info, new OnClientInfoCallback()）;
``` 
以下字段是美洽定义好的，开发者可通过上方提到的接口，直接对下方的字段进行设置：

|Key|说明|
|---|---|
|name|真实姓名|
|sex|性别|
|age|年龄|
|job|职业|
|avatar|头像 URL|
|comment|备注|
|tel|电话|
|email|邮箱|
|address|地址|
|qq|QQ号|
|weibo|微博 ID|
|weixin|微信号|

### 获取当前正在接待的客服信息
``` java
/**
 * 获取当前正在接待的客服信息
 *
 * @return 如果存在，返回当前客服信息；不存在，返回 null
 */
getCurrentAgent()
``` 
### 获取当前顾客的 id
``` java
/**
 * 获取当前顾客的顾客 id，开发者可保存该顾客id，下次使用 setClientOnlineWithMQClientId 接口来让该顾客登陆美洽客服系统
 *
 * @return 当前顾客 id
 */
getCurrentClientId()
``` 
### 获取一个新的顾客
``` java
/**
 * 获取一个新的顾客
 *
 * @param onGetMQClientIdCallBack 回调
 */
createMQClient(OnGetMQClientIdCallBackOn onGetMQClientIdCallBack)
``` 
如果开发者想初始化一个新的顾客，可调用此接口。

该顾客没有任何历史记录及用户信息。

### 更新消息阅读状态
``` java
/**
 * 更新消息阅读状态
 *
 * @param messageId 消息id
 * @param isRead    将替换的状态
 */
updateMessage(long messageId, boolean isRead)
``` 
### 结束当前对话
``` java
/**
 * 结束当前对话
 *
 * @param onEndConversationCallback 回调
 */
endCurrentConversation(OnEndConversationCallback onEndConversationCallback)
``` 
### 给客服发送「正在输入」
``` java
/**
 * 将用户正在输入的内容，提供给客服查看。该接口没有调用限制，但每1秒内只会向服务器发送一次数据
 *
 * @param content 正在输入的内容
 */
sendClientInputtingWithContent(String content)
``` 
### 开启美洽推送
``` java
/**
 * App 退到后台时，需要开启美洽推送
 */
openMeiQiaRemotePushService()
``` 
参考 [消息推送](#消息推送)

### 关闭美洽推送
``` java
/**
 * App 进入前台时，需要关闭美洽推送
 */
closeMeiQiaRemotePushService()
``` 
参考 [消息推送](#消息推送)

### 接收即时消息
注册一个 BroadcastReceiver ，监听广播
``` java
public class MessageReceiver extends BroadcastReceiver {

	@Override
    public void onReceive(Context context, Intent intent) {
		 //只接收当前应用的广播
		 String packageName = intent.getStringExtra("packageName");
		
		 if (context.getPackageName().equals(packageName)) {
			 // 获取 ACTION
		     final String action = intent.getAction();
		     // 接收新消息
		     if (MQMessageManager.ACTION_NEW_MESSAGE_RECEIVED.equals(action)) {
		         // 从 intent 获取消息 id
		         String msgId = intent.getStringExtra("msgId");
		         // 从 MCMessageManager 获取消息对象
		         MQMessageManager messageManager = MQMessageManager.getInstance(context);
		         MQMessage message = messageManager.getMQMessage(msgId);
		         // do something
		     }
		
		     // 客服正在输入
		     else if (MQMessageManager.ACTION_AGENT_INPUTTING.equals(action)) {
		         // do something
		     }
		
		     // 客服转接
		     else if (MQMessageManager.ACTION_AGENT_CHANGE_EVENT.equals(action)) {
		         // 获取转接后的客服
		         MQAgent mqAgent = messageManager.getCurrentAgent();
		         // do something
		     }
		  }
	 }
 }
``` 
### 获取 SDK 版本号
``` java
/**
 * 获取 SDK 版本号
 */
getMeiQiaSDKVersion()
``` 

## 消息推送
当前仅支持一种推送方案，即美洽服务端发送消息至开发者的服务端，开发者再推送消息到 App。
### 设置接收推送的服务器地址
推送消息将会发送至开发者的服务器。

设置服务器地址，请使用美洽管理员帐号登录 [美洽](http://www.meiqia.com)，在「设置」 -> 「SDK」中设置。
 
![设置推送地址](https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/8fbdaa6076d0b9d0)

### 通知美洽服务端发送消息至开发者的服务端
目前，美洽的推送是通过推送消息给开发者提供的 URL 上来实现的。

在 App 进入后台时，应该通知美洽服务端，让其将以后的消息推送给开发者提供的服务器地址，如下代码：
``` java
MQManager.getInstance(context).openMeiQiaRemotePushService();
``` 

### 关闭美洽推送
在 App 进入前台时，应该通知美洽服务端，让其将以后的消息发送给SDK，而不再推送给开发者提供的服务端，如下代码：
``` java
MQManager.getInstance(context).closeMeiQiaRemotePushService();
``` 
### 推送消息数据结构
(待补充)

当有消息需要推送时，美洽服务器会向开发者设置的服务器地址发送推送消息，方法类型为 POST，数据格式为 JSON 。

