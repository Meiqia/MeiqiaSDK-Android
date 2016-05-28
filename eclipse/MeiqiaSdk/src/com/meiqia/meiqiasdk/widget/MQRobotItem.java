package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.RobotMessage;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/1 下午3:55
 * 描述:机器人消息item
 */
public class MQRobotItem extends MQBaseCustomCompositeView {
    private MQImageView mAvatarIv;
    private LinearLayout mContainerLl;
    private LinearLayout mContentLl;
    private TextView mMenuTipTv;
    private LinearLayout mEvaluateLl;
    private TextView mUsefulTv;
    private TextView mUselessTv;
    private TextView mAlreadyFeedbackTv;

    private Callback mCallback;

    private int mPadding;
    private int mTextSize;

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
        mAvatarIv = getViewById(R.id.iv_robot_avatar);
        mContainerLl = getViewById(R.id.ll_robot_container);
        mContentLl = getViewById(R.id.ll_robot_content);
        mEvaluateLl = getViewById(R.id.ll_robot_evaluate);
        mUsefulTv = getViewById(R.id.tv_robot_useful);
        mUselessTv = getViewById(R.id.tv_robot_useless);
        mMenuTipTv = getViewById(R.id.tv_robot_menu_tip);
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
        MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_robot_menu_tip_textColor, MQConfig.ui.robotMenuTipTextColorResId, null, mMenuTipTv);
        MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_robot_evaluate_textColor, MQConfig.ui.robotEvaluateTextColorResId, null, mUsefulTv, mUselessTv);

        mPadding = getResources().getDimensionPixelSize(R.dimen.mq_size_level2);
        mTextSize = getResources().getDimensionPixelSize(R.dimen.mq_textSize_level2);
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

    public void setMessage(RobotMessage robotMessage) {
        reset();

        mRobotMessage = robotMessage;
        MQConfig.getImageLoader(getContext()).displayImage(mAvatarIv, mRobotMessage.getAvatar(), R.drawable.mq_ic_holder_avatar, R.drawable.mq_ic_holder_avatar, 100, 100, null);
        handleEvaluateStatus();
        fillContentLl();
    }

    private void reset() {
        mContentLl.removeAllViews();
        mEvaluateLl.setVisibility(View.GONE);
        mMenuTipTv.setVisibility(View.GONE);
    }

    private void handleEvaluateStatus() {
        if (TextUtils.equals(RobotMessage.SUB_TYPE_EVALUATE, mRobotMessage.getSubType())) {
            mEvaluateLl.setVisibility(View.VISIBLE);
            if (mRobotMessage.isAlreadyFeedback()) {
                mUselessTv.setVisibility(View.GONE);
                mUsefulTv.setVisibility(View.GONE);
                mAlreadyFeedbackTv.setVisibility(View.VISIBLE);
            } else {
                mUselessTv.setVisibility(View.VISIBLE);
                mUsefulTv.setVisibility(View.VISIBLE);
                mAlreadyFeedbackTv.setVisibility(View.GONE);
            }
        }
    }

    private void fillContentLl() {
        try {
            JSONArray contentJsonArray = new JSONArray(mRobotMessage.getContentRobot());
            for (int i = 0; i < contentJsonArray.length(); i++) {
                JSONObject itemJsonObject = contentJsonArray.optJSONObject(i);
                if (TextUtils.equals("text", itemJsonObject.optString("type"))) {
                    addNormalTextView(itemJsonObject.optString("text"));
                } else if (TextUtils.equals("menu", itemJsonObject.optString("type"))) {
                    addMenuList(itemJsonObject.optJSONArray("items"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加普通的文本内容
     *
     * @param text
     */
    private void addNormalTextView(String text) {
        if (!TextUtils.isEmpty(text)) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            textView.setTextColor(getResources().getColor(R.color.mq_chat_left_textColor));
            textView.setPadding(mPadding, mPadding, mPadding, mPadding);
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
            mContentLl.addView(textView);
        }
    }

    /**
     * 添加菜单列表
     *
     * @param jsonArray
     */
    private void addMenuList(JSONArray jsonArray) {
        mMenuTipTv.setVisibility(View.VISIBLE);
        if (jsonArray != null && jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                addMenuItem(jsonArray.optJSONObject(i));
            }
        }
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

    public interface Callback {
        void onEvaluateRobotAnswer(RobotMessage robotMessage, int useful);

        void onClickRobotMenuItem(String text);
    }
}
