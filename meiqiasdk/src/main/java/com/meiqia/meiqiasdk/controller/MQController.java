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
     * @param messageCreateOn   获取该日期之前的消息
     * @param length                获取的消息长度
     * @param onGetMessageListCallBack 回调
     */
    void getMessageFromService(final long messageCreateOn, final int length, final OnGetMessageListCallBack onGetMessageListCallBack);

    /**
     * 从本地服务器取历史消息
     *
     * @param lastMessageCreateOn   获取该日期之前的消息
     * @param length                获取的消息长度
     * @param onGetMessageListCallBack 回调
     */
    void getMessagesFromDatabase(final long lastMessageCreateOn, final int length, final OnGetMessageListCallBack onGetMessageListCallBack);

    void setCurrentClientOnline(OnClientOnlineCallback onClientOnlineCallback);

}
