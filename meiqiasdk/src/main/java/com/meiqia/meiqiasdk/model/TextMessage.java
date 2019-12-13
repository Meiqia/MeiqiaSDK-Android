package com.meiqia.meiqiasdk.model;

import com.meiqia.core.bean.MQMessage;

public class TextMessage extends BaseMessage {

    private boolean isContainsSensitiveWords;
    private String replaceContent;

    public TextMessage() {
        setItemViewType(TYPE_CLIENT);
        setContentType(MQMessage.TYPE_CONTENT_TEXT);
    }

    public TextMessage(String content) {
        this();
        setContent(content);
    }

    /**
     * 模拟一条由客服发的消息
     *
     * @param content
     * @param avatar
     */
    public TextMessage(String content, String avatar) {
        setItemViewType(TYPE_AGENT);
        setContent(content);
        setContentType(MQMessage.TYPE_CONTENT_TEXT);
        setAvatar(avatar);
        setStatus(STATE_ARRIVE);
    }

    public boolean isContainsSensitiveWords() {
        return isContainsSensitiveWords;
    }

    public void setContainsSensitiveWords(boolean containsSensitiveWords) {
        isContainsSensitiveWords = containsSensitiveWords;
    }

    public String getReplaceContent() {
        return replaceContent;
    }

    public void setReplaceContent(String replaceContent) {
        this.replaceContent = replaceContent;
    }
}