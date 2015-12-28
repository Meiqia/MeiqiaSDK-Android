package com.meiqia.meiqiasdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;

public class MQConfirmDialog extends Dialog implements View.OnClickListener {
    private TextView mTitleTv;
    private TextView mContentTv;
    private OnDialogCallback mOnDialogCallback;

    public MQConfirmDialog(Activity activity, @StringRes int titleResId, @StringRes int contentResId, OnDialogCallback onDialogCallback) {
        this(activity, activity.getString(titleResId), activity.getString(contentResId), onDialogCallback);
    }

    public MQConfirmDialog(Activity activity, String title, String content, OnDialogCallback onDialogCallback) {
        super(activity, R.style.MQDialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.mq_dialog_confirm);
        mTitleTv = (TextView) findViewById(R.id.tv_comfirm_title);
        mContentTv = (TextView) findViewById(R.id.tv_comfirm_content);


        findViewById(R.id.tv_confirm_cancel).setOnClickListener(this);
        findViewById(R.id.tv_confirm_confirm).setOnClickListener(this);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mOnDialogCallback.onClickCancel();
            }
        });
        setCanceledOnTouchOutside(true);
        setCancelable(true);

        mOnDialogCallback = onDialogCallback;
        mTitleTv.setText(title);
        mContentTv.setText(content);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.tv_confirm_cancel) {
            mOnDialogCallback.onClickCancel();
        } else if (v.getId() == R.id.tv_confirm_confirm) {
            mOnDialogCallback.onClickConfirm();
        }
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    public void setTtitle(@StringRes int titleResId) {
        mTitleTv.setText(titleResId);
    }

    public void setContent(String content) {
        mContentTv.setText(content);
    }

    public void setContent(@StringRes int contentResId) {
        mContentTv.setText(contentResId);
    }

    public interface OnDialogCallback {
        void onClickConfirm();

        void onClickCancel();
    }
}