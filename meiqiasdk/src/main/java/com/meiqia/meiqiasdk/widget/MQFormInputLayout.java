package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 上午11:19
 * 描述:
 */
public class MQFormInputLayout extends MQBaseCustomCompositeView {
    private TextView mTipTv;
    private EditText mContentEt;
    private boolean mIsRequired;

    public MQFormInputLayout(Context context) {
        super(context);
    }

    public MQFormInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MQFormInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    protected int[] getAttrs() {
        return R.styleable.MQFormInputLayout;
    }

    @Override
    protected void initAttr(int attr, TypedArray typedArray) {
        // 作为library时，android studio中不能用switch
        if (attr == R.styleable.MQFormInputLayout_android_hint) {
            mContentEt.setHint(typedArray.getString(attr));
        } else if (attr == R.styleable.MQFormInputLayout_mq_fil_tip) {
            mTipTv.setText(typedArray.getString(attr));
        } else if (attr == R.styleable.MQFormInputLayout_mq_fil_required) {
            mIsRequired = typedArray.getBoolean(attr, mIsRequired);
        } else if (attr == R.styleable.MQFormInputLayout_android_inputType) {
            mContentEt.setInputType(typedArray.getInt(attr, EditorInfo.TYPE_NULL));
        } else if (attr == R.styleable.MQFormInputLayout_android_singleLine) {
            mContentEt.setSingleLine(typedArray.getBoolean(attr, false));
        }
    }

    @Override
    protected void processLogic() {
        if (mIsRequired) {
            setRequired();
        }

    }

    public void setRequired() {
        SpannableStringBuilder tipSsb = new SpannableStringBuilder(mTipTv.getText() + " *");
        tipSsb.setSpan(new ForegroundColorSpan(Color.RED), mTipTv.getText().length() + 1, tipSsb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTipTv.setText(tipSsb);
    }

    public String getText() {
        return mContentEt.getText().toString().trim();
    }

    public void setTip(String tip) {
        mTipTv.setText(tip);
    }

    public void setHint(String hint) {
        mContentEt.setHint(hint);
    }

    public void setSingleLine() {
        mContentEt.setSingleLine();
    }

    public void setInputType(int type) {
        mContentEt.setInputType(type);
    }
}
