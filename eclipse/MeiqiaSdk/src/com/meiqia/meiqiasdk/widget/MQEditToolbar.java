package com.meiqia.meiqiasdk.widget;

import android.app.Activity;
import android.content.Context;
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

    private LinearLayout mEmotionLl;
    private ViewPager mEmotionVp;
    private LinearLayout mIndicatorLl;
    private ArrayList<ImageView> mIndicatorIvList;
    private ArrayList<GridView> mGridViewList;
    private EditText mContentEt;
    private Activity mActivity;

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
    public void toggleKeyboard() {
        if (mContentEt != null) {
            if (isEmotionKeyboardVisible()) {
                changeToOriginalKeyboard();
            } else {
                changeToEmotionKeyboard();
            }
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

        postDelayed(new Runnable() {
            @Override
            public void run() {
                mEmotionLl.setVisibility(VISIBLE);
            }
        }, 300);
    }

    /**
     * 切换到系统原始键盘
     */
    public void changeToOriginalKeyboard() {
        closeEmotionKeyboard();
        MQUtils.openKeyboard(getContext(), mContentEt);
    }

    /**
     * 关闭表情键盘
     */
    public void closeEmotionKeyboard() {

        mEmotionLl.setVisibility(GONE);
    }

    /**
     * 关闭所有键盘
     */
    public void closeAllKeyboard() {
        closeEmotionKeyboard();
        MQUtils.closeKeyboard(mActivity);
    }

    /**
     * @return
     */
    public boolean isEmotionKeyboardVisible() {
        return mEmotionLl.getVisibility() == View.VISIBLE;
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

    public void init(Activity activity, EditText contentEt) {
        mActivity = activity;
        mContentEt = contentEt;
        mContentEt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmotionKeyboardVisible()) {
                    closeEmotionKeyboard();
                }
            }
        });
        mContentEt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    closeAllKeyboard();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
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