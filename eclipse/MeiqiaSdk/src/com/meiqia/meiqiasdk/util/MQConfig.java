package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import com.meiqia.core.MQManager;
import com.meiqia.core.callback.OnInitCallback;
import com.meiqia.meiqiasdk.controller.ControllerImpl;
import com.meiqia.meiqiasdk.controller.MQController;


public final class MQConfig {
    public static final int DEFAULT = -1;

    public static final class ui {
        public static MQTitleGravity titleGravity = MQTitleGravity.CENTER; // 标题文字对其方式
        @ColorRes
        public static int titleBackgroundResId = DEFAULT; // 标题栏背景颜色
        @ColorRes
        public static int titleTextColorResId = DEFAULT; // 标题栏文字颜色
        @ColorRes
        public static int leftChatBubbleColorResId = DEFAULT; // 左边气泡背景颜色
        @ColorRes
        public static int rightChatBubbleColorResId = DEFAULT; // 右边气泡背景颜色
        @ColorRes
        public static int leftChatTextColorResId = DEFAULT; // 左边气泡文字颜色
        @ColorRes
        public static int rightChatTextColorResId = DEFAULT; // 右边气泡文字颜色
        @DrawableRes
        public static int backArrowIconResId = DEFAULT; // 返回箭头图标资源id

        public enum MQTitleGravity {
            LEFT, CENTER
        }
    }

    public static boolean isVoiceSwitchOpen = true; // 语音开关
    public static boolean isSoundSwitchOpen = true; // 声音开关
    public static boolean isLoadMessagesFromNativeOpen = false; // 加载本地数据开关

    private static MQController sController;

    public static MQController getController(Context context) {
        if (sController == null) {
            synchronized (MQConfig.class) {
                if (sController == null) {
                    sController = new ControllerImpl(context.getApplicationContext());
                }
            }
        }
        return sController;
    }

    public static void registerController(MQController controller) {
        sController = controller;
    }

    private static MQImageLoader sImageLoader;

    public static MQImageLoader getImageLoader(Context context) {
        if (sImageLoader == null) {
            synchronized (MQConfig.class) {
                if (sImageLoader == null) {
                    throw new RuntimeException("请调用MQConfig.init方法初始化美洽 SDK，并传入MQImageLoader接口的实现类");
                }
            }
        }
        return sImageLoader;
    }

    public static void init(Context context, String appKey, MQImageLoader imageLoader, final OnInitCallback onInitCallBack) {
        sImageLoader = imageLoader;

        MQManager.init(context, appKey, onInitCallBack);
    }
}

