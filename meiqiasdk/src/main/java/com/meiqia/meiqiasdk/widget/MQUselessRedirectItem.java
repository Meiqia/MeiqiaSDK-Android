package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.view.View;

import com.meiqia.meiqiasdk.R;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/22 上午10:23
 * 描述:用户对机器人的回答评价无用时，提示转人工的提示消息item
 */
public class MQUselessRedirectItem extends MQBaseCustomCompositeView {

    private Callback mCallback;

    public MQUselessRedirectItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_useless_redirect;
    }

    @Override
    protected void initView() {
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

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onClickForceRedirectHuman();
    }
}
