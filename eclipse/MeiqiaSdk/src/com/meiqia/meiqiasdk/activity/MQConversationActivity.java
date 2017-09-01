package com.meiqia.meiqiasdk.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.MQMessageManager;
import com.meiqia.core.callback.OnClientPositionInQueueCallback;
import com.meiqia.core.callback.SuccessCallback;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.LeaveMessageCallback;
import com.meiqia.meiqiasdk.callback.OnClientOnlineCallback;
import com.meiqia.meiqiasdk.callback.OnEvaluateRobotAnswerCallback;
import com.meiqia.meiqiasdk.callback.OnFinishCallback;
import com.meiqia.meiqiasdk.callback.OnGetMessageListCallBack;
import com.meiqia.meiqiasdk.callback.OnMessageSendCallback;
import com.meiqia.meiqiasdk.callback.SimpleCallback;
import com.meiqia.meiqiasdk.chatitem.MQInitiativeRedirectItem;
import com.meiqia.meiqiasdk.chatitem.MQRobotItem;
import com.meiqia.meiqiasdk.controller.ControllerImpl;
import com.meiqia.meiqiasdk.controller.MQController;
import com.meiqia.meiqiasdk.dialog.MQEvaluateDialog;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.AgentChangeMessage;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.EvaluateMessage;
import com.meiqia.meiqiasdk.model.FileMessage;
import com.meiqia.meiqiasdk.model.InitiativeRedirectMessage;
import com.meiqia.meiqiasdk.model.LeaveTipMessage;
import com.meiqia.meiqiasdk.model.NoAgentLeaveMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.RedirectQueueMessage;
import com.meiqia.meiqiasdk.model.RobotMessage;
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
import com.meiqia.meiqiasdk.widget.MQCustomKeyboardLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MQConversationActivity extends Activity implements View.OnClickListener, MQEvaluateDialog.Callback, MQCustomKeyboardLayout.Callback, View.OnTouchListener, MQRobotItem.Callback, LeaveMessageCallback, MQInitiativeRedirectItem.Callback {
    private static final String TAG = MQConversationActivity.class.getSimpleName();

    // 权限
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int RECORD_AUDIO_REQUEST_CODE = 2;
    private static final int WRITE_EXTERNAL_STORAGE_AND_CAMERA_REQUEST_CODE = 3;

    private static final int WHAT_GET_CLIENT_POSITION_IN_QUEUE = 1;

    public static final String CLIENT_ID = "clientId";
    public static final String CUSTOMIZED_ID = "customizedId";
    public static final String CLIENT_INFO = "clientInfo";
    public static final String PRE_SEND_TEXT = "preSendText";
    public static final String PRE_SEND_IMAGE_PATH = "preSendImagePath";

    public static final int REQUEST_CODE_CAMERA = 0;
    public static final int REQUEST_CODE_PHOTO = 1;
    private static int MESSAGE_PAGE_COUNT = 30; //消息每页加载数量
    private static final long AUTO_DISMISS_TOP_TIP_TIME = 2000; // TopTip 自动隐藏时间

    private MQController mController;

    // 控件
    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private TextView mRedirectHumanTv;
    private RelativeLayout mChatBodyRl;
    private ListView mConversationListView;
    private EditText mInputEt;
    private ImageButton mSendTextBtn;
    private View mEmojiSelectBtn;
    private View mPhotoSelectBtn;
    private View mCameraSelectBtn;
    private View mVoiceBtn;
    private View mEvaluateBtn;
    private ProgressBar mLoadProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mEmojiSelectIndicator;
    private ImageView mEmojiSelectImg;
    private View mVoiceSelectIndicator;
    private ImageView mVoiceSelectImg;

    private List<BaseMessage> mChatMessageList = new ArrayList<>();
    private MQChatAdapter mChatMsgAdapter;
    private MessageReceiver mMessageReceiver;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    // 改变title状态
    private Handler mHandler;
    private MQSoundPoolManager mSoundPoolManager;

    // 是否已经加载数据的标识
    private boolean mHasLoadData = false;
    private boolean isPause;
    private boolean isDestroy;

    // 是否被拉黑
    private boolean isBlackState;

    private Agent mCurrentAgent; // 当前客服

    private MQCustomKeyboardLayout mCustomKeyboardLayout;
    private MQEvaluateDialog mEvaluateDialog;
    private String mCameraPicPath;

    private String mConversationId;

    private RedirectQueueMessage mRedirectQueueMessage;

    private TextView mTopTipViewTv;
    private Runnable mAutoDismissTopTipRunnable;

    // 上一次发送机器人消息的时间戳
    private long mLastSendRobotMessageTime;
    private boolean mIsAllocatingAgent;
    private boolean mIsShowRedirectHumanButton;
    private boolean isAddLeaveTip;

    private boolean isNeedDelayOnline;
    private boolean hasSetClientOnline = false; // 设置 online 之后，发消息的时候才检测 socket 状态
    private List<BaseMessage> delaySendList = new ArrayList<>();
    private BaseMessage entWelcomeMsg; // 分配成功后要删除

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = MQConfig.getController(this);
        mController.onConversationOpen();
        if (savedInstanceState != null) {
            mCameraPicPath = savedInstanceState.getString("mCameraPicPath");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕长亮
        setContentView(R.layout.mq_activity_conversation);

        findViews();
        init();
        setListeners();
        applyCustomUIConfig();
        registerReceiver();
        refreshEnterpriseConfig();


        // 恢复之前未发送的文本消息
        String clientId = mController.getCurrentClientId();
        if (!TextUtils.isEmpty(clientId)) {
            String text = MQUtils.getUnSendTextMessage(this, clientId);
            mInputEt.setText(text);
            mInputEt.setSelection(mInputEt.getText().length());
        }

        MQConfig.getActivityLifecycleCallback().onActivityCreated(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mCameraPicPath", mCameraPicPath);
        MQConfig.getActivityLifecycleCallback().onActivitySaveInstanceState(this, outState);
    }

    /**
     * 如果配置了界面相关的 config，在这里应用
     */
    private void applyCustomUIConfig() {
        if (MQConfig.DEFAULT != MQConfig.ui.backArrowIconResId) {
            mBackIv.setImageResource(MQConfig.ui.backArrowIconResId);
        }

        // 处理标题栏背景色
        MQUtils.applyCustomUITintDrawable(mTitleRl, android.R.color.white, R.color.mq_activity_title_bg, MQConfig.ui.titleBackgroundResId);

        // 处理标题、返回、返回箭头颜色
        MQUtils.applyCustomUITextAndImageColor(R.color.mq_activity_title_textColor, MQConfig.ui.titleTextColorResId, null, mBackTv, mTitleTv, mRedirectHumanTv);

        // 处理标题文本的对其方式
        MQUtils.applyCustomUITitleGravity(mBackTv, mTitleTv);

        // 处理底部功能按钮图片
        MQUtils.tintPressedIndicator((ImageView) findViewById(R.id.photo_select_iv), R.drawable.mq_ic_image_normal, R.drawable.mq_ic_image_active);
        MQUtils.tintPressedIndicator((ImageView) findViewById(R.id.camera_select_iv), R.drawable.mq_ic_camera_normal, R.drawable.mq_ic_camera_active);
        MQUtils.tintPressedIndicator((ImageView) findViewById(R.id.evaluate_select_iv), R.drawable.mq_ic_evaluate_normal, R.drawable.mq_ic_evaluate_active);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 在已经加载数据的情况下,重新进入界面,需要再次打开服务
        if (mHasLoadData) {
            mController.openService();

            sendGetClientPositionInQueueMsg();
        }
        MQConfig.getActivityLifecycleCallback().onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 根据开关，是否发送消息才分配客服 已分配过客服就不再受开关影响
        if (isNeedDelayOnline && mController.getCurrentAgent() == null) {
            if (!mHasLoadData) {
                mTitleTv.setText(mController.getEnterpriseConfig().public_nickname);
                mLoadProgressBar.setVisibility(View.VISIBLE);
                getMessageFromServiceAndLoad();
            }
        } else {
            // 设置顾客上线，请求分配客服
            setClientOnline(false);
        }
        isPause = false;
        MQConfig.getActivityLifecycleCallback().onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
        MQConfig.getActivityLifecycleCallback().onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);

        if (mChatMsgAdapter != null) {
            mChatMsgAdapter.stopPlayVoice();
            MQAudioPlayerManager.release();
        }
        if (mChatMessageList != null && mChatMessageList.size() > 0) {
            mController.saveConversationOnStopTime(mChatMessageList.get(mChatMessageList.size() - 1).getCreatedOn());
        } else {
            mController.saveConversationOnStopTime(System.currentTimeMillis());
        }
        MQConfig.getActivityLifecycleCallback().onActivityStopped(this);
    }

    @Override
    protected void onDestroy() {
        MQUtils.closeKeyboard(this);
        try {
            mSoundPoolManager.release();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            unregisterReceiver(mNetworkChangeReceiver);
        } catch (Exception e) {
            //有些时候会出现未注册就取消注册的情况，暂时不知道为什么
        }
        isDestroy = true;
        cancelAllDownload();
        mController.onConversationClose();

        // 保存未发送的文本消息
        String clientId = mController.getCurrentClientId();
        if (!TextUtils.isEmpty(clientId)) {
            String msg = mInputEt.getText().toString().trim();
            MQUtils.setUnSendTextMessage(this, clientId, msg);
        }

        MQConfig.getActivityLifecycleCallback().onActivityDestroyed(this);
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //如果在表情选择的时候按下 Back 键，隐藏表情 panel
        if (keyCode == KeyEvent.KEYCODE_BACK && mCustomKeyboardLayout.isEmotionKeyboardVisible()) {
            mCustomKeyboardLayout.closeEmotionKeyboard();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void init() {
        if (mController == null) {
            mController = new ControllerImpl(this);
        }
        MQTimeUtils.init(this);

        // handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WHAT_GET_CLIENT_POSITION_IN_QUEUE) {
                    getClientPositionInQueue();
                }
            }
        };

        mSoundPoolManager = MQSoundPoolManager.getInstance(this);
        mChatMsgAdapter = new MQChatAdapter(MQConversationActivity.this, mChatMessageList, mConversationListView);
        mConversationListView.setAdapter(mChatMsgAdapter);

        mVoiceBtn.setVisibility(MQConfig.isVoiceSwitchOpen ? View.VISIBLE : View.GONE);
        mEvaluateBtn.setVisibility(MQConfig.isEvaluateSwitchOpen ? View.VISIBLE : View.GONE);

        mCustomKeyboardLayout.init(this, mInputEt, this);
        isDestroy = false;
        isNeedDelayOnline = mController.getEnterpriseConfig().scheduler_after_client_send_msg;
    }

    private void findViews() {
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mBackRl = (RelativeLayout) findViewById(R.id.back_rl);
        mBackTv = (TextView) findViewById(R.id.back_tv);
        mBackIv = (ImageView) findViewById(R.id.back_iv);
        mRedirectHumanTv = (TextView) findViewById(R.id.redirect_human_tv);
        mChatBodyRl = (RelativeLayout) findViewById(R.id.chat_body_rl);
        mConversationListView = (ListView) findViewById(R.id.messages_lv);
        mInputEt = (EditText) findViewById(R.id.input_et);
        mEmojiSelectBtn = findViewById(R.id.emoji_select_btn);
        mCustomKeyboardLayout = (MQCustomKeyboardLayout) findViewById(R.id.customKeyboardLayout);
        mSendTextBtn = (ImageButton) findViewById(R.id.send_text_btn);
        mPhotoSelectBtn = findViewById(R.id.photo_select_btn);
        mCameraSelectBtn = findViewById(R.id.camera_select_btn);
        mVoiceBtn = findViewById(R.id.mic_select_btn);
        mEvaluateBtn = findViewById(R.id.evaluate_select_btn);
        mLoadProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mEmojiSelectIndicator = findViewById(R.id.emoji_select_indicator);
        mEmojiSelectImg = (ImageView) findViewById(R.id.emoji_select_img);
        mVoiceSelectIndicator = findViewById(R.id.conversation_voice_indicator);
        mVoiceSelectImg = (ImageView) findViewById(R.id.conversation_voice_img);
    }

    private void setListeners() {
        mBackRl.setOnClickListener(this);
        mRedirectHumanTv.setOnClickListener(this);
        mSendTextBtn.setOnClickListener(this);
        mPhotoSelectBtn.setOnClickListener(this);
        mCameraSelectBtn.setOnClickListener(this);
        mVoiceBtn.setOnClickListener(this);
        mEvaluateBtn.setOnClickListener(this);
        // 绑定 EditText 的监听器
        mInputEt.addTextChangedListener(inputTextWatcher);
        mInputEt.setOnTouchListener(this);
        mInputEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSendTextBtn.performClick();
                    MQUtils.closeKeyboard(MQConversationActivity.this);
                    return true;
                }
                return false;
            }
        });
        // 表情
        mEmojiSelectBtn.setOnClickListener(this);
        // 对话列表，单击「隐藏键盘」、「表情 panel」
        mConversationListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (MotionEvent.ACTION_DOWN == arg1.getAction()) {
                    mCustomKeyboardLayout.closeAllKeyboard();
                    hideEmojiSelectIndicator();
                    hideVoiceSelectIndicator();
                }
                return false;
            }
        });
        // 添加长按复制功能
        mConversationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String content = mChatMessageList.get(arg2).getContent();
                if (!TextUtils.isEmpty(content)) {
                    MQUtils.clip(MQConversationActivity.this, content);
                    MQUtils.show(MQConversationActivity.this, R.string.mq_copy_success);
                    return true;
                }
                return false;
            }
        });
        // 下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
     * 注册广播
     */
    private void registerReceiver() {
        // 注册消息接收
        mMessageReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MQController.ACTION_AGENT_INPUTTING);
        intentFilter.addAction(MQController.ACTION_NEW_MESSAGE_RECEIVED);
        intentFilter.addAction(MQController.ACTION_CLIENT_IS_REDIRECTED_EVENT);
        intentFilter.addAction(MQController.ACTION_INVITE_EVALUATION);
        intentFilter.addAction(MQController.ACTION_AGENT_STATUS_UPDATE_EVENT);
        intentFilter.addAction(MQController.ACTION_BLACK_ADD);
        intentFilter.addAction(MQController.ACTION_BLACK_DEL);
        intentFilter.addAction(MQController.ACTION_QUEUEING_REMOVE);
        intentFilter.addAction(MQController.ACTION_QUEUEING_INIT_CONV);
        intentFilter.addAction(MQMessageManager.ACTION_END_CONV_AGENT);
        intentFilter.addAction(MQMessageManager.ACTION_END_CONV_TIMEOUT);
        intentFilter.addAction(MQMessageManager.ACTION_SOCKET_OPEN);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        // 网络监听
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkChangeReceiver, mFilter);
    }

    /**
     * 将 title 改为 正在输入
     */
    protected void changeTitleToInputting() {
        mTitleTv.setText(getResources().getString(R.string.mq_title_inputting));

        updateAgentOnlineOfflineStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为 正在分配客服
     */
    protected void changeTitleToAllocatingAgent() {
        mTitleTv.setText(getResources().getString(R.string.mq_allocate_agent));

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为 排队等待中
     */
    protected void changeTitleToQueue() {
        mTitleTv.setText(getResources().getString(R.string.mq_allocate_queue_title));

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为没有客服的状态
     */
    protected void changeTitleToNoAgentState() {
        mTitleTv.setText(getResources().getString(R.string.mq_title_leave_msg));

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为没有网络状态
     */
    protected void changeTitleToNetErrorState() {
        mTitleTv.setText(getResources().getString(R.string.mq_title_net_not_work));

        mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为未知错误状态
     */
    protected void changeTitleToUnknownErrorState() {
        mTitleTv.setText(getResources().getString(R.string.mq_title_unknown_error));

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 添加 转接客服 的消息 Tip 到列表
     *
     * @param agentNickName 客服名字
     */
    protected void addDirectAgentMessageTip(String agentNickName) {
        AgentChangeMessage agentChangeMessage = new AgentChangeMessage();
        agentChangeMessage.setAgentNickname(agentNickName);
        mChatMessageList.add(mChatMessageList.size(), agentChangeMessage);
        mChatMsgAdapter.notifyDataSetChanged();
    }

    /**
     * 添加 被拉黑 的消息 Tip 到列表
     */
    protected void addBlacklistTip(int blackTipRes) {
        isBlackState = true;
        changeTitleToNoAgentState();
        BaseMessage blacklistMessage = new BaseMessage();
        blacklistMessage.setItemViewType(BaseMessage.TYPE_TIP);
        blacklistMessage.setContent(getResources().getString(blackTipRes));
        mChatMsgAdapter.addMQMessage(blacklistMessage);
    }

    /**
     * 添加 留言 的 Tip
     */
    protected void addLeaveMessageTip() {
        changeTitleToNoAgentState();
        if (!isAddLeaveTip) {
            LeaveTipMessage leaveTip = new LeaveTipMessage();
            String leaveContent = getResources().getString(R.string.mq_leave_msg_tips);
            if (!TextUtils.isEmpty(mController.getEnterpriseConfig().ticketConfig.getIntro())) {
                leaveContent = mController.getEnterpriseConfig().ticketConfig.getIntro();
            }
            leaveTip.setContent(leaveContent);
            //添加到当前消息的上一个位置
            int position = mChatMessageList.size();
            if (position != 0) {
                position = position - 1;
            }
            mChatMsgAdapter.addMQMessage(leaveTip, position);
            isAddLeaveTip = true;
        }
    }

    /**
     * 从列表移除 留言 的 Tip
     */
    protected void removeLeaveMessageTip() {
        Iterator<BaseMessage> chatItemViewBaseIterator = mChatMessageList.iterator();
        while (chatItemViewBaseIterator.hasNext()) {
            BaseMessage baseMessage = chatItemViewBaseIterator.next();
            if (baseMessage instanceof LeaveTipMessage) {
                chatItemViewBaseIterator.remove();
                mChatMsgAdapter.notifyDataSetChanged();
                return;
            }
        }
        isAddLeaveTip = false;
    }

    /**
     * 弹出顶部 Tip
     *
     * @param contentRes tip 文本内容的资源 id
     */
    public void popTopTip(final int contentRes) {
        if (mTopTipViewTv == null) {
            mTopTipViewTv = (TextView) getLayoutInflater().inflate(R.layout.mq_top_pop_tip, null);
            mTopTipViewTv.setText(contentRes);
            int height = getResources().getDimensionPixelOffset(R.dimen.mq_top_tip_height);
            mChatBodyRl.addView(mTopTipViewTv, ViewGroup.LayoutParams.MATCH_PARENT, height);
            ViewCompat.setTranslationY(mTopTipViewTv, -height); // 初始化位置
            ViewCompat.animate(mTopTipViewTv).translationY(0).setDuration(300).start();
            if (mAutoDismissTopTipRunnable == null) {
                mAutoDismissTopTipRunnable = new Runnable() {
                    @Override
                    public void run() {
                        popTopTip(contentRes);
                    }
                };
            }
            mHandler.postDelayed(mAutoDismissTopTipRunnable, AUTO_DISMISS_TOP_TIP_TIME);
        } else {
            mHandler.removeCallbacks(mAutoDismissTopTipRunnable);
            ViewCompat.animate(mTopTipViewTv).translationY(-mTopTipViewTv.getHeight()).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    mChatBodyRl.removeView(mTopTipViewTv);
                    mTopTipViewTv = null;
                }
            }).setDuration(300).start();
        }
    }

    /**
     * 设置当前agent
     *
     * @param newAgent
     */
    private void setCurrentAgent(Agent newAgent) {
        // 处理机器人客服转人工排队时发消息的情况
        if (mRedirectQueueMessage != null && mCurrentAgent != null) {
            return;
        }

        Agent oldAgent = mCurrentAgent;
        mCurrentAgent = newAgent;


        if (mController.getIsWaitingInQueue()) {
            return;
        }

        if (mCurrentAgent == null) {
            changeTitleToNoAgentState();
        } else {
            mTitleTv.setText(newAgent.getNickname());
            updateAgentOnlineOfflineStatusAndRedirectHuman();

            if (oldAgent != mCurrentAgent) {
                // 新老agent不相等时才去移除「留言提示消息」
                removeLeaveMessageTip();
                // 新老agent不相等，并且新的agent不是人工时才移除「没有客服的提示留言消息」和「转接人工的排队消息」
                if (!mCurrentAgent.isRobot()) {
                    removeNoAgentLeaveMsg();
                    removeInitiativeRedirectMessage();
                    removeRedirectQueueLeaveMsg();
                }
            }
        }
    }

    /**
     * 从服务器获取更多消息并加载
     */
    private void loadMoreDataFromService() {
        // 最早消息的创建时间
        long lastMessageCreateOn = System.currentTimeMillis();
        if (mChatMessageList.size() > 0)
            lastMessageCreateOn = mChatMessageList.get(0).getCreatedOn();
        // 获取该时间之前的消息
        mController.getMessageFromService(lastMessageCreateOn, MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {
            @Override
            public void onSuccess(final List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanVoiceMessage(messageList);
                //添加时间戳
                MQTimeUtils.refreshMQTimeItem(messageList);
                mChatMsgAdapter.loadMoreMessage(cleanDupMessages(mChatMessageList, messageList));
                mConversationListView.setSelection(messageList.size());
                mSwipeRefreshLayout.setRefreshing(false);
                // 没有消息后，禁止下拉加载
                if (messageList.size() == 0) {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
                mChatMsgAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 从数据库更多消息并加载
     */
    private void loadMoreDataFromDatabase() {
        // 最早消息的创建时间
        long lastMessageCreateOn = System.currentTimeMillis();
        if (mChatMessageList.size() > 0)
            lastMessageCreateOn = mChatMessageList.get(0).getCreatedOn();
        // 获取该时间之前的消息
        mController.getMessagesFromDatabase(lastMessageCreateOn, MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {
            @Override
            public void onSuccess(final List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanVoiceMessage(messageList);
                //添加时间戳
                MQTimeUtils.refreshMQTimeItem(messageList);
                mChatMsgAdapter.loadMoreMessage(cleanDupMessages(mChatMessageList, messageList));
                mConversationListView.setSelection(messageList.size());
                mSwipeRefreshLayout.setRefreshing(false);
                // 没有消息后，禁止下拉加载
                if (messageList.size() == 0) {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
                mChatMsgAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 过滤掉列表存在的消息
     *
     * @param messageList    列表中的消息
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
     * 强制转人工
     */
    private void forceRedirectHuman() {
        if (mController.getCurrentAgent() != null && mController.getCurrentAgent().isRobot()) {
            mController.setForceRedirectHuman(true);
            setClientOnline(true);
        }
    }

    /**
     * 设置顾客上线
     *
     * @param isForceRedirectHuman 是否强制转人工
     */
    private void setClientOnline(final boolean isForceRedirectHuman) {
        if (isForceRedirectHuman || (!isForceRedirectHuman && mCurrentAgent == null)) {
            mIsAllocatingAgent = true;
            isNeedDelayOnline = false;

            // Title 显示正在分配客服
            changeTitleToAllocatingAgent();

            // 从 intent 获取 clientId、customizedId 和 clientInfo
            Intent intent = getIntent();
            String clientId = null;
            String customizedId = null;
            if (intent != null) {
                clientId = getIntent().getStringExtra(CLIENT_ID);
                customizedId = getIntent().getStringExtra(CUSTOMIZED_ID);
            }

            // 上线
            mController.setCurrentClientOnline(clientId, customizedId, new OnClientOnlineCallback() {

                @Override
                public void onSuccess(Agent agent, String conversationId, List<BaseMessage> conversationMessageList) {
                    mIsAllocatingAgent = false;

                    setCurrentAgent(agent);

                    mConversationId = conversationId;
                    mMessageReceiver.setConversationId(conversationId);

                    // 根据设置，过滤语音消息
                    cleanVoiceMessage(conversationMessageList);

                    mChatMessageList.clear();
                    mChatMessageList.addAll(conversationMessageList);

                    // 如果是强转人工，并且服务端返回的最后一条消息是欢迎消息，则显示「接下来有 xxx 为你服务」
                    if (isForceRedirectHuman && mChatMessageList.size() > 0 && TextUtils.equals(BaseMessage.TYPE_WELCOME, mChatMessageList.get(mChatMessageList.size() - 1).getType())) {
                        AgentChangeMessage agentChangeMessage = new AgentChangeMessage();
                        agentChangeMessage.setAgentNickname(agent.getNickname());
                        mChatMessageList.add(conversationMessageList.size() - 1, agentChangeMessage);
                    }
                    setClientInfo();

                    loadData();


                    if (mController.getIsWaitingInQueue()) {
                        getClientPositionInQueue();

                        removeNoAgentLeaveMsg();
                        changeTitleToQueue();
                    } else {
                        removeRedirectQueueLeaveMsg();
                    }
                    // 发送待发送的消息
                    sendDelayMessages();
                    // 必须放在发送待发送消息后
                    hasSetClientOnline = true;
                }

                @Override
                public void onFailure(int code, String message) {
                    mIsAllocatingAgent = false;

                    if (ErrorCode.NET_NOT_WORK == code) {
                        changeTitleToNetErrorState();
                    } else if (ErrorCode.NO_AGENT_ONLINE == code) {
                        if (isForceRedirectHuman) {
                            setCurrentAgent(mCurrentAgent);
                            addNoAgentLeaveMsg();
                        } else {
                            setCurrentAgent(null);
                            // 没有分配到客服，也根据设置是否上传顾客信息
                            setClientInfo();
                        }
                    } else if (ErrorCode.BLACKLIST == code) {
                        setCurrentAgent(null);
                        isBlackState = true;
                    } else {
                        changeTitleToUnknownErrorState();
                        Toast.makeText(MQConversationActivity.this, "code = " + code + "\n" + "message = " + message, Toast.LENGTH_SHORT).show();
                    }
                    // 如果没有加载数据，则加载数据
                    if (!mHasLoadData) {
                        getMessageDataFromDatabaseAndLoad();
                    }
                    if (ErrorCode.NO_AGENT_ONLINE == code) {
                        // 发送待发送的消息
                        sendDelayMessages();
                    }
                    // 必须放在发送待发送消息后
                    hasSetClientOnline = true;
                }
            });
        } else {
            setCurrentAgent(mCurrentAgent);
        }
    }

    /**
     * 发送延迟消息
     */
    private void sendDelayMessages() {
        if (delaySendList.size() != 0) {
            for (BaseMessage delaySendMessage : delaySendList) {
                delaySendMessage.setCreatedOn(System.currentTimeMillis()); // 更新消息时间
                sendMessage(delaySendMessage);
            }
            delaySendList.clear();
        }
    }

    /**
     * 根据设置是否上传顾客信息
     */
    private void setClientInfo() {
        if (getIntent() != null) {
            Serializable clientInfoSerializable = getIntent().getSerializableExtra(CLIENT_INFO);
            if (clientInfoSerializable != null) {
                HashMap<String, String> clientInfo = (HashMap<String, String>) clientInfoSerializable;
                mController.setClientInfo(clientInfo, null);
            }
        }
    }

    /**
     * 检查是否需要切换当前顾客
     *
     * @param onFinishCallback
     */
    private void checkIfNeedUpdateClient(final OnFinishCallback onFinishCallback) {
        // 从 intent 获取 clientId、customizedId 和 clientInfo
        Intent intent = getIntent();
        String clientId = null;
        String customizedId = null;
        if (intent != null) {
            clientId = getIntent().getStringExtra(CLIENT_ID);
            customizedId = getIntent().getStringExtra(CUSTOMIZED_ID);
        }
        // 如果有传 id，要切换顾客身份
        if (!TextUtils.isEmpty(clientId) || !TextUtils.isEmpty(customizedId)) {
            String clientOrCustomizedId = TextUtils.isEmpty(clientId) ? customizedId : clientId;
            MQManager.getInstance(this).setCurrentClient(clientOrCustomizedId, new SuccessCallback() {
                @Override
                public void onSuccess() {
                    onFinishCallback.onFinish();
                }

                @Override
                public void onFailure(int code, String message) {
                    onFinishCallback.onFinish();
                }
            });
        } else {
            onFinishCallback.onFinish();
        }
    }

    /**
     * 拉取最新消息，并从数据库加载显示
     * PS：注意切换用户
     */
    private void getMessageFromServiceAndLoad() {
        checkIfNeedUpdateClient(new OnFinishCallback() {
            @Override
            public void onFinish() {
                // 先从服务器拉取最新消息, 不用关心结果，有新消息会保存到本地数据库，加载消息的时候会取出来
                mController.getMessageFromService(System.currentTimeMillis(), MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {
                    @Override
                    public void onSuccess(List<BaseMessage> messageList) {
                        getMessageDataFromDatabaseAndLoad();
                    }

                    @Override
                    public void onFailure(int code, String message) {
                        getMessageDataFromDatabaseAndLoad();
                    }
                });
            }
        });
    }

    /**
     * 从数据库获取消息并加载
     */
    private void getMessageDataFromDatabaseAndLoad() {
        // 从数据库获取数据
        mController.getMessagesFromDatabase(System.currentTimeMillis(), MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {

            @Override
            public void onSuccess(List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanVoiceMessage(messageList);

                mChatMessageList.addAll(messageList);
                loadData();
                if (entWelcomeMsg != null) {
                    mChatMessageList.remove(entWelcomeMsg);
                }
                // 添加企业欢迎消息
                if (mController.getEnterpriseConfig().scheduler_after_client_send_msg
                        && entWelcomeMsg == null
                        && !TextUtils.isEmpty(mController.getEnterpriseConfig().ent_welcome_message)) {
                    entWelcomeMsg = new TextMessage();
                    entWelcomeMsg.setAvatar(mController.getEnterpriseConfig().avatar);
                    entWelcomeMsg.setAgentNickname(mController.getEnterpriseConfig().public_nickname);
                    entWelcomeMsg.setContent(mController.getEnterpriseConfig().ent_welcome_message);
                    entWelcomeMsg.setItemViewType(BaseMessage.TYPE_AGENT);
                    entWelcomeMsg.setStatus(BaseMessage.STATE_ARRIVE);
                    entWelcomeMsg.setId(System.currentTimeMillis());
                    receiveNewMsg(entWelcomeMsg);
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
            }
        });
    }

    private String getClientAvatarUrl() {
        if (getIntent() != null) {
            Serializable clientInfoSerializable = getIntent().getSerializableExtra(CLIENT_INFO);
            if (clientInfoSerializable != null) {
                HashMap<String, String> clientInfo = (HashMap<String, String>) clientInfoSerializable;
                if (clientInfo.containsKey("avatar")) {
                    return clientInfo.get("avatar");
                }
            }
        }
        return "";
    }

    /**
     * 加载消息到列表中
     */
    private void loadData() {
        // 添加TimeItem
        MQTimeUtils.refreshMQTimeItem(mChatMessageList);
        // 加载到UI
        mLoadProgressBar.setVisibility(View.GONE);
        Iterator<BaseMessage> messageIterator = mChatMessageList.iterator();


        String clientAvatarUrl = getClientAvatarUrl();

        while (messageIterator.hasNext()) {
            BaseMessage message = messageIterator.next();
            // 将正在发送显示为已发送
            if (BaseMessage.STATE_SENDING.equals(message.getStatus())) {
                message.setStatus(BaseMessage.STATE_ARRIVE);
            }
            // 如果是黑名单状态，不显示结束对话的消息
            else if (BaseMessage.TYPE_ENDING.equals(message.getType()) && isBlackState) {
                messageIterator.remove();
            }

            // 处理设置客户头像后，第一次进来时没有头像
            if (MQConfig.isShowClientAvatar && !TextUtils.isEmpty(clientAvatarUrl) && message.getItemViewType() == BaseMessage.TYPE_CLIENT) {
                message.setAvatar(clientAvatarUrl);
            }
        }
        if (isBlackState) {
            addBlacklistTip(R.string.mq_blacklist_tips);
        }
        MQUtils.scrollListViewToBottom(mConversationListView);
        mChatMsgAdapter.downloadAndNotifyDataSetChanged(mChatMessageList);
        mChatMsgAdapter.notifyDataSetChanged();

        if (!mHasLoadData) {
            onLoadDataComplete(MQConversationActivity.this, mCurrentAgent);
        }
        mHasLoadData = true;
    }

    /**
     * 数据加载完成后的回调
     *
     * @param mqConversationActivity 当前 Activity
     * @param agent                  当前客服，可能为 null
     */
    protected void onLoadDataComplete(MQConversationActivity mqConversationActivity, Agent agent) {
        sendPreMessage();
    }

    private void sendPreMessage() {
        if (getIntent() != null && !mController.getIsWaitingInQueue()) {
            String preSendTextContent = getIntent().getStringExtra(PRE_SEND_TEXT);
            String preSendImageFilePath = getIntent().getStringExtra(PRE_SEND_IMAGE_PATH);
            if (!TextUtils.isEmpty(preSendTextContent)) {
                // 加入到待发送，等分配成功，并且 socket 连上的时候发送
                TextMessage preSendMsg = new TextMessage(preSendTextContent);
                delaySendList.add(preSendMsg);
            }
            if (!TextUtils.isEmpty(preSendImageFilePath)) {
                File imageFile = new File(preSendImageFilePath);
                createAndSendImageMessage(imageFile);
            }
            // 清空 intent 里面的数据,因为排队成功可能还会再发一次,如果为空就不再发了
            getIntent().putExtra(PRE_SEND_TEXT, "");
            getIntent().putExtra(PRE_SEND_IMAGE_PATH, "");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_rl) {
            MQUtils.closeKeyboard(this);
            // 返回按钮
            onBackPressed();
        } else if (id == R.id.emoji_select_btn) {
            // 表情按钮

            if (mCustomKeyboardLayout.isEmotionKeyboardVisible()) {
                hideEmojiSelectIndicator();
            } else {
                showEmojiSelectIndicator();
            }

            hideVoiceSelectIndicator();

            mCustomKeyboardLayout.toggleEmotionOriginKeyboard();
        } else if (id == R.id.send_text_btn) {

            if (!checkSendable()) {
                return;
            }

            String msg = mInputEt.getText().toString();
            createAndSendTextMessage(msg);

        } else if (id == R.id.photo_select_btn) {
            if (!checkSendable()) {
                return;
            }

            if (checkStoragePermission()) {
                // 选择图片
                hideEmojiSelectIndicator();
                hideVoiceSelectIndicator();
                chooseFromPhotoPicker();
            }
        } else if (id == R.id.camera_select_btn) {
            if (!checkSendable()) {
                return;
            }

            if (checkStorageAndCameraPermission()) {
                // 打开相机
                hideEmojiSelectIndicator();
                hideVoiceSelectIndicator();
                choosePhotoFromCamera();
            }
        } else if (id == R.id.mic_select_btn) {
            if (!checkSendable()) {
                return;
            }

            if (checkAudioPermission()) {
                if (mCustomKeyboardLayout.isVoiceKeyboardVisible()) {
                    hideVoiceSelectIndicator();
                } else {
                    showVoiceSelectIndicator();
                }

                hideEmojiSelectIndicator();

                mCustomKeyboardLayout.toggleVoiceOriginKeyboard();
            }
        } else if (id == R.id.evaluate_select_btn) {
            hideEmojiSelectIndicator();
            hideVoiceSelectIndicator();
            showEvaluateDialog();
        } else if (id == R.id.redirect_human_tv) {
            forceRedirectHuman();
        }
    }

    /**
     * 获取当前顾客在排队队列中的位置
     */
    private void getClientPositionInQueue() {
        // 避免多次获取排队位置，先移除
        mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);

        if (mController.getIsWaitingInQueue() && MQUtils.isNetworkAvailable(getApplicationContext())) {
            mController.getClientPositionInQueue(new OnClientPositionInQueueCallback() {
                @Override
                public void onSuccess(int position) {
                    if (position > 0) {
                        addRedirectQueueLeaveMsg(position);
                        sendGetClientPositionInQueueMsg();
                    } else {
                        setClientOnline(true);
                    }
                }

                @Override
                public void onFailure(int code, String message) {
                    sendGetClientPositionInQueueMsg();
                }
            });
        }
    }

    /**
     * 延迟15秒获取当前顾客在排队队列中的位置
     */
    private void sendGetClientPositionInQueueMsg() {
        // 避免多次获取排队位置，先移除
        mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);

        if (mController.getIsWaitingInQueue() && MQUtils.isNetworkAvailable(getApplicationContext())) {
            changeTitleToQueue();
            mHandler.sendEmptyMessageDelayed(WHAT_GET_CLIENT_POSITION_IN_QUEUE, 15 * 1000);
        }
    }

    /**
     * 检查存储权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检查存储权限 和 相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkStorageAndCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,},
                    WRITE_EXTERNAL_STORAGE_AND_CAMERA_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检查录音权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private void showEvaluateDialog() {
        // 如果没有正在录音才弹出评价对话框
        if (!mCustomKeyboardLayout.isRecording()) {
            mCustomKeyboardLayout.closeAllKeyboard();
            if (!TextUtils.isEmpty(mConversationId)) {
                if (mEvaluateDialog == null) {
                    mEvaluateDialog = new MQEvaluateDialog(this, mController.getEnterpriseConfig().serviceEvaluationConfig.getPrompt_text());
                    mEvaluateDialog.setCallback(this);
                }
                mEvaluateDialog.show();
            }
        }
    }

    private void showEmojiSelectIndicator() {
        mEmojiSelectIndicator.setVisibility(View.VISIBLE);
        mEmojiSelectImg.setImageResource(R.drawable.mq_ic_emoji_active);
        mEmojiSelectImg.setColorFilter(getResources().getColor(R.color.mq_indicator_selected));
    }

    private void hideEmojiSelectIndicator() {
        mEmojiSelectIndicator.setVisibility(View.GONE);
        mEmojiSelectImg.setImageResource(R.drawable.mq_ic_emoji_normal);
        mEmojiSelectImg.clearColorFilter();
    }

    private void showVoiceSelectIndicator() {
        mVoiceSelectIndicator.setVisibility(View.VISIBLE);
        mVoiceSelectImg.setImageResource(R.drawable.mq_ic_mic_active);
        mVoiceSelectImg.setColorFilter(getResources().getColor(R.color.mq_indicator_selected));
    }

    private void hideVoiceSelectIndicator() {
        mVoiceSelectIndicator.setVisibility(View.GONE);
        mVoiceSelectImg.setImageResource(R.drawable.mq_ic_mic_normal);
        mVoiceSelectImg.clearColorFilter();
    }


    /**
     * 从本地选择图片
     */
    private void chooseFromPhotoPicker() {
        try {
            startActivityForResult(MQPhotoPickerActivity.newIntent(this, null, 3, null, getString(R.string.mq_send)), REQUEST_CODE_PHOTO);
        } catch (Exception e) {
            MQUtils.show(this, R.string.mq_photo_not_support);
        }
    }


    /**
     * 打开相机
     */
    private void choosePhotoFromCamera() {
        MQUtils.closeKeyboard(MQConversationActivity.this);

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(MQUtils.getPicStorePath(this));
        file.mkdirs();
        String path = MQUtils.getPicStorePath(this) + "/" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(path);
        mCameraPicPath = path;
        Uri uri;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            } else {
                uri = Uri.fromFile(imageFile);
            }
            camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(camera, MQConversationActivity.REQUEST_CODE_CAMERA);
        } catch (Exception e) {
            MQUtils.show(this, R.string.mq_photo_not_support);
        }
    }

    /**
     * 创建并发送TextMessage。如果没有客服在线，发送离线消息
     */
    private void createAndSendTextMessage(String msg) {
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
        if (!imageFile.exists()) {
            return;
        }
        PhotoMessage imageMessage = new PhotoMessage();
        imageMessage.setLocalPath(imageFile.getAbsolutePath());
        sendMessage(imageMessage);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // nothing
                } else {
                    MQUtils.show(this, com.meiqia.meiqiasdk.R.string.mq_sdcard_no_permission);
                }
                break;
            }
            case RECORD_AUDIO_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mVoiceBtn.performClick();
                } else {
                    MQUtils.show(this, R.string.mq_recorder_no_permission);
                }
                break;
            }
            case WRITE_EXTERNAL_STORAGE_AND_CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        mCameraSelectBtn.performClick();
                        // 有存储权限
                    } else {
                        MQUtils.show(this, com.meiqia.meiqiasdk.R.string.mq_camera_or_storage_no_permission);
                    }
                } else {
                    MQUtils.show(this, R.string.mq_camera_or_storage_no_permission);
                }
                break;
            }
        }
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
            } else if (requestCode == REQUEST_CODE_PHOTO) {
                // 从 相册 获取的图片

                ArrayList<String> selectedPhotos = MQPhotoPickerActivity.getSelectedImages(data);
                for (String photoPath : selectedPhotos) {
                    createAndSendImageMessage(new File(photoPath));
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
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String s = intent.getData().getScheme();
            if (!TextUtils.isEmpty(s) && s.startsWith("http")) {
                if (MQConfig.getOnLinkClickCallback() != null) {
                    MQConfig.getOnLinkClickCallback().onClick(this, intent, intent.getDataString());
                    return;
                }
            }
        }
        super.startActivity(intent);
    }

    /**
     * 调用父类的 startActivity 方法
     *
     * @param intent intent
     */
    public void superStartActivity(Intent intent) {
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
        if (mChatMsgAdapter == null) {
            return false;
        }
        if (mRedirectQueueMessage != null && mCurrentAgent == null) {
            popTopTip(R.string.mq_allocate_queue_tip);
            return false;
        }
        if (!MQManager.getInstance(this).isSocketConnect() && hasSetClientOnline) {
            popTopTip(R.string.mq_title_connect_service);
            if (!TextUtils.isEmpty(message.getContent())) {
                // 保存到输入框
                mInputEt.setText(message.getContent());
                mInputEt.setSelection(message.getContent().length());
            }
            return false;
        }

        // 状态改为「正在发送」，以便在数据列表中展示正在发送消息的状态
        message.setStatus(BaseMessage.STATE_SENDING);
        // 添加到对话列表
        mChatMessageList.add(message);
        mInputEt.setText("");

        // 清空未发送的文本消息
        String clientId = mController.getCurrentClientId();
        if (!TextUtils.isEmpty(clientId)) {
            MQUtils.setUnSendTextMessage(this, clientId, "");
        }

        MQTimeUtils.refreshMQTimeItem(mChatMessageList);
        mChatMsgAdapter.notifyDataSetChanged();
        return true;
    }

    private boolean checkSendable() {
        if (mIsAllocatingAgent) {
            MQUtils.show(this, R.string.mq_allocate_agent_tip);
            return false;
        }
        if (!mHasLoadData) {
            MQUtils.show(this, R.string.mq_data_is_loading);
            return false;
        }
        if (mRedirectQueueMessage != null && mCurrentAgent == null) {
            popTopTip(R.string.mq_allocate_queue_tip);
            return false;
        }

        // 如果当前客服是机器人，则限制发送频率为1秒
        if (mCurrentAgent != null && mCurrentAgent.isRobot()) {
            if (System.currentTimeMillis() - mLastSendRobotMessageTime <= 1000) {
                MQUtils.show(this, R.string.mq_send_robot_msg_time_limit_tip);
                return false;
            } else {
                mLastSendRobotMessageTime = System.currentTimeMillis();
            }
        }
        return true;
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    public void sendMessage(final BaseMessage message) {
        if (mController.getEnterpriseConfig().scheduler_after_client_send_msg
                && isNeedDelayOnline) {
            // 没有分配客服的时候，点了发送，但是延迟上线是打开的，就尝试分配
            isNeedDelayOnline = false;
            mHasLoadData = false;
            mChatMessageList.clear();
            if (mChatMsgAdapter != null) {
                mChatMsgAdapter.notifyDataSetChanged();
            }
            MQUtils.closeKeyboard(this);
            mLoadProgressBar.setVisibility(View.VISIBLE);
            // 保存消息，待上线成功之后发送
            message.setStatus(BaseMessage.STATE_SENDING);
            delaySendList.add(message);
            if (message instanceof TextMessage) {
                mInputEt.setText("");
            }
            setClientOnline(false);
            return;
        }

        boolean isPreSendSuc = checkAndPreSend(message);
        if (!isPreSendSuc) {
            return;
        }

        // 开始发送
        mController.sendMessage(message, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                renameVoiceFilename(message);

                // 刷新界面
                mChatMsgAdapter.notifyDataSetChanged();

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
                if (code == ErrorCode.BLACKLIST) {
                    addBlacklistTip(R.string.mq_blacklist_tips);
                } else if (code == ErrorCode.QUEUEING) {
                    if (mCurrentAgent != null && !mCurrentAgent.isRobot()) {
                        mCurrentAgent = null;
                    }
                    popTopTip(R.string.mq_allocate_queue_tip);
                    getClientPositionInQueue();

                    removeNoAgentLeaveMsg();
                    changeTitleToQueue();
                }
                mChatMsgAdapter.notifyDataSetChanged();
            }
        });
        MQUtils.scrollListViewToBottom(mConversationListView);
    }

    /**
     * 重发消息
     *
     * @param message 待重发的消息
     */
    public void resendMessage(final BaseMessage message) {
        if (mRedirectQueueMessage != null && mCurrentAgent == null) {
            popTopTip(R.string.mq_allocate_queue_tip);
            return;
        }

        // 开始重发
        message.setStatus(BaseMessage.STATE_SENDING);
        mController.resendMessage(message, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                renameVoiceFilename(message);
                updateResendMessage(message, 0);
                // 客服不在线的时候，会自动发送留言消息，这个时候要添加一个 tip 到列表
                if (ErrorCode.NO_AGENT_ONLINE == state) {
                    addLeaveMessageTip();
                }
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                updateResendMessage(failureMessage, code);
            }
        });
    }

    private void updateResendMessage(BaseMessage message, int code) {
        // 重发失败，移动到列表最下面
        int messagePosition = mChatMessageList.indexOf(message); // 当前消息的位置
        mChatMessageList.remove(message);
        // 如果下一条 消息是黑名单 tip，黑名单的 tip 也要删除
        if (isBlackState && mChatMessageList.size() > messagePosition && mChatMessageList.get(messagePosition).getItemViewType() == BaseMessage.TYPE_TIP) {
            mChatMessageList.remove(messagePosition);
        }
        MQTimeUtils.refreshMQTimeItem(mChatMessageList);
        mChatMsgAdapter.addMQMessage(message);

        if (code == ErrorCode.BLACKLIST) {
            addBlacklistTip(R.string.mq_blacklist_tips);
        }
        scrollContentToBottom();
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
            mChatMsgAdapter.downloadAndNotifyDataSetChanged(Arrays.asList(message));
        }
    }

    // 监听EditText输入框数据到变化
    private TextWatcher inputTextWatcher = new MQSimpleTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 向服务器发送一个正在输入的函数
            if (!TextUtils.isEmpty(s)) {
                inputting(s.toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mSendTextBtn.setElevation(MQUtils.dip2px(MQConversationActivity.this, 3));
                }
                mSendTextBtn.setImageResource(R.drawable.mq_ic_send_icon_white);
                mSendTextBtn.setBackgroundResource(R.drawable.mq_shape_send_back_pressed);
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mSendTextBtn.setElevation(0);
                }
                mSendTextBtn.setImageResource(R.drawable.mq_ic_send_icon_grey);
                mSendTextBtn.setBackgroundResource(R.drawable.mq_shape_send_back_normal);
            }
        }
    };

    /**
     * 向服务器发送「顾客正在输入」的状态
     *
     * @param content 内容
     */
    private void inputting(String content) {
        mController.sendClientInputtingWithContent(content);
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
    protected void addEvaluateMessageTip(int level, String content) {
        EvaluateMessage evaluateMessage = new EvaluateMessage(level, content);
        mChatMsgAdapter.addMQMessage(evaluateMessage);
    }

    @Override
    public void executeEvaluate(final int level, final String content) {
        if (!checkSendable()) {
            return;
        }

        mController.executeEvaluate(mConversationId, level, content, new SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
                MQUtils.show(MQConversationActivity.this, R.string.mq_evaluate_failure);
            }

            @Override
            public void onSuccess() {
                addEvaluateMessageTip(level, content);
            }
        });
    }

    @Override
    public void onAudioRecorderFinish(int time, String filePath) {
        if (!checkSendable()) {
            return;
        }

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
        MQUtils.scrollListViewToBottom(mConversationListView);
    }

    @Override
    public void onAudioRecorderNoPermission() {
        MQUtils.show(this, R.string.mq_recorder_no_permission);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideEmojiSelectIndicator();
        hideVoiceSelectIndicator();
        return false;
    }

    /**
     * 修改客服在线状态和转人工按钮
     */
    private void updateAgentOnlineOfflineStatusAndRedirectHuman() {
        Agent agent = mController.getCurrentAgent();

        if (agent != null) {
            if (!agent.isOnline()) {
                mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mq_shape_agent_status_offline, 0);
            } else if (agent.isOffDuty()) {
                mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mq_shape_agent_status_off_duty, 0);
            } else {
                mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mq_shape_agent_status_online, 0);
            }

            if (agent.isRobot()) {
                mRedirectHumanTv.setVisibility(mIsShowRedirectHumanButton ? View.VISIBLE : View.GONE);
                mEvaluateBtn.setVisibility(View.GONE);
            } else {
                mRedirectHumanTv.setVisibility(View.GONE);
                mEvaluateBtn.setVisibility(MQConfig.isEvaluateSwitchOpen ? View.VISIBLE : View.GONE);
            }
        } else {
            hiddenAgentStatusAndRedirectHuman();
        }
    }

    /**
     * 隐藏客服在线状态和转人工按钮
     */
    private void hiddenAgentStatusAndRedirectHuman() {
        mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        mRedirectHumanTv.setVisibility(View.GONE);
        mEvaluateBtn.setVisibility(View.GONE);
    }

    public void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message) {
        // 避免界面销毁了还更新 UI
        if (isDestroy) {
            return;
        }

        popTopTip(R.string.mq_download_error);
    }

    public void onFileMessageExpired(FileMessage fileMessage) {
        // 避免界面销毁了还更新 UI
        if (isDestroy) {
            return;
        }

        popTopTip(R.string.mq_expired_top_tip);
    }

    /**
     * 退出界面后，取消所有下载
     */
    private void cancelAllDownload() {
        for (BaseMessage message : mChatMessageList) {
            if (message instanceof FileMessage) {
                MQConfig.getController(this).cancelDownload(((FileMessage) message).getUrl());
            }
        }
    }

    @Override
    public void onEvaluateRobotAnswer(final RobotMessage robotMessage, final int useful) {
        mController.evaluateRobotAnswer(robotMessage.getId(), robotMessage.getQuestionId(), useful, new OnEvaluateRobotAnswerCallback() {
            @Override
            public void onFailure(int code, String message) {
                MQUtils.show(MQConversationActivity.this, R.string.mq_evaluate_failure);
            }

            @Override
            public void onSuccess(String message) {
                robotMessage.setAlreadyFeedback(true);
                mChatMsgAdapter.notifyDataSetChanged();

                if (RobotMessage.EVALUATE_USELESS == useful) {
                    addInitiativeRedirectMessage(R.string.mq_useless_redirect_tip);
                }

                // 如果评价后返回的message不为空，则模拟一条客服发的文本消息
                if (!TextUtils.isEmpty(message)) {
                    String avatar = null;
                    if (mCurrentAgent != null) {
                        avatar = mCurrentAgent.getAvatar();
                    }
                    mChatMsgAdapter.addMQMessage(new TextMessage(message, avatar));
                }
            }
        });
    }

    @Override
    public void onClickRobotMenuItem(String text) {
        sendMessage(new TextMessage(text));
    }

    /**
     * 刷新企业配置信息
     */
    private void refreshEnterpriseConfig() {
        refreshRedirectHumanBtn();
        MQConfig.getController(this).refreshEnterpriseConfig(new SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
            }

            @Override
            public void onSuccess() {
                refreshRedirectHumanBtn();
            }
        });
    }

    /**
     * 刷新强转人工按钮
     */
    private void refreshRedirectHumanBtn() {
        mIsShowRedirectHumanButton = MQConfig.getController(this).getEnterpriseConfig().robotSettings.isShow_switch();
        // mCurrentAgent不为空时才刷新，否则会导致正在分配客服的标题不会显示
        if (mCurrentAgent != null) {
            // 把当前agent传进去，复用之前的逻辑
            setCurrentAgent(mCurrentAgent);
        }
    }

    /**
     * 添加【没有客服请留言】的提示消息到消息流的末尾
     */
    private void addNoAgentLeaveMsg() {
        // 处理机器人客服转人工排队时发消息的情况
        if (mRedirectQueueMessage != null && mCurrentAgent != null) {
            addRedirectQueueLeaveMsg(mRedirectQueueMessage.getQueueSize());
            return;
        }

        removeRedirectQueueLeaveMsg();

        // 如果之前已经添加过【没有客服请留言】的提示消息，并且是在消息流的末尾，则不再执行后面的操作。否则先移除再添加到消息流的末尾
        if (mChatMessageList != null && mChatMessageList.size() > 0 && mChatMessageList.get(mChatMessageList.size() - 1) instanceof NoAgentLeaveMessage) {
            return;
        }

        removeNoAgentLeaveMsg();

        if (mCurrentAgent == null) {
            changeTitleToNoAgentState();
        }

        mChatMsgAdapter.addMQMessage(new NoAgentLeaveMessage());
        MQUtils.scrollListViewToBottom(mConversationListView);
    }

    /**
     * 从消息流中移除【没有客服请留言】的提示消息
     */
    private void removeNoAgentLeaveMsg() {
        Iterator<BaseMessage> chatItemViewBaseIterator = mChatMessageList.iterator();
        while (chatItemViewBaseIterator.hasNext()) {
            BaseMessage baseMessage = chatItemViewBaseIterator.next();
            if (baseMessage instanceof NoAgentLeaveMessage) {
                chatItemViewBaseIterator.remove();
                mChatMsgAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    /**
     * 添加【提示转人工】的提示消息到消息流的末尾
     */
    private void addInitiativeRedirectMessage(@StringRes int tipResId) {
        // 如果当前客服不为空，并且是真人客服时，则不再执行后面的操作
        if (mCurrentAgent != null && !mCurrentAgent.isRobot()) {
            return;
        }

        // 如果之前已经添加过【提示转人工】的提示消息，并且是在消息流的末尾，则不再执行后面的操作。否则先移除再添加到消息流的末尾
        if (mChatMessageList != null && mChatMessageList.size() > 0 && mChatMessageList.get(mChatMessageList.size() - 1) instanceof InitiativeRedirectMessage) {
            return;
        }
        removeInitiativeRedirectMessage();

        mChatMsgAdapter.addMQMessage(new InitiativeRedirectMessage(tipResId));
        MQUtils.scrollListViewToBottom(mConversationListView);
    }

    /**
     * 移除【提示转人工】的提示消息到消息流的末尾
     */
    private void removeInitiativeRedirectMessage() {
        Iterator<BaseMessage> chatItemViewBaseIterator = mChatMessageList.iterator();
        while (chatItemViewBaseIterator.hasNext()) {
            BaseMessage baseMessage = chatItemViewBaseIterator.next();
            if (baseMessage instanceof InitiativeRedirectMessage) {
                chatItemViewBaseIterator.remove();
                mChatMsgAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    /**
     * 添加或更新【排队人数或留言】的提示消息到消息流的末尾
     *
     * @param queueSize
     */
    private void addRedirectQueueLeaveMsg(int queueSize) {
        removeNoAgentLeaveMsg();

        changeTitleToQueue();

        removeRedirectQueueLeaveMsg();

        mRedirectQueueMessage = new RedirectQueueMessage(queueSize);
        mChatMsgAdapter.addMQMessage(mRedirectQueueMessage);

        MQUtils.scrollListViewToBottom(mConversationListView);
    }

    /**
     * 从消息流中移除【排队人数或留言】的提示消息
     */
    private void removeRedirectQueueLeaveMsg() {
        Iterator<BaseMessage> chatItemViewBaseIterator = mChatMessageList.iterator();
        while (chatItemViewBaseIterator.hasNext()) {
            BaseMessage baseMessage = chatItemViewBaseIterator.next();
            if (baseMessage instanceof RedirectQueueMessage) {
                chatItemViewBaseIterator.remove();
                mChatMsgAdapter.notifyDataSetChanged();
                break;
            }
        }
        mRedirectQueueMessage = null;
    }

    @Override
    public void onClickLeaveMessage() {
        startActivity(new Intent(this, MQMessageFormActivity.class));
    }

    @Override
    public void onClickForceRedirectHuman() {
        forceRedirectHuman();
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
                    MQConversationActivity.this.setCurrentAgent(mCurrentAgent);
                }
            }, 2000);

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
            if (!checkSendable()) {
                return;
            }

            showEvaluateDialog();
        }

        @Override
        public void setNewConversationId(String newConversationId) {
            mConversationId = newConversationId;
        }

        @Override
        public void updateAgentOnlineOfflineStatus() {
            MQConversationActivity.this.updateAgentOnlineOfflineStatusAndRedirectHuman();
        }

        @Override
        public void blackAdd() {
            isBlackState = true;
            changeTitleToNoAgentState();
        }

        @Override
        public void blackDel() {
            isBlackState = false;
        }

        @Override
        public void removeQueue() {
            mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);
            removeRedirectQueueLeaveMsg();
            sendPreMessage();
        }

        @Override
        public void queueingInitConv() {
            removeQueue();
            setCurrentAgent(mController.getCurrentAgent());
        }

        @Override
        public void socketOpen() {

        }
    }

    /**
     * 处理收到的新消息
     *
     * @param baseMessage 新消息
     */
    private void receiveNewMsg(BaseMessage baseMessage) {
        if (mChatMsgAdapter != null && !isDupMessage(baseMessage)) {
            // 如果是配置了不显示语音，收到语音消息直接过滤
            if (!MQConfig.isVoiceSwitchOpen && BaseMessage.TYPE_CONTENT_VOICE.equals(baseMessage.getContentType())) {
                return;
            }

            // 被拉黑状态，不显示结束对话消息
            if (BaseMessage.TYPE_ENDING.equals(baseMessage.getType()) && isBlackState) {
                return;
            }

            mChatMessageList.add(baseMessage);
            MQTimeUtils.refreshMQTimeItem(mChatMessageList);

            if (baseMessage instanceof VoiceMessage) {
                mChatMsgAdapter.downloadAndNotifyDataSetChanged(Arrays.asList(baseMessage));
            } else if (baseMessage instanceof RobotMessage) {
                RobotMessage robotMessage = (RobotMessage) baseMessage;
                if (RobotMessage.SUB_TYPE_REDIRECT.equals(robotMessage.getSubType())) {
                    forceRedirectHuman();
                } else if (RobotMessage.SUB_TYPE_REPLY.equals(robotMessage.getSubType())) {
                    addNoAgentLeaveMsg();
                } else if (RobotMessage.SUB_TYPE_QUEUEING.equals(robotMessage.getSubType())) {
                    forceRedirectHuman();
                } else if (RobotMessage.SUB_TYPE_MANUAL_REDIRECT.equals(robotMessage.getSubType())) {
                    addInitiativeRedirectMessage(R.string.mq_manual_redirect_tip);
                } else {
                    mChatMsgAdapter.notifyDataSetChanged();
                }
            } else {
                mChatMsgAdapter.notifyDataSetChanged();
            }

            int lastVisiblePosition = mConversationListView.getLastVisiblePosition();
            // -2 因为是先添加
            if (lastVisiblePosition == (mChatMsgAdapter.getCount() - 2)) {
                MQUtils.scrollListViewToBottom(mConversationListView);
            }
            // 在界面中播放声音
            if (!isPause && MQConfig.isSoundSwitchOpen) {
                mSoundPoolManager.playSound(R.raw.mq_new_message);
            }

            // 保存最后一条消息时间
            mController.saveConversationLastMessageTime(baseMessage.getCreatedOn());
        }

    }

    /**
     * 消息是否已经在列表中
     *
     * @param baseMessage
     * @return true，已经存在与列表；false，不存在
     */
    private boolean isDupMessage(BaseMessage baseMessage) {
        for (BaseMessage message : mChatMessageList) {
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
        // 第一次进入的时候，会立即收到广播，需要避免以下
        private boolean isFirstReceiveBroadcast = true;

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (!isFirstReceiveBroadcast) {
                    // 有网络
                    if (MQUtils.isNetworkAvailable(getApplicationContext())) {
                        // 断网后，返回重新进入， 又有网了刷新 Agent
                        setCurrentAgent(mController.getCurrentAgent());

                        getClientPositionInQueue();
                    }
                    // 没有网络
                    else {
                        changeTitleToNetErrorState();

                        mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);
                    }
                } else {
                    isFirstReceiveBroadcast = false;
                }
            }
        }

    }
}
