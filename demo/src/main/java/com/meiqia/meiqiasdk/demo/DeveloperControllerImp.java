package com.meiqia.meiqiasdk.demo;

import android.content.Context;
import android.text.TextUtils;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQAgent;
import com.meiqia.core.bean.MQMessage;
import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.controller.ControllerImpl;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.util.List;

public class DeveloperControllerImp extends ControllerImpl {

    private String customizedId;
    private String clientId;

    public DeveloperControllerImp(Context context) {
        super(context);
    }

    @Override
    public void setCurrentClientOnline(final OnClientOnlineCallback onClientOnlineCallback) {
        if (!TextUtils.isEmpty(customizedId)) {
            MQManager.getInstance(context).setClientOnlineWithCustomizedId(customizedId, new com.meiqia.core.callback.OnClientOnlineCallback() {
                @Override
                public void onSuccess(MQAgent mqAgent, List<MQMessage> conversationMessageList) {
                    Agent agent = MQUtils.parseMQAgentToAgent(mqAgent);
                    List<BaseMessage> messageList = MQUtils.parseMQMessageToChatBaseList(conversationMessageList);
                    onClientOnlineCallback.onSuccess(agent, messageList);
                    //清除
                    clearCache();
                }

                @Override
                public void onFailure(int code, String message) {
                    onClientOnlineCallback.onFailure(code, message);
                }
            });
        } else if (!TextUtils.isEmpty(clientId)) {
            MQManager.getInstance(context).setClientOnlineWithClientId(clientId, new com.meiqia.core.callback.OnClientOnlineCallback() {
                @Override
                public void onSuccess(MQAgent mqAgent, List<MQMessage> conversationMessageList) {
                    Agent agent = MQUtils.parseMQAgentToAgent(mqAgent);
                    List<BaseMessage> messageList = MQUtils.parseMQMessageToChatBaseList(conversationMessageList);
                    onClientOnlineCallback.onSuccess(agent, messageList);
                    //清除
                    clearCache();
                }

                @Override
                public void onFailure(int code, String message) {
                    onClientOnlineCallback.onFailure(code, message);
                }
            });
        } else {
            super.setCurrentClientOnline(onClientOnlineCallback);
        }
    }

    public void setCustomizedId(String customizedId) {
        this.customizedId = customizedId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private void clearCache() {
        this.clientId = null;
        this.customizedId = null;
    }
}
