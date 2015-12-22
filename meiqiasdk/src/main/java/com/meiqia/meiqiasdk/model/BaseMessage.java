package com.meiqia.meiqiasdk.model;

public class BaseMessage {

    public static final String TYPE_FROM_CLIENT = "client";
    public static final String TYPE_FROM_AGENT = "agent";

    public static final String TYPE_WELCOME = "welcome";
    public static final String TYPE_ENDING = "ending";
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_INTERNAL = "internal";
    public static final String TYPE_REMARK = "remark";
    public static final String TYPE_REPLY = "reply";

    public static final String STATE_ARRIVE = "arrived";
    public static final String STATE_SENDING = "sending";
    public static final String STATE_FAILED = "failed";

    public static final String TYPE_CONTENT_TEXT = "text";
    public static final String TYPE_CONTENT_PHOTO = "photo";
    public static final String TYPE_CONTENT_VOICE = "audio";

    public static final int TYPE_CLIENT = 0;
    public static final int TYPE_AGENT = 1;
    public static final int TYPE_TIME = 2;
    public static final int TYPE_TIP = 3;

    public static final int MAX_TYPE = 4;
    private long createdOn;
    private String agentNickname;
    private String status;
    private long id;
    private String contentType;
    private String content;
    private String avatar;
    private boolean isRead;

    public BaseMessage() {
        this.createdOn = System.currentTimeMillis();
    }

    public int getItemViewType() {
        return itemViewType;
    }

    public void setItemViewType(int itemViewType) {
        this.itemViewType = itemViewType;
    }

    private int itemViewType;

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public String getAgentNickname() {
        return agentNickname;
    }

    public void setAgentNickname(String agentNickname) {
        this.agentNickname = agentNickname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseMessage)) {
            return false;
        } else {
            BaseMessage baseMessage = (BaseMessage) o;
            return id == baseMessage.getId();
        }
    }
}
