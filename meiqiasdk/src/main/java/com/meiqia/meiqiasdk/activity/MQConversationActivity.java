package com.meiqia.meiqiasdk.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.controller.ControllerImpl;
import com.meiqia.meiqiasdk.controller.MQController;
import com.meiqia.meiqiasdk.controller.MediaRecordFunc;
import com.meiqia.meiqiasdk.dialog.MQViewPhotoDialog;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.AgentChangeMessage;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.LeaveTipMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.TextMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.util.ErrorCode;
import com.meiqia.meiqiasdk.util.MQChatAdapter;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQTimeUtils;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQEditToolbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MQConversationActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MQConversationActivity.class.getSimpleName();

    public static final String CLIENT_ID = "clientId";
    public static final String CUSTOMIZED_ID = "customizedId";

    private static final int REQUEST_CODE_CAMERA = 0;
    private static final int REQUEST_CODE_PHOTO = 1;
    private static final int REQUEST_CODE_PERMISSIONS = 2;
    private static int MESSAGE_PAGE_COUNT = 30; //消息每页加载数量
    private static final long MIN_RECORD_INTERNAL_TIME = 800;

    private static MQController controller;

    // 控件
    private View backBtn;
    private TextView titleTv;
    private ListView conversationListView;
    private EditText inputEt;
    private TextView voiceOrSendTv;
    private Button emojiSelectBtn;
    private Button photoSelectBtn;
    private ProgressBar loadProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    // 语音
    private View voiceHoldView;
    private View voiceMicOnHoldViewIv;
    private float voiceHoldViewPressMinY; // 录音控件触摸y坐标
    private int recordState; // 标记录音的状态
    private long recordStartTime;

    // 用来标识 voiceOrSendTv 的状态
    private static final int VOICE_STATE = 0;
    private static final int SEND_STATE = 1;
    private static final int KEYBOARD_STATE = 3;
    private int voiceOrSendBtnState = VOICE_STATE;

    private List<BaseMessage> chatMessageList = new ArrayList<>();
    private MQChatAdapter chatMsgAdapter;
    private MessageReceiver messageReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
    // 改变title状态
    private Handler mHandler;
    // Sound
    private SoundPool soundPool;

    // 是否已经加载数据的标识
    private boolean hasLoadData = false;
    private boolean isPause;

    private Agent currentAgent; // 当前客服
    private MQConfig mqConfig;

    private MQEditToolbar mEditToolbar;
    private MQViewPhotoDialog mMQViewPhotoDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕长亮
        setContentView(R.layout.mq_activity_conversation);

        init();
        findViews();
        setListeners();

        // 初始化输入栏状态
        changeInputStateToTextOrVoice();
        // 注册广播
        registerReceiver();

        mEditToolbar.init(this, inputEt);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置顾客上线，请求分配客服
        setClientOnline();
        isPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(messageReceiver);
            unregisterReceiver(networkChangeReceiver);
        } catch (Exception e) {
            //有些时候会出现未注册就取消注册的情况，暂时不知道为什么
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //如果在表情选择的时候按下 Back 键，隐藏表情 panel
        if (keyCode == KeyEvent.KEYCODE_BACK && mEditToolbar.isEmotionKeyboardVisible()) {
            mEditToolbar.closeEmotionKeyboard();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void init() {
        if (controller == null) {
            controller = new ControllerImpl(this);
        }
        MQTimeUtils.init(this);
        mqConfig = new MQConfig(this);
        // 初始化 ImageLoader
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);

        // handler
        mHandler = new Handler();
        // sound
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        soundPool.load(this, R.raw.mq_message, 1);
    }

    private void findViews() {
        backBtn = findViewById(R.id.back_rl);
        conversationListView = (ListView) findViewById(R.id.messages_lv);
        inputEt = (EditText) findViewById(R.id.input_et);
        voiceHoldView = findViewById(R.id.voice_hold_view);
        voiceMicOnHoldViewIv = findViewById(R.id.voice_mic_iv);
        emojiSelectBtn = (Button) findViewById(R.id.emoji_select_btn);
        mEditToolbar = (MQEditToolbar) findViewById(R.id.editToolbar);
        voiceOrSendTv = (TextView) findViewById(R.id.voice_or_send_tv);
        photoSelectBtn = (Button) findViewById(R.id.photo_select_btn);
        loadProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        titleTv = (TextView) findViewById(R.id.title_tv);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    }

    private void setListeners() {
        backBtn.setOnClickListener(this);
        voiceOrSendTv.setOnClickListener(this);
        photoSelectBtn.setOnClickListener(this);
        // 绑定 EditText 的监听器
        inputEt.addTextChangedListener(inputTextWatcher);
        // 表情
        emojiSelectBtn.setOnClickListener(this);
        // 对话列表，单击「隐藏键盘」、「表情 panel」
        conversationListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (MotionEvent.ACTION_DOWN == arg1.getAction()) {
                    mEditToolbar.closeAllKeyboard();
                }
                return false;
            }
        });
        // 添加长按复制功能
        conversationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String content = chatMessageList.get(arg2).getContent();
                if (!TextUtils.isEmpty(content)) {
                    MQUtils.clip(MQConversationActivity.this, content);
                    MQUtils.show(MQConversationActivity.this, R.string.mq_copy_success);
                    return true;
                }
                return false;
            }
        });
        // 下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMoreDataFromService();
            }
        });
        // 录音
        voiceHoldView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        recordStartTime = System.currentTimeMillis();
                        if (!isSdcardAvailable()) {
                            return true;
                        }
                        voiceHoldViewPressMinY = event.getRawY();

                        // 处理用户连续点击
                        mHandler.removeCallbacks(startRecord);
                        mHandler.postDelayed(startRecord, 100);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (recordState == MediaRecordFunc.SUCCESS || recordState == MediaRecordFunc.CANCEL) {
                            // 判断用户是否主动取消录音
                            if (Math.abs(voiceHoldViewPressMinY - event.getRawY()) > MQUtils.dip2px(MQConversationActivity.this, 40)) {
                                MediaRecordFunc.getInstance(MQConversationActivity.this).showCancelContent();
                                recordState = MediaRecordFunc.CANCEL;
                            } else {
                                MediaRecordFunc.getInstance(MQConversationActivity.this).showUpThenCancelContent();
                                recordState = MediaRecordFunc.SUCCESS;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        long internalTime = System.currentTimeMillis() - recordStartTime;
                        if (internalTime <= MIN_RECORD_INTERNAL_TIME) {
                            mHandler.removeCallbacks(startRecord);
                            stopRecord();
                            // 连续点击一直提示太烦了
                            if (internalTime > 500) {
                                MQUtils.show(MQConversationActivity.this, R.string.mq_record_record_time_is_short);
                            }
                            return true;
                        }

                        if (recordState == MediaRecordFunc.SUCCESS || recordState == MediaRecordFunc.CANCEL) {
                            String voicePath = stopRecord();
                            if (!TextUtils.isEmpty(voicePath) && recordState == MediaRecordFunc.SUCCESS && MediaRecordFunc.isVoiceFileAvailable(MQConversationActivity.this, voicePath)) {
                                createAndPreSendVoiceMessage(voicePath);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        stopRecord();
                        break;
                }
                return true;
            }
        });

        // 语音倒计时监听
        MediaRecordFunc.getInstance(MQConversationActivity.this).setOnCountDownListener(new MediaRecordFunc.OnCountDownListener() {
            @Override
            public void timeUp() {
                String voicePath = stopRecord();
                if (!TextUtils.isEmpty(voicePath) && recordState == MediaRecordFunc.SUCCESS) {
                    createAndPreSendVoiceMessage(voicePath);
                }
            }
        });
    }

    private Runnable startRecord = new Runnable() {
        @Override
        public void run() {
            voiceHoldView.setBackgroundResource(R.drawable.mq_bg_edit_view_pressed);
            voiceMicOnHoldViewIv.setBackgroundResource(R.drawable.mq_ic_mid_record_pressed);
            recordState = MediaRecordFunc.getInstance(MQConversationActivity.this).startRecordAndFile();
            if (recordState == MediaRecordFunc.SUCCESS) {
                MediaRecordFunc.getInstance(MQConversationActivity.this).showContent(MQConversationActivity.this, conversationListView);
            } else {
                MQUtils.show(MQConversationActivity.this, R.string.mq_record_failed);
                voiceHoldView.setBackgroundResource(R.drawable.mq_bg_edit_view);
                voiceMicOnHoldViewIv.setBackgroundResource(R.drawable.mq_ic_mid_record_mic_nor);
            }
        }
    };

    /**
     * 停止录音
     *
     * @return 录音文件路径
     */
    private String stopRecord() {
        String voicePath = MediaRecordFunc.getInstance(MQConversationActivity.this).stopRecordAndFile();
        MediaRecordFunc.getInstance(MQConversationActivity.this).dismissContent();
        voiceHoldView.setBackgroundResource(R.drawable.mq_bg_edit_view);
        voiceMicOnHoldViewIv.setBackgroundResource(R.drawable.mq_ic_mid_record_mic_nor);
        return voicePath;
    }

    /**
     * 注册 controller
     *
     * @param controller
     */
    public static void registerController(MQController controller) {
        MQConversationActivity.controller = controller;
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        // 注册消息接收
        messageReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MQController.ACTION_AGENT_INPUTTING);
        intentFilter.addAction(MQController.ACTION_NEW_MESSAGE_RECEIVED);
        intentFilter.addAction(MQController.ACTION_CLIENT_IS_REDIRECTED_EVENT);
        registerReceiver(messageReceiver, intentFilter);

        // 网络监听
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, mFilter);
    }

    /**
     * 将 title 改为客服名字
     *
     * @param agentName 客服名
     */
    private void changeTitleToAgentName(String agentName) {
        titleTv.setText(agentName);
    }

    /**
     * 将 title 改为客服名字
     *
     * @param agent 客服实体
     */
    private void changeTitleToAgentName(Agent agent) {
        if (agent != null) {
            titleTv.setText(agent.getNickname());
        } else {
            changeTitleToNoAgentState();
        }
    }

    /**
     * 将 title 改为 正在输入
     */
    private void changeTitleToInputting() {
        titleTv.setText(getResources().getString(R.string.mq_title_inputting));
    }

    /**
     * 将 title 改为 正在分配客服
     */
    private void changeTitleToAllocatingAgent() {
        titleTv.setText(getResources().getString(R.string.mq_allocate_agent));
    }

    /**
     * 将 title 改为没有客服的状态
     */
    private void changeTitleToNoAgentState() {
        titleTv.setText(getResources().getString(R.string.mq_title_leave_msg));
    }

    /**
     * 将 title 改为没有网络状态
     */
    private void changeTitleToNetErrorState() {
        titleTv.setText(getResources().getString(R.string.mq_title_net_not_work));
    }

    /**
     * 将 title 改为未知错误状态
     */
    private void changeTitleToUnknownErrorState() {
        titleTv.setText(getResources().getString(R.string.mq_title_unknown_error));
    }

    /**
     * 添加 转接客服 的消息 Tip 到列表
     *
     * @param agentNickName 客服名字
     */
    private void addDirectAgentMessageTip(String agentNickName) {
        titleTv.setText(agentNickName);
        AgentChangeMessage agentChangeMessage = new AgentChangeMessage();
        agentChangeMessage.setAgentNickname(agentNickName);
        chatMsgAdapter.addMQMessage(agentChangeMessage);
    }

    private boolean isAddLeaveTip;

    /**
     * 添加 留言 的 Tip
     */
    private void addLeaveMessageTip() {
        if (!isAddLeaveTip) {
            titleTv.setText(getResources().getString(R.string.mq_title_leave_msg));
            LeaveTipMessage leaveTip = new LeaveTipMessage();
            //添加到当前消息的上一个位置
            int position = chatMessageList.size();
            if (position != 0) {
                position = position - 1;
            }
            chatMsgAdapter.addMQMessage(leaveTip, position);
            isAddLeaveTip = true;
        }
    }

    /**
     * 从列表移除 留言 的 Tip
     */
    private void removeLeaveMessageTip() {
        Iterator<BaseMessage> chatItemViewBaseIterator = chatMessageList.iterator();
        while (chatItemViewBaseIterator.hasNext()) {
            BaseMessage baseMessage = chatItemViewBaseIterator.next();
            if (baseMessage.getItemViewType() == BaseMessage.TYPE_TIP) {
                chatItemViewBaseIterator.remove();
                chatMsgAdapter.notifyDataSetChanged();
                return;
            }
        }
        isAddLeaveTip = false;
    }

    /**
     * 将输入状态改为 可以发送文字和语音 的状态
     */
    private void changeInputStateToTextOrVoice() {
        inputEt.setVisibility(View.VISIBLE);
        emojiSelectBtn.setVisibility(View.VISIBLE);
        voiceHoldView.setVisibility(View.GONE);
        voiceMicOnHoldViewIv.setVisibility(View.GONE);
        // 根据语音开关，显示隐藏右边的录音图标
        if (mqConfig.getShowVoiceMessage()) {
            voiceOrSendTv.setBackgroundResource(R.drawable.mq_voice_btn_background);
            voiceOrSendTv.setText("");
            voiceOrSendBtnState = VOICE_STATE;
        } else {
            voiceOrSendTv.setBackgroundResource(R.drawable.mq_bg_transparent);
            voiceOrSendTv.setText(getResources().getString(R.string.mq_send));
            voiceOrSendBtnState = SEND_STATE;
        }
        if (!TextUtils.isEmpty(inputEt.getText().toString())) {
            changeInputStateToSend();
        }
    }

    /**
     * 将输入状态改为 可以录音 的状态
     */
    private void changeInputStateToRecord() {
        inputEt.setVisibility(View.GONE);
        emojiSelectBtn.setVisibility(View.GONE);
        voiceOrSendTv.setBackgroundResource(R.drawable.mq_keyboard_btn_background);
        voiceOrSendTv.setText("");
        voiceHoldView.setVisibility(View.VISIBLE);
        voiceMicOnHoldViewIv.setVisibility(View.VISIBLE);
        voiceOrSendBtnState = KEYBOARD_STATE;
        mEditToolbar.closeAllKeyboard();
    }

    /**
     * 将输入状态改为 可以发送 的状态
     */
    private void changeInputStateToSend() {
        inputEt.setVisibility(View.VISIBLE);
        emojiSelectBtn.setVisibility(View.VISIBLE);
        voiceHoldView.setVisibility(View.GONE);
        voiceMicOnHoldViewIv.setVisibility(View.GONE);
        voiceOrSendTv.setBackgroundResource(R.drawable.mq_bg_transparent);
        voiceOrSendTv.setText(getResources().getString(R.string.mq_send));
        voiceOrSendBtnState = SEND_STATE;
    }

    private void setCurrentAgent(Agent agent) {
        this.currentAgent = agent;
    }

    /**
     * 从服务器获取更多消息并加载
     */
    private void loadMoreDataFromService() {
        // 最早消息的创建时间
        long lastMessageCreateOn = System.currentTimeMillis();
        if (chatMessageList.size() > 0) lastMessageCreateOn = chatMessageList.get(0).getCreatedOn();
        // 获取该时间之前的消息
        controller.getMessageFromService(lastMessageCreateOn, MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {
            @Override
            public void onSuccess(final List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanVoiceMessage(messageList);
                //添加时间戳
                MQTimeUtils.refreshMQTimeItem(messageList);
                chatMsgAdapter.loadMoreMessage(messageList);
                conversationListView.setSelection(messageList.size());
                swipeRefreshLayout.setRefreshing(false);
                // 没有消息后，禁止下拉加载
                if (messageList.size() == 0) {
                    swipeRefreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
                chatMsgAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 从数据库更多消息并加载
     */
    private void loadMoreDataFromDatabase() {
        // 最早消息的创建时间
        long lastMessageCreateOn = System.currentTimeMillis();
        if (chatMessageList.size() > 0) lastMessageCreateOn = chatMessageList.get(0).getCreatedOn();
        // 获取该时间之前的消息
        controller.getMessagesFromDatabase(lastMessageCreateOn, MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {
            @Override
            public void onSuccess(final List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanVoiceMessage(messageList);
                //添加时间戳
                MQTimeUtils.refreshMQTimeItem(messageList);
                chatMsgAdapter.loadMoreMessage(messageList);
                conversationListView.setSelection(messageList.size());
                swipeRefreshLayout.setRefreshing(false);
                // 没有消息后，禁止下拉加载
                if (messageList.size() == 0) {
                    swipeRefreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
                chatMsgAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 设置顾客上线
     */
    private void setClientOnline() {
        if (currentAgent == null) {
            // Title 显示正在分配客服
            changeTitleToAllocatingAgent();

            // 从 intent 获取 clientId 和 customizedId
            Intent intent = getIntent();
            String clientId = null;
            String customizedId = null;
            if (intent != null) {
                clientId = getIntent().getStringExtra(CLIENT_ID);
                customizedId = getIntent().getStringExtra(CUSTOMIZED_ID);
            }

            // 上线
            controller.setCurrentClientOnline(clientId, customizedId, new OnClientOnlineCallback() {

                @Override
                public void onSuccess(Agent agent, List<BaseMessage> conversationMessageList) {
                    setCurrentAgent(agent);
                    changeTitleToAgentName(agent);
                    removeLeaveMessageTip();

                    // 根据设置，过滤语音消息
                    cleanVoiceMessage(conversationMessageList);

                    //加载数据
                    chatMessageList.clear();
                    chatMessageList.addAll(conversationMessageList);
                    loadData();
                }

                @Override
                public void onFailure(int code, String message) {
                    if (ErrorCode.NET_NOT_WORK == code) {
                        changeTitleToNetErrorState();
                    } else if (ErrorCode.NO_AGENT_ONLINE == code) {
                        setCurrentAgent(null);
                        changeTitleToNoAgentState();
                    } else {
                        changeTitleToUnknownErrorState();
                    }
                    //如果没有加载数据，则加载数据
                    if (!hasLoadData) {
                        getMessageDataFromDatabaseAndLoad();
                    }

                }
            });
        } else {
            changeTitleToAgentName(currentAgent);
        }
    }

    /**
     * 从数据库获取消息并加载
     */
    private void getMessageDataFromDatabaseAndLoad() {
        // 从数据库获取数据
        controller.getMessagesFromDatabase(System.currentTimeMillis(), MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {

            @Override
            public void onSuccess(List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanVoiceMessage(messageList);

                chatMessageList.addAll(messageList);
                loadData();
            }

            @Override
            public void onFailure(int code, String responseString) {

            }
        });
    }

    /**
     * 加载消息到列表中
     */
    private void loadData() {
        // 添加TimeItem
        MQTimeUtils.refreshMQTimeItem(chatMessageList);
        // 加载到UI
        loadProgressBar.setVisibility(View.GONE);
        // 将正在发送显示为已发送
        for (BaseMessage message : chatMessageList) {
            if (BaseMessage.STATE_SENDING.equals(message.getStatus())) {
                message.setStatus(BaseMessage.STATE_ARRIVE);
            }
        }
        chatMsgAdapter = new MQChatAdapter(MQConversationActivity.this, chatMessageList, conversationListView);
        conversationListView.setAdapter(chatMsgAdapter);
        conversationListView.setSelection(chatMsgAdapter.getCount() - 1);
        hasLoadData = true;
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        // 返回按钮
        if (id == R.id.back_rl) {
            onBackPressed();
        }
        // 表情按钮
        else if (id == R.id.emoji_select_btn) {
            mEditToolbar.toggleKeyboard();
        }
        // 发送按钮
        else if (id == R.id.voice_or_send_tv) {
            if (!hasLoadData) {
                Toast.makeText(this, R.string.mq_data_is_loading, Toast.LENGTH_SHORT).show();
                return;
            }

            if (voiceOrSendBtnState == SEND_STATE) {
                createAndSendTextMessage();
                changeInputStateToTextOrVoice();
            } else if (voiceOrSendBtnState == VOICE_STATE) {
                changeInputStateToRecord();
                MQUtils.closeKeyboard(MQConversationActivity.this);
            } else if (voiceOrSendBtnState == KEYBOARD_STATE) {
                changeInputStateToTextOrVoice();
                mEditToolbar.changeToOriginalKeyboard();
            }

        }
        // 选择图片按钮
        else if (id == R.id.photo_select_btn) {

            if (!hasLoadData) {
                Toast.makeText(this, R.string.mq_data_is_loading, Toast.LENGTH_SHORT).show();
                return;
            }

            MQUtils.closeKeyboard(MQConversationActivity.this);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final String[] choice = {getString(R.string.mq_dialog_select_camera), getString(R.string.mq_dialog_select_photo)};
            builder.setItems(choice, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        // 相机
                        case 0:
                            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File file = new File(MQUtils.getPicStorePath(MQConversationActivity.this));
                            file.mkdirs();
                            String path = MQUtils.getPicStorePath(MQConversationActivity.this) + "/" + System.currentTimeMillis() + ".jpg";
                            File imageFile = new File(path);
                            camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                            cameraPicPath = path;
                            try {
                                startActivityForResult(camera, REQUEST_CODE_CAMERA);
                            } catch (Exception e) {
                                Toast.makeText(MQConversationActivity.this, R.string.mq_photo_not_support, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        // 图库
                        case 1:
                            Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            try {
                                startActivityForResult(picture, REQUEST_CODE_PHOTO);
                            } catch (Exception e) {
                                Toast.makeText(MQConversationActivity.this, R.string.mq_photo_not_support, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setTitle(getString(R.string.mq_dialog_select_title));
            alertDialog.show();
        }
    }

    /**
     * 创建并发送TextMessage。如果没有客服在线，发送离线消息
     */
    private void createAndSendTextMessage() {
        //内容为空不发送
        if (TextUtils.isEmpty(inputEt.getText())) {
            inputEt.setText("");
            return;
        }
        TextMessage message = new TextMessage(inputEt.getText().toString());
        boolean isPreSendSuc = checkAndPreSend(message);
        if (isPreSendSuc) {
            sendMessage(message);
        }
    }

    /**
     * 创建并发送ImageMessage
     *
     * @param imageFile 需要上传的imageFile
     */
    private void createAndSendImageMessage(File imageFile) {
        PhotoMessage imageMessage = new PhotoMessage();
        imageMessage.setLocalPath(imageFile.getAbsolutePath());
        boolean isPreSendSuc = checkAndPreSend(imageMessage);
        if (isPreSendSuc) {
            sendMessage(imageMessage);
        }
    }

    /**
     * 创建并发送语音消息
     *
     * @param voicePath 语音路径
     */
    private void createAndPreSendVoiceMessage(String voicePath) {
        VoiceMessage voiceMessage = new VoiceMessage();
        voiceMessage.setLocalPath(voicePath);
        boolean isPreSendSuc = checkAndPreSend(voiceMessage);
        if (isPreSendSuc) {
            sendMessage(voiceMessage);
        }
    }

    private String cameraPicPath;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 从 相机 获取的图片
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            String sdState = Environment.getExternalStorageState();
            if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            String path = cameraPicPath;
            File imageFile = new File(path);
            if (imageFile.exists()) {
                createAndSendImageMessage(imageFile);
            }
        }

        // 从 相册 获取的图片
        if (requestCode == REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK && null != data) {
            String picturePath;
            try {
                Uri selectedImage = data.getData();
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                picturePath = c.getString(columnIndex);
                c.close();
            } catch (Exception e) {
                picturePath = data.getData().getPath();
            }
            // 获取图片并显示
            File imageFile = new File(picturePath);
            if (imageFile.exists()) {
                createAndSendImageMessage(imageFile);
            }
        }

        if (requestCode == REQUEST_CODE_CAMERA || requestCode == REQUEST_CODE_PHOTO) {
            changeInputStateToTextOrVoice();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        // 如果当前系统中没有邮件客户端可供调用，程序会直接挂掉，系统抛出了ActivityNotFoundException
        if (intent.toString().contains("mailto")) {
            PackageManager pm = getPackageManager();
            // The first Method
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            if (activities == null || activities.size() == 0) {
                // Do anything you like, or just return
                return;
            }
        }
        super.startActivity(intent);
    }

    /**
     * 检查发送条件并且处理一些准备发送的状态
     *
     * @param message 待发送消息
     * @return true，可以发送；false，不能发送
     */
    private boolean checkAndPreSend(BaseMessage message) {
        // 数据还没有加载的时候
        if (chatMsgAdapter == null) {
            return false;
        }
        message.setStatus(BaseMessage.STATE_SENDING);
        // 添加到对话列表
        chatMessageList.add(message);
        inputEt.setText("");
        MQTimeUtils.refreshMQTimeItem(chatMessageList);
        chatMsgAdapter.notifyDataSetChanged();
        return true;
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    public void sendMessage(final BaseMessage message) {
        // 状态改为「正在发送」
        message.setStatus(BaseMessage.STATE_SENDING);

        // 开始发送
        controller.sendMessage(message, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                // 刷新界面
                chatMsgAdapter.notifyDataSetChanged();

                // 客服不在线的时候，会自动发送留言消息，这个时候要添加一个 tip 到列表
                if (ErrorCode.NO_AGENT_ONLINE == state) {
                    addLeaveMessageTip();
                }
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                chatMsgAdapter.notifyDataSetChanged();
            }
        });


        // 滑动到底部
        conversationListView.setSelection(conversationListView.getBottom());
    }

    // 监听EditText输入框数据到变化
    private TextWatcher inputTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 向服务器发送一个正在输入的函数
            if (!TextUtils.isEmpty(s)) {
                inputting(s.toString());
                changeInputStateToSend();
            } else {
                // 清空输入内容后，需要恢复状态
                if (voiceOrSendBtnState == SEND_STATE) {
                    changeInputStateToTextOrVoice();
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * 向服务器发送「顾客正在输入」的状态
     *
     * @param content 内容
     */
    private void inputting(String content) {
        MQManager.getInstance(this).sendClientInputtingWithContent(content);
    }

    /**
     * 过滤语音消息
     *
     * @param messageList 消息列表
     */
    private void cleanVoiceMessage(List<BaseMessage> messageList) {
        if (!mqConfig.getShowVoiceMessage() && messageList.size() > 0) {
            Iterator<BaseMessage> baseMessageIterator = messageList.iterator();
            while (baseMessageIterator.hasNext()) {
                BaseMessage baseMessage = baseMessageIterator.next();
                if (BaseMessage.TYPE_CONTENT_VOICE.equals(baseMessage.getContentType())) {
                    baseMessageIterator.remove();
                }
            }
        }
    }

    private class MessageReceiver extends com.meiqia.meiqiasdk.controller.MessageReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }

        @Override
        public void receiveNewMsg(BaseMessage message) {
            MQConversationActivity.this.receiveNewMsg(message);
        }

        @Override
        public void changeTitleToInputting() {
            MQConversationActivity.this.changeTitleToInputting();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MQConversationActivity.this.changeTitleToAgentName(currentAgent);
                }
            }, 2000);

        }

        @Override
        public void changeTitleToAgentName(String agentNickname) {
            MQConversationActivity.this.changeTitleToAgentName(agentNickname);
        }

        @Override
        public void addDirectAgentMessageTip(String agentNickname) {
            MQConversationActivity.this.addDirectAgentMessageTip(agentNickname);
        }

        @Override
        public void setCurrentAgent(Agent agent) {
            MQConversationActivity.this.setCurrentAgent(agent);
        }
    }

    /**
     * 处理收到的新消息
     *
     * @param baseMessage 新消息
     */
    private void receiveNewMsg(BaseMessage baseMessage) {
        if (chatMsgAdapter != null) {
            chatMessageList.add(baseMessage);
            MQTimeUtils.refreshMQTimeItem(chatMessageList);
            chatMsgAdapter.notifyDataSetChanged();

            int lastVisiblePosition = conversationListView.getLastVisiblePosition();
            // -2 因为是先添加
            if (lastVisiblePosition == (chatMsgAdapter.getCount() - 2)) {
                conversationListView.setSelection(chatMsgAdapter.getCount() - 1); // 往下挪一截
            }
            // 在界面中播放声音
            if (!isPause) {
                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                    soundPool.play(1, 1, 1, 0, 0, 1);
                }
            }
        }
    }

    /**
     * 监听网络
     */
    private class NetworkChangeReceiver extends BroadcastReceiver {

        private ConnectivityManager connectivityManager;
        // 第一次进入的时候，会立即收到广播，需要避免以下
        private boolean isFirstReceiveBroadcast = true;

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (!isFirstReceiveBroadcast) {
                    // 有网络
                    if (info != null && info.isAvailable()) {
                        changeTitleToAgentName(currentAgent);
                    }
                    // 没有网络
                    else {
                        changeTitleToNetErrorState();
                    }
                } else {
                    isFirstReceiveBroadcast = false;
                }
            }
        }

    }

    private boolean isSdcardAvailable() {
        boolean isSdcardAvailable = MQUtils.isSdcardAvailable();
        if (!isSdcardAvailable) MQUtils.show(MQConversationActivity.this, R.string.mq_no_sdcard);
        return isSdcardAvailable;
    }

    /**
     * 展示图片，可以缩放
     *
     * @param picUrl 图片地址
     */
    public void displayPhoto(String picUrl) {
        if (mMQViewPhotoDialog == null) {
            mMQViewPhotoDialog = new MQViewPhotoDialog(this);
        }
        mMQViewPhotoDialog.show(picUrl);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MQUtils.requestPermission(this, REQUEST_CODE_PERMISSIONS, new MQUtils.Delegate() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied() {
                MQUtils.show(MQConversationActivity.this, R.string.mq_permission_denied_tip);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                MQUtils.handlePermissionResult(permissions, grantResults, new MQUtils.Delegate() {
                    @Override
                    public void onPermissionGranted() {
                    }

                    @Override
                    public void onPermissionDenied() {
                        MQUtils.show(MQConversationActivity.this, R.string.mq_permission_denied_tip);
                    }
                });
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}