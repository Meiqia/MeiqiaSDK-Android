package com.meiqia.meiqiasdk.demo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.TextMessage;

/**
 * 集成自 MQConversationActivity，可以动态改变其中的一些方法实现
 */
public class CustomizedMQConversationActivity extends MQConversationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 这里可以动态添加一些 View 到布局中
        // eg: 右边添加一个按钮
        RelativeLayout titleRL = (RelativeLayout) findViewById(R.id.title_rl);
        Button rightBtn = new Button(this);
        rightBtn.setText("RightBtn");
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        titleRL.addView(rightBtn, params);

        // do something
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CustomizedMQConversationActivity.this, "RightBtn OnClick", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onLoadDataComplete(MQConversationActivity mqConversationActivity, Agent agent) {
        if (agent != null) {
            BaseMessage message = new TextMessage("这是一条自动发送的消息");
            // 在打开对话界面的时候，自动发送一条消息
            mqConversationActivity.sendMessage(message);
        }
    }

}
