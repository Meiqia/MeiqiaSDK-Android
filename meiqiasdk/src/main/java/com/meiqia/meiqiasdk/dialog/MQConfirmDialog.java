package com.meiqia.meiqiasdk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;

public class MQConfirmDialog extends Dialog {

    private TextView titleTv;
    private TextView contentTv;
    private View confirmBtn;
    private View cancelBtn;

    public MQConfirmDialog(@NonNull Context context, String title, String content, @Nullable final View.OnClickListener onPositiveClickListener, @Nullable final View.OnClickListener onNegativeClickListener) {
        super(context, R.style.MQDialog);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.mq_dialog_confirm);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        titleTv = findViewById(R.id.tv_comfirm_title);
        contentTv = findViewById(R.id.et_evaluate_content);
        confirmBtn = findViewById(R.id.tv_evaluate_confirm);
        cancelBtn = findViewById(R.id.tv_evaluate_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onNegativeClickListener != null) {
                    onNegativeClickListener.onClick(v);
                }
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onPositiveClickListener != null) {
                    onPositiveClickListener.onClick(v);
                }
            }
        });

        titleTv.setText(title);
        contentTv.setText(content);
    }
}
