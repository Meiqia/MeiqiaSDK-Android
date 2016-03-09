package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQHackyViewPager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MQPhotoPreviewActivity extends Activity implements PhotoViewAttacher.OnViewTapListener, View.OnClickListener {
    private static final String EXTRA_SAVE_IMG_DIR = "EXTRA_SAVE_IMG_DIR";
    private static final String EXTRA_PREVIEW_IMAGES = "EXTRA_PREVIEW_IMAGES";
    private static final String EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION";
    private static final String EXTRA_IS_SINGLE_PREVIEW = "EXTRA_IS_SINGLE_PREVIEW";
    private static final String EXTRA_PHOTO_PATH = "EXTRA_PHOTO_PATH";


    private RelativeLayout mTitleRl;
    private TextView mTitleTv;
    private ImageView mDownloadIv;
    private MQHackyViewPager mContentHvp;

    private ArrayList<String> mPreviewImages;
    private boolean mIsSinglePreview;

    private File mSaveImgDir;

    private boolean mIsHidden = false;
    private Semaphore mSemaphore;

    /**
     * 上一次标题栏显示或隐藏的时间戳
     */
    private long mLastShowHiddenTime;

    /**
     * 获取查看图片的intent
     *
     * @param context
     * @param saveImgDir      保存图片的目录，如果传null，则没有保存图片功能
     * @param previewImages   当前预览的图片目录里的图片路径集合
     * @param currentPosition 当前预览图片的位置
     * @return
     */
    public static Intent newIntent(Context context, File saveImgDir, ArrayList<String> previewImages, int currentPosition) {
        Intent intent = new Intent(context, MQPhotoPreviewActivity.class);
        intent.putExtra(EXTRA_SAVE_IMG_DIR, saveImgDir);
        intent.putStringArrayListExtra(EXTRA_PREVIEW_IMAGES, previewImages);
        intent.putExtra(EXTRA_CURRENT_POSITION, currentPosition);
        intent.putExtra(EXTRA_IS_SINGLE_PREVIEW, false);
        return intent;
    }

    /**
     * 获取查看图片的intent
     *
     * @param context
     * @param saveImgDir 保存图片的目录，如果传null，则没有保存图片功能
     * @param photoPath  图片路径
     * @return
     */
    public static Intent newIntent(Context context, File saveImgDir, String photoPath) {
        Intent intent = new Intent(context, MQPhotoPreviewActivity.class);
        intent.putExtra(EXTRA_SAVE_IMG_DIR, saveImgDir);
        intent.putExtra(EXTRA_PHOTO_PATH, photoPath);
        intent.putExtra(EXTRA_CURRENT_POSITION, 0);
        intent.putExtra(EXTRA_IS_SINGLE_PREVIEW, true);
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
        setContentView(R.layout.mq_activity_photo_preview);
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mDownloadIv = (ImageView) findViewById(R.id.download_iv);
        mContentHvp = (MQHackyViewPager) findViewById(R.id.content_hvp);
    }

    private void initListener() {
        findViewById(R.id.back_iv).setOnClickListener(this);
        mDownloadIv.setOnClickListener(this);

        mContentHvp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                renderTitleTv();
            }
        });
    }

    private void processLogic(Bundle savedInstanceState) {
        MQUtils.initImageLoader(this);

        mSaveImgDir = (File) getIntent().getSerializableExtra(EXTRA_SAVE_IMG_DIR);
        if (mSaveImgDir == null) {
            mDownloadIv.setVisibility(View.INVISIBLE);
        }

        mPreviewImages = getIntent().getStringArrayListExtra(EXTRA_PREVIEW_IMAGES);

        mIsSinglePreview = getIntent().getBooleanExtra(EXTRA_IS_SINGLE_PREVIEW, false);
        if (mIsSinglePreview) {
            mPreviewImages = new ArrayList<>();
            mPreviewImages.add(getIntent().getStringExtra(EXTRA_PHOTO_PATH));
        }

        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);
        mContentHvp.setAdapter(new ImagePageAdapter());
        mContentHvp.setCurrentItem(currentPosition);


        // 处理第一次进来时指示数字
        renderTitleTv();

        mSemaphore = new Semaphore(1);

        // 过2秒隐藏标题栏
        mTitleRl.postDelayed(new Runnable() {
            @Override
            public void run() {
                hiddenTitlebar();
            }
        }, 2000);
    }

    private void renderTitleTv() {
        if (mIsSinglePreview) {
            mTitleTv.setText(R.string.mq_view_photo);
        } else {
            mTitleTv.setText((mContentHvp.getCurrentItem() + 1) + "/" + mPreviewImages.size());
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        if (System.currentTimeMillis() - mLastShowHiddenTime > 500) {
            mLastShowHiddenTime = System.currentTimeMillis();
            if (mIsHidden) {
                showTitlebar();
            } else {
                hiddenTitlebar();
            }
        }
    }

    private void showTitlebar() {
        ViewCompat.animate(mTitleRl).translationY(0).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(View view) {
                mIsHidden = false;
            }
        }).start();
    }

    private void hiddenTitlebar() {
        ViewCompat.animate(mTitleRl).translationY(-mTitleRl.getHeight()).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(View view) {
                mIsHidden = true;
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_iv) {
            onBackPressed();
        } else if (v.getId() == R.id.download_iv) {
            try {
                mSemaphore.acquire();
                savePic();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void savePic() {
        final String url = mPreviewImages.get(mContentHvp.getCurrentItem());
        File file;
        if (url.startsWith("file")) {
            file = new File(url.replace("file://", ""));
            if (file.exists()) {
                MQUtils.showSafe(MQPhotoPreviewActivity.this, getString(R.string.mq_save_img_success_folder, file.getParentFile().getAbsolutePath()));
                mSemaphore.release();
                return;
            }
        }

        file = new File(mSaveImgDir, MQUtils.stringToMD5(url) + ".png");
        if (file.exists()) {
            MQUtils.showSafe(MQPhotoPreviewActivity.this, getString(R.string.mq_save_img_success_folder, mSaveImgDir.getAbsolutePath()));
            mSemaphore.release();
            return;
        }

        ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                // 图片处理
                new Thread() {
                    @Override
                    public void run() {
                        FileOutputStream fos = null;
                        try {
                            // 通过MD5加密url生成文件名，避免多次保存同一张图片
                            File newFile = new File(mSaveImgDir, MQUtils.stringToMD5(url) + ".png");
                            if (!newFile.exists()) {
                                fos = new FileOutputStream(newFile);
                                loadedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.flush();

                                // 通知图库更新
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)));
                            }
                            MQUtils.showSafe(MQPhotoPreviewActivity.this, getString(R.string.mq_save_img_success_folder, mSaveImgDir.getAbsolutePath()));
                        } catch (IOException e) {
                            MQUtils.showSafe(MQPhotoPreviewActivity.this, R.string.mq_save_img_failure);
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    MQUtils.showSafe(MQPhotoPreviewActivity.this, R.string.mq_save_img_failure);
                                }
                            }
                            mSemaphore.release();
                        }
                    }
                }.start();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                MQUtils.showSafe(MQPhotoPreviewActivity.this, R.string.mq_save_img_failure);
                mSemaphore.release();
            }
        });
    }

    private class ImagePageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPreviewImages.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final PhotoView photoView = new PhotoView(container.getContext());
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            photoView.setOnViewTapListener(MQPhotoPreviewActivity.this);

            String photoPath = mPreviewImages.get(position);
            if (photoPath.startsWith("http") || photoPath.startsWith("file")) {
                ImageLoader.getInstance().displayImage(mPreviewImages.get(position), photoView);
            } else {
                ImageLoader.getInstance().displayImage("file://" + mPreviewImages.get(position), photoView);
            }
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
