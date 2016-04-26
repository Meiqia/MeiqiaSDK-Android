package com.meiqia.meiqiasdk.xutilsimageloader;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.meiqia.meiqiasdk.util.MQImageLoader;
import com.meiqia.meiqiasdk.widget.MQImageView;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/4/11 上午11:08
 * 描述:
 */
public class XUtilsImageLoader implements MQImageLoader {

    @Override
    public void displayImage(final MQImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final MQDisplayImageListener displayImageListener) {
        x.Ext.init((Application) imageView.getContext().getApplicationContext());

        if (path == null) {
            path = "";
        }

        if (!path.startsWith("http") && !path.startsWith("file")) {
            path = "file://" + path;
        }

        ImageOptions options = new ImageOptions.Builder()
                .setLoadingDrawableId(loadingResId)
                .setFailureDrawableId(failResId)
                .setSize(width, height)
                .build();

        final String finalPath = path;
        x.image().bind(imageView, finalPath, options, new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                if (displayImageListener != null) {
                    displayImageListener.onSuccess(imageView, finalPath);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    @Override
    public void downloadImage(Context context, String path, final MQDownloadImageListener downloadImageListener) {
        if (path == null) {
            path = "";
        }

        if (!path.startsWith("http") && !path.startsWith("file")) {
            path = "file://" + path;
        }

        final String finalPath = path;
        x.image().loadDrawable(finalPath, new ImageOptions.Builder().build(), new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                if (downloadImageListener != null) {
                    downloadImageListener.onSuccess(finalPath, ((BitmapDrawable) result).getBitmap());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (downloadImageListener != null) {
                    downloadImageListener.onFailed(finalPath);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

}