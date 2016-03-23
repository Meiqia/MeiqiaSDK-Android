package com.meiqia.meiqiasdk.callback;


import com.meiqia.meiqiasdk.model.BaseMessage;

import java.util.List;

public interface OnGetMessageListCallBack extends OnFailureCallBack {

    void onSuccess(List<BaseMessage> messageList);

}
