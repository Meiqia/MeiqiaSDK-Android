package com.meiqia.meiqiasdk.util;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import com.meiqia.meiqiasdk.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/6/7 下午2:28
 * 描述:
 */
public class MQSavePhotoTask extends MQAsyncTask<Void, Void> {
    private Application mApplication;
    private Bitmap mBitmap;
    private File mNewFile;

    public MQSavePhotoTask(Callback<Void> callback, Application application, File newFile) {
        super(callback);
        mApplication = application;
        mNewFile = newFile;
    }

    public void setBitmapAndPerform(Bitmap bitmap) {
        mBitmap = bitmap;

        if (Build.VERSION.SDK_INT >= 11) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            execute();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mNewFile);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();

            // 通知图库更新
            mApplication.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mNewFile)));

            MQUtils.showSafe(mApplication, mApplication.getString(R.string.mq_save_img_success_folder, mNewFile.getParentFile().getAbsolutePath()));
        } catch (Exception e) {
            MQUtils.showSafe(mApplication, R.string.mq_save_img_failure);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    MQUtils.showSafe(mApplication, R.string.mq_save_img_failure);
                }
            }
        }
        return null;
    }
}
