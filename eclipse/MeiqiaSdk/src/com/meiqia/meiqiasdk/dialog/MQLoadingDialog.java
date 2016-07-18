package com.meiqia.meiqiasdk.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.meiqia.meiqiasdk.R;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/3/4 上午11:03
 * 描述:
 */
public class MQLoadingDialog extends Dialog {

    public MQLoadingDialog(Activity activity) {
        super(activity, R.style.MQDialog);
        setContentView(R.layout.mq_dialog_loading);
    }
}
