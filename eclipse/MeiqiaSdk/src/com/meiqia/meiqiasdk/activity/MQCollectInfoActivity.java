package com.meiqia.meiqiasdk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQInquireForm;
import com.meiqia.core.callback.SimpleCallback;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.util.ErrorCode;
import com.meiqia.meiqiasdk.util.HttpUtils;
import com.meiqia.meiqiasdk.util.MQUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OnePiece
 * Created by xukq on 6/27/16.
 */
public class MQCollectInfoActivity extends MQBaseActivity implements View.OnClickListener {

    public static final String GROUP_ID = "group_id";
    public static final String AGENT_ID = "agent_id";

    private static final String TYPE_TEXT = "text";
    private static final String TYPE_SINGLE_CHOICE = "single_choice";
    private static final String TYPE_MULTIPLE_CHOICE = "multiple_choice";

    private static final long AUTO_DISMISS_TOP_TIP_TIME = 2000; // TopTip 自动隐藏时间

    private ProgressBar mLoadingProgressBar;
    private RelativeLayout mRootView;
    private View mScrollView;
    private TextView mSubmitTv;
    private LinearLayout mContainerLl;
    private TextView mTopTipViewTv;
    private RelativeLayout mBodyRl;
    private Handler mHandler;
    private Runnable mAutoDismissTopTipRunnable;

    private List<BaseItem> mBaseItemList;
    private CodeAuthItem mCodeAuthItem;
    private MQInquireForm mInquireForm;

