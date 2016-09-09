package com.meiqia.meiqiasdk.callback;

import android.content.Intent;

import com.meiqia.meiqiasdk.activity.MQConversationActivity;

/**
 * OnePiece
 * Created by xukq on 9/7/16.
 */
public interface OnLinkClickCallback {

    void onClick(MQConversationActivity conversationActivity, Intent intent, String url);

}
