package com.meiqia.meiqiasdk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/12/22 下午4:30
 * 描述:
 */
public class MQViewPhotoDialog extends Dialog {
    private ImageView mPhotoIv;
    private ImageLoader mImageLoader;

    public MQViewPhotoDialog(Context context) {
        super(context, R.style.MQViewPhotoDialog);
        setContentView(R.layout.mq_dialog_view_photo);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, MQUtils.getScreenHeight(context) - MQUtils.getStatusBarHeight(context));
        mPhotoIv = (ImageView) findViewById(R.id.iv_view_photo);
        mImageLoader = ImageLoader.getInstance();
    }

    public void show(String url) {
        mImageLoader.displayImage(url, mPhotoIv, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                PhotoViewAttacher mAttacher = new PhotoViewAttacher(mPhotoIv);
                mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float x, float y) {
                        dismiss();
                    }
                });
            }
        });
        show();
    }
}