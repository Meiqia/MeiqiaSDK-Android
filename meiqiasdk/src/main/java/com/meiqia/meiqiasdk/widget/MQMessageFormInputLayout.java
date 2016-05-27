package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.MessageFormInputModel;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 上午11:19
 * 描述:
 */
public class MQMessageFormInputLayout extends MQBaseCustomCompositeView {
    private TextView mTipTv;
    private EditText mContentEt;

    public MQMessageFormInputLayout(Context context, MessageFormInputModel messageFormInputModel) {
        super(context);
        setFormInputModel(messageFormInputModel);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_layout_form_input;
    }

    @Override
    protected void initView() {
        mTipTv = getViewById(R.id.tip_tv);
        mContentEt = getViewById(R.id.content_et);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {
    }

    private void setFormInputModel(MessageFormInputModel messageFormInputModel) {
        mTipTv.setText(messageFormInputModel.tip);
        mContentEt.setHint(messageFormInputModel.hint);
        if (messageFormInputModel.inputType != 0) {
            mContentEt.setInputType(messageFormInputModel.inputType);
        }
        if (messageFormInputModel.required) {
            SpannableStringBuilder tipSsb = new SpannableStringBuilder(mTipTv.getText() + " *");
            tipSsb.setSpan(new ForegroundColorSpan(Color.RED), mTipTv.getText().length() + 1, tipSsb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTipTv.setText(tipSsb);
        }

        if (messageFormInputModel.singleLine) {
            mContentEt.setSingleLine();
        } else {
            mContentEt.setSingleLine(false);
            mContentEt.setMaxLines(4);
        }
    }

    public String getText() {
        return mContentEt.getText().toString().trim();
    }
}
