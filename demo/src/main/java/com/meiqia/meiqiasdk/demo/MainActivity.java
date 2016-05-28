package com.meiqia.meiqiasdk.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.meiqia.meiqiasdk.activity.MQMessageFormActivity;
import com.meiqia.meiqiasdk.util.MQIntentBuilder;
import com.meiqia.meiqiasdk.util.MQUtils;

public class MainActivity extends Activity {
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 咨询客服
     *
     * @param v
     */
    public void conversation(View v) {
        // 不兼容Android6.0动态权限
//        conversation();

        // 兼容Android6.0动态权限
        conversationWrapper();
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

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    conversationWrapper();
                } else {
                    MQUtils.show(this, com.meiqia.meiqiasdk.R.string.mq_sdcard_no_permission);
                }
                break;
            }
        }

    }

    private void conversationWrapper() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            conversation();
        }
    }

    private void conversation() {
        Intent intent = new MQIntentBuilder(this).build();
        startActivity(intent);
    }

    public void leaveMessageForm(View view) {
        startActivity(new Intent(this, MQMessageFormActivity.class));
    }
}
