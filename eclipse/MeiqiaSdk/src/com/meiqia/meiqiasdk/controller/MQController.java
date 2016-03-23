package com.meiqia.meiqiasdk.controller;


import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.callback.SimpleCallback;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;

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
     * 设置当前顾客的自定义信息
     *
     * @param clientInfo           当前顾客的自定义信息
     * @param onClientInfoCallback 回调
     */
    void setClientInfo(Map<String, String> clientInfo, SimpleCallback onClientInfoCallback);

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
     * 获取当前客服
     * @return 当前客服，如果没有返回 null
     */
    Agent getCurrentAgent();

    void updateMessage(long messageId, boolean isRead);

    /**
     * 保存聊天界面不可见时的最后一条消息的时间
     *
     * @param stopTime
     */
    void saveConversationOnStopTime(long stopTime);
}
