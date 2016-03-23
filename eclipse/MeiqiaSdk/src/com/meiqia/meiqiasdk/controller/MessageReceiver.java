package com.meiqia.meiqiasdk.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.meiqia.core.MQMessageManager;
import com.meiqia.core.bean.MQAgent;
import com.meiqia.core.bean.MQMessage;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.util.MQUtils;

public abstract class MessageReceiver extends BroadcastReceiver {
    private String mConversationId;

    public void setConversationId(String conversationId) {
        this.mConversationId = conversationId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        MQMessageManager messageManager = MQMessageManager.getInstance(context);
        BaseMessage baseMessage;

        // 接收新消息
        if (MQMessageManager.ACTION_NEW_MESSAGE_RECEIVED.equals(action)) {
            // 从 intent 获取消息 id
            String msgId = intent.getStringExtra("msgId");
            // 从 MCMessageManager 获取消息对象
            MQMessage message = messageManager.getMQMessage(msgId);
            if (message != null) {
                //处理消息，并发送广播
                baseMessage = MQUtils.parseMQMessageToBaseMessage(message);
                receiveNewMsg(baseMessage);
            }
        }

        // 客服正在输入
        else if (MQMessageManager.ACTION_AGENT_INPUTTING.equals(action)) {
            changeTitleToInputting();
        }

        // 客服转接
        else if (MQMessageManager.ACTION_AGENT_CHANGE_EVENT.equals(action)) {
            // 更新标题栏
            MQAgent mqAgent = messageManager.getCurrentAgent();

            // 如果顾客被转接，才添加 Tip
            boolean isClientDirect = intent.getBooleanExtra("client_is_redirected", false);
            if (isClientDirect) {
                addDirectAgentMessageTip(mqAgent.getNickname());
            }

            changeTitleToAgentName(mqAgent.getNickname());
            Agent agent = MQUtils.parseMQAgentToAgent(mqAgent);
            setCurrentAgent(agent);

            String conversationId = intent.getStringExtra("conversation_id");
            if (!TextUtils.isEmpty(conversationId)) {
                mConversationId = conversationId;
                setNewConversationId(conversationId);
            }
        } else if (MQMessageManager.ACTION_INVITE_EVALUATION.equals(action)) {
            String conversationId = intent.getStringExtra("conversation_id");
            if (conversationId.equals(mConversationId)) {
                inviteEvaluation();
            }
        }
    }

    public abstract void receiveNewMsg(BaseMessage message);

    public abstract void changeTitleToInputting();

    public abstract void changeTitleToAgentName(String agentNickname);

    public abstract void addDirectAgentMessageTip(String agentNickname);

    public abstract void setCurrentAgent(Agent agent);


    public abstract void inviteEvaluation();

    public abstract void setNewConversationId(String newConversationId);
}
