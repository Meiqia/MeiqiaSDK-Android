package com.meiqia.meiqiasdk.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQInquireForm;
import com.meiqia.meiqiasdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * OnePiece
 * Created by xukq on 6/27/16.
 */
public class MQInquiryFormActivity extends MQBaseActivity {

    public static final String CURRENT_CLIENT = "CURRENT_CLIENT";

    private TextView mQuestionTitleTv;
    private LinearLayout mContainer;

    private MQInquireForm mInquireForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.mq_activity_inquiry_form;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mQuestionTitleTv = (TextView) findViewById(R.id.question_title);
        mContainer = (LinearLayout) findViewById(R.id.container_ll);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        try {
            JSONObject menusObj = getInquireForm().getMenus();
            String title = menusObj.optString(MQInquireForm.KEY_MENUS_TITLE);
            mQuestionTitleTv.setText(title);

            JSONArray assignments = menusObj.optJSONArray(MQInquireForm.KEY_MENUS_ASSIGNMENTS);
            if (assignments != null) {
                for (int i = 0; i < assignments.length(); i++) {
                    JSONObject assignment = assignments.getJSONObject(i);
                    String target_kind = assignment.optString(MQInquireForm.KEY_MENUS_ASSIGNMENTS_TARGET_KIND);
                    String target = assignment.optString(MQInquireForm.KEY_MENUS_ASSIGNMENTS_TARGET);
                    String description = assignment.optString(MQInquireForm.KEY_MENUS_ASSIGNMENTS_DESCRIPTION);
                    FormItem item = new FormItem(this, target_kind, target);
                    item.setContent(description);
                    mContainer.addView(item.getItem());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MQInquireForm getInquireForm() {
        if (mInquireForm == null) {
            mInquireForm = MQManager.getInstance(this).getMQInquireForm();
        }
        return mInquireForm;
    }

    private class FormItem implements View.OnClickListener {

        private View rootView;
        private TextView contentTb;
        private String target_kind;
        private String target;

        public FormItem(Context context, String target_kind, String target) {
            this.target_kind = target_kind;
            this.target = target;
            rootView = LayoutInflater.from(context).inflate(R.layout.mq_item_form_inquiry, null);
            contentTb = (TextView) rootView.findViewById(R.id.content_tv);
            rootView.setOnClickListener(this);
        }

        public void setContent(String content) {
            contentTb.setText(content);
        }

        private String getContent() {
            return contentTb.getText().toString();
        }

        public View getItem() {
            return rootView;
        }

        @Override
        public void onClick(View v) {
            String agentId = null;
            String groupId = null;
            // 指定分配
            if (!TextUtils.isEmpty(target_kind)) {
                if ("group".equals(target_kind)) {
                    groupId = target;
                }
                if ("agent".equals(target_kind)) {
                    agentId = target;
                }
            } else {
                // 这里不处理就表示用开发者之前指定的分配:  具体见 MQIntentBuilder.java
                // 如果开发者之前没有指定分配,那就调用默认分配
            }

            JSONArray fields = getInquireForm().getInputs().optJSONArray(MQInquireForm.KEY_INPUTS_FIELDS);
            boolean isCustomOpen = getInquireForm().isInputsOpen();
            // 跳转自定义表单: 开关开启,并且至少有一个不是回头客,至少有一个输入项
            if (isCustomOpen && !isSubmitAndAllReturnedCustomer() && fields.length() > 0) {
                Intent collectionIntent = new Intent(MQInquiryFormActivity.this, MQCollectInfoActivity.class);
                if (getIntent() != null) {
                    collectionIntent.putExtras(getIntent());
                }
                if (!TextUtils.isEmpty(groupId)) {
                    collectionIntent.putExtra(MQCollectInfoActivity.GROUP_ID, groupId);
                }
                if (!TextUtils.isEmpty(agentId)) {
                    collectionIntent.putExtra(MQCollectInfoActivity.AGENT_ID, agentId);
                }
                collectionIntent.putExtra(MQConversationActivity.PRE_SEND_TEXT, getContent());
                startActivity(collectionIntent);
            }
            // 跳转聊天界面
            else {
                Intent chatIntent = new Intent(MQInquiryFormActivity.this, MQConversationActivity.class);
                if (getIntent() != null) {
                    chatIntent.putExtras(getIntent());
                }
                chatIntent.putExtra(MQConversationActivity.PRE_SEND_TEXT, getContent());
                // 不为空才设置,不设置表示用开发者之前定义的
                if (!TextUtils.isEmpty(agentId) || !TextUtils.isEmpty(groupId)) {
                    MQManager.getInstance(MQInquiryFormActivity.this).setScheduledAgentOrGroupWithId(agentId, groupId);
                }
                startActivity(chatIntent);
            }
            onBackPressed();
        }
    }

    /**
     * 已经填写过表单,并且回头客不用填写
     *
     * @return
     */
    private boolean isSubmitAndAllReturnedCustomer() {
        if (!getInquireForm().isSubmitForm()) {
            return false;
        }
        boolean isAllReturnedCustomer = true;
        JSONArray fields = getInquireForm().getInputs().optJSONArray(MQInquireForm.KEY_INPUTS_FIELDS);
        try {
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                boolean ignore_returned_customer = field.optBoolean(MQInquireForm.KEY_INPUTS_FIELDS_IGNORE_RETURNED_CUSTOMER);
                if (!ignore_returned_customer) {
                    isAllReturnedCustomer = false;
                    break;
                }
            }
        } catch (Exception e) {

        }
        return isAllReturnedCustomer;
    }

}
