package com.meiqia.meiqiasdk.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQEmotionUtil;
import com.meiqia.meiqiasdk.util.MQUtils;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/12/21 下午2:19
 * 描述:
 */
public class MQCustomKeyboardLayout extends MQBaseCustomCompositeView {
    private static final int WHAT_SCROLL_CONTENT_TO_BOTTOM = 1;
    private static final int WHAT_CHANGE_TO_EMOTION_KEYBOARD = 2;
    private static final int WHAT_CHANGE_TO_VOICE_KEYBOARD = 3;

    private MQEmotionKeyboardLayout mEmotionKeyboardLayout;
    private MQRecorderKeyboardLayout mRecorderKeyboardLayout;

    private Activity mActivity;
    private EditText mContentEt;
    private Callback mCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CHANGE_TO_EMOTION_KEYBOARD:
                    showEmotionKeyboard();
                    break;
                case WHAT_CHANGE_TO_VOICE_KEYBOARD:
                    showVoiceKeyboard();
                    break;
                case WHAT_SCROLL_CONTENT_TO_BOTTOM:
                    mCallback.scrollContentToBottom();
                    break;
            }
        }
    };

    public MQCustomKeyboardLayout(Context context) {
        super(context);
    }

    public MQCustomKeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MQCustomKeyboardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_layout_custom_keyboard;
    }

    @Override
    protected void initView() {
        mEmotionKeyboardLayout = getViewById(R.id.emotionKeyboardLayout);
        mRecorderKeyboardLayout = getViewById(R.id.recorderKeyboardLayout);
    }

    @Override
    protected void setListener() {
        mEmotionKeyboardLayout.setCallback(new MQEmotionKeyboardLayout.Callback() {
            @Override
            public void onDelete() {
                mContentEt.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }

            @Override
            public void onInsert(String text) {
                // 在当前光标位置插入文本
                int cursorPosition = mContentEt.getSelectionStart();
                StringBuilder sb = new StringBuilder(mContentEt.getText());
                sb.insert(cursorPosition, text);
                mContentEt.setText(MQEmotionUtil.getEmotionText(getContext(), sb.toString(), 20));
                mContentEt.setSelection(cursorPosition + text.length());
            }
        });

        mRecorderKeyboardLayout.setCallback(new MQRecorderKeyboardLayout.Callback() {
            @Override
            public void onAudioRecorderFinish(int time, String filePath) {
                if (mCallback != null) {
                    mCallback.onAudioRecorderFinish(time, filePath);
                }
            }

            @Override
            public void onAudioRecorderTooShort() {
                if (mCallback != null) {
                    mCallback.onAudioRecorderTooShort();
                }
            }

            @Override
            public void onAudioRecorderNoPermission() {
                if (mCallback != null) {
                    mCallback.onAudioRecorderNoPermission();
                }
            }
        });
    }

    @Override
    protected int[] getAttrs() {
        return new int[0];
    }

    @Override
    protected void initAttr(int attr, TypedArray typedArray) {
    }

    @Override
    protected void processLogic() {
    }

    /**
     * 切换表情键盘和软键盘
     */
    public void toggleEmotionOriginKeyboard() {
        if (isEmotionKeyboardVisible()) {
            changeToOriginalKeyboard();
        } else {
            changeToEmotionKeyboard();
        }
    }

    /**
     * 切换语音键盘和软键盘
     */
    public void toggleVoiceOriginKeyboard() {
        if (isVoiceKeyboardVisible()) {
            changeToOriginalKeyboard();
        } else {
            changeToVoiceKeyboard();
        }
    }

    /**
     * 切换到语音键盘
     */
    public void changeToVoiceKeyboard() {
        MQUtils.closeKeyboard(mActivity);

        if (isCustomKeyboardVisible()) {
            showVoiceKeyboard();
        } else {
            mHandler.sendEmptyMessageDelayed(WHAT_CHANGE_TO_VOICE_KEYBOARD, MQUtils.KEYBOARD_CHANGE_DELAY);
        }
    }

    /**
     * 切换到表情键盘
     */
    public void changeToEmotionKeyboard() {
        if (!mContentEt.isFocused()) {
            mContentEt.requestFocus();
            mContentEt.setSelection(mContentEt.getText().toString().length());
        }

        MQUtils.closeKeyboard(mActivity);

        if (isCustomKeyboardVisible()) {
            showEmotionKeyboard();
        } else {
            mHandler.sendEmptyMessageDelayed(WHAT_CHANGE_TO_EMOTION_KEYBOARD, MQUtils.KEYBOARD_CHANGE_DELAY);
        }
    }

    /**
     * 切换到系统原始键盘
     */
    public void changeToOriginalKeyboard() {
        closeCustomKeyboard();
        MQUtils.openKeyboard(mContentEt);
        // 打开系统键盘也延时了的，这里延时2倍再滚动到底部
        mHandler.sendEmptyMessageDelayed(WHAT_SCROLL_CONTENT_TO_BOTTOM, MQUtils.KEYBOARD_CHANGE_DELAY * 2);
    }

    /**
     * 显示表情键盘
     */
    private void showEmotionKeyboard() {
        mEmotionKeyboardLayout.setVisibility(VISIBLE);
        sendScrollContentToBottomMsg();

        closeVoiceKeyboard();
    }

    /**
     * 显示语音键盘
     */
    private void showVoiceKeyboard() {
        mRecorderKeyboardLayout.setVisibility(VISIBLE);
        sendScrollContentToBottomMsg();

        closeEmotionKeyboard();
    }

    /**
     * 延时发送滚动内容到底部的消息给Handler
     */
    private void sendScrollContentToBottomMsg() {
        mHandler.sendEmptyMessageDelayed(WHAT_SCROLL_CONTENT_TO_BOTTOM, MQUtils.KEYBOARD_CHANGE_DELAY);
    }

    /**
     * 关闭表情键盘
     */
    public void closeEmotionKeyboard() {
        mEmotionKeyboardLayout.setVisibility(GONE);
    }

    /**
     * 关闭语音键盘
     */
    public void closeVoiceKeyboard() {
        mRecorderKeyboardLayout.setVisibility(GONE);
    }

    /**
     * 关闭自定义键盘
     */
    public void closeCustomKeyboard() {
        closeEmotionKeyboard();
        closeVoiceKeyboard();
    }

    /**
     * 关闭所有键盘
     */
    public void closeAllKeyboard() {
        closeCustomKeyboard();
        MQUtils.closeKeyboard(mActivity);
    }

    /**
     * 表情键盘是否可见
     *
     * @return
     */
    public boolean isEmotionKeyboardVisible() {
        return mEmotionKeyboardLayout.getVisibility() == View.VISIBLE;
    }

    /**
     * 语音键盘是否可见
     *
     * @return
     */
    public boolean isVoiceKeyboardVisible() {
        return mRecorderKeyboardLayout.getVisibility() == View.VISIBLE;
    }

    /**
     * 自定义键盘是否可见，在Activity的onBackPressed中处理返回按钮
     *
     * @return
     */
    public boolean isCustomKeyboardVisible() {
        return isEmotionKeyboardVisible() || isVoiceKeyboardVisible();
    }

    /**
     * 初始化，必须调用该方法
     *
     * @param activity
     * @param contentEt
     */
    public void init(Activity activity, EditText contentEt, Callback callback) {
        if (activity == null || contentEt == null || callback == null) {
            throw new RuntimeException(MQCustomKeyboardLayout.class.getSimpleName() + "的init方法的参数均不能为null");
        }

        mActivity = activity;
        mContentEt = contentEt;
        mCallback = callback;


        mContentEt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCustomKeyboardVisible()) {
                    closeCustomKeyboard();
                }
                sendScrollContentToBottomMsg();
            }
        });

        mContentEt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeAllKeyboard();
                } else {
                    sendScrollContentToBottomMsg();
                }
            }
        });
    }

    /**
     * 是否正在录音
     *
     * @return
     */
    public boolean isRecording() {
        return mRecorderKeyboardLayout.isRecording();
    }

    public interface Callback {
        /**
         * 录音完成
         *
         * @param time     录音时长
         * @param filePath 音频文件路径
         */
        void onAudioRecorderFinish(int time, String filePath);

        /**
         * 录音时间太短
         */
        void onAudioRecorderTooShort();

        /**
         * 滚动内容到最底部
         */
        void scrollContentToBottom();

        /**
         * 没有录音权限
         */
        void onAudioRecorderNoPermission();
    }

    /**
     * 查找View
     *
     * @param id   控件的id
     * @param <VT> View类型
     * @return
     */
    protected <VT extends View> VT getViewById(@IdRes int id) {
        return (VT) findViewById(id);
    }
}