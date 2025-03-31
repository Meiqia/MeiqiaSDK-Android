package com.meiqia.meiqiasdk.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        Intent intent = new MQIntentBuilder(this).build();
        startActivity(intent);
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

    public void leaveMessageForm(View view) {
        startActivity(new Intent(this, MQMessageFormActivity.class));
    }

    public void linkWebView(View view) {
        Intent intent = new Intent(MainActivity.this, ActivityWebView.class);
        intent.putExtra("link", "https://chatlink-new.meiqia.cn/widget/standalone.html?eid=ab6e3a7cda04cc8bff00237214c3bcc6");
        startActivity(intent);
    }
}
