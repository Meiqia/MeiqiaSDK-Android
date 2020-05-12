package com.meiqia.meiqiasdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.TextMessage;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午5:18
 * 描述:顾客消息item
 */
public class MQClientItem extends MQBaseBubbleItem {
    private ProgressBar sendingProgressBar;
    private ImageView sendState;
    private TextView sensitiveWordTipTv;

    public MQClientItem(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_chat_right;
    }

    @Override
    protected void initView() {
        super.initView();

        sendingProgressBar = getViewById(R.id.progress_bar);
        sendState = getViewById(R.id.send_state);
        sensitiveWordTipTv = getViewById(R.id.sensitive_words_tip_tv);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        applyConfig(false);
    }

    @Override
    public void setMessage(BaseMessage baseMessage, int position, Activity activity) {
        super.setMessage(baseMessage, position, activity);

        if (!MQConfig.isShowClientAvatar) {
            usAvatar.setVisibility(GONE);
            LayoutParams lp = (LayoutParams) chatBox.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            chatBox.setLayoutParams(lp);
        }

        sensitiveWordTipTv.setVisibility(GONE);
        if (sendingProgressBar != null) {
            switch (baseMessage.getStatus()) {
                case BaseMessage.STATE_SENDING:
                    sendingProgressBar.setVisibility(View.VISIBLE);
                    sendState.setVisibility(View.GONE);
                    break;
                case BaseMessage.STATE_ARRIVE:
                    sendingProgressBar.setVisibility(View.GONE);
                    sendState.setVisibility(View.GONE);
                    // 如果包含敏感词，就进行敏感词提示
                    if (baseMessage instanceof TextMessage && ((TextMessage) baseMessage).isContainsSensitiveWords()) {
                        sendingProgressBar.setVisibility(View.GONE);
                        sendState.setVisibility(View.GONE);
                        sensitiveWordTipTv.setVisibility(VISIBLE);
                        TextMessage textMessage = (TextMessage) baseMessage;
                        if (!TextUtils.isEmpty(textMessage.getReplaceContent())) {
                            contentText.setText(textMessage.getReplaceContent());
                        }
                    }
                    break;
                case BaseMessage.STATE_FAILED:
                    sendingProgressBar.setVisibility(View.GONE);
                    sendState.setVisibility(View.VISIBLE);
                    sendState.setBackgroundResource(R.drawable.mq_ic_msg_failed);
                    sendState.setOnClickListener(new FailedMessageOnClickListener(baseMessage));
                    sendState.setTag(baseMessage.getId());
                    break;
            }
        }
    }

    private class FailedMessageOnClickListener implements OnClickListener {

        private BaseMessage mFailedMessage;

        public FailedMessageOnClickListener(BaseMessage failedMessage) {
            mFailedMessage = failedMessage;
        }

        @Override
        public void onClick(View v) {
            if (!MQUtils.isFastClick()) {
                mCallback.resendFailedMessage(mFailedMessage);
            }
        }

    }
}
