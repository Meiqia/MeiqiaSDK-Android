package com.meiqia.meiqiasdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQEnterpriseConfig;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.activity.MQPhotoPreviewActivity;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.RobotMessage;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.util.RichText;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;
import com.meiqia.meiqiasdk.widget.MQFAQContainer;
import com.meiqia.meiqiasdk.widget.MQImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/1 下午3:55
 * 描述:机器人消息item
 */
public class MQRobotItem extends MQBaseCustomCompositeView implements RichText.OnImageClickListener {
    private LinearLayout rootLl;
    private MQImageView mAvatarIv;
    private LinearLayout mContainerLl;
    private TextView mRobotRichTextFl;
    private LinearLayout mContentLl;
    private LinearLayout mRelativeOrFAQContentLl;
    private LinearLayout mEvaluateLl;
    private View mUsefulTv;
    private View mUselessTv;
    private LinearLayout mAlreadyFeedbackLl;
    private TextView mAlreadyFeedbackTv;
    private ImageView mAlreadyFeedbackIv;

    private Callback mCallback;

    private int mPadding;
    private int mTextSize;
    private int mTextTipSize;

    private RobotMessage mRobotMessage;

    public MQRobotItem(Context context, Callback callback) {
        super(context);
        mCallback = callback;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_robot;
    }

    @Override
    protected void initView() {
        rootLl = getViewById(R.id.root_ll);
        mAvatarIv = getViewById(R.id.iv_robot_avatar);
        mContainerLl = getViewById(R.id.ll_robot_container);
        mRobotRichTextFl = getViewById(R.id.mq_robot_rich_text_container);
        mContentLl = getViewById(R.id.ll_robot_content);
        mRelativeOrFAQContentLl = getViewById(R.id.ll_robot_relative_container);
        mEvaluateLl = getViewById(R.id.ll_robot_evaluate);
        mUsefulTv = getViewById(R.id.tv_robot_useful);
        mUselessTv = getViewById(R.id.tv_robot_useless);
        mAlreadyFeedbackLl = getViewById(R.id.ll_robot_already_feedback);
        mAlreadyFeedbackIv = getViewById(R.id.iv_robot_already_feedback);
        mAlreadyFeedbackTv = getViewById(R.id.tv_robot_already_feedback);
    }

    @Override
    protected void setListener() {
        mUsefulTv.setOnClickListener(this);
        mUselessTv.setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
        MQUtils.applyCustomUITintDrawable(mContainerLl, R.color.mq_chat_left_bubble_final, R.color.mq_chat_left_bubble, MQConfig.ui.leftChatBubbleColorResId);

        mPadding = getResources().getDimensionPixelSize(R.dimen.mq_size_level2);
        mTextSize = getResources().getDimensionPixelSize(R.dimen.mq_textSize_level2);
        mTextTipSize = getResources().getDimensionPixelSize(R.dimen.mq_textSize_level1);
    }

    @Override
    public void onClick(View view) {
        if (mCallback != null) {
            if (view.getId() == R.id.tv_robot_useful) {
                mCallback.onEvaluateRobotAnswer(mRobotMessage, RobotMessage.EVALUATE_USEFUL);
            } else if (view.getId() == R.id.tv_robot_useless) {
                mCallback.onEvaluateRobotAnswer(mRobotMessage, RobotMessage.EVALUATE_USELESS);
            }
        }
    }

    public void setMessage(RobotMessage robotMessage, Activity activity) {
        reset();

        mRobotMessage = robotMessage;
        MQImage.displayImage(activity, mAvatarIv, mRobotMessage.getAvatar(), R.drawable.mq_ic_holder_avatar, R.drawable.mq_ic_holder_avatar, 100, 100, null);
        handleEvaluateStatus();
        fillContentLl();
    }

    private void reset() {
        mContentLl.removeAllViews();
        mRelativeOrFAQContentLl.removeAllViews();
        mContainerLl.setVisibility(View.GONE);
        mContentLl.setVisibility(View.GONE);
        mRelativeOrFAQContentLl.setVisibility(View.GONE);
        mEvaluateLl.setVisibility(View.GONE);
        mRobotRichTextFl.setVisibility(View.GONE);
        ViewGroup.LayoutParams containerLlParams = mContainerLl.getLayoutParams();
        containerLlParams.width = MQUtils.dip2px(getContext(), 240);
        mContainerLl.setLayoutParams(containerLlParams);
    }