    private boolean isDestroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler();
        mBaseItemList = new ArrayList<>();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        isDestroy = true;
        super.onDestroy();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.mq_activity_collect_info;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mSubmitTv = (TextView) findViewById(R.id.submit_tv);
        mContainerLl = (LinearLayout) findViewById(R.id.container_ll);
        mRootView = (RelativeLayout) findViewById(R.id.root);
        mBodyRl = (RelativeLayout) findViewById(R.id.body_rl);
        mScrollView = findViewById(R.id.content_sv);
    }

    @Override
    protected void setListener() {
        mSubmitTv.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        setTitle(getInquireForm().getInputs().optString(MQInquireForm.KEY_INPUTS_TITLE));

        // 如果全是回头客，直接跳转到对话界面
        if (isSubmitAndAllReturnedCustomer()) {
            goToChatActivity();
            return;
        }

        try {
            JSONArray fields = getInquireForm().getInputs().optJSONArray(MQInquireForm.KEY_INPUTS_FIELDS);
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                String display_name = field.optString(MQInquireForm.KEY_INPUTS_FIELDS_DISPLAY_NAME);
                String field_name = field.optString(MQInquireForm.KEY_INPUTS_FIELDS_FIELD_NAME);
                hookField(field_name, field);
                String type = field.optString(MQInquireForm.KEY_INPUTS_FIELDS_TYPE);
                String choices = field.optString(MQInquireForm.KEY_INPUTS_FIELDS_CHOICES);
                boolean optional = field.optBoolean(MQInquireForm.KEY_INPUTS_FIELDS_OPTIONAL);
                boolean ignore_returned_customer = field.optBoolean(MQInquireForm.KEY_INPUTS_FIELDS_IGNORE_RETURNED_CUSTOMER);

                BaseItem item = null;
                switch (type) {
                    case TYPE_TEXT:
                        item = new TextItem(display_name, field_name, type, optional, ignore_returned_customer);
                        break;
                    case TYPE_SINGLE_CHOICE:
                        item = new SingleChoiceItem(display_name, field_name, type, choices, optional, ignore_returned_customer);
                        break;
                    case TYPE_MULTIPLE_CHOICE:
                        item = new MultipleChoiceItem(display_name, field_name, type, choices, optional, ignore_returned_customer);
                        break;
                }
                if (item != null && item.getView() != null) {
                    mBaseItemList.add(item);
                    mContainerLl.addView(item.getView());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean captcha = getInquireForm().isCaptcha();
        if (captcha) {
            CodeAuthItem codeAuthItem = new CodeAuthItem();
            mCodeAuthItem = codeAuthItem;
            mBaseItemList.add(mCodeAuthItem);
            mContainerLl.addView(codeAuthItem.getView());
            codeAuthItem.refreshAuthCode();
        }
    }

    private void hookField(String field_name, JSONObject field) {
        // 前端改成单选模式
        if ("gender".equals(field_name)) {
            try {
                field.put(MQInquireForm.KEY_INPUTS_FIELDS_TYPE, TYPE_SINGLE_CHOICE);
                String genderChoice = getResources().getString(R.string.mq_inquire_gender_choice);
                field.put(MQInquireForm.KEY_INPUTS_FIELDS_CHOICES, genderChoice);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private MQInquireForm getInquireForm() {
        if (mInquireForm == null) {
            mInquireForm = MQManager.getInstance(this).getMQInquireForm();
        }
        return mInquireForm;
    }

    public void popInvalidTip() {
        if (mTopTipViewTv == null) {
            mTopTipViewTv = (TextView) getLayoutInflater().inflate(R.layout.mq_top_pop_tip, null);
            mTopTipViewTv.setText(R.string.mq_tip_required_before_submit);
            mTopTipViewTv.setBackgroundColor(getResources().getColor(R.color.mq_error));
            int height = getResources().getDimensionPixelOffset(R.dimen.mq_top_tip_height);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            params.addRule(RelativeLayout.ALIGN_TOP, R.id.content_sv);
            // 这里写死了位置,如果改了布局要注意
            mRootView.addView(mTopTipViewTv, 1, params);
            ViewCompat.setTranslationY(mTopTipViewTv, -height); // 初始化位置
            ViewCompat.animate(mTopTipViewTv).translationY(0).setDuration(300).start();
            if (mAutoDismissTopTipRunnable == null) {
                mAutoDismissTopTipRunnable = new Runnable() {
                    @Override
                    public void run() {
                        popInvalidTip();
                    }
                };
            }
            mHandler.postDelayed(mAutoDismissTopTipRunnable, AUTO_DISMISS_TOP_TIP_TIME);
        } else {
            mHandler.removeCallbacks(mAutoDismissTopTipRunnable);
            ViewCompat.animate(mTopTipViewTv).translationY(-mTopTipViewTv.getHeight()).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    mRootView.removeView(mTopTipViewTv);
                    mTopTipViewTv = null;
                }
            }).setDuration(300).start();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit_tv) {
            boolean isValid = checkData();
            if (isValid) {
                submitData();
            } else {
                popInvalidTip();
            }
        }
    }

    private boolean checkData() {
        boolean isAllValid = true;
        if (mBaseItemList.size() > 0) {
            for (BaseItem baseItem : mBaseItemList) {
                boolean isValid = baseItem.checkValid();
                if (!isValid) {
                    isAllValid = false;
                }
            }
        }
        return isAllValid;
    }

    private void submitData() {
        Map<String, Object> params = new HashMap<>();
        if (mBaseItemList.size() > 0) {
            for (BaseItem baseItem : mBaseItemList) {
                if (baseItem instanceof CodeAuthItem) {
                    continue;
                }

                Object value = baseItem.getValue();
                if (value != null && !TextUtils.isEmpty(value.toString())) {
                    params.put(baseItem.getFileName(), value);
                }
            }
        }
        Map<String, String> headers = null;
        if (mCodeAuthItem != null) {
            headers = new HashMap<>();
            headers.put("Captcha-Token", mCodeAuthItem.getCaptcha_token());
            headers.put("Captcha-Value", mCodeAuthItem.getValue());
        }
        if (getIntent() == null) {
            finish();
        }
        String clientIdOrCustomizedId;
        String clientId = getIntent().getStringExtra(MQConversationActivity.CLIENT_ID);
        String customizedId = getIntent().getStringExtra(MQConversationActivity.CUSTOMIZED_ID);
        if (!TextUtils.isEmpty(clientId)) {
            clientIdOrCustomizedId = clientId;
        } else if (!TextUtils.isEmpty(customizedId)) {
            clientIdOrCustomizedId = customizedId;
        } else {
            clientIdOrCustomizedId = MQManager.getInstance(this).getCurrentClientId();
        }
        if (getInquireForm().isCaptcha()) {
            submitState(true);
            MQManager.getInstance(this).submitInquireForm(clientIdOrCustomizedId, params, headers, new SimpleCallback() {
                @Override
                public void onSuccess() {
                    goToChatActivity();
                }

                @Override
                public void onFailure(int code, String message) {
                    submitState(false);
                    if (code == 400) {
                        mCodeAuthItem.refreshAuthCode();
                        Toast.makeText(MQCollectInfoActivity.this, R.string.mq_error_auth_code_wrong, Toast.LENGTH_SHORT).show();
                    } else if (code == ErrorCode.NET_NOT_WORK) {
                        Toast.makeText(MQCollectInfoActivity.this, R.string.mq_title_net_not_work, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MQCollectInfoActivity.this, R.string.mq_error_submit_form, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            MQManager.getInstance(this).submitInquireForm(clientIdOrCustomizedId, params, headers, null);
            goToChatActivity();
        }
    }

    private void goToChatActivity() {
        // 跳转到聊天界面
        Intent intent = new Intent(this, MQConversationActivity.class);
        String agentId = null;
        String groupId = null;
        if (getIntent() != null) {
            agentId = getIntent().getStringExtra(AGENT_ID);
            groupId = getIntent().getStringExtra(GROUP_ID);
            intent.putExtras(getIntent());
        }
        intent.putExtra(MQConversationActivity.PRE_SEND_TEXT, getIntent().getStringExtra(MQConversationActivity.PRE_SEND_TEXT));
        // 不为空才设置,不设置表示用开发者之前定义的
        if (!TextUtils.isEmpty(agentId) || !TextUtils.isEmpty(groupId)) {
            MQManager.getInstance(this).setScheduledAgentOrGroupWithId(agentId, groupId);
        }
        startActivity(intent);
        onBackPressed();
    }

    private void submitState(boolean isSubmitState) {
        if (isSubmitState) {
            mLoadingProgressBar.setVisibility(View.VISIBLE);
            mSubmitTv.setVisibility(View.GONE);
            mScrollView.setVisibility(View.GONE);
        } else {
            mLoadingProgressBar.setVisibility(View.GONE);
            mSubmitTv.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.VISIBLE);
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

    private abstract class BaseItem {

        public View rootView;
        public TextView titleTv;

        public String displayName;
        public String fieldName;
        public String type;
        public boolean optional;
        public boolean ignoreReturnCustomer;

        BaseItem(String displayName, String fieldName, String type, boolean optional, boolean ignoreReturnCustomer) {
            this.displayName = displayName;
            this.fieldName = fieldName;
            this.type = type;
            this.optional = optional;
            this.ignoreReturnCustomer = ignoreReturnCustomer;
            init();
        }

        public BaseItem() {
            optional = false;
            init();
        }

        protected void init() {
            findViews();
            initTitle();
        }

        public void initTitle() {
            if (!TextUtils.isEmpty(displayName)) {
                titleTv.setText(displayName);
            }
            // 如果是必填,需要添加红色星号
            if (!optional) {
                SpannableStringBuilder tipSsb = new SpannableStringBuilder(titleTv.getText() + " *");
                tipSsb.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mq_error)), titleTv.getText().length() + 1, tipSsb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                titleTv.setText(tipSsb);
            }
        }

        abstract void findViews();

        public boolean checkValid() {
            if (optional) {
                return true;
            }

            boolean isValid = isValid();
            if (!isValid) {
                invalidState();
            } else {
                validState();
            }
            return isValid;
        }

        protected void validState() {
            titleTv.setTextColor(getResources().getColor(R.color.mq_form_tip_textColor));
        }

        protected void invalidState() {
            titleTv.setTextColor(getResources().getColor(R.color.mq_error));
        }

        public abstract boolean isValid();

        public abstract Object getValue();

        public String getFileName() {
            return this.fieldName;
        }


        public View getView() {
            // 如果是回头客不显示的开关是开启的,并且是回头客
            if (ignoreReturnCustomer && getInquireForm().isSubmitForm()) {
                return null;
            }
            return rootView;
        }
    }

    private class TextItem extends BaseItem {

        EditText contentEt;

        TextItem(String displayName, String fieldName, String type, boolean optional, boolean ignoreReturnCustomer) {
            super(displayName, fieldName, type, optional, ignoreReturnCustomer);
            setListeners();
            setInputType();
        }

        @Override
        void findViews() {
            rootView = getLayoutInflater().inflate(R.layout.mq_item_form_type_text, null);
            titleTv = (TextView) rootView.findViewById(R.id.title_tv);
            contentEt = (EditText) rootView.findViewById(R.id.content_et);
        }

        private void setListeners() {
            contentEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    checkValid();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        private void setInputType() {
            if ("tel".equals(fieldName)) {
                contentEt.setInputType(InputType.TYPE_CLASS_PHONE);
            } else if ("qq".equals(fieldName) || "age".equals(fieldName)) {
                contentEt.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if ("email".equals(fieldName)) {
                contentEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            }
        }

        public String getContent() {
            return contentEt.getText().toString();
        }

        @Override
        public boolean isValid() {
            // 必填并且没有内容,校验失败
            return !(TextUtils.isEmpty(contentEt.getText().toString()));
        }

        @Override
        public String getValue() {
            return contentEt.getText().toString();
        }

    }

    private class SingleChoiceItem extends BaseItem implements CompoundButton.OnCheckedChangeListener {

        RadioGroup radioGroup;
        private String choices;

        SingleChoiceItem(String displayName, String fieldName, String type, String choices, boolean optional, boolean ignoreReturnCustomer) {
            super(displayName, fieldName, type, optional, ignoreReturnCustomer);
            this.choices = choices;
            initData();
        }

        private void initData() {
            try {
                JSONArray choiceArray = new JSONArray(choices);
                for (int i = 0; i < choiceArray.length(); i++) {
                    RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(R.layout.mq_item_form_radio_btn, null);
                    radioButton.setText(choiceArray.getString(i));
                    radioButton.setTag(choiceArray.get(i));
                    radioButton.setId(View.NO_ID);
                    radioButton.setOnCheckedChangeListener(this);
                    MQUtils.tintCompoundButton(radioButton, R.drawable.mq_radio_btn_uncheck, R.drawable.mq_radio_btn_checked);
                    radioGroup.addView(radioButton, LinearLayout.LayoutParams.MATCH_PARENT, MQUtils.dip2px(MQCollectInfoActivity.this, 48));
                }
            } catch (JSONException e) {
                rootView.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }

        @Override
        void findViews() {
            rootView = getLayoutInflater().inflate(R.layout.mq_item_form_type_single_choice, null);
            titleTv = (TextView) rootView.findViewById(R.id.title_tv);
            radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group);
        }

        @Override
        public boolean isValid() {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            return checkedId != View.NO_ID;
        }

        @Override
        public Object getValue() {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                View child = radioGroup.getChildAt(i);
                if (radioGroup.getCheckedRadioButtonId() == child.getId()) {
                    return child.getTag();
                }
            }
            return null;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // 为什么不直接 checkValid : 因为第一次选中, isChecked = true 的时候,radioGroup.getCheckedRadioButtonId() 会等于 View.NO_ID
            if (isChecked) {
                validState();
            } else {
                checkValid();
            }
        }
    }

    private class MultipleChoiceItem extends BaseItem implements CompoundButton.OnCheckedChangeListener {

        private LinearLayout checkboxContainer;
        private String choices;
        private List<CheckBox> checkBoxList;

        MultipleChoiceItem(String displayName, String fieldName, String type, String choices, boolean optional, boolean ignoreReturnCustomer) {
            super(displayName, fieldName, type, optional, ignoreReturnCustomer);
            this.choices = choices;
            this.checkBoxList = new ArrayList<>();
            initData();
        }

        private void initData() {
            try {
                JSONArray choiceArray = new JSONArray(choices);
                for (int i = 0; i < choiceArray.length(); i++) {
                    CheckBox checkBox = (CheckBox) getLayoutInflater().inflate(R.layout.mq_item_form_checkbox, null);
                    checkBox.setText(choiceArray.getString(i));
                    checkBox.setOnCheckedChangeListener(this);
                    checkBox.setTag(choiceArray.get(i));
                    MQUtils.tintCompoundButton(checkBox, R.drawable.mq_checkbox_uncheck, R.drawable.mq_checkbox_unchecked);
                    checkboxContainer.addView(checkBox, LinearLayout.LayoutParams.MATCH_PARENT, MQUtils.dip2px(MQCollectInfoActivity.this, 48));
                    checkBoxList.add(checkBox);
                }
            } catch (JSONException e) {
                rootView.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }

        @Override
        void findViews() {
            rootView = getLayoutInflater().inflate(R.layout.mq_item_form_type_multiple_choice, null);
            titleTv = (TextView) rootView.findViewById(R.id.title_tv);
            checkboxContainer = (LinearLayout) rootView.findViewById(R.id.checkbox_container);
        }

        @Override
        public boolean isValid() {
            // 至少选中一个
            for (CheckBox checkBox : checkBoxList) {
                boolean isChecked = checkBox.isChecked();
                if (isChecked) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object getValue() {
            JSONArray checkedArray = new JSONArray();
            for (CheckBox checkBox : checkBoxList) {
                if (checkBox.isChecked()) {
                    checkedArray.put(checkBox.getTag());
                }
            }
            return checkedArray;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkValid();
        }
    }

    private class CodeAuthItem extends BaseItem {

        private EditText authCodeEt;
        private ImageView authCodeIv;

        private String captcha_token;
        private String captcha_image;

        public CodeAuthItem() {
            super();
            authCodeIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshAuthCode();
                }
            });
        }

        @Override
        void findViews() {
            rootView = getLayoutInflater().inflate(R.layout.mq_item_form_type_auth_code, null);
            titleTv = (TextView) rootView.findViewById(R.id.title_tv);
            authCodeEt = (EditText) rootView.findViewById(R.id.auth_code_et);
            authCodeIv = (ImageView) rootView.findViewById(R.id.auth_code_iv);
        }

        @Override
        public boolean isValid() {
            return !TextUtils.isEmpty(authCodeEt.getText().toString());
        }

        @Override
        public String getValue() {
            return authCodeEt.getText().toString();
        }

        public String getCaptcha_token() {
            return captcha_token;
        }

        public View getView() {
            return rootView;
        }

        public void refreshAuthCode() {
            authCodeIv.setClickable(false);
            authCodeIv.setImageBitmap(null);
            authCodeEt.setText("");
            Thread refreshAuthCodeThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject authCodeObj = HttpUtils.getInstance().getAuthCode();
                        captcha_image = authCodeObj.optString("captcha_image_url");
                        captcha_token = authCodeObj.optString("captcha_token");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isDestroy) {
                                    try {
                                        MQImage.displayImage(MQCollectInfoActivity.this, authCodeIv, captcha_image, R.drawable.mq_ic_holder_avatar, R.drawable.mq_ic_holder_avatar, authCodeIv.getWidth(), authCodeIv.getHeight(), null);
                                    } catch (Exception e) {
                                        // 如果 Activity 销毁再加载就会抛异常
                                    }
                                }
//                                authCodeIv.setImageBitmap(authCodeBitmap);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        authCodeIv.setClickable(true);
                    }
                }
            });
            refreshAuthCodeThread.start();
        }

    }

}
