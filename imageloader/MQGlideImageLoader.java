package com.meiqia.meiqiasdk.imageloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.meiqia.meiqiasdk.util.MQUtils;


/**
 * OnePiece
 * Created by xukq on 8/08/19.
 *
 * 针对 glide3.0 的 ImageLoader
 *
 * 用法：启动对话前，通过
 * MQImage.setImageLoader(new MQGlideImageLoader());
 * 设置自定义 ImageLoader
 */
public class MQGlideImageLoader extends MQImageLoader {

    @Override
    public void displayImage(Activity activity, final ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final MQDisplayImageListener listener) {
        final String finalPath = getPath(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri uri = MQUtils.getImageContentUri(activity, path);
            displayImage(activity, imageView, uri, loadingResId, failResId, width, height, listener);
        } else {
            Glide.with(activity).load(finalPath).asBitmap().placeholder(loadingResId).error(failResId).override(width, height).listener(new RequestListener<String, Bitmap>() {
                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    if (listener != null) {
                        listener.onSuccess(imageView, finalPath);
                    }
                    return false;
                }
            }).into(imageView);
        }
    }

    @Override
    protected void displayImage(final Activity activity, final ImageView imageView, final Uri uri, int loadingResId, int failResId, int width, int height, final MQDisplayImageListener displayImageListener) {
        Glide.with(activity).load(uri).asBitmap().placeholder(loadingResId).error(failResId).override(width, height).listener(new RequestListener<Uri, Bitmap>() {
            @Override
            public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (displayImageListener != null) {
                    displayImageListener.onSuccess(imageView, getRealFilePath(activity, uri));
                }
                return false;
            }
        }).into(imageView);
    }

    @Override
    public void downloadImage(Context context, String path, final MQDownloadImageListener listener) {
        final String finalPath = getPath(path);
        Glide.with(context.getApplicationContext()).load(finalPath).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (listener != null) {
                    listener.onSuccess(finalPath, resource);
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                if (listener != null) {
                    listener.onFailed(finalPath);
                }
            }
        });
    }

}