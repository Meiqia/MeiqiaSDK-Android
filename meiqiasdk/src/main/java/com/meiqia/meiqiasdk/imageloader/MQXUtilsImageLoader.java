package com.meiqia.meiqiasdk.imageloader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/6/28 下午6:55
 * 描述:
 */
public class MQXUtilsImageLoader extends MQImageLoader {

    @Override
    public void displayImage(Activity activity, final ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final MQDisplayImageListener listener) {
        x.Ext.init(activity.getApplication());

        ImageOptions options = new ImageOptions.Builder()
                .setLoadingDrawableId(loadingResId)
                .setFailureDrawableId(failResId)
                .setSize(width, height)
                .build();

        final String finalPath = getPath(path);
        x.image().bind(imageView, finalPath, options, new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                if (listener != null) {
                    listener.onSuccess(imageView, finalPath);
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
    public void downloadImage(Context context, String path, final MQDownloadImageListener listener) {
        x.Ext.init((Application) context.getApplicationContext());

        final String finalPath = getPath(path);
        x.image().loadDrawable(finalPath, new ImageOptions.Builder().build(), new Callback.CommonCallback<Drawable>() {
            @Override
            public void onSuccess(Drawable result) {
                if (listener != null) {
                    listener.onSuccess(finalPath, ((BitmapDrawable) result).getBitmap());
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (listener != null) {
                    listener.onFailed(finalPath);
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
