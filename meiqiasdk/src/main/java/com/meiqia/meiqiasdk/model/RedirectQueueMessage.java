package com.meiqia.meiqiasdk.model;

public class RedirectQueueMessage extends BaseMessage {
    private final int queueSize;

    public RedirectQueueMessage(int queueSize) {
        setItemViewType(TYPE_QUEUE_TIP);
        this.queueSize = queueSize;
    }

    public int getQueueSize() {
        return queueSize;
    }
}