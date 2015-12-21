package com.meiqia.meiqiasdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.callback.OnInitCallBackOn;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 替换成自己的key
        String meiqiaKey = "a71c257c80dfe883d92a64dca323ec20";

        MQManager.init(this, meiqiaKey, new OnInitCallBackOn() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "init success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String message) {
                Toast.makeText(MainActivity.this, "int failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 咨询客服
     *
     * @param v
     */
    public void conversation(View v) {
        startActivity(new Intent(MainActivity.this, MQConversationActivity.class));
    }

    /**
     * 开发者功能
     *
     * @param v
     */
    public void developer(View v) {
        startActivity(new Intent(MainActivity.this, DeveloperActivity.class));
    }

}