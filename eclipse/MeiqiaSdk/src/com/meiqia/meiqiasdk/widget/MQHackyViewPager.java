package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/3/1 下午6:21
 * 描述:解决PhotoView缩放冲突的ViewPager
 */
public class MQHackyViewPager extends ViewPager {

    public MQHackyViewPager(Context context) {
        super(context);
    }

    public MQHackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}