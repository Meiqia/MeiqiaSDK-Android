package com.meiqia.meiqiasdk.model;

import android.support.annotation.StringRes;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/13 下午2:21
 * 描述:提示主动点击转人工的消息
 */
public class InitiativeRedirectMessage extends BaseMessage {
    @StringRes
    private int mTipResId;

    public InitiativeRedirectMessage(@StringRes int tipResId) {
        setItemViewType(TYPE_INITIATIVE_REDIRECT_TIP);
        mTipResId = tipResId;
    }

    public int getTipResId() {
        return mTipResId;
    }

}