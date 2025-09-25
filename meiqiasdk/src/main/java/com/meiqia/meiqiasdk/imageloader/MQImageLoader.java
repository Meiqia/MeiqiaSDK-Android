package com.meiqia.meiqiasdk.imageloader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

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

    protected String getRealFilePath(Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public abstract void displayImage(Activity activity, ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, MQDisplayImageListener displayImageListener);

    @TargetApi(Build.VERSION_CODES.Q)
    protected void displayImage(final Activity activity, final ImageView imageView, final Uri uri, int loadingResId, int failResId, int width, int height, final MQDisplayImageListener displayImageListener) {

    }

    public abstract void downloadImage(Context context, String path, MQDownloadImageListener downloadImageListener);

    public interface MQDisplayImageListener {
        void onSuccess(View view, String path);
    }

    public interface MQDownloadImageListener {
        void onSuccess(String path, Bitmap bitmap);

        void onFailed(String path);
    }
}