    private void handleEvaluateStatus() {
        if (TextUtils.equals(RobotMessage.SUB_TYPE_EVALUATE, mRobotMessage.getSubType())) {
            ViewGroup.LayoutParams containerLlParams = mContainerLl.getLayoutParams();
            if (TextUtils.equals(MQManager.getInstance(getContext()).getEnterpriseConfig().robotSettings.getResponse_eval_switch(), MQEnterpriseConfig.OPEN)) {
                containerLlParams.width = MQUtils.dip2px(getContext(), 240);
                containerLlParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mContainerLl.setLayoutParams(containerLlParams);

                mEvaluateLl.setVisibility(View.VISIBLE);
                if (mRobotMessage.isAlreadyFeedback()) {
                    mUselessTv.setVisibility(View.GONE);
                    mUsefulTv.setVisibility(View.GONE);
                    mAlreadyFeedbackLl.setVisibility(View.VISIBLE);
                    mAlreadyFeedbackIv.clearColorFilter();
                    if (mRobotMessage.getFeedbackUseful() == RobotMessage.EVALUATE_USEFUL) {
                        mAlreadyFeedbackIv.setColorFilter(getResources().getColor(R.color.mq_evaluate_good));
                        mAlreadyFeedbackIv.setRotation(0);
                        mAlreadyFeedbackTv.setText(R.string.mq_useful);
                    } else {
                        mAlreadyFeedbackIv.setColorFilter(getResources().getColor(R.color.mq_warning_primary));
                        mAlreadyFeedbackIv.setRotation(180);
                        mAlreadyFeedbackTv.setText(R.string.mq_useless);
                    }
                } else {
                    mUselessTv.setVisibility(View.VISIBLE);
                    mUsefulTv.setVisibility(View.VISIBLE);
                    mAlreadyFeedbackLl.setVisibility(View.GONE);
                }
            } else {
                containerLlParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                containerLlParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                mContainerLl.setLayoutParams(containerLlParams);
            }
        }
    }

