package com.meiqia.meiqiasdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.callback.OnInitCallBackOn;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.util.MQConfig;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MQConfig config = new MQConfig(this);
        //xiaoxin key 67c9f3bcdc08c7cf435d7f2527378fa4
        //zhufu e6fda248b552a821f6c96146caf2bffe
        MQManager.init(this, "67c9f3bcdc08c7cf435d7f2527378fa4", new OnInitCallBackOn() {
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