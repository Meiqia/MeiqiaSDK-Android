package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MQViewPhotoActivity extends Activity implements PhotoViewAttacher.OnViewTapListener, View.OnClickListener {
    private static final String EXTRA_IMG_URL = "EXTRA_IMG_URL";
    private static final String EXTRA_SAVE_IMG_DIR = "EXTRA_SAVE_IMG_DIR";

    private RelativeLayout mTitleRl;
    private ImageView mPhotoIv;
    private ProgressBar mProgressBar;

    private String mImgUrl;
    private File mSaveImgDir;

    private boolean mIsHidden = false;
    private Bitmap mBitmap;
    private Semaphore mSemaphore;

    /**
     * 获取查看图片的intent
     *
     * @param context
     * @param saveImgDir 保存图片的目录【加该参数是为了在美洽app里复用该界面】
     * @param imgUrl     图片路径
     * @return
     */
    public static Intent newInstance(Context context, File saveImgDir, String imgUrl) {
        Intent intent = new Intent(context, MQViewPhotoActivity.class);
        intent.putExtra(EXTRA_IMG_URL, imgUrl);
        intent.putExtra(EXTRA_SAVE_IMG_DIR, saveImgDir);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    private void initView() {
        setContentView(R.layout.mq_activity_view_photo);
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mPhotoIv = (ImageView) findViewById(R.id.photo_iv);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void initListener() {
        findViewById(R.id.back_iv).setOnClickListener(this);
        findViewById(R.id.download_iv).setOnClickListener(this);
    }

    private void processLogic(Bundle savedInstanceState) {
        mImgUrl = getIntent().getStringExtra(EXTRA_IMG_URL);
        mSaveImgDir = (File) getIntent().getSerializableExtra(EXTRA_SAVE_IMG_DIR);

        mSemaphore = new Semaphore(1);

        loadImage();

        // 过2秒隐藏标题栏
        mTitleRl.postDelayed(new Runnable() {
            @Override
            public void run() {
                hiddenTitlebar();
            }
        }, 2000);
    }

    private void loadImage() {
        // 初始化 ImageLoader
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);

        ImageLoader.getInstance().displayImage(mImgUrl, mPhotoIv, new ImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.GONE);
                PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(mPhotoIv);
                photoViewAttacher.setOnViewTapListener(MQViewPhotoActivity.this);
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
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        if (mIsHidden) {
            showTitlebar();
        } else {
            hiddenTitlebar();
        }
    }

    private void showTitlebar() {
        ViewCompat.animate(mTitleRl).translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        mIsHidden = false;
    }

    private void hiddenTitlebar() {
        ViewCompat.animate(mTitleRl).translationY(-mTitleRl.getHeight()).setInterpolator(new DecelerateInterpolator(2)).start();
        mIsHidden = true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_iv) {
            onBackPressed();
        } else if (v.getId() == R.id.download_iv) {
            if (mBitmap == null) {
                MQUtils.show(this, R.string.mq_download_img_failure);
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
            MQUtils.show(this, R.string.mq_download_img_failure);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    // 通过MD5加密url生成文件名，避免多次保存同一张图片
                    File file = new File(mSaveImgDir, MQUtils.stringToMD5(mImgUrl) + ".png");
                    if (!file.exists()) {
                        fos = new FileOutputStream(file);
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();

                        // 通知图库更新
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    }
                    MQUtils.showSafe(MQViewPhotoActivity.this, getString(R.string.mq_save_img_success, MQUtils.getImageDir(MQViewPhotoActivity.this).getAbsolutePath()));
                } catch (IOException e) {
                    MQUtils.showSafe(MQViewPhotoActivity.this, R.string.mq_save_img_failure);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            MQUtils.showSafe(MQViewPhotoActivity.this, R.string.mq_save_img_failure);
                        }
                    }
                    mSemaphore.release();
                }
            }
        }.start();
    }
}
