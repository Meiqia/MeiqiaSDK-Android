package com.meiqia.meiqiasdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.meiqia.core.MQManager;
import com.meiqia.core.MQScheduleRule;
import com.meiqia.core.bean.MQAgent;
import com.meiqia.meiqiasdk.activity.MQCollectInfoActivity;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.activity.MQInquiryFormActivity;

import java.io.File;
import java.util.HashMap;

/**
 * OnePiece
 * Created by xukq on 3/18/16.
 */
public class MQIntentBuilder {

    private Context mContext;
    private Intent mIntent;

    public MQIntentBuilder(Context context) {
        mContext = context;
        mIntent = getIntent(context, MQConversationActivity.class);
    }

    public MQIntentBuilder(Context context, Class<? extends MQConversationActivity> clazz) {
        mContext = context;
        mIntent = getIntent(context, clazz);
    }

    /**
     * @param context
     * @param clazz
     * @return
     */
    private Intent getIntent(Context context, Class<? extends MQConversationActivity> clazz) {
        mIntent = new Intent(context, clazz);
        return mIntent;
    }

    public MQIntentBuilder setClientId(String clientId) {
        mIntent.putExtra(MQConversationActivity.CLIENT_ID, clientId);
        checkClient(clientId);
        return this;
    }

    public MQIntentBuilder setCustomizedId(String customizedId) {
        mIntent.putExtra(MQConversationActivity.CUSTOMIZED_ID, customizedId);
        checkClient(customizedId);
        return this;
    }

    public MQIntentBuilder setClientInfo(HashMap<String, String> clientInfo) {
        mIntent.putExtra(MQConversationActivity.CLIENT_INFO, clientInfo);
        return this;
    }

    public MQIntentBuilder updateClientInfo(HashMap<String, String> clientInfo) {
        mIntent.putExtra(MQConversationActivity.UPDATE_CLIENT_INFO, clientInfo);
        return this;
    }

    public MQIntentBuilder setScheduledAgent(String agentId) {
        mIntent.putExtra(MQConversationActivity.SCHEDULED_AGENT, agentId);
        return this;
    }

    public MQIntentBuilder setScheduledGroup(String groupId) {
        mIntent.putExtra(MQConversationActivity.SCHEDULED_GROUP, groupId);
        return this;
    }

    public MQIntentBuilder setScheduleRule(MQScheduleRule scheduleRule) {
        MQManager.getInstance(mContext).setScheduleRule(scheduleRule); // 和以前逻辑保持一致，SDK 设置的分配规则，同样影响询前表单的分配
        mIntent.putExtra(MQConversationActivity.SCHEDULED_RULE, scheduleRule.getValue());
        return this;
    }

    public MQIntentBuilder setPreSendTextMessage(String content) {
        mIntent.putExtra(MQConversationActivity.PRE_SEND_TEXT, content);
        return this;
    }

    public MQIntentBuilder setPreSendImageMessage(File imageFile) {
        if (imageFile != null && imageFile.exists()) {
            mIntent.putExtra(MQConversationActivity.PRE_SEND_IMAGE_PATH, imageFile.getAbsolutePath());
        }
        return this;
    }

    public MQIntentBuilder setPreSendProductCardMessage(Bundle productCardMessageBundle) {
        mIntent.putExtra(MQConversationActivity.PRE_SEND_PRODUCT_CARD, productCardMessageBundle);
        return this;
    }

    public Intent build() {
        if (!(mContext instanceof Activity)) {
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return mIntent;
    }

    private void checkClient(String id) {
        String currentId = MQUtils.getString(mContext, MQInquiryFormActivity.CURRENT_CLIENT, null);
        // 切换了用户,就默认不是回头客
        if (!TextUtils.equals(currentId, id)) {
            MQManager.getInstance(mContext).getEnterpriseConfig().survey.setHas_submitted_form(false);
        }
        MQUtils.putString(mContext, MQInquiryFormActivity.CURRENT_CLIENT, id);
    }

}
