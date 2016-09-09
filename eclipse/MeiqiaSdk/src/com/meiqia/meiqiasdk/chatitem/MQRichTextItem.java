package com.meiqia.meiqiasdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.activity.MQWebViewActivity;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.imageloader.MQImageLoader;
import com.meiqia.meiqiasdk.model.RichTextMessage;
import com.meiqia.meiqiasdk.model.RobotMessage;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;
import com.meiqia.meiqiasdk.widget.MQImageView;

import org.json.JSONObject;

/**
 * OnePiece
 * Created by xukq on 5/31/16.
 */
public class MQRichTextItem extends MQBaseCustomCompositeView {

    private View mChatBox;
    private TextView mSummaryTv;
    private MQImageView mPicIv;
    String mContent;
    private int mImageWidth;
    private int mImageHeight;
    private RobotMessage mRobotMessage;

    public MQRichTextItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_rich_text;
    }

    @Override
    protected void initView() {
        mChatBox = findViewById(R.id.root);
        mSummaryTv = (TextView) findViewById(R.id.content_summary_tv);
        mPicIv = (MQImageView) findViewById(R.id.content_pic_iv);
    }

    @Override
    protected void setListener() {
        mChatBox.setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
        int screenWidth = MQUtils.getScreenWidth(getContext());
        mImageWidth = screenWidth / 3;
        mImageHeight = mImageWidth;
        configChatBubbleTextColor(mSummaryTv, true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.root) {
            if (!TextUtils.isEmpty(mContent)) {
                Intent it = new Intent(getContext(), MQWebViewActivity.class);
                it.putExtra(MQWebViewActivity.CONTENT, mContent);
                MQWebViewActivity.sRobotMessage = mRobotMessage;
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(it);
            }
        }
    }

    public void setMessage(RichTextMessage message, Activity activity) {
        try {
            JSONObject jsonObject = new JSONObject(message.getExtra());
            String summary = optString(jsonObject, "summary");
            mContent = optString(jsonObject, "content");
            String thumbnail = optString(jsonObject, "thumbnail");

            if (TextUtils.isEmpty(summary)) {
                // 用透明图片代替,不然显示很难看
                if (!TextUtils.isEmpty(mContent)) {
                    Spanned htmlContentSpanned = Html.fromHtml(mContent, new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String source) {
                            return getResources().getDrawable(android.R.color.transparent);
                        }
                    }, null);
                    mSummaryTv.setText(htmlContentSpanned);
                }
            } else {
                mSummaryTv.setText(summary);
            }
            if (!TextUtils.isEmpty(thumbnail)) {
                MQImage.displayImage(activity, mPicIv, thumbnail, R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageWidth, mImageHeight, new MQImageLoader.MQDisplayImageListener() {
                    @Override
                    public void onSuccess(View view, final String url) {
                    }
                });
            } else {
                mPicIv.setImageResource(R.drawable.mq_ic_holder_light);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRobotMessage(RobotMessage robotMessage) {
        this.mRobotMessage = robotMessage;
    }

    /**
     * 如果开发者有配置气泡内文字的颜色，改变气泡文字的颜色
     */
    private void configChatBubbleTextColor(TextView textView, boolean isLeft) {
        if (isLeft) {
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
        } else {
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_right_textColor, MQConfig.ui.rightChatTextColorResId, null, textView);
        }
    }

    private String optString(JSONObject json, String key) {
        // http://code.google.com/p/android/issues/detail?id=13830
        if (json.isNull(key))
            return null;
        else
            return json.optString(key, null);
    }

}
