package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.chatitem.MQRobotItem;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MQFAQContainer extends LinearLayout {

    private static int ONE_LINE_HEIGHT = 0;
    private int maxPageContentSize = 5;
    private int currentPagePosition = 0;
    private TextView titleTv;
    private LinearLayout tabContainer;
    private HorizontalScrollView tabScrollView;
    private ViewPager contentVp;
    private View nextPageBtn;
    private View prePageBtn;
    private MQRobotItem.Callback mCallback;

    private List<ItemContentLinearLayout> itemContentLinearLayoutList = new ArrayList<>();

    public MQFAQContainer(Context context) {
        super(context);
        init(context);
    }

    public MQFAQContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MQFAQContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setCallback(MQRobotItem.Callback callback) {
        mCallback = callback;
    }

    private void init(Context context) {
        ONE_LINE_HEIGHT = MQUtils.dip2px(context, 27);
        setOrientation(LinearLayout.VERTICAL);
        inflate(context, R.layout.mq_robot_faq_container, this);

        titleTv = findViewById(R.id.mq_title_tv);
        tabScrollView = findViewById(R.id.mq_robot_faq_detail_tab_sv);
        tabContainer = findViewById(R.id.mq_robot_faq_detail_tab_container);
        contentVp = findViewById(R.id.mq_robot_faq_detail_content_vp);
        prePageBtn = findViewById(R.id.mq_pre_page);
        nextPageBtn = findViewById(R.id.mq_next_page);

        prePageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                itemContentLinearLayoutList.get(currentPagePosition).prePage();
                updatePageBtnStatus();
            }
        });
        nextPageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                itemContentLinearLayoutList.get(currentPagePosition).nextPage();
                updatePageBtnStatus();
            }
        });
    }

    public void setTabsAndItems(String title, boolean isShowTab, Map<String, String[]> contentData, int maxPageContentSize) {
        if (contentData == null || contentData.size() == 0) {
            return;
        }
        this.maxPageContentSize = maxPageContentSize;
        titleTv.setText(title);
        currentPagePosition = 0;
        tabContainer.removeAllViews();
        tabScrollView.setVisibility(isShowTab ? VISIBLE : GONE);

        OnClickListener tabOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedTabIndex = tabContainer.indexOfChild(v);
                setTabSelected(selectedTabIndex);
                contentVp.setCurrentItem(selectedTabIndex);
            }
        };
        int padding = MQUtils.dip2px(getContext(), 8);
        for (String tab : contentData.keySet()) {
            MQTabView tabView = new MQTabView(getContext());
            tabView.setText(tab);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.leftMargin = padding;
            params.rightMargin = padding;
            tabView.setOnClickListener(tabOnClickListener);
            tabContainer.addView(tabView, params);

            ItemContentLinearLayout itemContentLinearLayout = new ItemContentLinearLayout(getContext());
            itemContentLinearLayout.setData(contentData.get(tab));
            itemContentLinearLayoutList.add(itemContentLinearLayout);
        }
        setTabSelected(currentPagePosition);
        contentVp.setAdapter(new ItemContentPagerAdapter());
        contentVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // This method will be called when the current page is scrolled
            }

            @Override
            public void onPageSelected(int position) {
                // This method will be called when a new page becomes selected
                setTabSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // This method will be called when the scroll state changes
            }
        });
        ViewGroup.LayoutParams vpLayoutParams = contentVp.getLayoutParams();
        if (!isShowTab) {
            vpLayoutParams.height = ONE_LINE_HEIGHT * Math.min(maxPageContentSize, contentData.get("title").length);
        } else {
            vpLayoutParams.height = ONE_LINE_HEIGHT * maxPageContentSize;
        }
        contentVp.setLayoutParams(vpLayoutParams);
    }

    public void setTabSelected(int position) {
        currentPagePosition = position;
        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            ((MQTabView) tabContainer.getChildAt(i)).setTabSelected(i == position);
        }
        View view = tabContainer.getChildAt(position);
        // if view is not visible, scroll to it
        if (view != null && (view.getLeft() < tabScrollView.getScrollX() || view.getRight() > tabScrollView.getScrollX() + tabScrollView.getWidth())) {
            tabScrollView.smoothScrollTo(view.getLeft(), 0);
        }
        ItemContentLinearLayout itemContentLinearLayout = itemContentLinearLayoutList.get(position);
        itemContentLinearLayout.setPage(0);
        updatePageBtnStatus();
    }

    private void updatePageBtnStatus() {
        ItemContentLinearLayout itemContentLinearLayout = itemContentLinearLayoutList.get(currentPagePosition);
        if (itemContentLinearLayout.hasNexPage()) {
            nextPageBtn.setVisibility(VISIBLE);
        } else {
            nextPageBtn.setVisibility(GONE);
        }
        if (itemContentLinearLayout.hasPrePage()) {
            prePageBtn.setVisibility(VISIBLE);
        } else {
            prePageBtn.setVisibility(GONE);
        }
    }

    private class MQTabView extends LinearLayout {

        private TextView tabTv;
        private View tabLine;

        public MQTabView(Context context) {
            super(context);
            setOrientation(VERTICAL);
            inflate(context, R.layout.mq_item_robot_faq_tab, this);
            tabTv = findViewById(R.id.tab_tv);
            tabLine = findViewById(R.id.tab_line);
        }

        public void setText(String text) {
            tabTv.setText(text);
        }

        public void setTabSelected(boolean isSelected) {
            if (isSelected) {
                tabTv.setTextColor(getResources().getColor(R.color.mq_colorPrimary));
                tabLine.setVisibility(VISIBLE);
            } else {
                tabTv.setTextColor(getResources().getColor(R.color.mq_activity_title_textColor));
                tabLine.setVisibility(INVISIBLE);
            }
        }
    }

    private class ItemContentLinearLayout extends LinearLayout {

        private int page;

        public ItemContentLinearLayout(Context context) {
            super(context);
            setOrientation(VERTICAL);
        }

        public void setData(String[] data) {
            for (int i = 0; i < data.length; i++) {
                TextView item = (TextView) View.inflate(getContext(), R.layout.mq_item_robot_menu_faq, null);
                MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_robot_menu_item_textColor, MQConfig.ui.robotMenuItemTextColorResId, null, item);
                item.setText(data[i]);
                item.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = ((TextView) v).getText().toString();
                        if (mCallback != null) {
                            if (text.indexOf(".") == 1 && text.length() > 2) {
                                mCallback.onClickRobotMenuItem(text.substring(2));
                            } else {
                                mCallback.onClickRobotMenuItem(text);
                            }
                        }
                    }
                });
                addView(item);
            }
            setPage(0);
        }

        public void setPage(int index) {
            page = index;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).setVisibility(i >= page * maxPageContentSize && i < (page + 1) * maxPageContentSize ? VISIBLE : GONE);
            }
        }

        public boolean hasNexPage() {
            return getChildCount() - (page + 1) * maxPageContentSize > 0;
        }

        public boolean hasPrePage() {
            return page > 0;
        }

        public void prePage() {
            if (hasPrePage()) {
                setPage(page - 1);
            }
        }

        public void nextPage() {
            if (hasNexPage()) {
                setPage(page + 1);
            }
        }
    }

    private class ItemContentPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return tabContainer.getChildCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(itemContentLinearLayoutList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(itemContentLinearLayoutList.get(position));
            return itemContentLinearLayoutList.get(position);
        }
    }

}
