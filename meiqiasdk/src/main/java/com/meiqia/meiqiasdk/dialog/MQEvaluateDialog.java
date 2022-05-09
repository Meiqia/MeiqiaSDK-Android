package com.meiqia.meiqiasdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.EvaluateMessage;
import com.meiqia.meiqiasdk.util.MQUtils;

public class MQEvaluateDialog extends Dialog implements View.OnClickListener {

    private TextView mTipTv;
    private EditText mContentEt;
    private TextView mConfirmTv;
    private Callback mCallback;

    private int checkedState = EvaluateMessage.EVALUATE_GOOD;

    public MQEvaluateDialog(Activity activity, String tip) {
        super(activity, R.style.MQDialog);
        setContentView(R.layout.mq_dialog_evaluate);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        setCancelable(true);

        mTipTv = (TextView) findViewById(R.id.tv_evaluate_tip);
        mContentEt = (EditText) findViewById(R.id.et_evaluate_content);
        findViewById(R.id.mq_good_ll).setOnClickListener(this);
        findViewById(R.id.mq_mid_ll).setOnClickListener(this);
        findViewById(R.id.mq_bad_ll).setOnClickListener(this);
        checkState(EvaluateMessage.EVALUATE_GOOD);

        findViewById(R.id.tv_evaluate_cancel).setOnClickListener(this);
        mConfirmTv = (TextView) findViewById(R.id.tv_evaluate_confirm);
        mConfirmTv.setOnClickListener(this);

        if (!TextUtils.isEmpty(tip)) {
            mTipTv.setText(tip);
        }
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_evaluate_confirm && mCallback != null) {
            MQUtils.closeKeyboard(this);
            dismiss();

            String content = mContentEt.getText().toString().trim();
            mCallback.executeEvaluate(checkedState, content);
            // 重置状态
            mContentEt.setText("");
            mContentEt.clearFocus();
            checkState(EvaluateMessage.EVALUATE_GOOD);
        } else if (v.getId() == R.id.tv_evaluate_cancel) {
            MQUtils.closeKeyboard(this);
            dismiss();
            // 重置状态
            mContentEt.setText("");
            mContentEt.clearFocus();
            checkState(EvaluateMessage.EVALUATE_GOOD);
        } else if (v.getId() == R.id.mq_good_ll) {
            checkState(EvaluateMessage.EVALUATE_GOOD);
        } else if (v.getId() == R.id.mq_mid_ll) {
            checkState(EvaluateMessage.EVALUATE_MEDIUM);
        } else if (v.getId() == R.id.mq_bad_ll) {
            checkState(EvaluateMessage.EVALUATE_BAD);
        }
    }

    private void checkState(int state) {
        checkedState = state;
        ImageView goodCheckIv = findViewById(R.id.mq_good_check_iv);
        ImageView midCheckIv = findViewById(R.id.mq_mid_check_iv);
        ImageView badCheckIv = findViewById(R.id.mq_bad_check_iv);
        goodCheckIv.setImageResource(R.drawable.mq_radio_btn_uncheck);
        midCheckIv.setImageResource(R.drawable.mq_radio_btn_uncheck);
        badCheckIv.setImageResource(R.drawable.mq_radio_btn_uncheck);
        goodCheckIv.clearColorFilter();
        midCheckIv.clearColorFilter();
        badCheckIv.clearColorFilter();
        goodCheckIv.setColorFilter(getContext().getResources().getColor(R.color.mq_chat_event_gray));
        midCheckIv.setColorFilter(getContext().getResources().getColor(R.color.mq_chat_event_gray));
        badCheckIv.setColorFilter(getContext().getResources().getColor(R.color.mq_chat_event_gray));
        if (state == EvaluateMessage.EVALUATE_GOOD) {
            goodCheckIv.setImageResource(R.drawable.mq_radio_btn_checked);
            goodCheckIv.setColorFilter(getContext().getResources().getColor(R.color.mq_chat_robot_evaluate_textColor));
        } else if (state == EvaluateMessage.EVALUATE_MEDIUM) {
            midCheckIv.setImageResource(R.drawable.mq_radio_btn_checked);
            midCheckIv.setColorFilter(getContext().getResources().getColor(R.color.mq_chat_robot_evaluate_textColor));
        } else if (state == EvaluateMessage.EVALUATE_BAD) {
            badCheckIv.setImageResource(R.drawable.mq_radio_btn_checked);
            badCheckIv.setColorFilter(getContext().getResources().getColor(R.color.mq_chat_robot_evaluate_textColor));
        }
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