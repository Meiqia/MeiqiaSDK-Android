package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.content.Intent;

import com.meiqia.core.MQManager;
import com.meiqia.core.MQScheduleRule;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;

import java.util.HashMap;

/**
 * OnePiece
 * Created by xukq on 3/18/16.
 */
public class MQIntentBuilder {

    private Context mContext;
    private Intent mIntent;
    private String mAgentId;
    private String mGroupId;
    private MQScheduleRule mScheduleRule = MQScheduleRule.REDIRECT_ENTERPRISE; // 默认指定分配失败后全企业分配

    public MQIntentBuilder(Context context) {
        mContext = context;
        mIntent = new Intent(context, MQConversationActivity.class);
    }

    public MQIntentBuilder(Context context, Class<? extends MQConversationActivity> clazz) {
        mContext = context;
        mIntent = new Intent(context, clazz);
    }

    public MQIntentBuilder setClientId(String clientId) {
        mIntent.putExtra(MQConversationActivity.CLIENT_ID, clientId);
        return this;
    }

    public MQIntentBuilder setCustomizedId(String customizedId) {
        mIntent.putExtra(MQConversationActivity.CUSTOMIZED_ID, customizedId);
        return this;
    }

    public MQIntentBuilder setClientInfo(HashMap<String, String> clientInfo) {
        mIntent.putExtra(MQConversationActivity.CLIENT_INFO, clientInfo);
        return this;
    }

    public MQIntentBuilder setScheduledAgent(String agentId) {
        mAgentId = agentId;
        return this;
    }

    public MQIntentBuilder setScheduledGroup(String groupId) {
        mGroupId = groupId;
        return this;
    }

    public MQIntentBuilder setScheduleRule(MQScheduleRule scheduleRule) {
        mScheduleRule = scheduleRule;
        return this;
    }

    public Intent build() {
        MQManager.getInstance(mContext).setScheduledAgentOrGroupWithId(mAgentId,mGroupId,mScheduleRule);
        return mIntent;
    }

}
