package com.meiqia.meiqiasdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQUtils;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/1/25 上午11:54
 * 描述:
 */
public class MQEvaluateDialog extends Dialog implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final int EVALUATE_GOOD = 2;
    private static final int EVALUATE_MEDIUM = 1;
    private static final int EVALUATE_BAD = 0;

    private RadioGroup mContentRg;
    private EditText mContentEt;
    private TextView mConfirmTv;
    private Callback mCallback;

    public MQEvaluateDialog(Activity activity) {
        super(activity, R.style.MQDialog);
        setContentView(R.layout.mq_dialog_evaluate);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        setCanceledOnTouchOutside(true);
        setCancelable(true);

        mContentEt = (EditText) findViewById(R.id.et_evaluate_content);
        mContentRg = (RadioGroup) findViewById(R.id.rg_evaluate_content);
        mContentRg.setOnCheckedChangeListener(this);

        findViewById(R.id.tv_evaluate_cancel).setOnClickListener(this);
        mConfirmTv = (TextView) findViewById(R.id.tv_evaluate_confirm);
        mConfirmTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.tv_evaluate_confirm && mCallback != null) {
            int level = EVALUATE_GOOD;
            int checkedId = mContentRg.getCheckedRadioButtonId();
            if (checkedId == R.id.rv_evaluate_medium) {
                level = EVALUATE_MEDIUM;
            } else if (checkedId == R.id.rv_evaluate_bad) {
                level = EVALUATE_BAD;
            }
            String content = mContentEt.getText().toString().trim();
            mCallback.executeEvaluate(level, content);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // 任何一个RadioButton选中后都让确定按钮可用
        mConfirmTv.setEnabled(true);

        // 点击RadioButton后关闭键盘，让Editext失去焦点
        mContentEt.clearFocus();
        MQUtils.closeKeyboard(this);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        /**
         * @param level   评价等级
         * @param content 评价内容
         */
        void executeEvaluate(int level, String content);
    }
}