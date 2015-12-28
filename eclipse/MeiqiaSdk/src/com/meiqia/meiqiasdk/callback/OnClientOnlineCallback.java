package com.meiqia.meiqiasdk.callback;


import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;

import java.util.List;

public interface OnClientOnlineCallback extends OnFailureCallBack {
    void onSuccess(Agent agent, List<BaseMessage> messageList);
}
