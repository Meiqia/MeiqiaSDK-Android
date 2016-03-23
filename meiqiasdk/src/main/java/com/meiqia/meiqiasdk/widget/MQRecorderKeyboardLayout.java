package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQAudioPlayerManager;
import com.meiqia.meiqiasdk.util.MQAudioRecorderManager;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.io.File;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/1/25 下午2:46
 * 描述:
 */
public class MQRecorderKeyboardLayout extends RelativeLayout implements MQAudioRecorderManager.Callback, View.OnTouchListener {
    /**
     * 录音的最大时间
     */
    private static final int RECORDER_MAX_TIME = 60;
    private static final int VOICE_LEVEL_COUNT = 9;

    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_CANCEL = 3;

    private static final int WHAT_AUDIO_PREPARED = 1;
    private static final int WHAT_VOICE_CHANGED = 2;
    private static final int WHAT_HANDLE_OVERTIME = 3;


    private int mCurrentState = STATE_NORMAL;
    private boolean mIsRecording;
    /**
     * 是否超时，默认不超时
     */
    private boolean mIsOvertime = false;
    private boolean mHasPermission = false;

    private int mDistanceCancel;

    private MQAudioRecorderManager mAudioRecorderManager;
    private float mTime;
    private Callback mCallback;
    private TextView mStatusTv;
    private ImageView mAnimIv;

    /**
     * 上一次提示录音时间太短的时间戳
     */
    private long mLastTipTooShortTime;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_AUDIO_PREPARED:
                    mIsRecording = true;
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case WHAT_VOICE_CHANGED:
                    if (mCurrentState == STATE_RECORDING) {
                        int resId = getContext().getResources().getIdentifier("mq_voice_level" + mAudioRecorderManager.getVoiceLevel(VOICE_LEVEL_COUNT), "drawable", getContext().getPackageName());
                        mAnimIv.setImageResource(resId);
                        mAnimIv.setColorFilter(getResources().getColor(R.color.mq_chat_audio_recorder_icon));

                        int remainingTime = Math.round(RECORDER_MAX_TIME - mTime);
                        if (remainingTime <= 10) {
                            mStatusTv.setText(getContext().getString(R.string.mq_recorder_remaining_time, remainingTime));
                        }
                    }
                    break;
                case WHAT_HANDLE_OVERTIME:
                    mIsOvertime = true;
                    handleActionUp();
                    break;
            }
        }
    };

    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (mIsRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    if (mTime <= RECORDER_MAX_TIME) {
                        mHandler.sendEmptyMessage(WHAT_VOICE_CHANGED);
                    } else {
                        mHandler.sendEmptyMessage(WHAT_HANDLE_OVERTIME);
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public MQRecorderKeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.mq_layout_recorder_keyboard, this);
        mStatusTv = (TextView) findViewById(R.id.tv_recorder_keyboard_status);
        mAnimIv = (ImageView) findViewById(R.id.iv_recorder_keyboard_anim);
        mAnimIv.setColorFilter(getResources().getColor(R.color.mq_chat_audio_recorder_icon));
        mAnimIv.setOnTouchListener(this);

        mDistanceCancel = MQUtils.dip2px(context, 10);
        mAudioRecorderManager = MQAudioRecorderManager.getInstance(context);
        mAudioRecorderManager.setCallback(this);
    }

    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(WHAT_AUDIO_PREPARED);
    }

    @Override
    public void onAudioRecorderNoPermission() {
        endRecorder();
        reset();
    }

    private void changeState(int status) {
        if (mCurrentState != status) {
            mCurrentState = status;
            switch (mCurrentState) {
                case STATE_NORMAL:
                    mStatusTv.setText(R.string.mq_audio_status_normal);
                    mAnimIv.setImageResource(R.drawable.mq_voice_level1);
                    mAnimIv.setColorFilter(getResources().getColor(R.color.mq_chat_audio_recorder_icon));
                    break;
                case STATE_RECORDING:
                    mStatusTv.setText(R.string.mq_audio_status_recording);
                    break;
                case STATE_WANT_CANCEL:
                    mStatusTv.setText(R.string.mq_audio_status_want_cancel);
                    mAnimIv.setImageResource(R.drawable.mq_voice_want_cancel);
                    mAnimIv.clearColorFilter();
                    break;
            }
        }
    }

    private boolean isWantCancel(int x, int y) {
        if (y < -mDistanceCancel) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsOvertime = false;
                mHasPermission = true;
                changeState(STATE_RECORDING);
                mAudioRecorderManager.prepareAudio();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsOvertime && mIsRecording && mHasPermission) {
                    if (isWantCancel(x, y)) {
                        changeState(STATE_WANT_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                handleActionUp();
                break;
            case MotionEvent.ACTION_CANCEL:
                mAudioRecorderManager.cancel();
                reset();
                break;
        }
        return true;
    }

    private void handleActionUp() {
        if (!mIsOvertime && mHasPermission) {
            // 录音没有超时的情况

            if (!mIsRecording || mTime < 1) {
                // prepare未完成
                mAudioRecorderManager.cancel();

                if (System.currentTimeMillis() - mLastTipTooShortTime > 1000) {
                    mLastTipTooShortTime = System.currentTimeMillis();
                    mCallback.onAudioRecorderTooShort();
                }
            } else if (mCurrentState == STATE_RECORDING) {
                endRecorder();
            } else if (mCurrentState == STATE_WANT_CANCEL) {
                mAudioRecorderManager.cancel();
            }
        } else if (mIsRecording) {
            // 录音超时，并且正在录音时

            endRecorder();
        }

        reset();
    }

    /**
     * 结束录音
     */
    private void endRecorder() {
        mAudioRecorderManager.release();
        if (mCallback != null) {
            String currentFilePath = mAudioRecorderManager.getCurrenFilePath();
            if (!TextUtils.isEmpty(currentFilePath)) {
                File currentFile = new File(currentFilePath);
                // 在某些手机上，没有权限录音时，文件大小为6
                if (currentFile.exists() && currentFile.length() > 6) {
                    mCallback.onAudioRecorderFinish(MQAudioPlayerManager.getDurationByFilePath(getContext(), currentFile.getAbsolutePath()), currentFile.getAbsolutePath());
                } else {
                    mAudioRecorderManager.cancel();
                    mCallback.onAudioRecorderNoPermission();
                }
            }
        }
    }

    /**
     * 恢复到初始状态
     */
    private void reset() {
        mIsRecording = false;
        mHasPermission = false;
        mTime = 0;
        changeState(STATE_NORMAL);
    }

    /**
     * 是否正在录音
     *
     * @return
     */
    public boolean isRecording() {
        return mCurrentState != STATE_NORMAL;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onAudioRecorderFinish(int time, String filePath);

        void onAudioRecorderTooShort();

        void onAudioRecorderNoPermission();
    }
}