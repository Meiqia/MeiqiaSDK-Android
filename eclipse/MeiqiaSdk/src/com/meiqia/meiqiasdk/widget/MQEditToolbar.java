package com.meiqia.meiqiasdk.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQEmotionUtil;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/12/21 下午2:19
 * 描述:
 */
public class MQEditToolbar extends RelativeLayout implements View.OnClickListener {
    private static final int EMOTION_COLUMN = 7;
    private static final int EMOTION_ROW = 4;
    private static final int EMOTION_PAGE_SIZE = EMOTION_COLUMN * EMOTION_ROW - 1;

    private static final int WHAT_CHANGE_TO_EMOTION_KEYBOARD = 1;
    private static final int WHAT_CHANGE_TO_VOICE_KEYBOARD = 2;
    private static final int WHAT_SCROLL_CONTENT_TO_BOTTOM = 3;

    // emotion表情 START
    private LinearLayout mEmotionLl;
    private ViewPager mEmotionVp;
    private LinearLayout mIndicatorLl;
    private ArrayList<ImageView> mIndicatorIvList;
    private ArrayList<GridView> mGridViewList;
    // emotion表情 END

    // 扩展语音 START
    private MQAudioRecorderLayout mVoiceArl;
    // 扩展语音 END

    private Activity mActivity;
    private EditText mContentEt;
    private Callback mCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CHANGE_TO_EMOTION_KEYBOARD:
                    showEmotionKeyboard();
                    closeVoiceKeyboard();
                    break;
                case WHAT_CHANGE_TO_VOICE_KEYBOARD:
                    showVoiceKeyboard();
                    closeEmotionKeyboard();
                    break;
                case WHAT_SCROLL_CONTENT_TO_BOTTOM:
                    mCallback.scrollContentToBottom();
                    break;
            }
        }
    };

    public MQEditToolbar(Context context) {
        this(context, null);
    }

    public MQEditToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MQEditToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.mq_edit_toolbar, this);
        initView();
        setListener();
        processLogic();
    }

    private void initView() {
        mEmotionLl = getViewById(R.id.ll_edit_toolbar_emotion);
        mEmotionVp = getViewById(R.id.vp_edit_toolbar_emotion);
        mIndicatorLl = getViewById(R.id.ll_edit_toolbar_indicator);

        mVoiceArl = getViewById(R.id.arl_edit_toolbar_audio);
    }

    private void setListener() {
        mEmotionVp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mIndicatorIvList.size(); i++) {
                    mIndicatorIvList.get(i).setEnabled(false);
                }
                mIndicatorIvList.get(position).setEnabled(true);
            }
        });

        mVoiceArl.setCallback(new MQAudioRecorderLayout.Callback() {
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


    private void processLogic() {
        mIndicatorIvList = new ArrayList<>();
        mGridViewList = new ArrayList<>();

        int emotionPageCount = (MQEmotionUtil.sEmotionKeyArr.length - 1) / EMOTION_PAGE_SIZE + 1;

        ImageView indicatorIv;
        LinearLayout.LayoutParams indicatorIvLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = MQUtils.dip2px(getContext(), 5);
        indicatorIvLp.setMargins(margin, margin, margin, margin);
        for (int i = 0; i < emotionPageCount; i++) {
            indicatorIv = new ImageView(getContext());
            indicatorIv.setLayoutParams(indicatorIvLp);
            indicatorIv.setImageResource(R.drawable.mq_selector_emotion_indicator);
            indicatorIv.setEnabled(false);
            mIndicatorIvList.add(indicatorIv);
            mIndicatorLl.addView(indicatorIv);

            mGridViewList.add(getGridView(i));
        }
        mIndicatorIvList.get(0).setEnabled(true);
        mEmotionVp.setAdapter(new EmotionPagerAdapter());
    }

    private GridView getGridView(int position) {
        GridView gridView = new GridView(getContext());
        gridView.setNumColumns(EMOTION_COLUMN);
        gridView.setVerticalSpacing(MQUtils.dip2px(getContext(), 5));
        gridView.setHorizontalSpacing(MQUtils.dip2px(getContext(), 5));
        gridView.setOverScrollMode(OVER_SCROLL_NEVER);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setVerticalFadingEdgeEnabled(false);
        gridView.setHorizontalScrollBarEnabled(false);
        gridView.setHorizontalFadingEdgeEnabled(false);
        gridView.setSelector(android.R.color.transparent);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mContentEt != null) {
                    EmotionAdapter adapter = (EmotionAdapter) parent.getAdapter();
                    if (position == adapter.getCount() - 1) {
                        // 删除
                        mContentEt.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    } else {
                        // 插入表情
                        insertText(adapter.getItem(position));
                    }
                }
            }
        });

        int start = position * EMOTION_PAGE_SIZE;
        List<String> tempEmotionList = Arrays.asList(Arrays.copyOfRange(MQEmotionUtil.sEmotionKeyArr, start, start + EMOTION_PAGE_SIZE));
        List<String> emotionList = new ArrayList<>();
        emotionList.addAll(tempEmotionList);
        emotionList.add("");

        gridView.setAdapter(new EmotionAdapter(emotionList));
        return gridView;
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
        mHandler.sendEmptyMessageDelayed(WHAT_CHANGE_TO_VOICE_KEYBOARD, MQUtils.KEYBOARD_CHANGE_DELAY);
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
        mHandler.sendEmptyMessageDelayed(WHAT_CHANGE_TO_EMOTION_KEYBOARD, MQUtils.KEYBOARD_CHANGE_DELAY);
    }

    /**
     * 切换到系统原始键盘
     */
    public void changeToOriginalKeyboard() {
        closeCustomKeyboard();
        MQUtils.openKeyboard(getContext(), mContentEt);
        // 打开系统键盘也延时了的，这里延时2倍再滚动到底部
        mHandler.sendEmptyMessageDelayed(WHAT_SCROLL_CONTENT_TO_BOTTOM, MQUtils.KEYBOARD_CHANGE_DELAY * 2);
    }

    /**
     * 显示表情键盘
     */
    private void showEmotionKeyboard() {
        mEmotionLl.setVisibility(VISIBLE);
        sendScrollContentToBottomMsg();
    }

    /**
     * 显示语音键盘
     */
    private void showVoiceKeyboard() {
        mVoiceArl.setVisibility(VISIBLE);
        sendScrollContentToBottomMsg();
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
        mEmotionLl.setVisibility(GONE);
    }

    /**
     * 关闭语音键盘
     */
    public void closeVoiceKeyboard() {
        mVoiceArl.setVisibility(GONE);
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
        return mEmotionLl.getVisibility() == View.VISIBLE;
    }

    /**
     * 语音键盘是否可见
     *
     * @return
     */
    public boolean isVoiceKeyboardVisible() {
        return mVoiceArl.getVisibility() == View.VISIBLE;
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
     * 在当前光标位置插入文本
     *
     * @param text
     */
    public void insertText(String text) {
        int cursorPosition = mContentEt.getSelectionStart();
        StringBuilder sb = new StringBuilder(mContentEt.getText());
        sb.insert(cursorPosition, text);
        mContentEt.setText(MQEmotionUtil.getEmotionText(getContext(), sb.toString()));
        mContentEt.setSelection(cursorPosition + text.length());
    }

    /**
     * 初始化，必须调用该方法
     *
     * @param activity
     * @param contentEt
     */
    public void init(Activity activity, EditText contentEt, Callback callback) {
        if (activity == null || contentEt == null || callback == null) {
            throw new RuntimeException(MQEditToolbar.class.getSimpleName() + "的init方法的参数均不能为null");
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
        return mVoiceArl.isRecording();
    }

    @Override
    public void onClick(View v) {
    }

    class EmotionPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mGridViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mGridViewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mGridViewList.get(position));
            return mGridViewList.get(position);
        }
    }

    class EmotionAdapter extends BaseAdapter {
        private List<String> mDatas;

        public EmotionAdapter(List<String> datas) {
            mDatas = datas;
        }

        @Override
        public int getCount() {
            return mDatas == null ? 0 : mDatas.size();
        }

        @Override
        public String getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.mq_item_emotion, null);
            }

            ImageView iconIv = (ImageView) convertView;
            if (position == getCount() - 1) {
                iconIv.setImageResource(R.drawable.mq_emoji_delete);
                iconIv.setVisibility(VISIBLE);
            } else {
                String key = mDatas.get(position);
                if (TextUtils.isEmpty(key)) {
                    iconIv.setVisibility(INVISIBLE);
                } else {
                    iconIv.setImageResource(MQEmotionUtil.getImgByName(key));
                    iconIv.setVisibility(VISIBLE);
                }
            }

            return convertView;
        }
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