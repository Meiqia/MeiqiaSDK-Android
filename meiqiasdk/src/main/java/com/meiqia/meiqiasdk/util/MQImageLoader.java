package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.view.View;

import com.meiqia.meiqiasdk.widget.MQImageView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/3/28 下午3:33
 * 描述:
 */
public interface MQImageLoader {

    void displayImage(MQImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, MQDisplayImageListener displayImageListener);

    void downloadImage(Context context, String path, MQDownloadImageListener downloadImageListener);

    interface MQDisplayImageListener {
        void onSuccess(View view, String path);
    }

    interface MQDownloadImageListener {
        void onSuccess(String path, Bitmap bitmap);
        void onFailed(String path);
    }
}
