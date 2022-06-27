package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;

import com.meiqia.core.MQManager;
import com.meiqia.core.MQNotificationMessageConfig;
import com.meiqia.core.bean.MQNotificationMessage;
import com.meiqia.core.callback.OnInitCallback;
import com.meiqia.core.callback.OnNotificationMessageOnClickListener;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.MQActivityLifecycleCallback;
import com.meiqia.meiqiasdk.callback.MQSimpleActivityLifecyleCallback;
import com.meiqia.meiqiasdk.callback.OnLinkClickCallback;
import com.meiqia.meiqiasdk.controller.ControllerImpl;
import com.meiqia.meiqiasdk.controller.MQController;
import com.meiqia.meiqiasdk.imageloader.MQImageLoader;

import java.util.HashMap;
import java.util.Map;


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
        @ColorRes
        public static int robotMenuItemTextColorResId = DEFAULT; // 机器人菜单消息列表文字颜色
        @ColorRes
        public static int robotMenuTipTextColorResId = DEFAULT; // 机器人菜单消息提示文本颜色
        @ColorRes
        public static int robotEvaluateTextColorResId = DEFAULT; // 机器人消息评价按钮的文字颜色

        public static String titleBackgroundColor = "";
        public static String titleTextColor = "";
        public static boolean isShowTitle = true; // 是否显示 Title

        public static Bitmap backNavIcon = null; // 返回按钮图标
        public static int backNavWidth = 0; // 返回按钮图标宽度
        public static int backNavHeight = 0; // 返回按钮图标高度
        public static int backNavMarginLeft = 0; // 返回按钮图左侧 margin
        public static String navRightButtonTxt = ""; // 导航栏右侧按钮文字
        public static String navRightButtonImageUrl = null; // 导航栏右侧按钮图标 url
        public static int navRightButtonImageWidth = 0; // 导航栏右侧按钮宽度
        public static int navRightButtonImageHeight = 0; // 导航栏右侧按钮高度
        public static View.OnClickListener navRightButtonOnClickListener; // 导航栏右侧按钮的点击回调

        public static View.OnClickListener navBackButtonOnClickListener; // 返回按钮点击回调

        public enum MQTitleGravity {
            LEFT, CENTER
        }
    }

    public static boolean isVoiceSwitchOpen = true; // 语音开关
    public static boolean isSoundSwitchOpen = true; // 声音开关
    public static boolean isLoadMessagesFromNativeOpen = false; // 加载本地数据开关
    public static boolean isEvaluateSwitchOpen = true; // 是否开启评价
    public static boolean isShowClientAvatar = false; // 是否显示客户头像
    public static boolean isPhotoSendOpen = true; // 是否显示发送图片消息按钮
    public static boolean isCameraImageSendOpen = true; // 是否显示发送相机图片消息按钮
    public static boolean isEmojiSendOpen = true; // 是否显示发送 Emoji 表情消息按钮

    private static MQActivityLifecycleCallback sActivityLifecycleCallback;
    private static OnLinkClickCallback sOnLinkClickCallback;

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

    public static void setActivityLifecycleCallback(MQActivityLifecycleCallback lifecycleCallback) {
        sActivityLifecycleCallback = lifecycleCallback;
    }

    public static MQActivityLifecycleCallback getActivityLifecycleCallback() {
        if (sActivityLifecycleCallback == null) {
            sActivityLifecycleCallback = new MQSimpleActivityLifecyleCallback();
        }
        return sActivityLifecycleCallback;
    }


    /**
     * 设置链接点击的回调
     * 注意:设置监听回调后,将不再跳转网页.如果需要跳转,开发者需要自行处理,例如: ac
     *
     * @param onLinkClickCallback 回调
     */
    public static void setOnLinkClickCallback(OnLinkClickCallback onLinkClickCallback) {
        MQConfig.sOnLinkClickCallback = onLinkClickCallback;
    }

    public static OnLinkClickCallback getOnLinkClickCallback() {
        return MQConfig.sOnLinkClickCallback;
    }

    @Deprecated
    public static void init(Context context, String appKey, MQImageLoader imageLoader, final OnInitCallback onInitCallBack) {
        MQManager.init(context, appKey, onInitCallBack);
        initDefaultNotificationCardResource(context);
    }

    public static void init(Context context, String appKey, OnInitCallback onInitCallBack) {
        MQManager.init(context, appKey, onInitCallBack);
        initDefaultNotificationCardResource(context);
    }

    private static void initDefaultNotificationCardResource(final Context context) {
        Map<String, Object> resource = new HashMap<>();
        resource.put("notificationCardLayoutId", R.layout.mq_notification_card);
        resource.put("titleTvId", R.id.mq_title_tv);
        resource.put("firstContentTvId", R.id.mq_first_content_tv);
        resource.put("avatarIvId", R.id.mq_title_iv);
        MQNotificationMessageConfig.getInstance().setNotificationCardResource(resource);
        MQNotificationMessageConfig.getInstance().setOnNotificationMessageOnClickListener(new OnNotificationMessageOnClickListener() {
            @Override
            public void onClick(View view, MQNotificationMessage notificationMessage) {
                Intent intent = new MQIntentBuilder(context).build();
                context.startActivity(intent);
            }
        });
    }
}

