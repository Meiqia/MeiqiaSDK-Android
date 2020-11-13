package com.meiqia.meiqiasdk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQUtils;

public class MQInputDialog extends Dialog {

    private TextView titleTv;
    private EditText inputEt;
    private View confirmBtn;
    private View cancelBtn;

    public MQInputDialog(@NonNull Context context, String title, String input, String hint, int inputType, final OnContentChangeListener onContentChangeListener) {
        super(context, R.style.MQDialog);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.mq_dialog_input);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        titleTv = findViewById(R.id.tv_comfirm_title);
        inputEt = findViewById(R.id.et_evaluate_content);
        confirmBtn = findViewById(R.id.tv_evaluate_confirm);
        cancelBtn = findViewById(R.id.tv_evaluate_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onContentChangeListener.onContentChange(inputEt.getText().toString());
            }
        });

        titleTv.setText(title);
        inputEt.setText(input);
        inputEt.setHint(hint);
        inputEt.setInputType(inputType);
        MQUtils.openKeyboard(inputEt);
    }

    public interface OnContentChangeListener {
        void onContentChange(String content);
    }
}
