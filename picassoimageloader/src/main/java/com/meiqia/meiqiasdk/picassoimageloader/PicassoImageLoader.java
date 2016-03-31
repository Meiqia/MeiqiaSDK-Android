package com.meiqia.meiqiasdk.picassoimageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.meiqia.meiqiasdk.util.MQImageLoader;
import com.meiqia.meiqiasdk.widget.MQImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/3/29 下午2:03
 * 描述:
 */
public class PicassoImageLoader implements MQImageLoader {

    @Override
    public void displayImage(final MQImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final MQDisplayImageListener displayImageListener) {
        if (path == null) {
            path = "";
        }

        if (!path.startsWith("http") && !path.startsWith("file")) {
            path = "file://" + path;
        }

        final String finalPath = path;
        Picasso.with(imageView.getContext()).load(finalPath).placeholder(loadingResId).error(failResId).resize(width, height).centerInside().into(imageView, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                if (displayImageListener != null) {
                    displayImageListener.onSuccess(imageView, finalPath);
                }
            }
        });
    }

    @Override
    public void downloadImage(Context context, String path, final MQDownloadImageListener downloadImageListener) {
        if (!path.startsWith("http") && !path.startsWith("file")) {
            path = "file://" + path;
        }

        final String finalPath = path;
        Picasso.with(context).load(finalPath).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (downloadImageListener != null) {
                    downloadImageListener.onSuccess(finalPath, bitmap);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (downloadImageListener != null) {
                    downloadImageListener.onFailed(finalPath);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }
}
