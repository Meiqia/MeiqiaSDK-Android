package com.meiqia.meiqiasdk.model;

/**
 * OnePiece
 * Created by xukq on 11/20/18.
 */
public class HybridMessage extends BaseMessage {

    private String extra;

    public HybridMessage() {
        setItemViewType(TYPE_HYBRID);
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
