package com.meiqia.meiqiasdk.model;

/**
 * OnePiece
 * Created by xukq on 6/21/16.
 */
public class RichTextMessage extends BaseMessage {

    private String extra;

    public RichTextMessage() {
        setItemViewType(TYPE_RICH_TEXT);
        setContentType(BaseMessage.TYPE_CONTENT_RICH_TEXT);
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
