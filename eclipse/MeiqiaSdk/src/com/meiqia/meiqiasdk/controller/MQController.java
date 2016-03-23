package com.meiqia.meiqiasdk.controller;


import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.callback.SimpleCallback;
import com.meiqia.meiqiasdk.model.BaseMessage;

import java.util.List;
import java.util.Map;

public interface MQController {

    String ACTION_NEW_MESSAGE_RECEIVED = "new_msg_received_action";
    String ACTION_AGENT_INPUTTING = "agent_inputting_action";
    String ACTION_CLIENT_IS_REDIRECTED_EVENT = "agent_change_action";
    String ACTION_INVITE_EVALUATION = "invite_evaluation";

    void sendMessage(BaseMessage baseMessage, OnMessageSendCallback onMessageSendCallback);

    void resendMessage(BaseMessage baseMessage, OnMessageSendCallback onMessageSendCallback);

    /**
     * 从服务器获取历史消息
     *
     * @param messageCreateOn          获取该日期之前的消息
     * @param length                   获取的消息长度
     * @param onGetMessageListCallBack 回调
     */
    void getMessageFromService(final long messageCreateOn, final int length, final OnGetMessageListCallBack onGetMessageListCallBack);

    /**
     * 从本地服务器取历史消息
     *
     * @param lastMessageCreateOn      获取该日期之前的消息
     * @param length                   获取的消息长度
     * @param onGetMessageListCallBack 回调
     */
    void getMessagesFromDatabase(final long lastMessageCreateOn, final int length, final OnGetMessageListCallBack onGetMessageListCallBack);

    /**
     * 设置顾客上线
     *
     * @param clientId               美洽顾客 id：如果传了，将用美洽 id 上线
     * @param customizedId           开发者用户 id：如果传了，将绑定开发者 id 上线
     * @param onClientOnlineCallback 回调
     */
    void setCurrentClientOnline(String clientId, String customizedId, OnClientOnlineCallback onClientOnlineCallback);

    /**
     * 发送「顾客正在输入」状态
     *
     * @param content 内容
     */
    void sendClientInputtingWithContent(String content);

    /**
     * 对客服进行评价
     *
     * @param conversationId 当前会话id
     * @param level          评价的等级
     * @param content        评价的内容
     * @param simpleCallback 评价的回调接口
     */
    void executeEvaluate(String conversationId, int level, String content, SimpleCallback simpleCallback);

    void closeService();

    /**
     * 添加留言表单
     *
     * @param message        留言消息
     * @param pictures       图片消息集合
     * @param customInfoMap  自定义消息
     * @param simpleCallback 添加留言表单的回调接口
     */
    void submitMessageForm(String message, List<String> pictures, Map<String, String> customInfoMap, SimpleCallback simpleCallback);

    /**
     * 刷新企业配置信息
     *
     * @param simpleCallback
     */
    void refreshEnterpriseConfig(SimpleCallback simpleCallback);

    /**
     * 获取离线消息模板
     *
     * @return
     */
    String getLeaveMessageTemplete();
}
