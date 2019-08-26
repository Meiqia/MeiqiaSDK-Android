package com.meiqia.meiqiasdk.imageloader;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.widget.ImageView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/6/28 下午6:02
 * 描述:
 */
public class MQImage {
    private static MQImageLoader sImageLoader;

    private MQImage() {
    }

    private static final MQImageLoader getImageLoader() {
        if (sImageLoader == null) {
            synchronized (MQImage.class) {
                if (sImageLoader == null) {
                    if (isClassExists("com.bumptech.glide.Glide")) {
                        sImageLoader = new MQGlideImageLoader4();
                    } else if (isClassExists("com.squareup.picasso.Picasso")) {
                        sImageLoader = new MQPicassoImageLoader();
                    } else if (isClassExists("com.nostra13.universalimageloader.core.ImageLoader")) {
                        sImageLoader = new MQUILImageLoader();
                    } else if (isClassExists("org.xutils.x")) {
                        sImageLoader = new MQXUtilsImageLoader();
                    } else {
                        throw new RuntimeException("必须在你的 build.gradle 文件中配置「Glide、Picasso、universal-image-loader、XUtils3」中的某一个图片加载库的依赖,或者检查是否添加了图库的混淆配置");
                    }
                }
            }
        }
        return sImageLoader;
    }

    /**
     * 设置开发者自定义 ImageLoader
     *
     * @param imageLoader
     */
    public static void setImageLoader(MQImageLoader imageLoader) {
        sImageLoader = imageLoader;
    }

    private static final boolean isClassExists(String classFullName) {
        try {
            Class.forName(classFullName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void displayImage(Activity activity, ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final MQImageLoader.MQDisplayImageListener delegate) {
        try {
            getImageLoader().displayImage(activity, imageView, path, loadingResId, failResId, width, height, delegate);
        } catch (Exception e) {
            Log.d("meiqia", "displayImage exception " + e.toString());
        } catch (Error error) {
            Log.d("meiqia", "displayImage error " + error.toString());
        }
    }

    public static void downloadImage(Context context, String path, final MQImageLoader.MQDownloadImageListener delegate) {
        try {
            getImageLoader().downloadImage(context, path, delegate);
        } catch (Exception e) {
            Log.d("meiqia", "downloadImage exception");
        } catch (Error error) {
            Log.d("meiqia", "displayImage error " + error.toString());
        }
    }
}