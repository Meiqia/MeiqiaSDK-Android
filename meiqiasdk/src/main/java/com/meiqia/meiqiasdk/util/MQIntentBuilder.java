package com.meiqia.meiqiasdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private String mAgentId;
    private String mGroupId;
    private MQScheduleRule mScheduleRule = MQScheduleRule.REDIRECT_ENTERPRISE; // 默认指定分配失败后全企业分配

    public MQIntentBuilder(Context context) {
        mContext = context;
        mIntent = getIntent(context, MQConversationActivity.class);
    }

    public MQIntentBuilder(Context context, Class<? extends MQConversationActivity> clazz) {
        mContext = context;
        mIntent = getIntent(context, clazz);
    }

    /**
     * 根据后台设置,判断是直接跳转到聊天界面,还是询前表单
     *
     * @param context
     * @param clazz
     * @return
     */
    private Intent getIntent(Context context, Class<? extends MQConversationActivity> clazz) {
        MQAgent agent = MQManager.getInstance(context).getCurrentAgent();
        if (agent != null) {
            mIntent = new Intent(context, clazz);
            return mIntent;
        }
        boolean isMenusOpen = MQManager.getInstance(context).getMQInquireForm().isMenusOpen();
        boolean isInputsOpen = MQManager.getInstance(context).getMQInquireForm().isInputsOpen();
        if (isMenusOpen) {
            mIntent = new Intent(context, MQInquiryFormActivity.class);
        } else if (isInputsOpen) {
            mIntent = new Intent(context, MQCollectInfoActivity.class);
        } else {
            mIntent = new Intent(context, clazz);
        }
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

    public Intent build() {
        MQManager.getInstance(mContext).setScheduledAgentOrGroupWithId(mAgentId, mGroupId, mScheduleRule);
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
