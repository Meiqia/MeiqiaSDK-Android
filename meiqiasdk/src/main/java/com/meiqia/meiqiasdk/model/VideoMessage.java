package com.meiqia.meiqiasdk.model;

import com.meiqia.core.bean.MQMessage;

public class VideoMessage extends BaseMessage{

    private String localPath;
    private String url;
    private String thumbUrl;

    public VideoMessage() {
        setItemViewType(TYPE_CLIENT);
        setContentType(MQMessage.TYPE_CONTENT_VIDEO);
    }

    public VideoMessage(String url) {
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

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

}
