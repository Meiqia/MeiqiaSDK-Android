package com.meiqia.meiqiasdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.activity.MQPhotoPreviewActivity;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.model.HybridMessage;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.util.RichText;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;
import com.meiqia.meiqiasdk.widget.MQImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OnePiece
 * Created by xukq on 11/19/18.
 */
public class MQHybridItem extends MQBaseCustomCompositeView implements RichText.OnImageClickListener {

    private MQImageView mAvatarIv;
    private LinearLayout mContainerLl;
    private TextView mRobotRichTextFl;
    private TextView mMenuTipTv;
    private LinearLayout mEvaluateLl;
    private TextView mUsefulTv;
    private TextView mUselessTv;
    private TextView mAlreadyFeedbackTv;

    private MQRobotItem.Callback mCallback;

    private int mPadding;
    private int mTextSize;
    private int mTextTipSize;

    private HybridMessage mHybridMessage;

    public MQHybridItem(Context context, MQRobotItem.Callback callback) {
        super(context);
        mCallback = callback;
    }

    @Override
    public void onImageClicked(String url) {
        getContext().startActivity(MQPhotoPreviewActivity.newIntent(getContext(), MQUtils.getImageDir(getContext()), url));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_hybrid;
    }

    @Override
    protected void initView() {
        mAvatarIv = getViewById(R.id.iv_robot_avatar);
        mContainerLl = getViewById(R.id.ll_robot_container);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic() {
        MQUtils.applyCustomUITintDrawable(mContainerLl, R.color.mq_chat_left_bubble_final, R.color.mq_chat_left_bubble, MQConfig.ui.leftChatBubbleColorResId);

        mPadding = getResources().getDimensionPixelSize(R.dimen.mq_size_level2);
        mTextSize = getResources().getDimensionPixelSize(R.dimen.mq_textSize_level2);
        mTextTipSize = getResources().getDimensionPixelSize(R.dimen.mq_textSize_level1);
    }

    public void setMessage(HybridMessage hybridMessage, Activity activity) {
        mContainerLl.removeAllViews();
        mHybridMessage = hybridMessage;
        MQImage.displayImage(activity, mAvatarIv, mHybridMessage.getAvatar(), R.drawable.mq_ic_holder_avatar, R.drawable.mq_ic_holder_avatar, 100, 100, null);
        fillContentLl(mHybridMessage.getContent());
    }

    private void fillContentLl(String content) {
        try {
            JSONArray contentArray = new JSONArray(content);
            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                String type = item.getString("type");
                switch (type) {
                    case "rich_text":
                        addNormalOrRichTextView(item.getString("body"));
                        break;
                    case "choices":
                        addChoices(item.optJSONObject("body").optString("choices"));
                        break;
                    case "list":
                        fillContentLl(item.getString("body"));
                        break;
                    case "wait":
                        break;
                    default:
                        addNormalOrRichTextView(item.getString("body"));
                        break;
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
    private void addNormalOrRichTextView(String text) {
        if (!TextUtils.isEmpty(text)) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            textView.setTextColor(getResources().getColor(R.color.mq_chat_left_textColor));
            textView.setPadding(mPadding, mPadding, mPadding, mPadding);
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
            mContainerLl.addView(textView);
            RichText richText = new RichText();
            richText.fromHtml(text).setOnImageClickListener(this).into(textView);
        }
    }

    private void addChoices(String choicesStr) throws JSONException {
        JSONArray choices = new JSONArray(choicesStr);
        for (int i = 0; i < choices.length(); i++) {
            final String text = choices.optString(i);
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
                mContainerLl.addView(itemTv);
            }
        }
    }

}