    private void fillContentLl() {
        try {
            if (RobotMessage.SUB_TYPE_UNKNOWN.equals(mRobotMessage.getSubType())) {
                addNormalTextView(getResources().getString(R.string.mq_unknown_msg_tip));
                return;
            }

            JSONArray contentJsonArray = new JSONArray(mRobotMessage.getContentRobot());
            boolean isRelated = isRelated(contentJsonArray);
            if (isRelated) {
                fillRelatedContent(contentJsonArray);
                return;
            }
            boolean isFaq = isFaq(contentJsonArray); // 是否是常见问题
            if (isFaq) {
                fillFaqContent(contentJsonArray);
                return;
            }
            for (int i = 0; i < contentJsonArray.length(); i++) {
                JSONObject itemJsonObject = contentJsonArray.optJSONObject(i);
                String rich_text = itemJsonObject.optString("rich_text");
                if (isRelated) {
                    if (TextUtils.equals("text", itemJsonObject.optString("type"))) {
                        // rich_text 为空的话，就当作纯文本处理
                        String text = TextUtils.isEmpty(itemJsonObject.optString("rich_text")) ? itemJsonObject.optString("text") : itemJsonObject.optString("rich_text");
                        addNormalTextView(text);
                    } else if (TextUtils.equals("related", itemJsonObject.optString("type"))) {
                        String text_before = itemJsonObject.optString("text_before");
                        addMenuList(itemJsonObject.optJSONArray("items"), text_before);
                    }
                }
                // rich_text 放在后面,不然会先解析成 rich_text
                else if (isRichText(itemJsonObject, rich_text)) {
                    addRichText(rich_text);
                } else if (TextUtils.equals("text", itemJsonObject.optString("type"))) {
                    // 如果有 richText，就当做富文本处理
                    if (TextUtils.isEmpty(rich_text)) {
                        addNormalTextView(itemJsonObject.optString("text"));
                    } else {
                        addRichText(rich_text);
                    }
                } else if (TextUtils.equals("menu", itemJsonObject.optString("type"))) {
                    addMenuList(itemJsonObject.optJSONArray("items"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillRelatedContent(JSONArray contentJsonArray) {
        for (int i = 0; i < contentJsonArray.length(); i++) {
            JSONObject itemJsonObject = contentJsonArray.optJSONObject(i);
            String rich_text = itemJsonObject.optString("rich_text");
            String normal_text = itemJsonObject.optString("text");
            String text = TextUtils.isEmpty(rich_text) ? normal_text : rich_text;

            // 添加问题
            if (!TextUtils.isEmpty(text)) {
                TextView textView = new TextView(getContext());
                textView.setText(text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                textView.setTextColor(getResources().getColor(R.color.mq_chat_left_textColor));
                MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
                mContentLl.addView(textView);
                RichText richText = new RichText();
                richText.fromHtml(text).setOnImageClickListener(this).into(textView);
            } else {
                // 添加具体相关问题
                if (TextUtils.equals("related", itemJsonObject.optString("type"))) {
                    JSONArray data = itemJsonObject.optJSONArray("items");
                    MQFAQContainer mqfaqContainer = new MQFAQContainer(getContext());
                    Map<String, String[]> contentData = new HashMap<>();
                    String[] items = new String[data.length()];
                    for (int j = 0; j < data.length(); j++) {
                        items[j] = data.optJSONObject(j).optString("text");
                    }
                    contentData.put("title", items);
                    mqfaqContainer.setTabsAndItems(itemJsonObject.optString("text_before"), false, contentData, 100);
                    mqfaqContainer.setCallback(mCallback);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.topMargin = mPadding;
                    mRelativeOrFAQContentLl.addView(mqfaqContainer, params);
                }
            }
        }
        mContentLl.setVisibility(VISIBLE);
        mContainerLl.setVisibility(VISIBLE);
        mRelativeOrFAQContentLl.setVisibility(VISIBLE);
    }


    private void fillFaqContent(JSONArray contentJsonArray) {
        for (int i = 0; i < contentJsonArray.length(); i++) {
            JSONObject itemJsonObject = contentJsonArray.optJSONObject(i);
            String rich_text = itemJsonObject.optString("rich_text");
            String normal_text = itemJsonObject.optString("text");
            String text = TextUtils.isEmpty(rich_text) ? normal_text : rich_text;

            // 添加问题
            if (!TextUtils.isEmpty(text)) {
                TextView textView = new TextView(getContext());
                textView.setText(text);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                textView.setTextColor(getResources().getColor(R.color.mq_chat_left_textColor));
                MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
                mContentLl.addView(textView);
                RichText richText = new RichText();
                richText.fromHtml(text).setOnImageClickListener(this).into(textView);
            } else {
                // 添加具体常见问题
                if (TextUtils.equals("menu", itemJsonObject.optString("type"))) {
                    String c_type = itemJsonObject.optString("c_type");
                    int pageSize = itemJsonObject.optInt("page_size", 5);
                    JSONArray data = itemJsonObject.optJSONArray("data");
                    MQFAQContainer mqfaqContainer = new MQFAQContainer(getContext());
                    if (TextUtils.equals(c_type, "advanced")) {
                        Map<String, String[]> contentData = new HashMap<>();
                        for (int j = 0; j < data.length(); j++) {
                            JSONObject dataItem = data.optJSONObject(j);
                            String category = dataItem.optString("category");
                            JSONArray categoryItems = dataItem.optJSONArray("items");
                            contentData.put(category, new String[categoryItems.length()]);
                            for (int k = 0; k < categoryItems.length(); k++) {
                                contentData.get(category)[k] = categoryItems.optString(k);
                            }
                        }
                        String faqTitle = getResources().getString(R.string.mq_faq);
                        mqfaqContainer.setTabsAndItems(faqTitle, true, contentData, pageSize);
                    } else {
                        Map<String, String[]> contentData = new HashMap<>();
                        String[] items = new String[data.length()];
                        for (int j = 0; j < data.length(); j++) {
                            items[j] = data.optString(j);
                        }
                        contentData.put("title", items);
                        String faqTitle = getResources().getString(R.string.mq_faq);
                        mqfaqContainer.setTabsAndItems(faqTitle, false, contentData, pageSize);
                    }
                    mqfaqContainer.setCallback(mCallback);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.topMargin = mPadding;
                    mRelativeOrFAQContentLl.addView(mqfaqContainer, params);
                }
            }
        }
        mContentLl.setVisibility(VISIBLE);
        mContainerLl.setVisibility(VISIBLE);
        mRelativeOrFAQContentLl.setVisibility(VISIBLE);
    }

    private boolean isRichText(JSONObject itemJsonObject, String rich_text) {
        return TextUtils.equals("text", itemJsonObject.optString("type"))
                && !TextUtils.isEmpty(rich_text)
                && (TextUtils.equals("evaluate", mRobotMessage.getSubType()) || TextUtils.equals("menu", mRobotMessage.getSubType()));
    }

    private boolean isRelated(JSONArray contentJsonArray) {
        boolean isRelated = false;
        try {
            for (int i = 0; i < contentJsonArray.length(); i++) {
                JSONObject jsonObject = contentJsonArray.getJSONObject(i);
                String type = jsonObject.optString("type");
                if (TextUtils.equals("related", type)) {
                    isRelated = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRelated;
    }

    private boolean isFaq(JSONArray contentJsonArray) {
        boolean isRelated = false;
        try {
            for (int i = 0; i < contentJsonArray.length(); i++) {
                JSONObject jsonObject = contentJsonArray.getJSONObject(i);
                String type = jsonObject.optString("type");
                if (TextUtils.equals("menu", type)) {
                    isRelated = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRelated;
    }

    private void addRichText(String rich_text) {
        mContainerLl.setVisibility(VISIBLE);
        mRobotRichTextFl.setVisibility(VISIBLE);
        RichText richText = new RichText();
        richText.fromHtml(rich_text).setOnImageClickListener(this).into(mRobotRichTextFl);
    }

    /**
     * 添加普通的文本内容
     *
     * @param text
     */
    private void addNormalTextView(String text) {
        mContainerLl.setVisibility(VISIBLE);
        mContentLl.setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(text)) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            textView.setTextColor(getResources().getColor(R.color.mq_chat_left_textColor));
            textView.setPadding(mPadding, mPadding, mPadding, mPadding);
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
            mContentLl.addView(textView);
            RichText richText = new RichText();
            richText.fromHtml(text).setOnImageClickListener(this).into(textView);
        }
    }

    /**
     * 添加菜单列表
     *
     * @param jsonArray
     */
    private void addMenuList(JSONArray jsonArray) {
        mContainerLl.setVisibility(VISIBLE);
        mContentLl.setVisibility(VISIBLE);
        if (jsonArray != null && jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                addMenuItem(jsonArray.optJSONObject(i));
            }
        }
    }

    private void addMenuList(JSONArray items, String text_before) {
        if (!TextUtils.isEmpty(text_before)) {
            TextView textView = new TextView(getContext());
            textView.setText(text_before);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextTipSize);
            textView.setTextColor(getResources().getColor(R.color.mq_chat_robot_menu_tip_textColor));
            textView.setPadding(mPadding, mPadding, mPadding, mPadding);
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_robot_menu_tip_textColor, MQConfig.ui.robotMenuTipTextColorResId, null, textView);
            mContentLl.addView(textView);
        }
        addMenuList(items);
    }

    /**
     * 添加菜单列表item
     *
     * @param jsonObject
     */
    private void addMenuItem(JSONObject jsonObject) {
        final String text = jsonObject.optString("text");
        if (!TextUtils.isEmpty(text)) {
            TextView itemTv = (TextView) View.inflate(getContext(), R.layout.mq_item_robot_menu, null);
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_robot_menu_item_textColor, MQConfig.ui.robotMenuItemTextColorResId, null, itemTv);
            itemTv.setText(text);
            itemTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        if (text.indexOf(".") == 1 && text.length() > 2) {
                            mCallback.onClickRobotMenuItem(text.substring(2));
                        } else {
                            mCallback.onClickRobotMenuItem(text);
                        }
                    }
                }
            });
            mContentLl.addView(itemTv);
        }
    }

    @Override
    public void onImageClicked(String url, String imgLink) {
        try {
            if (TextUtils.isEmpty(imgLink)) {
                getContext().startActivity(MQPhotoPreviewActivity.newIntent(getContext(), MQUtils.getImageDir(getContext()), url));
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imgLink));
                getContext().startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.mq_title_unknown_error, Toast.LENGTH_SHORT).show();
        }
    }

    public interface Callback {
        void onEvaluateRobotAnswer(RobotMessage robotMessage, int useful);

        void onClickRobotMenuItem(String text);

        boolean isLastMessage(BaseMessage message);
    }
}
