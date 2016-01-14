package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.content.SharedPreferences;


public class MQConfig {

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

    public void setShowVoiceMessage(boolean value) {
        putBoolean("meiqia_show_voice_message", value);
    }

    public int getTitleBackgroundColor() {
        return sp.getInt("meiqia_title_background_color", -1);
    }

    public void setTitleBackgroundColor(int backgroundColor) {
        putInt("meiqia_title_background_color", backgroundColor);
    }

    public void setTitleTextColor(int backIconColor) {
        putInt("meiqia_title_text_color", backIconColor);
    }

    public int getTitleTextColor() {
        return sp.getInt("meiqia_title_text_color", -1);
    }

}

