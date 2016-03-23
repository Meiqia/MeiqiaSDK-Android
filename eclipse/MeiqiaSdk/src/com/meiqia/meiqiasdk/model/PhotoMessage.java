package com.meiqia.meiqiasdk.model;

import com.meiqia.core.bean.MQMessage;

public class PhotoMessage extends BaseMessage {

    private String localPath;
    private String url;

    public PhotoMessage() {
        setItemViewType(TYPE_CLIENT);
        setContentType(MQMessage.TYPE_CONTENT_PHOTO);
    }

    public PhotoMessage(String url) {
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

}