package com.meiqia.meiqiasdk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.core.callback.OnEvaluateCallback;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.controller.ControllerImpl;
import com.meiqia.meiqiasdk.controller.MQController;
import com.meiqia.meiqiasdk.dialog.MQEvaluateDialog;
import com.meiqia.meiqiasdk.dialog.MQViewPhotoDialog;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.AgentChangeMessage;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.EvaluateMessage;
import com.meiqia.meiqiasdk.model.LeaveTipMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.TextMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.util.ErrorCode;
import com.meiqia.meiqiasdk.util.MQAudioPlayerManager;
import com.meiqia.meiqiasdk.util.MQAudioRecorderManager;
import com.meiqia.meiqiasdk.util.MQChatAdapter;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQSimpleTextWatcher;
import com.meiqia.meiqiasdk.util.MQSoundPoolManager;
import com.meiqia.meiqiasdk.util.MQTimeUtils;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQEditToolbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MQConversationActivity extends Activity implements View.OnClickListener, MQEvaluateDialog.Callback, MQEditToolbar.Callback {
    private static final String TAG = MQConversationActivity.class.getSimpleName();

    public static final String CLIENT_ID = "clientId";
    public static final String CUSTOMIZED_ID = "customizedId";

    public static final int REQUEST_CODE_CAMERA = 0;
    public static final int REQUEST_CODE_PHOTO = 1;
    private static int MESSAGE_PAGE_COUNT = 30; //消息每页加载数量

    private static MQController controller;

    // 控件
    private RelativeLayout titleRl;
    private RelativeLayout backRl;
    private TextView backTv;
    private ImageView backIv;
    private TextView titleTv;
    private ListView conversationListView;
    private EditText inputEt;
    private ImageButton sendTextBtn;
    private View emojiSelectBtn;
    private View photoSelectBtn;
    private View cameraSelectBtn;
    private View mVoiceBtn;
    private View mEvaluateBtn;
    private ProgressBar loadProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emojiSelectIndicator;
    private ImageView emojiSelectImg;

    private List<BaseMessage> chatMessageList = new ArrayList<>();
    private MQChatAdapter chatMsgAdapter;
    private MessageReceiver messageReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
    // 改变title状态
    private Handler mHandler;
    private MQSoundPoolManager mSoundPoolManager;

    // 是否已经加载数据的标识
    private boolean hasLoadData = false;
    private boolean isPause;

    private Agent currentAgent; // 当前客服

    private MQEditToolbar mEditToolbar;
    private MQViewPhotoDialog mMQViewPhotoDialog;
    private MQEvaluateDialog mEvaluateDialog;
    private String mCameraPicPath;

    private String mConversationId;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕长亮
        setContentView(R.layout.mq_activity_conversation);

        findViews();
        init();
        setListeners();
        applyConfig();
        registerReceiver();

        mEditToolbar.init(this, inputEt, this);
    }

    /**
     * 如果配置了界面相关的 config，在这里应用
     */
    private void applyConfig() {
        if (MQConfig.DEFAULT != MQConfig.bgColorTitle) {
            Drawable tintDrawable = MQUtils.tintDrawable(this, titleRl.getBackground(), MQConfig.bgColorTitle);
            MQUtils.setBackground(titleRl, tintDrawable);
        }

        if (MQConfig.DEFAULT != MQConfig.backArrowIconResId) {
            backIv.setImageResource(MQConfig.backArrowIconResId);
        }

        if (MQConfig.MQTitleGravity.LEFT == MQConfig.titleGravity) {
            RelativeLayout.LayoutParams titleTvParams = (RelativeLayout.LayoutParams) titleTv.getLayoutParams();
            titleTvParams.addRule(RelativeLayout.RIGHT_OF, R.id.back_rl);
            titleTv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            backTv.setVisibility(View.GONE);
        }

        if (MQConfig.DEFAULT != MQConfig.textColorTitle) {
            titleTv.setTextColor(getResources().getColor(MQConfig.textColorTitle));
            backTv.setTextColor(getResources().getColor(MQConfig.textColorTitle));
            backIv.setColorFilter(getResources().getColor(MQConfig.textColorTitle));
        }
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
    protected void onStop() {
        super.onStop();
        if (chatMsgAdapter != null) {
            chatMsgAdapter.stopPlayVoice();
            MQAudioPlayerManager.release();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mSoundPoolManager.release();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            unregisterReceiver(networkChangeReceiver);
            // 退出的时候，如果没有客服，关闭服务
            if (currentAgent == null) {
                controller.closeService();
            }
        } catch (Exception e) {
            //有些时候会出现未注册就取消注册的情况，暂时不知道为什么
        }
        super.onDestroy();
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
        // 初始化 ImageLoader
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);

        // handler
        mHandler = new Handler();

        mSoundPoolManager = MQSoundPoolManager.getInstance(this);
        chatMsgAdapter = new MQChatAdapter(MQConversationActivity.this, chatMessageList, conversationListView);
        conversationListView.setAdapter(chatMsgAdapter);
    }

    private void findViews() {
        titleRl = (RelativeLayout) findViewById(R.id.title_rl);
        backRl = (RelativeLayout) findViewById(R.id.back_rl);
        backTv = (TextView) findViewById(R.id.back_tv);
        backIv = (ImageView) findViewById(R.id.back_iv);
        conversationListView = (ListView) findViewById(R.id.messages_lv);
        inputEt = (EditText) findViewById(R.id.input_et);
        emojiSelectBtn = findViewById(R.id.emoji_select_btn);
        mEditToolbar = (MQEditToolbar) findViewById(R.id.editToolbar);
        sendTextBtn = (ImageButton) findViewById(R.id.send_text_btn);
        photoSelectBtn = findViewById(R.id.photo_select_btn);
        cameraSelectBtn = findViewById(R.id.camera_select_btn);
        mVoiceBtn = findViewById(R.id.mic_select_btn);
        mEvaluateBtn = findViewById(R.id.evaluate_select_btn);
        loadProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        titleTv = (TextView) findViewById(R.id.title_tv);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        emojiSelectIndicator = findViewById(R.id.emoji_select_indicator);
        emojiSelectImg = (ImageView) findViewById(R.id.emoji_select_img);
    }

    private void setListeners() {
        backRl.setOnClickListener(this);
        sendTextBtn.setOnClickListener(this);
        photoSelectBtn.setOnClickListener(this);
        cameraSelectBtn.setOnClickListener(this);
        mVoiceBtn.setOnClickListener(this);
        mEvaluateBtn.setOnClickListener(this);
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
                if (MQConfig.isLoadMessagesFromNativeOpen) {
                    loadMoreDataFromDatabase();
                } else {
                    loadMoreDataFromService();
                }
            }
        });
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
        intentFilter.addAction(MQController.ACTION_INVITE_EVALUATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);

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
    protected void changeTitleToAgentName(String agentName) {
        titleTv.setText(agentName);
    }

    /**
     * 将 title 改为客服名字
     *
     * @param agent 客服实体
     */
    protected void changeTitleToAgentName(Agent agent) {
        if (agent != null) {
            titleTv.setText(agent.getNickname());
        } else {
            changeTitleToNoAgentState();
        }
    }

    /**
     * 将 title 改为 正在输入
     */
    protected void changeTitleToInputting() {
        titleTv.setText(getResources().getString(R.string.mq_title_inputting));
    }

    /**
     * 将 title 改为 正在分配客服
     */
    protected void changeTitleToAllocatingAgent() {
        titleTv.setText(getResources().getString(R.string.mq_allocate_agent));
    }

    /**
     * 将 title 改为没有客服的状态
     */
    protected void changeTitleToNoAgentState() {
        titleTv.setText(getResources().getString(R.string.mq_title_leave_msg));
        mEvaluateBtn.setVisibility(View.GONE);
    }

    /**
     * 将 title 改为没有网络状态
     */
    protected void changeTitleToNetErrorState() {
        titleTv.setText(getResources().getString(R.string.mq_title_net_not_work));
    }

    /**
     * 将 title 改为未知错误状态
     */
    protected void changeTitleToUnknownErrorState() {
        titleTv.setText(getResources().getString(R.string.mq_title_unknown_error));
    }

    /**
     * 添加 转接客服 的消息 Tip 到列表
     *
     * @param agentNickName 客服名字
     */
    protected void addDirectAgentMessageTip(String agentNickName) {
        titleTv.setText(agentNickName);
        AgentChangeMessage agentChangeMessage = new AgentChangeMessage();
        agentChangeMessage.setAgentNickname(agentNickName);
        chatMsgAdapter.addMQMessage(agentChangeMessage);
    }

    private boolean isAddLeaveTip;

    /**
     * 添加 留言 的 Tip
     */
    protected void addLeaveMessageTip() {
        mEvaluateBtn.setVisibility(View.GONE);
        if (!isAddLeaveTip) {
            changeTitleToNoAgentState();
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
    protected void removeLeaveMessageTip() {
        mEvaluateBtn.setVisibility(View.VISIBLE);
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
                chatMsgAdapter.loadMoreMessage(cleanDupMessages(chatMessageList, messageList));
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
                chatMsgAdapter.loadMoreMessage(cleanDupMessages(chatMessageList, messageList));
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
     * 过滤掉列表存在的消息
     * @param messageList 列表中的消息
     * @param newMessageList 加载的新消息
     * @return
     */
    private List<BaseMessage> cleanDupMessages(List<BaseMessage> messageList, List<BaseMessage> newMessageList) {
        Iterator<BaseMessage> iterator = newMessageList.iterator();
        while (iterator.hasNext()) {
            BaseMessage newMessage = iterator.next();
            if (messageList.contains(newMessage)) {
                iterator.remove();
            }
        }
        return newMessageList;
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
                public void onSuccess(Agent agent, String conversationId, List<BaseMessage> conversationMessageList) {
                    setCurrentAgent(agent);
                    changeTitleToAgentName(agent);
                    removeLeaveMessageTip();
                    mConversationId = conversationId;
                    messageReceiver.setConversationId(conversationId);

                    // 根据设置，过滤语音消息
                    cleanVoiceMessage(conversationMessageList);

                    //加载数据
                    chatMessageList.clear();
                    chatMessageList.addAll(conversationMessageList);
                    loadData();
                    onLoadDataComplete(MQConversationActivity.this, agent);
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
                        onLoadDataComplete(MQConversationActivity.this, null);
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
        MQUtils.scrollListViewToBottom(conversationListView);
        chatMsgAdapter.downloadAndNotifyDataSetChanged(chatMessageList);
        chatMsgAdapter.notifyDataSetChanged();
        hasLoadData = true;
    }

    /**
     * 数据加载完成后的回调
     *
     * @param mqConversationActivity 当前 Activity
     * @param agent                  当前客服，可能为 null
     */
    protected void onLoadDataComplete(MQConversationActivity mqConversationActivity, Agent agent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_rl) {
            // 返回按钮

            onBackPressed();
        } else if (id == R.id.emoji_select_btn) {
            // 表情按钮

            showEmojiSelectIndicator();
            showEmojiSelectIndicator();
            mEditToolbar.toggleEmotionOriginKeyboard();
        } else if (id == R.id.send_text_btn) {
            // 发送按钮

            if (!hasLoadData) {
                MQUtils.show(this, R.string.mq_data_is_loading);
                return;
            }

            createAndSendTextMessage();

        } else if (id == R.id.photo_select_btn) {
            // 选择图片
            hideEmojiSelectIndicator();
            choosePhotoFromGallery();
        } else if (id == R.id.camera_select_btn) {
            // 打开相机
            hideEmojiSelectIndicator();
            choosePhotoFromCamera();
        } else if (id == R.id.mic_select_btn) {
            hideEmojiSelectIndicator();
            mEditToolbar.toggleVoiceOriginKeyboard();
        } else if (id == R.id.evaluate_select_btn) {
            hideEmojiSelectIndicator();
            showEvaluateDialog();
        }
    }

    private void showEvaluateDialog() {
        // 如果没有正在录音才弹出评价对话框
        if (!mEditToolbar.isRecording()) {
            mEditToolbar.closeAllKeyboard();
            if (!TextUtils.isEmpty(mConversationId)) {
                if (mEvaluateDialog == null) {
                    mEvaluateDialog = new MQEvaluateDialog(this);
                    mEvaluateDialog.setCallback(this);
                }
                mEvaluateDialog.show();
            }
        }
    }

    private void showEmojiSelectIndicator() {
        emojiSelectIndicator.setVisibility(View.VISIBLE);
        emojiSelectImg.setImageResource(R.drawable.mq_ic_emoji_active);
    }

    private void hideEmojiSelectIndicator() {
        mEditToolbar.closeEmotionKeyboard();
        emojiSelectIndicator.setVisibility(View.GONE);
        emojiSelectImg.setImageResource(R.drawable.mq_ic_emoji_normal);
    }


    /**
     * 从本地选择图片
     */
    private void choosePhotoFromGallery() {
        if (!hasLoadData) {
            MQUtils.show(this, R.string.mq_data_is_loading);
            return;
        }

        MQUtils.closeKeyboard(MQConversationActivity.this);

        try {
            startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), MQConversationActivity.REQUEST_CODE_PHOTO);
        } catch (Exception e) {
            MQUtils.show(this, R.string.mq_photo_not_support);
        }
    }


    /**
     * 打开相机
     */
    private void choosePhotoFromCamera() {
        if (!hasLoadData) {
            MQUtils.show(this, R.string.mq_data_is_loading);
            return;
        }

        MQUtils.closeKeyboard(MQConversationActivity.this);

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(MQUtils.getPicStorePath(this));
        file.mkdirs();
        String path = MQUtils.getPicStorePath(this) + "/" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(path);
        camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        mCameraPicPath = path;
        try {
            startActivityForResult(camera, MQConversationActivity.REQUEST_CODE_CAMERA);
        } catch (Exception e) {
            MQUtils.show(this, R.string.mq_photo_not_support);
        }
    }

    /**
     * 创建并发送TextMessage。如果没有客服在线，发送离线消息
     */
    private void createAndSendTextMessage() {
        String msg = inputEt.getText().toString();
        //内容为空不发送，只有空格时也不发送
        if (TextUtils.isEmpty(msg.trim())) {
            return;
        }
        sendMessage(new TextMessage(msg));
    }

    /**
     * 创建并发送ImageMessage
     *
     * @param imageFile 需要上传的imageFile
     */
    private void createAndSendImageMessage(File imageFile) {
        PhotoMessage imageMessage = new PhotoMessage();
        imageMessage.setLocalPath(imageFile.getAbsolutePath());
        sendMessage(imageMessage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                // 从 相机 获取的图片

                File cameraPicFile = getCameraPicFile();
                if (cameraPicFile != null) {
                    createAndSendImageMessage(cameraPicFile);
                }
            } else if (requestCode == REQUEST_CODE_PHOTO && null != data) {
                // 从 相册 获取的图片

                File imageFile = new File(MQUtils.getRealPathByUri(this, data.getData()));
                if (imageFile.exists()) {
                    createAndSendImageMessage(imageFile);
                }
            }
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

    public File getCameraPicFile() {
        String sdState = Environment.getExternalStorageState();
        if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        File imageFile = new File(mCameraPicPath);
        if (imageFile.exists()) {
            return imageFile;
        } else {
            return null;
        }
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
        // 状态改为「正在发送」，以便在数据列表中展示正在发送消息的状态
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
        boolean isPreSendSuc = checkAndPreSend(message);
        if (!isPreSendSuc) {
            return;
        }

        // 开始发送
        controller.sendMessage(message, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                renameVoiceFilename(message);

                // 刷新界面
                chatMsgAdapter.notifyDataSetChanged();

                // 客服不在线的时候，会自动发送留言消息，这个时候要添加一个 tip 到列表
                if (ErrorCode.NO_AGENT_ONLINE == state) {
                    addLeaveMessageTip();
                }
                // 发送成功播放声音
                if (MQConfig.isSoundSwitchOpen) {
                    mSoundPoolManager.playSound(R.raw.mq_send_message);
                }
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                chatMsgAdapter.notifyDataSetChanged();
            }
        });
        MQUtils.scrollListViewToBottom(conversationListView);
    }

    /**
     * 重发消息
     *
     * @param message 待重发的消息
     */
    public void resendMessage(final BaseMessage message) {
        // 开始重发
        controller.resendMessage(message, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                renameVoiceFilename(message);

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
    }

    /**
     * 重命名本地语音文件
     *
     * @param message
     */
    private void renameVoiceFilename(BaseMessage message) {
        if (message instanceof VoiceMessage) {
            VoiceMessage voiceMessage = (VoiceMessage) message;
            MQAudioRecorderManager.renameVoiceFilename(MQConversationActivity.this, voiceMessage.getLocalPath(), voiceMessage.getContent());
            chatMsgAdapter.downloadAndNotifyDataSetChanged(Arrays.asList(message));
        }
    }

    // 监听EditText输入框数据到变化
    private TextWatcher inputTextWatcher = new MQSimpleTextWatcher() {
        @SuppressLint("NewApi")
		@Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 向服务器发送一个正在输入的函数
            if (!TextUtils.isEmpty(s)) {
                inputting(s.toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sendTextBtn.setElevation(MQUtils.dip2px(MQConversationActivity.this, 3));
                }
                sendTextBtn.setImageResource(R.drawable.mq_ic_send_icon_white);
                sendTextBtn.setBackgroundResource(R.drawable.mq_shape_send_back_pressed);
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sendTextBtn.setElevation(0);
                }
                sendTextBtn.setImageResource(R.drawable.mq_ic_send_icon_grey);
                sendTextBtn.setBackgroundResource(R.drawable.mq_shape_send_back_normal);
            }
        }
    };

    /**
     * 向服务器发送「顾客正在输入」的状态
     *
     * @param content 内容
     */
    private void inputting(String content) {
        controller.sendClientInputtingWithContent(content);
    }

    /**
     * 过滤语音消息
     *
     * @param messageList 消息列表
     */
    private void cleanVoiceMessage(List<BaseMessage> messageList) {
        if (!MQConfig.isVoiceSwitchOpen && messageList.size() > 0) {
            Iterator<BaseMessage> baseMessageIterator = messageList.iterator();
            while (baseMessageIterator.hasNext()) {
                BaseMessage baseMessage = baseMessageIterator.next();
                if (BaseMessage.TYPE_CONTENT_VOICE.equals(baseMessage.getContentType())) {
                    baseMessageIterator.remove();
                }
            }
        }
    }

    /**
     * 添加 评价 的消息 Tip 到列表
     *
     * @param level 评价的等级
     */
    protected void addEvaluateMessageTip(int level) {
        EvaluateMessage evaluateMessage = new EvaluateMessage(level);
        chatMsgAdapter.addMQMessage(evaluateMessage);
    }

    @Override
    public void executeEvaluate(final int level, String content) {
        controller.executeEvaluate(mConversationId, level, content, new OnEvaluateCallback() {
            @Override
            public void onFailure(int code, String message) {
                MQUtils.show(MQConversationActivity.this, R.string.mq_evaluate_failure);
            }

            @Override
            public void onSuccess() {
                addEvaluateMessageTip(level);
            }
        });
    }

    @Override
    public void onAudioRecorderFinish(int time, String filePath) {
        VoiceMessage voiceMessage = new VoiceMessage();
        voiceMessage.setDuration(time);
        voiceMessage.setLocalPath(filePath);
        sendMessage(voiceMessage);
    }

    @Override
    public void onAudioRecorderTooShort() {
        MQUtils.show(this, R.string.mq_record_record_time_is_short);
    }

    @Override
    public void scrollContentToBottom() {
        MQUtils.scrollListViewToBottom(conversationListView);
    }

    @Override
    public void onAudioRecorderNoPermission() {
        MQUtils.show(this, R.string.mq_recorder_no_permission);
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

        @Override
        public void inviteEvaluation() {
            showEvaluateDialog();
        }

        @Override
        public void setNewConversationId(String newConversationId) {
            mConversationId = newConversationId;
            removeLeaveMessageTip();
        }
    }

    /**
     * 处理收到的新消息
     *
     * @param baseMessage 新消息
     */
    private void receiveNewMsg(BaseMessage baseMessage) {
        if (chatMsgAdapter != null && !isDupMessage(baseMessage)) {
            // 如果是配置了不显示语音，收到语音消息直接过滤
            if (!MQConfig.isVoiceSwitchOpen && BaseMessage.TYPE_CONTENT_VOICE.equals(baseMessage.getContentType())) {
                return;
            }

            chatMessageList.add(baseMessage);
            MQTimeUtils.refreshMQTimeItem(chatMessageList);

            if (baseMessage instanceof VoiceMessage) {
                chatMsgAdapter.downloadAndNotifyDataSetChanged(Arrays.asList(baseMessage));
            } else {
                chatMsgAdapter.notifyDataSetChanged();
            }

            int lastVisiblePosition = conversationListView.getLastVisiblePosition();
            // -2 因为是先添加
            if (lastVisiblePosition == (chatMsgAdapter.getCount() - 2)) {
                MQUtils.scrollListViewToBottom(conversationListView);
            }
            // 在界面中播放声音
            if (!isPause && MQConfig.isSoundSwitchOpen) {
                mSoundPoolManager.playSound(R.raw.mq_new_message);
            }
        }

    }

    /**
     * 消息是否已经在列表中
     *
     * @param baseMessage
     * @return true，已经存在与列表；false，不存在
     */
    private boolean isDupMessage(BaseMessage baseMessage) {
        for (BaseMessage message : chatMessageList) {
            if (message.equals(baseMessage)) {
                return true;
            }
        }
        return false;
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
}
