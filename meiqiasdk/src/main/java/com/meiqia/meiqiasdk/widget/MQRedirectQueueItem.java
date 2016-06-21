package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.LeaveMessageCallback;
import com.meiqia.meiqiasdk.model.RedirectQueueMessage;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/22 上午10:37
 * 描述:
 */
public class MQRedirectQueueItem extends MQBaseCustomCompositeView {
    private ImageView mQueueAnimIv;
    private TextView mTipTv;

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
        mQueueAnimIv = getViewById(R.id.iv_redirect_queue_anim);
        mTipTv = getViewById(R.id.tv_redirect_queue_tip);
    }

    @Override
    protected void setListener() {
        getViewById(R.id.tv_redirect_queue_leave_msg).setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
    }

    @Override
    public void onClick(View view) {
        if (mCallback != null) {
            mCallback.onClickLeaveMessage();
        }
    }

    public void setMessage(RedirectQueueMessage redirectQueueMessage) {
        mTipTv.setText(getResources().getString(R.string.mq_queue_leave_msg, redirectQueueMessage.getQueueSize()));
        ((AnimationDrawable) mQueueAnimIv.getDrawable()).start();
    }
}
