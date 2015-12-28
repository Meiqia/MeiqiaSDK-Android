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

    public boolean getShowVoiceMessage() {
        return sp.getBoolean("meiqia_show_voice_message", true);
    }

    public void setShowVoiceMessage(boolean value) {
        putBoolean("meiqia_show_voice_message", value);
    }

}

