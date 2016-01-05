package com.meiqia.meiqiasdk.controller;


import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.model.BaseMessage;

public interface MQController {

    String ACTION_NEW_MESSAGE_RECEIVED = "new_msg_received_action";
    String ACTION_AGENT_INPUTTING = "agent_inputting_action";
    String ACTION_CLIENT_IS_REDIRECTED_EVENT = "agent_change_action";

    void sendMessage(BaseMessage baseMessage, OnMessageSendCallback onMessageSendCallback);

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
     * @param clientId 美洽顾客 id：如果传了，将用美洽 id 上线
     * @param customizedId 开发者用户 id：如果传了，将绑定开发者 id 上线
     * @param onClientOnlineCallback 回调
     */
    void setCurrentClientOnline(String clientId, String customizedId, OnClientOnlineCallback onClientOnlineCallback);

    /**
     * 发送「顾客正在输入」状态
     * @param content 内容
     */
    void sendClientInputtingWithContent(String content);
}
