package com.meiqia.meiqiasdk.chatitem;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.AgentChangeMessage;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午4:08
 * 描述:提示消息item，包括「没有客服在线的提示」、「客服转接的提示」
 */
public class MQTipItem extends MQBaseCustomCompositeView {
    private TextView mContentTv;

    public MQTipItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_msg_tip;
    }

    @Override
    protected void initView() {
        mContentTv = getViewById(R.id.content_tv);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {
    }

    public void setMessage(BaseMessage baseMessage) {
        if (baseMessage instanceof AgentChangeMessage) {
            setDirectionMessageContent(baseMessage.getAgentNickname());
        } else {
            mContentTv.setText(baseMessage.getContent());
        }
    }

    private void setDirectionMessageContent(String agentNickName) {
        if (agentNickName != null) {
            String text = String.format(getResources().getString(R.string.mq_direct_content), agentNickName);
            int start = text.indexOf(agentNickName);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mq_chat_direct_agent_nickname_textColor)), start, start + agentNickName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            mContentTv.setText(style);
        }
    }
}
