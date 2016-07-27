package com.meiqia.meiqiasdk.chatitem;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.InitiativeRedirectMessage;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/22 上午10:23
 * 描述:提示主动点击转人工的消息item
 */
public class MQInitiativeRedirectItem extends MQBaseCustomCompositeView {
    private TextView mTipTv;
    private Callback mCallback;

    public MQInitiativeRedirectItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_useless_redirect;
    }

    @Override
    protected void initView() {
        mTipTv = getViewById(R.id.tv_item_redirect_tip);
    }

    @Override
    protected void setListener() {
        getViewById(R.id.tv_useless_redirect_redirect_human).setOnClickListener(this);
    }

    @Override
    protected void processLogic() {
    }

    @Override
    public void onClick(View view) {
        if (mCallback != null) {
            mCallback.onClickForceRedirectHuman();
        }
    }

    public void setMessage(InitiativeRedirectMessage initiativeRedirectMessage, Callback callback) {
        mCallback = callback;
        mTipTv.setText(initiativeRedirectMessage.getTipResId());
    }

    public interface Callback {
        void onClickForceRedirectHuman();
    }
}
