package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQEnterpriseConfig;
import com.meiqia.core.bean.MQMessage;
import com.meiqia.core.callback.OnMessageSendCallback;
import com.meiqia.core.callback.OnTicketCategoriesCallback;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.SimpleCallback;
import com.meiqia.meiqiasdk.dialog.MQListDialog;
import com.meiqia.meiqiasdk.dialog.MQLoadingDialog;
import com.meiqia.meiqiasdk.model.MessageFormInputModel;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQMessageFormInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/23 上午10:44
 * 描述:留言表单界面
 */
public class MQMessageFormActivity extends Activity implements View.OnClickListener {

    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private TextView mSubmitTv;

    private TextView mMessageTipTv;
    private LinearLayout mInputContainerLl;

    private ArrayList<MessageFormInputModel> mMessageFormInputModels = new ArrayList<>();
    private ArrayList<MQMessageFormInputLayout> mMessageFormInputLayouts = new ArrayList<>();

    private MQLoadingDialog mLoadingDialog;

    private List<Map<String, String>> mDataList = new ArrayList<Map<String, String>>();
    private MQListDialog mCategoryDialog;
    private String mCurrentCategoryId;
    private boolean isPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    private void initView() {
        setContentView(R.layout.mq_activity_message_form);
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mBackRl = (RelativeLayout) findViewById(R.id.back_rl);
        mBackTv = (TextView) findViewById(R.id.back_tv);
        mBackIv = (ImageView) findViewById(R.id.back_iv);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mSubmitTv = (TextView) findViewById(R.id.submit_tv);

        mMessageTipTv = (TextView) findViewById(R.id.message_tip_tv);
        mInputContainerLl = (LinearLayout) findViewById(R.id.input_container_ll);
    }

    private void initListener() {
        mBackRl.setOnClickListener(this);
        mSubmitTv.setOnClickListener(this);
    }

    private void processLogic(Bundle savedInstanceState) {
        applyCustomUIConfig();

        handleFormInputLayouts();

        handleLeaveMessageIntro();

        refreshEnterpriseConfigAndContent();

        popTicketCategoriesChooseDialog();
        refreshLeaveMessageSwitchStatus();
    }

    /**
     * 处理引导文案
     */
    private void handleLeaveMessageIntro() {
        refreshLeaveMessageIntro();
    }

    private void refreshLeaveMessageSwitchStatus() {
        boolean isTicketOpen = MQConfig.getController(this).getEnterpriseConfig().ticketConfig.isSdkEnabled();
        if (!isTicketOpen) {
            mSubmitTv.setVisibility(View.GONE);
            mInputContainerLl.setVisibility(View.GONE);
        }
    }

