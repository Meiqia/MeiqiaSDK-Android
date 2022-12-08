package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQEnterpriseConfig;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.LeaveMessageCallback;
import com.meiqia.meiqiasdk.model.RedirectQueueMessage;
import com.meiqia.meiqiasdk.util.MQConfig;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/22 上午10:37
 * 描述:
 */
public class MQRedirectQueueItem extends MQBaseCustomCompositeView {
    private TextView mWaitNumTv;
    private TextView mInfoTv;
    private TextView mTicketIntroTv;
    private TextView mLeaveMessageTv;

    private LeaveMessageCallback mCallback;

    public MQRedirectQueueItem(Context context, LeaveMessageCallback leaveMessageCallback) {
        super(context);
        mCallback = leaveMessageCallback;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_redirect_queue;
    }

    @Override
    protected void initView() {
        mWaitNumTv = getViewById(R.id.tv_wait_number);
        mInfoTv = getViewById(R.id.tv_queue_info_tv);
        mTicketIntroTv = getViewById(R.id.tv_ticket_intro);
        mLeaveMessageTv = getViewById(R.id.tv_redirect_queue_leave_msg);
        if (!MQManager.getInstance(getContext()).getEnterpriseConfig().ticketConfig.isSdkEnabled()) {
            mLeaveMessageTv.setVisibility(GONE);
            mTicketIntroTv.setVisibility(GONE);
        }
    }

    @Override
    protected void setListener() {
        getViewById(R.id.tv_redirect_queue_leave_msg).setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
        MQEnterpriseConfig enterpriseConfig = MQConfig.getController(getContext()).getEnterpriseConfig();
        mInfoTv.setText(enterpriseConfig.queueingSetting.getIntro());
        mTicketIntroTv.setText(enterpriseConfig.queueingSetting.getTicket_intro());
    }

    @Override
    public void onClick(View view) {
        if (mCallback != null) {
            mCallback.onClickLeaveMessage();
        }
    }

    public void setMessage(RedirectQueueMessage redirectQueueMessage) {
        mWaitNumTv.setText(String.valueOf(redirectQueueMessage.getQueueSize()));
    }
}
