package com.meiqia.meiqiasdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;

import com.meiqia.meiqiasdk.R;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/1/25 上午11:54
 * 描述:
 */
public class MQEvaluateDialog extends Dialog implements View.OnClickListener {

    public MQEvaluateDialog(Activity activity) {
        super(activity, R.style.MQDialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.mq_dialog_evaluate);
        findViewById(R.id.tv_evaluate_cancel).setOnClickListener(this);
        findViewById(R.id.tv_evaluate_confirm).setOnClickListener(this);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.tv_evaluate_cancel) {
            dismiss();
        } else if (v.getId() == R.id.tv_evaluate_confirm) {

        }
    }

}