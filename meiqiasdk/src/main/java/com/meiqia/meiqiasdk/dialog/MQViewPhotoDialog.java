package com.meiqia.meiqiasdk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/12/22 下午4:30
 * 描述:
 */
public class MQViewPhotoDialog extends Dialog implements PhotoViewAttacher.OnViewTapListener, View.OnClickListener {
    private ImageView mPhotoIv;
    private ImageLoader mImageLoader;
    private String mImgUrl;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap;
    private Semaphore mSemaphore;

    public MQViewPhotoDialog(Context context) {
        super(context, R.style.MQDialog_Nodim);
        setContentView(R.layout.mq_dialog_view_photo);
        getWindow().setWindowAnimations(R.style.MQPhotoDialogAnim);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, MQUtils.getScreenHeight(context) - MQUtils.getStatusBarHeight(context));

        mPhotoIv = (ImageView) findViewById(R.id.iv_view_photo);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mImageLoader = ImageLoader.getInstance();
        mSemaphore = new Semaphore(1);

        findViewById(R.id.btn_save).setOnClickListener(this);
    }

    public void show(String url) {
        mImgUrl = url;
        mImageLoader.displayImage(mImgUrl, mPhotoIv, new ImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.GONE);
                PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(mPhotoIv);
                photoViewAttacher.setOnViewTapListener(MQViewPhotoDialog.this);
                mBitmap = loadedImage;
            }

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
        show();
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        dismiss();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            if (mBitmap == null) {
                MQUtils.show(getContext(), R.string.mq_download_img_failure);
            } else {
                try {
                    mSemaphore.acquire();
                    savePic();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void savePic() {
        if (mBitmap == null) {
            MQUtils.show(getContext(), R.string.mq_download_img_failure);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    // 通过MD5加密url生成文件名，避免多次保存同一张图片
                    File file = new File(MQUtils.getImageDir(getContext()), MQUtils.stringToMD5(mImgUrl) + ".png");
                    if (!file.exists()) {
                        fos = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();

                        // 通知图库更新
                        getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    }
                    MQUtils.showSafe(getContext(), getContext().getString(R.string.mq_save_img_success, MQUtils.getImageDir(getContext()).getAbsolutePath()));
                } catch (IOException e) {
                    MQUtils.showSafe(getContext(), R.string.mq_save_img_failure);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            MQUtils.showSafe(getContext(), R.string.mq_save_img_failure);
                        }
                    }
                    mSemaphore.release();
                }
            }
        }.start();
    }
}