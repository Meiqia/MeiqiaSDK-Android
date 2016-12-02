package com.meiqia.meiqiasdk.controller;

import android.content.Context;
import android.text.TextUtils;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQAgent;
import com.meiqia.core.bean.MQEnterpriseConfig;
import com.meiqia.core.bean.MQMessage;
import com.meiqia.core.callback.OnClientInfoCallback;
import com.meiqia.core.callback.OnClientPositionInQueueCallback;
import com.meiqia.core.callback.OnGetMessageListCallback;
import com.meiqia.core.callback.OnProgressCallback;
import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnDownloadFileCallback;
import com.meiqia.meiqiasdk.callback.OnEvaluateRobotAnswerCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.callback.SimpleCallback;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.util.List;
import java.util.Map;

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
                MQUtils.parseMQMessageIntoBaseMessage(mcMessage, message);
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onSuccess(message, state);
                }
            }

            @Override
            public void onFailure(MQMessage failureMessage, int code, String response) {
                MQUtils.parseMQMessageIntoBaseMessage(failureMessage, message);
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onFailure(message, code, response);
                }
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
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onSuccess(message, state);
                }
                // 重发成功后删除之前保存的消息
                MQManager.getInstance(context).deleteMessage(preId);
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onFailure(failureMessage, code, failureInfo);
                }
                // 重发失败后删除之前保存的消息
                MQManager.getInstance(context).deleteMessage(preId);
            }
        });
    }

    @Override
    public void getMessageFromService(long lastMessageCreateOn, int length, final OnGetMessageListCallBack onGetMessageListCallBack) {
        MQManager.getInstance(context).getMQMessageFromService(lastMessageCreateOn, length, new OnGetMessageListCallback() {
            @Override
            public void onSuccess(List<MQMessage> mqMessageList) {
                List<BaseMessage> messageList = MQUtils.parseMQMessageToChatBaseList(mqMessageList);
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onSuccess(messageList);
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onFailure(code, message);
                }
            }
        });
    }

    @Override
    public void getMessagesFromDatabase(long lastMessageCreateOn, int length, final OnGetMessageListCallBack onGetMessageListCallBack) {
        MQManager.getInstance(context).getMQMessageFromDatabase(lastMessageCreateOn, length, new OnGetMessageListCallback() {

            @Override
            public void onSuccess(List<MQMessage> mqMessageList) {
                List<BaseMessage> messageList = MQUtils.parseMQMessageToChatBaseList(mqMessageList);
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onSuccess(messageList);
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onFailure(code, message);
                }
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
                if (onClientOnlineCallback != null) {
                    onClientOnlineCallback.onSuccess(agent, conversationId, messageList);
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onClientOnlineCallback != null) {
                    onClientOnlineCallback.onFailure(code, message);
                }
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
    public void setClientInfo(Map<String, String> clientInfo, final SimpleCallback onClientInfoCallback) {
        MQManager.getInstance(context).setClientInfo(clientInfo, new OnClientInfoCallback() {
            @Override
            public void onSuccess() {
                if (onClientInfoCallback != null) {
                    onClientInfoCallback.onSuccess();
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onClientInfoCallback != null) {
                    onClientInfoCallback.onFailure(code, message);
                }
            }
        });
    }

    @Override
    public void sendClientInputtingWithContent(String content) {
        MQManager.getInstance(context).sendClientInputtingWithContent(content);
    }

    @Override
    public void executeEvaluate(String conversationId, int level, String content, final SimpleCallback simpleCallback) {
        MQManager.getInstance(context).executeEvaluate(conversationId, level, content, new com.meiqia.core.callback.SimpleCallback() {

            @Override
            public void onFailure(int code, String message) {
                if (simpleCallback != null) {
                    simpleCallback.onFailure(code, message);
                }
            }

            @Override
            public void onSuccess() {
                if (simpleCallback != null) {
                    simpleCallback.onSuccess();
                }
            }
        });
    }

    @Override
    public Agent getCurrentAgent() {
        MQAgent mqAgent = MQManager.getInstance(context).getCurrentAgent();
        return MQUtils.parseMQAgentToAgent(mqAgent);
    }

    @Override
    public void updateMessage(long messageId, boolean isRead) {
        MQManager.getInstance(context).updateMessage(messageId, isRead);
    }

    @Override
    public void saveConversationOnStopTime(long stopTime) {
        MQManager.getInstance(context).saveConversationOnStopTime(stopTime);
    }

    @Override
    public void saveConversationLastMessageTime(long lastMessageTime) {
        MQManager.getInstance(context).saveConversationLastMessageTime(lastMessageTime);
    }

    @Override
    public void downloadFile(BaseMessage fileMessage, final OnDownloadFileCallback onDownloadFileCallback) {
        MQMessage message = MQUtils.parseBaseMessageToMQMessage(fileMessage);
        MQManager.getInstance(context).downloadFile(message, new OnProgressCallback() {
            @Override
            public void onSuccess() {
                if (onDownloadFileCallback == null) {
                    return;
                }
                onDownloadFileCallback.onSuccess(null);
            }

            @Override
            public void onProgress(int progress) {
                if (onDownloadFileCallback == null) {
                    return;
                }
                onDownloadFileCallback.onProgress(progress);
            }

            @Override
            public void onFailure(int code, String message) {
                if (onDownloadFileCallback == null) {
                    return;
                }
                onDownloadFileCallback.onFailure(code, message);
            }
        });
    }

    @Override
    public void cancelDownload(String url) {
        MQManager.getInstance(context).cancelDownload(url);
    }

    @Override
    public void onConversationClose() {
        MQManager.getInstance(context).onConversationClose();
    }

    @Override
    public void onConversationOpen() {
        MQManager.getInstance(context).onConversationOpen();
    }

    @Override
    public void closeService() {
        MQManager.getInstance(context).closeMeiqiaService();
    }

    @Override
    public void openService() {
        MQManager.getInstance(context).openMeiqiaService();
    }

    public void submitMessageForm(String message, List<String> pictures, Map<String, String> customInfoMap, final SimpleCallback simpleCallback) {
        MQManager.getInstance(context).submitMessageForm(message, pictures, customInfoMap, new com.meiqia.core.callback.SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
                if (simpleCallback != null) {
                    simpleCallback.onFailure(code, message);
                }
            }

            @Override
            public void onSuccess() {
                if (simpleCallback != null) {
                    simpleCallback.onSuccess();
                }
            }
        });
    }

    @Override
    public void refreshEnterpriseConfig(final SimpleCallback simpleCallback) {
        MQManager.getInstance(context).refreshEnterpriseConfig(new com.meiqia.core.callback.SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
                if (simpleCallback != null) {
                    simpleCallback.onFailure(code, message);
                }
            }

            @Override
            public void onSuccess() {
                if (simpleCallback != null) {
                    simpleCallback.onSuccess();
                }
            }
        });
    }

    @Override
    public MQEnterpriseConfig getEnterpriseConfig() {
        return MQManager.getInstance(context).getEnterpriseConfig();
    }

    @Override
    public void evaluateRobotAnswer(long messageId, long questionId, int useful, final OnEvaluateRobotAnswerCallback onEvaluateRobotAnswerCallback) {
        MQManager.getInstance(context).evaluateRobotAnswer(messageId, questionId, useful, new com.meiqia.core.callback.OnEvaluateRobotAnswerCallback() {
            @Override
            public void onFailure(int code, String message) {
                if (onEvaluateRobotAnswerCallback != null) {
                    onEvaluateRobotAnswerCallback.onFailure(code, message);
                }
            }

            @Override
            public void onSuccess(String message) {
                if (onEvaluateRobotAnswerCallback != null) {
                    onEvaluateRobotAnswerCallback.onSuccess(message);
                }
            }
        });
    }

    public void setForceRedirectHuman(boolean isForceRedirectHuman) {
        MQManager.getInstance(context).setForceRedirectHuman(isForceRedirectHuman);
    }

    @Override
    public void getClientPositionInQueue(final OnClientPositionInQueueCallback onClientPositionInQueueCallback) {
        MQManager.getInstance(context).getClientPositionInQueue(new OnClientPositionInQueueCallback() {
            @Override
            public void onSuccess(int position) {
                if (onClientPositionInQueueCallback != null) {
                    onClientPositionInQueueCallback.onSuccess(position);
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onClientPositionInQueueCallback != null) {
                    onClientPositionInQueueCallback.onFailure(code, message);
                }
            }
        });
    }

    @Override
    public boolean getIsWaitingInQueue() {
        return MQManager.getInstance(context).getIsWaitingInQueue();
    }

    @Override
    public String getCurrentClientId() {
        return MQManager.getInstance(context).getCurrentClientId();
    }
}
