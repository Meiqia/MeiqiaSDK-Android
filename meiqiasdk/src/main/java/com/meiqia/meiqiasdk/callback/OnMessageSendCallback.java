package com.meiqia.meiqiasdk.callback;


import com.meiqia.meiqiasdk.model.BaseMessage;

public interface OnMessageSendCallback {
    void onSuccess(BaseMessage message, int state);

    void onFailure(BaseMessage failureMessage, int code, String failureInfo);
}
