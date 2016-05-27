package com.meiqia.meiqiasdk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 上午11:37
 * 描述:自定义组合控件基类
 */
public abstract class MQBaseCustomCompositeView extends RelativeLayout implements View.OnClickListener {

    public MQBaseCustomCompositeView(Context context) {
        this(context, null);
    }

    public MQBaseCustomCompositeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MQBaseCustomCompositeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, getLayoutId(), this);
        initView();
        setListener();
        initAttrs(context, attrs);
        processLogic();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, getAttrs());
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void setListener();

    protected int[] getAttrs() {
        return new int[0];
    }

    protected void initAttr(int attr, TypedArray typedArray) {
    }

    protected abstract void processLogic();

    @Override
    public void onClick(View view) {
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