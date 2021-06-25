package com.meiqia.meiqiasdk.chatitem;

import android.content.Context;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQTimeUtils;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;

public class MQConvDividerItem extends MQBaseCustomCompositeView {

    private TextView contentTv;

    public MQConvDividerItem(Context context, long createTime) {
        super(context);
        contentTv.setText(MQTimeUtils.partLongToMonthDay(createTime));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_conv_divider;
    }

    @Override
    protected void initView() {
        contentTv = findViewById(R.id.content_tv);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {

    }

}