    private void refreshEnterpriseConfigAndContent() {
        MQConfig.getController(this).refreshEnterpriseConfig(new SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
            }

            @Override
            public void onSuccess() {
                handleFormInputLayouts();
                refreshLeaveMessageIntro();
            }
        });
    }

    /**
     * 弹出工单分类的对话框
     */
    private void popTicketCategoriesChooseDialog() {
        if (!getEnterpriseConfig().ticketConfig.isCategory()) {
            return;
        }
        if (!getEnterpriseConfig().ticketConfig.isSdkEnabled()) {
            return;
        }
        MQManager.getInstance(this).getTicketCategories(new OnTicketCategoriesCallback() {
            @Override
            public void onSuccess(JSONArray ticketCategories) {
                if (ticketCategories == null || isPause) {
                    return;
                }

                for (int i = 0; i < ticketCategories.length(); i++) {
                    JSONObject ticketCategory = ticketCategories.optJSONObject(i);
                    if (ticketCategory != null) {
                        String categoryId = ticketCategory.optString("id");
                        String categoryName = ticketCategory.optString("name");
                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", categoryName);
                        data.put("id", categoryId);
                        mDataList.add(data);
                    }
                }

                if (mDataList.size() == 0) {
                    return;
                }

                mCategoryDialog = new MQListDialog(MQMessageFormActivity.this, getResources().getString(R.string.mq_choose_ticket_category), mDataList, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Map<String, String> data = mDataList.get(position);
                        mCurrentCategoryId = data.get("id");
                    }
                }, false);
                try {
                    mCategoryDialog.show();
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(int code, String message) {

            }
        });
    }

    /**
     * 刷新引导文案
     */
    private void refreshLeaveMessageIntro() {
        String leaveMessageIntro = MQConfig.getController(this).getEnterpriseConfig().ticketConfig.getIntro();
        if (TextUtils.isEmpty(leaveMessageIntro)) {
            mMessageTipTv.setVisibility(View.GONE);
        } else {
            mMessageTipTv.setText(leaveMessageIntro);
            mMessageTipTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 如果配置了界面相关的 config，在这里应用
     */
    private void applyCustomUIConfig() {
        if (MQConfig.DEFAULT != MQConfig.ui.backArrowIconResId) {
            mBackIv.setImageResource(MQConfig.ui.backArrowIconResId);
        }

        // 处理标题栏背景色
        MQUtils.applyCustomUITintDrawable(mTitleRl, android.R.color.white, R.color.mq_activity_title_bg, MQConfig.ui.titleBackgroundResId);

        // 处理标题、返回、返回箭头颜色
        MQUtils.applyCustomUITextAndImageColor(R.color.mq_activity_title_textColor, MQConfig.ui.titleTextColorResId, mBackIv, mBackTv, mTitleTv, mSubmitTv);

        // 通过 #FFFFFF 方式设置颜色：处理标题栏背景色、处理标题、返回、返回箭头颜色
        if (!TextUtils.isEmpty(MQConfig.ui.titleBackgroundColor)) {
            mTitleRl.setBackgroundColor(Color.parseColor(MQConfig.ui.titleBackgroundColor));
        }
        if (!TextUtils.isEmpty(MQConfig.ui.titleTextColor)) {
            int color = Color.parseColor(MQConfig.ui.titleTextColor);
            mBackIv.clearColorFilter();
            mBackIv.setColorFilter(color);
            mBackTv.setTextColor(color);
            mTitleTv.setTextColor(color);
        }

        // 处理标题文本的对其方式
        MQUtils.applyCustomUITitleGravity(mBackTv, mTitleTv);
    }

    private void handleFormInputLayouts() {
        mInputContainerLl.removeAllViews();
        mMessageFormInputModels.clear();
        mMessageFormInputLayouts.clear();

        MessageFormInputModel messageMfim = new MessageFormInputModel();
        messageMfim.name = TextUtils.isEmpty(getEnterpriseConfig().ticketConfig.getContent_title()) ? getString(R.string.mq_leave_msg) : getEnterpriseConfig().ticketConfig.getContent_title();
        messageMfim.key = "content";
        messageMfim.required = true;
        if (TextUtils.equals(getEnterpriseConfig().ticketConfig.getContent_fill_type(), "placeholder")) {
            messageMfim.placeholder = getEnterpriseConfig().ticketConfig.getContent_placeholder();
        } else {
            messageMfim.preFill = getEnterpriseConfig().ticketConfig.getDefaultTemplateContent();
        }
        messageMfim.inputType = InputType.TYPE_CLASS_TEXT;
        messageMfim.singleLine = false;
        mMessageFormInputModels.add(messageMfim);

        try {
            JSONArray fields = getEnterpriseConfig().ticketConfig.getCustom_fields();
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                MessageFormInputModel inputModel = new MessageFormInputModel();
                inputModel.name = field.optString("name");
                inputModel.key = field.optString("name");
                inputModel.required = field.optBoolean("required");
                inputModel.placeholder = field.optString("placeholder");
                inputModel.type = field.optString("type");
                inputModel.metainfo = field.optJSONArray("metainfo");
                mMessageFormInputModels.add(inputModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (MessageFormInputModel messageFormInputModel : mMessageFormInputModels) {
            MQMessageFormInputLayout formInputLayout = new MQMessageFormInputLayout(this, messageFormInputModel);
            mInputContainerLl.addView(formInputLayout);
            mMessageFormInputLayouts.add(formInputLayout);
        }
    }

    private MQEnterpriseConfig getEnterpriseConfig() {
        return MQManager.getInstance(this).getEnterpriseConfig();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_rl) {
            finish();
        } else if (v.getId() == R.id.submit_tv) {
            submit();
        }
    }

    private void submit() {
        String content = mMessageFormInputLayouts.get(0).getValue(); // 第一个位置是留言内容
        if (TextUtils.isEmpty(content)) {
            MQUtils.show(this, getString(R.string.mq_param_not_allow_empty, getString(R.string.mq_leave_msg)));
            return;
        }

        Map<String, String> formInputModelMap = new HashMap<>();
        Set<String> objKeySet = new HashSet<>(); // 保存 value 是 obj 的所有 key
        int len = mMessageFormInputModels.size();
        MessageFormInputModel messageFormInputModel;
        for (int i = 1; i < len; i++) {
            messageFormInputModel = mMessageFormInputModels.get(i);
            String value = mMessageFormInputLayouts.get(i).getValue();
            String key = mMessageFormInputLayouts.get(i).getKey();
            String type = mMessageFormInputLayouts.get(i).getType();
            if (messageFormInputModel.required && TextUtils.isEmpty(value)) {
                MQUtils.show(this, getString(R.string.mq_param_not_allow_empty, messageFormInputModel.name));
                return;
            }

            // 校验格式
            if (TextUtils.equals(key, "qq") && !MQUtils.isQQ(value) && messageFormInputModel.required) {
                Toast.makeText(this, mMessageFormInputLayouts.get(i).getName() + " " + getResources().getString(R.string.mq_invalid_content), Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.equals(key, "tel") && !MQUtils.isPhone(value) && messageFormInputModel.required) {
                Toast.makeText(this, mMessageFormInputLayouts.get(i).getName() + " " + getResources().getString(R.string.mq_invalid_content), Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.equals(key, "email") && !MQUtils.isEmailValid(value) && messageFormInputModel.required) {
                Toast.makeText(this, mMessageFormInputLayouts.get(i).getName() + " " + getResources().getString(R.string.mq_invalid_content), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(value)) {
                formInputModelMap.put(key, value);
                if (TextUtils.equals(type, "check") || TextUtils.equals(type, "checkbox")) {
                    objKeySet.add(key);
                }
            }
        }
        if (objKeySet.size() != 0) {
            formInputModelMap.put("obj_key_array", Arrays.toString(objKeySet.toArray())); // 标记哪些 key 是 Object
        }

        final long submitTimeMillis = System.currentTimeMillis();

        showLoadingDialog();

        MQMessage message = new MQMessage();
        message.setContent_type(MQMessage.TYPE_CONTENT_TEXT);
        message.setContent(content);
        MQManager.getInstance(this).submitTickets(message, mCurrentCategoryId, formInputModelMap, new OnMessageSendCallback() {
            @Override
            public void onSuccess(MQMessage message, int state) {
                if (System.currentTimeMillis() - submitTimeMillis < 1500) {
                    MQUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            MQUtils.show(getApplicationContext(), R.string.mq_submit_leave_msg_success);
                            finish();
                        }
                    }, System.currentTimeMillis() - submitTimeMillis);
                } else {
                    dismissLoadingDialog();
                    MQUtils.show(getApplicationContext(), R.string.mq_submit_leave_msg_success);
                    finish();
                }
            }

            @Override
            public void onFailure(MQMessage failureMessage, final int code, final String message) {
                if (System.currentTimeMillis() - submitTimeMillis < 1500) {
                    MQUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            if (com.meiqia.meiqiasdk.util.ErrorCode.BLACKLIST == code) {
                                // 产品需求，提交留言表单时，如果用户被拉黑了依然提示提交成功
                                MQUtils.show(getApplicationContext(), R.string.mq_submit_leave_msg_success);
                                finish();
                            } else {
                                MQUtils.show(getApplicationContext(), message);
                            }
                        }
                    }, System.currentTimeMillis() - submitTimeMillis);
                } else {
                    dismissLoadingDialog();
                    MQUtils.show(getApplicationContext(), message);
                }
            }
        });
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new MQLoadingDialog(this);
            mLoadingDialog.setCancelable(false);
        }
        mLoadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCategoryDialog != null && mCategoryDialog.isShowing()) {
            mCategoryDialog.dismiss();
        }
    }
}
