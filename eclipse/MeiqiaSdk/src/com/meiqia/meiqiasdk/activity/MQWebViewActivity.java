package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;

/**
 * OnePiece
 * Created by xukq on 6/24/16.
 */
public class MQWebViewActivity extends Activity implements View.OnClickListener {

    public static final String CONTENT = "content";

    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mq_activity_webview);

        findViews();
        setListeners();
        applyCustomUIConfig();
        logic();
    }

    private void findViews() {
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mBackRl = (RelativeLayout) findViewById(R.id.back_rl);
        mBackTv = (TextView) findViewById(R.id.back_tv);
        mBackIv = (ImageView) findViewById(R.id.back_iv);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mWebView = (WebView) findViewById(R.id.webview);
    }

    private void setListeners() {
        mBackRl.setOnClickListener(this);
    }

    private void applyCustomUIConfig() {
        if (MQConfig.DEFAULT != MQConfig.ui.backArrowIconResId) {
            mBackIv.setImageResource(MQConfig.ui.backArrowIconResId);
        }

        // 处理标题栏背景色
        MQUtils.applyCustomUITintDrawable(mTitleRl, android.R.color.white, R.color.mq_activity_title_bg, MQConfig.ui.titleBackgroundResId);

        // 处理标题、返回、返回箭头颜色
        MQUtils.applyCustomUITextAndImageColor(R.color.mq_activity_title_textColor, MQConfig.ui.titleTextColorResId, mBackIv, mBackTv, mTitleTv);

        // 处理标题文本的对其方式
        MQUtils.applyCustomUITitleGravity(mBackTv, mTitleTv);
    }

    private void logic() {
        if (getIntent() != null) {
            String data = getIntent().getStringExtra(CONTENT);
            mWebView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_rl) {
            onBackPressed();
        }
    }
}
