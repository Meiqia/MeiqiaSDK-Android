package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.util.MQTimeUtils;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午4:20
 * 描述:
 */
public class MQTimeItem extends MQBaseCustomCompositeView {
    private TextView mContentTv;

    public MQTimeItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_chat_time;
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
        mContentTv.setText(MQTimeUtils.parseTime(baseMessage.getCreatedOn()));
    }
}
