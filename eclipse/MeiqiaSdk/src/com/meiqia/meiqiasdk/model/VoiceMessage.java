package com.meiqia.meiqiasdk.model;

import com.meiqia.core.bean.MQMessage;

public class VoiceMessage extends BaseMessage {

    public static final int NO_DURATION = -1;

    private String localPath;
    private String url;
    private int duration = -1;

    public VoiceMessage() {
        setItemViewType(TYPE_CLIENT);
        setContentType(MQMessage.TYPE_CONTENT_VOICE);
    }

    public VoiceMessage(String url) {
        this();
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
