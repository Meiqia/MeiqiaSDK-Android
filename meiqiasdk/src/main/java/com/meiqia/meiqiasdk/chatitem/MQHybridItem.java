package com.meiqia.meiqiasdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.activity.MQPhotoPreviewActivity;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.imageloader.MQImageLoader;
import com.meiqia.meiqiasdk.model.BaseMessage;
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
    private LinearLayout mRootLl;

    private MQRobotItem.Callback mCallback;

    private int mPadding;
    private int mTextSize;

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
        mRootLl = getViewById(R.id.root_ll);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic() {
        MQUtils.applyCustomUITintDrawable(mContainerLl, R.color.mq_chat_left_bubble_final, R.color.mq_chat_left_bubble, MQConfig.ui.leftChatBubbleColorResId);

        mPadding = getResources().getDimensionPixelSize(R.dimen.mq_size_level2);
        mTextSize = getResources().getDimensionPixelSize(R.dimen.mq_textSize_level2);
    }

    public void setMessage(HybridMessage hybridMessage, Activity activity) {
        mContainerLl.removeAllViews();
        mHybridMessage = hybridMessage;
        if (!TextUtils.isEmpty(mHybridMessage.getAvatar())) {
            MQImage.displayImage(activity, mAvatarIv, mHybridMessage.getAvatar(), R.drawable.mq_ic_holder_avatar, R.drawable.mq_ic_holder_avatar, 100, 100, null);
        }
        fillContentLl(mHybridMessage.getContent());
        // 根据来源，调整展示
        View firstView = mRootLl.getChildAt(0);
        View secondVIew = mRootLl.getChildAt(1);
        // 先移除，然后根据来源，重新调整布局
        mRootLl.removeAllViews();
        int avatarWidthAndHeight = MQUtils.dip2px(activity, 35);
        int avatarMargin = MQUtils.dip2px(activity, 6);
        if (TextUtils.equals(hybridMessage.getFromType(), BaseMessage.TYPE_FROM_CLIENT)) {
            if (!MQConfig.isShowClientAvatar) {
                mAvatarIv.setVisibility(GONE);
            }
            mRootLl.setGravity(Gravity.RIGHT);
            // 头像需要调整到右边
            if (firstView instanceof MQImageView) {
                mRootLl.addView(secondVIew);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(avatarWidthAndHeight, avatarWidthAndHeight);
                avatarParams.leftMargin = avatarMargin;
                mRootLl.addView(firstView, avatarParams);
            } else {
                mRootLl.addView(firstView);
                mRootLl.addView(secondVIew);
            }
        } else {
            mAvatarIv.setVisibility(VISIBLE);
            mRootLl.setGravity(Gravity.LEFT);
            // 头像需要调整到左边
            if (firstView instanceof LinearLayout) {
                mRootLl.addView(secondVIew);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(avatarWidthAndHeight, avatarWidthAndHeight);
                avatarParams.rightMargin = avatarMargin;
                mRootLl.addView(firstView, avatarParams);
            } else {
                mRootLl.addView(firstView);
                mRootLl.addView(secondVIew);
            }
        }
    }

    private void fillContentLl(String content) {
        try {
            JSONArray contentArray = new JSONArray(content);
            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                String type = item.getString("type");
                switch (type) {
                    case "rich_text":
                    case "text":
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
                    case "photo_card":
                        addPhotoCardView(item.optJSONObject("body"));
                        break;
                    case "product_card":
                        addProductCardView(item.optJSONObject("body"));
                        break;
                    default:
                        addNormalOrRichTextView(getContext().getString(R.string.mq_unknown_msg_tip));
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

    private void addPhotoCardView(JSONObject contentObj) {
        int screenWidth = MQUtils.getScreenWidth(getContext());
        int mImageWidth = (screenWidth / 3 * 2) - MQUtils.dip2px(getContext(), 16);
        int mImageHeight = mImageWidth;
        int margin = MQUtils.dip2px(getContext(), 12);

        ViewGroup.LayoutParams layoutParams = mContainerLl.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = mImageWidth;
        mContainerLl.setLayoutParams(layoutParams);
        mContainerLl.setBackgroundResource(R.drawable.mq_bg_card);

        String title = contentObj.optString("title");
        String description = contentObj.optString("description");
        final String target_url = contentObj.optString("target_url");
        String pic_url = contentObj.optString("pic_url");

        // 添加图片
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setAdjustViewBounds(true);
        MQImage.displayImage((Activity) getContext(), iv, pic_url, R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageWidth, mImageHeight, new MQImageLoader.MQDisplayImageListener() {
            @Override
            public void onSuccess(View view, final String url) {

            }
        });
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ivParams.leftMargin = margin;
        ivParams.rightMargin = margin;
        mContainerLl.addView(iv, ivParams);
        // 设置点击跳转
        mContainerLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Uri uri = Uri.parse(target_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    MQUtils.show(getContext(), R.string.mq_title_unknown_error);
                }
            }
        });
        // 添加标题
        if (!TextUtils.isEmpty(title)) {
            TextView titleTv = new TextView(getContext());
            titleTv.setText(title);
            titleTv.setMaxLines(1);
            titleTv.setEllipsize(TextUtils.TruncateAt.END);
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.mq_textSize_level3));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = MQUtils.dip2px(getContext(), 2);
            params.bottomMargin = MQUtils.dip2px(getContext(), 2);
            params.leftMargin = margin;
            params.rightMargin = margin;
            mContainerLl.addView(titleTv, params);
        }
        // 添加内容
        if (!TextUtils.isEmpty(description)) {
            TextView descriptionTv = new TextView(getContext());
            descriptionTv.setText(description);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = MQUtils.dip2px(getContext(), 2);
            params.bottomMargin = MQUtils.dip2px(getContext(), 2);
            params.leftMargin = margin;
            params.rightMargin = margin;
            mContainerLl.addView(descriptionTv, params);
        }
    }

    private void addProductCardView(JSONObject contentObj) {
        int screenWidth = MQUtils.getScreenWidth(getContext());
        int mImageWidth = (screenWidth / 3 * 2) - MQUtils.dip2px(getContext(), 16);
        int mImageHeight = mImageWidth;

        ViewGroup.LayoutParams layoutParams = mContainerLl.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = mImageWidth;
        mContainerLl.setLayoutParams(layoutParams);
        mContainerLl.setBackgroundResource(R.drawable.mq_bg_card);

        View productCardContent = LayoutInflater.from(getContext()).inflate(R.layout.mq_item_product_card, null);
        mContainerLl.addView(productCardContent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        String title = contentObj.optString("title");
        String description = contentObj.optString("description");
        final String product_url = contentObj.optString("product_url");
        String pic_url = contentObj.optString("pic_url");
        long sales_count = contentObj.optLong("sales_count");

        // 添加图片
        ImageView iv = productCardContent.findViewById(R.id.mq_pic_iv);
        MQImage.displayImage((Activity) getContext(), iv, pic_url, R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageWidth, mImageHeight, new MQImageLoader.MQDisplayImageListener() {
            @Override
            public void onSuccess(View view, final String url) {

            }
        });

        // 查看详情
        TextView detailTv = productCardContent.findViewById(R.id.mq_detail_tv);
        detailTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(product_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    MQUtils.show(getContext(), R.string.mq_title_unknown_error);
                }
            }
        });

        // 添加标题
        if (!TextUtils.isEmpty(title)) {
            TextView titleTv = productCardContent.findViewById(R.id.mq_title_tv);
            titleTv.setText(title);
        }
        // 添加内容
        if (!TextUtils.isEmpty(description)) {
            TextView descriptionTv = productCardContent.findViewById(R.id.mq_desc_tv);
            descriptionTv.setText(description);
        }
        // 销量
        if (sales_count != 0) {
            TextView countTv = productCardContent.findViewById(R.id.count_tv);
            countTv.setText(getResources().getString(R.string.mq_sale_count) + "：" + sales_count);
        }
    }

}
