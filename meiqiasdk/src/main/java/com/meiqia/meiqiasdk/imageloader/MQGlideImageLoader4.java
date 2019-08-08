package com.meiqia.meiqiasdk.imageloader;

/**
 * OnePiece
 * Created by xukq on 8/7/19.
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.meiqia.meiqiasdk.util.MQUtils;

/**
 * OnePiece
 * Created by xukq on 8/21/17.
 *
 * 针对 glide4.0 的 ImageLoader
 *
 * 用法：启动对话前，通过
 * MQImage.setImageLoader(new MQGlideImageLoader4());
 * 设置自定义 ImageLoader
 */

public class MQGlideImageLoader4 extends MQImageLoader {
    @Override
    public void displayImage(Activity activity, final ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final MQDisplayImageListener displayImageListener) {
        final String finalPath = getPath(path);
        Glide.with(activity)
                .load(finalPath)
                .apply(new RequestOptions().placeholder(loadingResId).error(failResId).override(width, height))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (displayImageListener != null) {
                            displayImageListener.onSuccess(imageView, finalPath);
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    @Override
    public void downloadImage(Context context, String path, final MQDownloadImageListener downloadImageListener) {
        final String finalPath = getPath(path);
        Glide.with(context.getApplicationContext()).load(finalPath).into(new SimpleTarget<Drawable>() {

            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                if (downloadImageListener != null) {
                    downloadImageListener.onSuccess(finalPath, MQUtils.drawableToBitmap(resource));
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (downloadImageListener != null) {
                    downloadImageListener.onFailed(finalPath);
                }
            }
        });
    }
}