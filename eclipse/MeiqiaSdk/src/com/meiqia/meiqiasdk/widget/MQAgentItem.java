package com.meiqia.meiqiasdk.widget;

import android.content.Context;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.util.MQConfig;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午5:17
 * 描述:客服消息item
 */
public class MQAgentItem extends MQBaseBubbleItem {
    private MQImageView usAvatar;

    public MQAgentItem(Context context, Callback calllback) {
        super(context, calllback);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_chat_left;
    }

    @Override
    protected void initView() {
        super.initView();

        usAvatar = getViewById(R.id.us_avatar_iv);
        unreadCircle = getViewById(R.id.unread_view);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        applyConfig(true);
    }

    @Override
    public void setMessage(BaseMessage baseMessage, int position) {
        super.setMessage(baseMessage, position);
        MQConfig.getImageLoader(getContext()).displayImage(usAvatar, baseMessage.getAvatar(), R.drawable.mq_ic_holder_avatar, R.drawable.mq_ic_holder_avatar, 100, 100, null);
    }
}
