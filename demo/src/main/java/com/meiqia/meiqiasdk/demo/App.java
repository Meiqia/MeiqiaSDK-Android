package com.meiqia.meiqiasdk.demo;

import android.app.Application;
import android.text.InputType;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.callback.OnInitCallback;
import com.meiqia.meiqiasdk.model.MessageFormInputModel;
import com.meiqia.meiqiasdk.uilimageloader.UILImageLoader;
import com.meiqia.meiqiasdk.util.MQConfig;

import java.util.ArrayList;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/25 下午4:28
 * 描述:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initMeiqiaSDK();

        MQManager.setDebugMode(true);
    }

    private void initMeiqiaSDK() {
        MQManager.setDebugMode(true);

        // 替换成自己的key
        String meiqiaKey = "a71c257c80dfe883d92a64dca323ec20";
        MQConfig.init(this, meiqiaKey, new UILImageLoader(), new OnInitCallback() {
            @Override
            public void onSuccess(String clientId) {
                Toast.makeText(App.this, "init success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String message) {
                Toast.makeText(App.this, "int failure message = " + message, Toast.LENGTH_SHORT).show();
            }
        });

        // 可选
        customMeiqiaSDK();
    }

    private void customMeiqiaSDK() {
        // 配置自定义信息
        MQConfig.ui.titleGravity = MQConfig.ui.MQTitleGravity.LEFT;
        MQConfig.ui.backArrowIconResId = android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha;
//        MQConfig.ui.titleBackgroundResId = R.color.test_red;
//        MQConfig.ui.titleTextColorResId = R.color.test_blue;
//        MQConfig.ui.leftChatBubbleColorResId = R.color.test_green;
//        MQConfig.ui.leftChatTextColorResId = R.color.test_red;
//        MQConfig.ui.rightChatBubbleColorResId = R.color.test_red;
//        MQConfig.ui.rightChatTextColorResId = R.color.test_green;
//        MQConfig.ui.robotEvaluateTextColorResId = R.color.test_red;
//        MQConfig.ui.robotMenuItemTextColorResId = R.color.test_blue;
//        MQConfig.ui.robotMenuTipTextColorResId = R.color.test_blue;


        // 自定义留言表单引导文案，配置了该引导文案后将不会读取工作台配置的引导文案
        MQConfig.leaveMessageIntro = "自定义留言表单引导文案";

        // 初始化自定义留言表单字段，如果不配置该选项则留言表单界面默认有留言、邮箱、手机三个输入项
        MQConfig.messageFormInputModels = new ArrayList<>();
        MessageFormInputModel phoneMfim = new MessageFormInputModel();
        phoneMfim.tip = "手机";
        phoneMfim.key = "tel";
        phoneMfim.required = true;
        phoneMfim.hint = "请输入你的手机号";
        phoneMfim.inputType = InputType.TYPE_CLASS_PHONE;

        MessageFormInputModel emailMfim = new MessageFormInputModel();
        emailMfim.tip = "邮箱";
        emailMfim.key = "email";
        emailMfim.required = true;
        emailMfim.hint = "请输入你的邮箱";
        emailMfim.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

        MessageFormInputModel nameMfim = new MessageFormInputModel();
        nameMfim.tip = "姓名";
        nameMfim.key = "name";
        nameMfim.hint = "请输入你的姓名";
        nameMfim.inputType = InputType.TYPE_CLASS_TEXT;

        MessageFormInputModel customMfim = new MessageFormInputModel();
        customMfim.tip = "自定义";
        customMfim.key = "自定义";
        customMfim.hint = "请输入你的自定义信息";
        customMfim.singleLine = false;
        customMfim.inputType = InputType.TYPE_CLASS_TEXT;


        MQConfig.messageFormInputModels.add(phoneMfim);
        MQConfig.messageFormInputModels.add(emailMfim);
        MQConfig.messageFormInputModels.add(nameMfim);
        MQConfig.messageFormInputModels.add(customMfim);
    }

}
