package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.imageloader.MQImageLoader;
import com.meiqia.meiqiasdk.third.photoview.PhotoViewAttacher;
import com.meiqia.meiqiasdk.util.MQAsyncTask;
import com.meiqia.meiqiasdk.util.MQBrowserPhotoViewAttacher;
import com.meiqia.meiqiasdk.util.MQSavePhotoTask;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQHackyViewPager;
import com.meiqia.meiqiasdk.widget.MQImageView;

import android.Manifest;

import java.io.File;
import java.util.ArrayList;


public class MQPhotoPreviewActivity extends Activity implements PhotoViewAttacher.OnViewTapListener, View.OnClickListener, MQAsyncTask.Callback<Void> {
    private static final String EXTRA_SAVE_IMG_DIR = "EXTRA_SAVE_IMG_DIR";
    private static final String EXTRA_PREVIEW_IMAGES = "EXTRA_PREVIEW_IMAGES";
    private static final String EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION";
    private static final String EXTRA_IS_SINGLE_PREVIEW = "EXTRA_IS_SINGLE_PREVIEW";
    private static final String EXTRA_PHOTO_PATH = "EXTRA_PHOTO_PATH";


    private static final int REQUEST_CODE_SAVE_IMAGE_STORAGE_PERMISSION = 0x301;

    private RelativeLayout mTitleRl;
    private TextView mTitleTv;
    private ImageView mDownloadIv;
    private MQHackyViewPager mContentHvp;
    private View mRequestPermTopView;

    private ArrayList<String> mPreviewImages;
    private boolean mIsSinglePreview;

    private File mSaveImgDir;

    private boolean mIsHidden = false;
    private MQSavePhotoTask mSavePhotoTask;

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
        MQUtils.updateLanguage(this);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    private void initView() {
        setContentView(R.layout.mq_activity_photo_preview);
        mTitleRl = findViewById(R.id.title_rl);
        mTitleTv = findViewById(R.id.title_tv);
        mDownloadIv = findViewById(R.id.download_iv);
        mContentHvp = findViewById(R.id.content_hvp);
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
        // 被回收，就直接退出
        if (getIntent() == null) {
            finish();
        }

        mSaveImgDir = (File) getIntent().getSerializableExtra(EXTRA_SAVE_IMG_DIR);
        if (mSaveImgDir == null) {
            mDownloadIv.setVisibility(View.INVISIBLE);
        }

        mPreviewImages = getIntent().getStringArrayListExtra(EXTRA_PREVIEW_IMAGES);
        if (mPreviewImages == null) {
            mPreviewImages = new ArrayList<>();
        }

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
            if (mSavePhotoTask == null) {
                savePic();
            }
        }
    }

    private synchronized void savePic() {
        if (mSavePhotoTask != null) {
            return;
        }

        // Android 10 以下，保存到公共相册需要存储权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                addRequestPermissionTopTip();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_SAVE_IMAGE_STORAGE_PERMISSION);
                return;
            }
        }

        final String url = mPreviewImages.get(mContentHvp.getCurrentItem());
        File file;
        if (url.startsWith("file")) {
            file = new File(url.replace("file://", ""));
            if (file.exists()) {
                MQUtils.showSafe(MQPhotoPreviewActivity.this, getString(R.string.mq_save_img_success_folder, file.getParentFile().getAbsolutePath()));
                return;
            }
        }

        // 通过MD5加密url生成文件名，避免多次保存同一张图片
        final String fileName = MQUtils.stringToMD5(url) + ".png";
        file = new File(mSaveImgDir, fileName);
        if (file.exists()) {
            MQUtils.showSafe(this, getString(R.string.mq_save_img_success_folder, mSaveImgDir.getAbsolutePath()));
            return;
        }

        mSavePhotoTask = new MQSavePhotoTask(this, this, file);
        MQImage.downloadImage(this, url, new MQImageLoader.MQDownloadImageListener() {
            @Override
            public void onSuccess(String url, Bitmap bitmap) {
                if (mSavePhotoTask != null) {
                    mSavePhotoTask.setBitmapAndPerform(bitmap);
                }
            }

            @Override
            public void onFailed(String url) {
                mSavePhotoTask = null;
                MQUtils.showSafe(MQPhotoPreviewActivity.this, R.string.mq_save_img_failure);
            }
        });
    }

    @Override
    public void onPostExecute(Void aVoid) {
        mSavePhotoTask = null;
    }

    @Override
    public void onTaskCancelled() {
        mSavePhotoTask = null;
    }

    @Override
    protected void onDestroy() {
        if (mSavePhotoTask != null) {
            mSavePhotoTask.cancelTask();
            mSavePhotoTask = null;
        }
        super.onDestroy();
    }

    /**
     * 顶部展示一条存储权限申请说明的卡片
     */
    private void addRequestPermissionTopTip() {
        try {
            if (mRequestPermTopView == null) {
                mRequestPermTopView = getLayoutInflater().inflate(R.layout.mq_request_storage_top_pop_tip, null);
                TextView contentTv = mRequestPermTopView.findViewById(R.id.content_tv);
                contentTv.setText(R.string.mq_content_request_storage_permission);
                ViewGroup root = findViewById(android.R.id.content);
                root.addView(mRequestPermTopView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SAVE_IMAGE_STORAGE_PERMISSION) {
            // 无论同意或拒绝，先移除顶部权限提示卡片
            if (mRequestPermTopView != null) {
                try {
                    ViewGroup root = findViewById(android.R.id.content);
                    root.removeView(mRequestPermTopView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mRequestPermTopView = null;
            }

            if (grantResults != null && grantResults.length > 0
                    && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // 用户同意后，重新尝试保存
                savePic();
            } else {
                // 拒绝后，不再继续保存，只给个失败提示即可
                MQUtils.showSafe(this, R.string.mq_save_img_failure);
            }
        }
    }

    private class ImagePageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mPreviewImages.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final MQImageView imageView = new MQImageView(container.getContext());
            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            final MQBrowserPhotoViewAttacher photoViewAttacher = new MQBrowserPhotoViewAttacher(imageView);
            photoViewAttacher.setOnViewTapListener(MQPhotoPreviewActivity.this);

            imageView.setDrawableChangedCallback(new MQImageView.OnDrawableChangedCallback() {
                @Override
                public void onDrawableChanged(Drawable drawable) {
                    if (drawable != null && drawable.getIntrinsicHeight() > drawable.getIntrinsicWidth() && drawable.getIntrinsicHeight() > MQUtils.getScreenHeight(imageView.getContext())) {
                        photoViewAttacher.setIsSetTopCrop(true);
                        photoViewAttacher.setUpdateBaseMatrix();
                    } else {
                        photoViewAttacher.update();
                    }
                }
            });

            MQImage.displayImage(MQPhotoPreviewActivity.this, imageView, mPreviewImages.get(position), R.drawable.mq_ic_holder_dark, R.drawable.mq_ic_holder_dark, MQUtils.getScreenWidth(MQPhotoPreviewActivity.this), MQUtils.getScreenHeight(MQPhotoPreviewActivity.this), null);

            return imageView;
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
