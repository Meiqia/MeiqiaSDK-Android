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
        mConversationId = conversationId;
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
                if (baseMessage != null) {
                    receiveNewMsg(baseMessage);
                }
            }
        }
        // 撤回消息
        else if (MQMessageManager.ACTION_RECALL_MESSAGE.equals(action)) {
            String nickname = intent.getStringExtra("nickname");
            long id = intent.getLongExtra("id", -1);
            recallMessage(id, nickname);
        }

        // 客服发送线索卡片
        else if (MQMessageManager.ACTION_AGENT_SEND_CLUE_CARD.equals(action)) {
            String clueCardMessageId = intent.getStringExtra("clueCardMessageId");
            MQMessage message = messageManager.getAgentClueCardMessage(clueCardMessageId);
            baseMessage = MQUtils.parseMQMessageToClueCardMessage(message);
            receiveNewMsg(baseMessage);
        }

        // 客服正在输入
        else if (MQMessageManager.ACTION_AGENT_INPUTTING.equals(action)) {
            int duration = intent.getIntExtra("duration", 2);
            changeTitleToInputting(duration);
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
        } else if (MQMessageManager.ACTION_AGENT_STATUS_UPDATE_EVENT.equals(action)) {
            updateAgentOnlineOfflineStatus();
        } else if (MQMessageManager.ACTION_BLACK_ADD.equals(action)) {
            blackAdd();
        } else if (MQMessageManager.ACTION_BLACK_DEL.equals(action)) {
            blackDel();
        } else if (TextUtils.equals(MQMessageManager.ACTION_QUEUEING_REMOVE, action)) {
            removeQueue();
        } else if (TextUtils.equals(MQMessageManager.ACTION_QUEUEING_INIT_CONV, action)) {
            long convId = intent.getLongExtra("convId", -1);
            queueingInitConv(convId);
        } else if (TextUtils.equals(MQMessageManager.ACTION_SOCKET_OPEN, action)) {
            socketOpen();
        } else if (TextUtils.equals(MQController.ACTION_SOCKET_RECONNECT, action)) {
            socketReconnect();
        } else if (TextUtils.equals(MQController.ACTION_NO_AGENT, action)) {
            noAgentStatus();
        } else if (TextUtils.equals(MQController.ACTION_QUEUEING_STATE, action)) {
            queueingState();
        }
    }

    public abstract void receiveNewMsg(BaseMessage message);

    public abstract void recallMessage(long id, String nickname);

    public abstract void changeTitleToInputting(int duration);

    public abstract void addDirectAgentMessageTip(String agentNickname);

    public abstract void setCurrentAgent(Agent agent);

    public abstract void inviteEvaluation();

    public abstract void setNewConversationId(String newConversationId);

    public abstract void updateAgentOnlineOfflineStatus();

    public abstract void blackAdd();

    public abstract void blackDel();

    public abstract void removeQueue();

    public abstract void queueingInitConv(long convId);

    public abstract void socketOpen();

    public abstract void socketReconnect();

    protected void noAgentStatus(){

    }

    protected void queueingState(){

    }
}
