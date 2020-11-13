package com.meiqia.meiqiasdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.model.VideoMessage;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;

public class MQChatVideoItem extends MQBaseCustomCompositeView {

    private ImageView picIv;
    protected int mImageWidth;
    protected int mImageHeight;

    public MQChatVideoItem(Context context) {
        super(context);
    }

    public MQChatVideoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MQChatVideoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_video_layout;
    }

    @Override
    protected void initView() {
        picIv = findViewById(R.id.content_pic);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic() {
        int screenWidth = MQUtils.getScreenWidth(getContext());

        mImageWidth = screenWidth / 3;
        mImageHeight = mImageWidth;
    }

    public void setVideoMessage(final VideoMessage videoMessage) {
        MQImage.displayImage((Activity) getContext(), picIv, videoMessage.getThumbUrl(), R.drawable.mq_ic_holder_white, R.drawable.mq_ic_holder_white, mImageWidth, mImageHeight, null);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Uri uri = Uri.parse(videoMessage.getUrl());
                    //调用系统自带的播放器
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "video/mp4");
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.mq_title_unknown_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
