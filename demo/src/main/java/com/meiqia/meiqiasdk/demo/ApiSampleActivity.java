package com.meiqia.meiqiasdk.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.MQScheduleRule;
import com.meiqia.core.callback.OnClientInfoCallback;
import com.meiqia.core.callback.OnEndConversationCallback;
import com.meiqia.core.callback.OnGetMQClientIdCallBackOn;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.controller.ControllerImpl;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.util.HashMap;
import java.util.Map;

public class ApiSampleActivity extends Activity implements View.OnClickListener {

    private TextView currentIdTv;
    private View setCurrentIdOnlineBtn;
    private View setInputIdOnlineBtn;
    private View setCustomizedIdOnlineBtn;
    private View getNewIdBtn;
    private View setAgentTokenOnlineBtn;
    private View setGroupTokenOnlineBtn;
    private View setClientInfoBtn;
    private View offlineClientBtn;
    private View endConversationBtn;

    private MQManager mqManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        mqManager = MQManager.getInstance(this);

        findViews();
        setListeners();
        updateId();
    }

    private void findViews() {
        currentIdTv = (TextView) findViewById(R.id.current_id_tv);
        setCurrentIdOnlineBtn = findViewById(R.id.set_current_client_id_online_btn);
        setInputIdOnlineBtn = findViewById(R.id.set_meiqia_client_id_online_btn);
        setCustomizedIdOnlineBtn = findViewById(R.id.set_customised_id_online_btn);
        getNewIdBtn = findViewById(R.id.get_new_meiqia_id_btn);
        setAgentTokenOnlineBtn = findViewById(R.id.set_specified_agent_token_btn);
        setGroupTokenOnlineBtn = findViewById(R.id.set_specified_agent_group_token_btn);
        setClientInfoBtn = findViewById(R.id.set_client_info);
        offlineClientBtn = findViewById(R.id.set_client_offline_btn);
        endConversationBtn = findViewById(R.id.end_conversation_btn);
    }

    private void setListeners() {
        setCurrentIdOnlineBtn.setOnClickListener(this);
        setInputIdOnlineBtn.setOnClickListener(this);
        setCustomizedIdOnlineBtn.setOnClickListener(this);
        getNewIdBtn.setOnClickListener(this);
        setAgentTokenOnlineBtn.setOnClickListener(this);
        setGroupTokenOnlineBtn.setOnClickListener(this);
        setClientInfoBtn.setOnClickListener(this);
        offlineClientBtn.setOnClickListener(this);
        endConversationBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            // 使用当前顾客上线
            case R.id.set_current_client_id_online_btn:
                MQConfig.registerController(new ControllerImpl(this));
                Intent intent = new Intent(ApiSampleActivity.this, MQConversationActivity.class);
                startActivity(intent);
                break;
            // 使用指定 美洽顾客id 上线
            case R.id.set_meiqia_client_id_online_btn:
                showDialog("输入美洽 ID", new EditDialogOnClickListener() {
                    @Override
                    public void onInput(String clientId) {
                        if (!TextUtils.isEmpty(clientId)) {
                            Intent intent = new Intent(ApiSampleActivity.this, MQConversationActivity.class);
                            intent.putExtra(MQConversationActivity.CLIENT_ID, clientId);
                            startActivity(intent);
                            updateId();
                        }
                    }
                });
                break;
            // 使用 开发者用户id 上线
            case R.id.set_customised_id_online_btn:
                showDialog("输入开发者用户 ID", new EditDialogOnClickListener() {
                    @Override
                    public void onInput(String customizedId) {
                        if (!TextUtils.isEmpty(customizedId)) {
                            Intent intent = new Intent(ApiSampleActivity.this, MQConversationActivity.class);
                            intent.putExtra(MQConversationActivity.CUSTOMIZED_ID, customizedId);
                            startActivity(intent);
                            updateId();
                        }
                    }
                });
                break;
            // 获取一个新的美洽 ID
            case R.id.get_new_meiqia_id_btn:
                MQManager.getInstance(this).createMQClient(new OnGetMQClientIdCallBackOn() {
                    @Override
                    public void onSuccess(String mqClientId) {
                        toast("成功复制到剪贴板 :\n" + mqClientId);
                        if (!TextUtils.isEmpty(mqClientId)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                android.content.ClipboardManager mClipboard = (android.content.ClipboardManager) ApiSampleActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                mClipboard.setPrimaryClip(ClipData.newPlainText("mq_content", mqClientId));
                            } else {
                                ClipboardManager mClipboard = (ClipboardManager) ApiSampleActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                mClipboard.setText(mqClientId);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int code, String message) {
                        toast(message);
                    }
                });
                break;
            // 指定客服分配上线
            case R.id.set_specified_agent_token_btn:
                showDialog("输入指定客服 ID", new EditDialogOnClickListener() {
                    @Override
                    public void onInput(String agentId) {
                        if (!TextUtils.isEmpty(agentId)) {
                            MQManager.getInstance(ApiSampleActivity.this).setScheduledAgentOrGroupWithId(agentId, "", MQScheduleRule.REDIRECT_ENTERPRISE);
                            Intent intent = new Intent(ApiSampleActivity.this, MQConversationActivity.class);
                            startActivity(intent);
                            updateId();
                        }
                    }
                });
                break;
            // 指定客服分组分配上线
            case R.id.set_specified_agent_group_token_btn:
                showDialog("输入指定分组 ID", new EditDialogOnClickListener() {
                    @Override
                    public void onInput(String groupId) {
                        if (!TextUtils.isEmpty(groupId)) {
                            MQManager.getInstance(ApiSampleActivity.this).setScheduledAgentOrGroupWithId("", groupId, MQScheduleRule.REDIRECT_ENTERPRISE);
                            Intent intent = new Intent(ApiSampleActivity.this, MQConversationActivity.class);
                            startActivity(intent);
                        }
                    }
                });
                break;
            // 上传自定义信息
            case R.id.set_client_info:
                final Map<String, String> info = new HashMap<>();
                info.put("name", "富坚义博");
                info.put("avatar", "https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/1dee88eabfbd7bd4");
                info.put("sex", "男");
                info.put("tel", "111111");
                info.put("技能1", "休刊");
                info.put("技能2", "外出取材");
                info.put("技能3", "打麻将");
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("上传自定义信息");
                alertBuilder.setMessage("avatar -> https://s3.cn-north-1.amazonaws.com.cn/pics.meiqia.bucket/1dee88eabfbd7bd4\n" +
                        "name -> 富坚义博\n" +
                        "技能1 -> 休刊\n" +
                        "sex -> 男\n" +
                        "tel -> 111111\n" +
                        "技能2 -> 外出取材\n" +
                        "技能3 -> 打麻将");
                AlertDialog dialog = alertBuilder.create();
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MQManager.getInstance(ApiSampleActivity.this).setClientInfo(info, new OnClientInfoCallback() {
                            @Override
                            public void onSuccess() {
                                toast("set client info success");
                            }

                            @Override
                            public void onFailure(int code, String message) {
                                toast("set client info failed");
                            }
                        });
                    }
                });
                dialog.show();
                break;
            // 设置顾客离线
            case R.id.set_client_offline_btn:
                MQManager.getInstance(this).setClientOffline();
                break;
            // 结束当前对话
            case R.id.end_conversation_btn:
                MQManager.getInstance(this).endCurrentConversation(new OnEndConversationCallback() {
                    @Override
                    public void onSuccess() {
                        toast("endCurrentConversation success");
                    }

                    @Override
                    public void onFailure(int code, String message) {
                        toast("endCurrentConversation failed:\n" + message);
                    }
                });
                break;
        }
    }

    private void updateId() {
        currentIdTv.setText(mqManager.getCurrentClientId());
    }

    private void toast(String content) {
        Toast.makeText(ApiSampleActivity.this, content, Toast.LENGTH_LONG).show();
    }

    private void showDialog(String title, final EditDialogOnClickListener editDialogOnClickListener) {
        final Dialog inputDialog = new Dialog(this, R.style.MQDialog);
        inputDialog.setCancelable(true);
        inputDialog.setContentView(R.layout.dialog_input);
        TextView titleTv = (TextView) inputDialog.findViewById(R.id.tv_input_title);
        titleTv.setText(title);
        final EditText valueEt = (EditText) inputDialog.findViewById(R.id.et_input_value);
        inputDialog.findViewById(R.id.tv_input_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MQUtils.closeKeyboard(inputDialog);
                inputDialog.dismiss();
                editDialogOnClickListener.onInput(valueEt.getText().toString());
            }
        });
        inputDialog.show();
        MQUtils.openKeyboard(this, valueEt);
    }

    public interface EditDialogOnClickListener {
        void onInput(String input);
    }

}
