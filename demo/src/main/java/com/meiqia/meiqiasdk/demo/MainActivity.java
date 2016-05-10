package com.meiqia.meiqiasdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.meiqia.core.callback.OnInitCallback;
import com.meiqia.meiqiasdk.glideimageloader.GlideImageloader;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQIntentBuilder;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_CONVERSATION_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMeiqiaSDK();

        customConfig();
    }

    private void initMeiqiaSDK() {
        // 替换成自己的key
        // 发布sdk时用
        String meiqiaKey = "a71c257c80dfe883d92a64dca323ec20";

        MQConfig.init(this, meiqiaKey, new GlideImageloader(), new OnInitCallback() {
            @Override
            public void onSuccess(String clientId) {
                Toast.makeText(MainActivity.this, "init success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String message) {
                Toast.makeText(MainActivity.this, "int failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void customConfig() {
        // 配置自定义信息
        MQConfig.ui.titleGravity = MQConfig.ui.MQTitleGravity.LEFT;
        MQConfig.ui.backArrowIconResId = android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha;
//        MQConfig.ui.titleBackgroundResId = R.color.test_red;
//        MQConfig.ui.titleTextColorResId = R.color.test_blue;
//        MQConfig.ui.leftChatBubbleColorResId = R.color.test_green;
//        MQConfig.ui.leftChatTextColorResId = R.color.test_red;
//        MQConfig.ui.rightChatBubbleColorResId = R.color.test_red;
//        MQConfig.ui.rightChatTextColorResId = R.color.test_green;

    }

    /**
     * 咨询客服
     *
     * @param v
     */
    public void conversation(View v) {
        // 不兼容Android6.0动态权限
        conversation();

        // 兼容Android6.0动态权限
//        conversationWrapper();
    }

    /**
     * 开发者功能
     *
     * @param v
     */
    public void developer(View v) {
        startActivity(new Intent(MainActivity.this, ApiSampleActivity.class));
    }

    /**
     * 自定义 Activity
     *
     * @param view
     */
    public void customizedConversation(View view) {
        Intent intent = new MQIntentBuilder(this, CustomizedMQConversationActivity.class).build();
        startActivity(intent);
    }

    // 处理 Android 6.0 的权限获取
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
//    }
//
//    @Override
//    public void onPermissionsGranted(int requestCode, List<String> perms) {
//    }
//
//    @Override
//    public void onPermissionsDenied(int requestCode, List<String> perms) {
//        MQUtils.show(this, R.string.mq_permission_denied_tip);
//    }
//
//    @AfterPermissionGranted(REQUEST_CODE_CONVERSATION_PERMISSIONS)
//    private void conversationWrapper() {
//        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
//        if (EasyPermissions.hasPermissions(this, perms)) {
//            conversation();
//        } else {
//            EasyPermissions.requestPermissions(this, getString(R.string.mq_runtime_permission_tip), REQUEST_CODE_CONVERSATION_PERMISSIONS, perms);
//        }
//    }

    private void conversation() {
        Intent intent = new MQIntentBuilder(this).build();
        startActivity(intent);
    }
}
