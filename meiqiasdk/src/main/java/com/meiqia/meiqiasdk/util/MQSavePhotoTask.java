package com.meiqia.meiqiasdk.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.meiqia.meiqiasdk.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/6/7 下午2:28
 * 描述:
 */
public class MQSavePhotoTask extends MQAsyncTask<Void, Void> {
    private Context mContext;
    private SoftReference<Bitmap> mBitmap;
    private File mNewFile;

    public MQSavePhotoTask(Callback<Void> callback, Context context, File newFile) {
        super(callback);
        mContext = context.getApplicationContext();
        mNewFile = newFile;
    }

    public void setBitmapAndPerform(Bitmap bitmap) {
        mBitmap = new SoftReference<>(bitmap);

        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Void doInBackground(Void... params) {
        FileOutputStream fos = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".png");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                values.put(MediaStore.Video.Media.IS_PENDING, 1);
                ContentResolver contentResolver = mContext.getContentResolver();

                InputStream is = null;
                OutputStream os = null;
                try {
                    Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    os = contentResolver.openOutputStream(imageUri);
                    mBitmap.get().compress(Bitmap.CompressFormat.PNG, 100, os);
                    values.clear();
                    values.put(MediaStore.Video.Media.IS_PENDING, 0);
                    contentResolver.update(imageUri, values, null, null);
                    if (os == null) {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (os != null) {
                            os.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                MQUtils.showSafe(mContext, mContext.getString(R.string.mq_save_img_success_folder,  Environment.DIRECTORY_PICTURES));
            } else {
                fos = new FileOutputStream(mNewFile);
                mBitmap.get().compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();

                // 通知图库更新
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mNewFile)));
                MQUtils.showSafe(mContext, mContext.getString(R.string.mq_save_img_success_folder, mNewFile.getParentFile().getAbsolutePath()));
            }
        } catch (Exception e) {
            MQUtils.showSafe(mContext, R.string.mq_save_img_failure);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    MQUtils.showSafe(mContext, R.string.mq_save_img_failure);
                }
            }
            recycleBitmap();
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        recycleBitmap();
    }

    private void recycleBitmap() {
        if (mBitmap != null && mBitmap.get() != null && !mBitmap.get().isRecycled()) {
            mBitmap.get().recycle();
            mBitmap = null;
        }
    }
}
