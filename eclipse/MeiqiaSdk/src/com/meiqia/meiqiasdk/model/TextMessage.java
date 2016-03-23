package com.meiqia.meiqiasdk.model;

import com.meiqia.core.bean.MQMessage;

public class TextMessage extends BaseMessage {
    public TextMessage() {
        setItemViewType(TYPE_CLIENT);
        setContentType(MQMessage.TYPE_CONTENT_TEXT);
    }

    public TextMessage(String content) {
        this();
        setContent(content);
    }
}