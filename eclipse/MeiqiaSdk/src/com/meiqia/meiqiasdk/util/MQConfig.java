package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.ColorInt;


public class MQConfig {
    public static final int DEFAULT = 0;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public MQConfig(Context context) {
        sp = context.getSharedPreferences("MeiQia_Config", Context.MODE_PRIVATE);
    }

    private void putString(String key, String value) {
        editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void putBoolean(String key, boolean value) {
        editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void putInt(String key, int value) {
        editor = sp.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean getShowVoiceMessage() {
        return sp.getBoolean("meiqia_show_voice_message", true);
    }

    public MQConfig setShowVoiceMessage(boolean value) {
        putBoolean("meiqia_show_voice_message", value);
        return this;
    }

    public int getTitleBackgroundColor() {
        return sp.getInt("meiqia_title_background_color", 0);
    }

    public MQConfig setTitleBackgroundColor(@ColorInt int backgroundColor) {
        putInt("meiqia_title_background_color", backgroundColor);
        return this;
    }

    public MQConfig setTitleTextColor(@ColorInt int titleTextColor) {
        putInt("meiqia_title_text_color", titleTextColor);
        return this;
    }

    public int getTitleTextColor() {
        return sp.getInt("meiqia_title_text_color", 0);
    }

}

