package com.meiqia.meiqiasdk.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.MQScheduleRule;
import com.meiqia.core.bean.MQInquireForm;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.util.RichText;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * OnePiece
 * Created by xukq on 6/27/16.
 */
public class MQInquiryFormActivity extends MQBaseActivity implements RichText.OnImageClickListener {
    public static final String CURRENT_CLIENT = "CURRENT_CLIENT";

    private TextView mQuestionTitleTv;
    private TextView mTitleTv;
    private TextView mContentTv;
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
        mTitleTv = findViewById(R.id.title_tv);
        mQuestionTitleTv = findViewById(R.id.question_title);
        mContentTv = findViewById(R.id.content_tv);
        mContainer = findViewById(R.id.container_ll);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        try {
            mTitleTv.setText(getInquireForm().getTitle());
            if (!TextUtils.isEmpty(getInquireForm().getContent()) && !TextUtils.equals(getInquireForm().getContent(), "<p></p>")) {
                RichText richText = new RichText();
                richText.fromHtml(getInquireForm().getContent()).setOnImageClickListener(this).into(mContentTv);
                mContentTv.setVisibility(View.VISIBLE);
            } else {
                mContentTv.setVisibility(View.GONE);
            }

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
                    int fallback = assignment.optInt(MQInquireForm.KEY_MENUS_ASSIGNMENTS_FALLBACK, 3);
                    FormItem item = new FormItem(this, target_kind, target, fallback);
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

        private final View rootView;
        private final TextView contentTb;
        private final String target_kind;
        private final String target;
        private final int fallback;

        public FormItem(Context context, String target_kind, String target, int fallback) {
            this.target_kind = target_kind;
            this.target = target;
            this.fallback = fallback;
            rootView = LayoutInflater.from(context).inflate(R.layout.mq_item_form_inquiry, null);
            contentTb = rootView.findViewById(R.id.content_tv);
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
            if (isCustomOpen && !isSubmitAndAllReturnedCustomer() && fields != null && fields.length() > 0) {
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
                collectionIntent.putExtra(MQCollectInfoActivity.FALLBACK, fallback);
                MQUtils.copyIntentExtra(getIntent(), collectionIntent);
                collectionIntent.putExtra(MQConversationActivity.SURVEY_MSG, getContent());
                collectionIntent.putExtra(MQConversationActivity.BOOL_IGNORE_CHECK_OTHER_ACTIVITY, true);
                startActivity(collectionIntent);
            }
            // 跳转聊天界面
            else {
                Intent chatIntent = new Intent(MQInquiryFormActivity.this, MQConversationActivity.class);
                if (getIntent() != null) {
                    chatIntent.putExtras(getIntent());
                }
                MQUtils.copyIntentExtra(getIntent(), chatIntent);
                chatIntent.putExtra(MQConversationActivity.SURVEY_MSG, getContent());
                chatIntent.putExtra(MQConversationActivity.BOOL_IGNORE_CHECK_OTHER_ACTIVITY, true);
                // 不为空才设置,不设置表示用开发者之前定义的
                if (!TextUtils.isEmpty(agentId) || !TextUtils.isEmpty(groupId)) {
                    MQManager.getInstance(MQInquiryFormActivity.this).setScheduledAgentOrGroupWithId(agentId, groupId);
                }
                MQManager.getInstance(MQInquiryFormActivity.this).setScheduleRule(MQScheduleRule.fromValue(fallback));
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

    @Override
    public void onImageClicked(String url, String imgLink) {
        try {
            if (TextUtils.isEmpty(imgLink)) {
                this.startActivity(MQPhotoPreviewActivity.newIntent(this, MQUtils.getImageDir(this), url));
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imgLink));
                this.startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.mq_title_unknown_error, Toast.LENGTH_SHORT).show();
        }
    }

}
