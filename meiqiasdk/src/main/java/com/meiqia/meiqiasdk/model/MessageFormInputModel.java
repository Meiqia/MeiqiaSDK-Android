package com.meiqia.meiqiasdk.model;

import android.text.InputType;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 下午2:25
 * 描述:
 */
public class MessageFormInputModel {
    public boolean singleLine = true;
    public int inputType = InputType.TYPE_CLASS_TEXT;
    public String tip;
    public String key;
    public String hint;
    public boolean required;
}
