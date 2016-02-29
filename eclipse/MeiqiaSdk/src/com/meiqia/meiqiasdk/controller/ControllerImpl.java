package com.meiqia.meiqiasdk.controller;

import android.content.Context;
import android.text.TextUtils;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQAgent;
import com.meiqia.core.bean.MQMessage;
import com.meiqia.core.callback.OnEvaluateCallback;
import com.meiqia.core.callback.OnGetMessageListCallback;
import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.util.List;

public class ControllerImpl implements MQController {

    public Context context;

    public ControllerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void sendMessage(final BaseMessage message, final OnMessageSendCallback onMessageSendCallback) {
        // 发送回调
        com.meiqia.core.callback.OnMessageSendCallback onMQMessageSendCallback = new com.meiqia.core.callback.OnMessageSendCallback() {
            @Override
            public void onSuccess(MQMessage mcMessage, int state) {
                MQUtils.parseMQMessageIntoChatBase(mcMessage, message);
                onMessageSendCallback.onSuccess(message, state);
            }

            @Override
            public void onFailure(MQMessage failureMessage, int code, String response) {
                MQUtils.parseMQMessageIntoChatBase(failureMessage, message);
                onMessageSendCallback.onFailure(message, code, response);
            }
        };

        // 开始发送
        if (BaseMessage.TYPE_CONTENT_TEXT.equals(message.getContentType())) {
            String content = message.getContent();
            MQManager.getInstance(context).sendMQTextMessage(content, onMQMessageSendCallback);
        } else if (BaseMessage.TYPE_CONTENT_PHOTO.equals(message.getContentType())) {
            PhotoMessage photoMessage = (PhotoMessage) message;
            MQManager.getInstance(context).sendMQPhotoMessage(photoMessage.getLocalPath(), onMQMessageSendCallback);
        } else if (BaseMessage.TYPE_CONTENT_VOICE.equals(message.getContentType())) {
            VoiceMessage voiceMessage = (VoiceMessage) message;
            MQManager.getInstance(context).sendMQVoiceMessage(voiceMessage.getLocalPath(), onMQMessageSendCallback);
        }
    }

    @Override
    public void resendMessage(final BaseMessage baseMessage, final OnMessageSendCallback onMessageSendCallback) {
        final long preId = baseMessage.getId();
        sendMessage(baseMessage, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                onMessageSendCallback.onSuccess(message, state);
                // 重发成功后删除之前保存的消息
                MQManager.getInstance(context).deleteMessage(preId);
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                onMessageSendCallback.onFailure(failureMessage, code, failureInfo);
            }
        });
    }

    @Override
    public void getMessageFromService(long lastMessageCreateOn, int length, final OnGetMessageListCallBack onGetMessageListCallBack) {
        MQManager.getInstance(context).getMQMessageFromService(lastMessageCreateOn, length, new OnGetMessageListCallback() {
            @Override
            public void onSuccess(List<MQMessage> mqMessageList) {
                List<BaseMessage> messageList = MQUtils.parseMQMessageToChatBaseList(mqMessageList);
                onGetMessageListCallBack.onSuccess(messageList);
            }

            @Override
            public void onFailure(int code, String message) {
                onGetMessageListCallBack.onFailure(code, message);
            }
        });
    }

    @Override
    public void getMessagesFromDatabase(long lastMessageCreateOn, int length, final OnGetMessageListCallBack onGetMessageListCallBack) {
        MQManager.getInstance(context).getMQMessageFromDatabase(lastMessageCreateOn, length, new OnGetMessageListCallback() {

            @Override
            public void onSuccess(List<MQMessage> mqMessageList) {
                List<BaseMessage> messageList = MQUtils.parseMQMessageToChatBaseList(mqMessageList);
                onGetMessageListCallBack.onSuccess(messageList);
            }

            @Override
            public void onFailure(int code, String message) {
                onGetMessageListCallBack.onFailure(code, message);
            }
        });
    }

    @Override
    public void setCurrentClientOnline(String clientId, String customizedId, final OnClientOnlineCallback onClientOnlineCallback) {
        com.meiqia.core.callback.OnClientOnlineCallback onlineCallback = new com.meiqia.core.callback.OnClientOnlineCallback() {
            @Override
            public void onSuccess(MQAgent mqAgent, String conversationId, List<MQMessage> conversationMessageList) {
                Agent agent = MQUtils.parseMQAgentToAgent(mqAgent);
                List<BaseMessage> messageList = MQUtils.parseMQMessageToChatBaseList(conversationMessageList);
                onClientOnlineCallback.onSuccess(agent, conversationId, messageList);
            }

            @Override
            public void onFailure(int code, String message) {
                onClientOnlineCallback.onFailure(code, message);
            }
        };

        if (!TextUtils.isEmpty(clientId)) {
            MQManager.getInstance(context).setClientOnlineWithClientId(clientId, onlineCallback);
        } else if (!TextUtils.isEmpty(customizedId)) {
            MQManager.getInstance(context).setClientOnlineWithCustomizedId(customizedId, onlineCallback);
        } else {
            MQManager.getInstance(context).setCurrentClientOnline(onlineCallback);
        }
    }

    @Override
    public void sendClientInputtingWithContent(String content) {
        MQManager.getInstance(context).sendClientInputtingWithContent(content);
    }

    @Override
    public void executeEvaluate(String conversationId, int level, String content, OnEvaluateCallback onEvaluateCallback) {
        MQManager.getInstance(context).executeEvaluate(conversationId, level, content, onEvaluateCallback);
    }

    @Override
    public void closeService() {
        MQManager.getInstance(context).closeMeiqiaService();
    }

}
