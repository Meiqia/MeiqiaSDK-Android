package com.meiqia.meiqiasdk.imageloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/6/28 下午5:58
 * 描述:
 */
public abstract class MQImageLoader {

    protected String getPath(String path) {
        if (path == null) {
            path = "";
        }

        if (!path.startsWith("http") && !path.startsWith("file")) {
            path = "file://" + path;
        }
        return path;
    }

    public abstract void displayImage(Activity activity, ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, MQDisplayImageListener displayImageListener);

    public abstract void downloadImage(Context context, String path, MQDownloadImageListener downloadImageListener);

    public interface MQDisplayImageListener {
        void onSuccess(View view, String path);
    }

    public interface MQDownloadImageListener {
        void onSuccess(String path, Bitmap bitmap);

        void onFailed(String path);
    }
}