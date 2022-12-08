package com.meiqia.meiqiasdk.chatitem;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.meiqia.core.MQManager;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.LeaveMessageCallback;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/22 上午10:23
 * 描述:当前没有客服在线，提示留言的提示消息item
 */
public class MQNoAgentItem extends MQBaseCustomCompositeView {

    private LeaveMessageCallback mCallback;
    private TextView contentTv;
    private TextView leaveMessageTv;

    public MQNoAgentItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_no_agent;
    }

    @Override
    protected void initView() {
        contentTv = findViewById(R.id.content_tv);
        leaveMessageTv = findViewById(R.id.tv_no_agent_leave_msg);
        if (!MQManager.getInstance(getContext()).getEnterpriseConfig().ticketConfig.isSdkEnabled()) {
            leaveMessageTv.setVisibility(GONE);
        }
    }

    @Override
    protected void setListener() {
        setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
    }

    public void setContent(String content) {
        contentTv.setText(content);
    }

    @Override
    public void onClick(View view) {
        if (mCallback != null) {
            mCallback.onClickLeaveMessage();
        }
    }

    public void setCallback(LeaveMessageCallback leaveMessageCallback) {
        mCallback = leaveMessageCallback;
    }
}
