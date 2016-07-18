package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.util.MQBrowserPhotoViewAttacher;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQHackyViewPager;
import com.meiqia.meiqiasdk.widget.MQImageView;

import java.util.ArrayList;

import com.meiqia.meiqiasdk.third.photoview.PhotoViewAttacher;


/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/3/1 下午5:21
 * 描述:图片选择预览界面
 */
public class MQPhotoPickerPreviewActivity extends Activity implements View.OnClickListener, PhotoViewAttacher.OnViewTapListener {
    private static final String EXTRA_PREVIEW_IMAGES = "EXTRA_PREVIEW_IMAGES";
    private static final String EXTRA_SELECTED_IMAGES = "EXTRA_SELECTED_IMAGES";
    private static final String EXTRA_MAX_CHOOSE_COUNT = "EXTRA_MAX_CHOOSE_COUNT";
    private static final String EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION";
    private static final String EXTRA_TOP_RIGHT_BTN_TEXT = "EXTRA_TOP_RIGHT_BTN_TEXT";
    private static final String EXTRA_IS_FROM_TAKE_PHOTO = "EXTRA_IS_FROM_TAKE_PHOTO";

    private RelativeLayout mTitleRl;
    private TextView mTitleTv;
    private TextView mSubmitTv;
    private MQHackyViewPager mContentHvp;
    private RelativeLayout mChooseRl;
    private TextView mChooseTv;

    private ArrayList<String> mSelectedImages;
    private ArrayList<String> mPreviewImages;
    private int mMaxChooseCount = 1;
    /**
     * 右上角按钮文本
     */
    private String mTopRightBtnText;

    private boolean mIsHidden = false;
    /**
     * 上一次标题栏显示或隐藏的时间戳
     */
    private long mLastShowHiddenTime;
    /**
     * 是否是拍完照后跳转过来
     */
    private boolean mIsFromTakePhoto;

    /**
     * @param context         应用程序上下文
     * @param maxChooseCount  图片选择张数的最大值
     * @param selectedImages  当前已选中的图片路径集合，可以传null
     * @param previewImages   当前预览的图片目录里的图片路径集合
     * @param currentPosition 当前预览图片的位置
     * @param topRightBtnText 右上角按钮的文本
     * @param isFromTakePhoto 是否是拍完照后跳转过来
     * @return
     */
    public static Intent newIntent(Context context, int maxChooseCount, ArrayList<String> selectedImages, ArrayList<String> previewImages, int currentPosition, String topRightBtnText, boolean isFromTakePhoto) {
        Intent intent = new Intent(context, MQPhotoPickerPreviewActivity.class);
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, selectedImages);
        intent.putStringArrayListExtra(EXTRA_PREVIEW_IMAGES, previewImages);
        intent.putExtra(EXTRA_MAX_CHOOSE_COUNT, maxChooseCount);
        intent.putExtra(EXTRA_CURRENT_POSITION, currentPosition);
        intent.putExtra(EXTRA_TOP_RIGHT_BTN_TEXT, topRightBtnText);
        intent.putExtra(EXTRA_IS_FROM_TAKE_PHOTO, isFromTakePhoto);
        return intent;
    }

    /**
     * 获取已选择的图片集合
     *
     * @param intent
     * @return
     */
    public static ArrayList<String> getSelectedImages(Intent intent) {
        return intent.getStringArrayListExtra(EXTRA_SELECTED_IMAGES);
    }

    /**
     * 是否是拍照预览
     *
     * @param intent
     * @return
     */
    public static boolean getIsFromTakePhoto(Intent intent) {
        return intent.getBooleanExtra(EXTRA_IS_FROM_TAKE_PHOTO, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    private void initView() {
        setContentView(R.layout.mq_activity_photo_picker_preview);
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mSubmitTv = (TextView) findViewById(R.id.submit_tv);
        mContentHvp = (MQHackyViewPager) findViewById(R.id.content_hvp);
        mChooseRl = (RelativeLayout) findViewById(R.id.choose_rl);
        mChooseTv = (TextView) findViewById(R.id.choose_tv);
    }

    private void initListener() {
        findViewById(R.id.back_iv).setOnClickListener(this);
        mSubmitTv.setOnClickListener(this);
        mChooseTv.setOnClickListener(this);

        mContentHvp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                handlePageSelectedStatus();
            }
        });
    }

    private void processLogic(Bundle savedInstanceState) {
        // 获取图片选择的最大张数
        mMaxChooseCount = getIntent().getIntExtra(EXTRA_MAX_CHOOSE_COUNT, 1);
        if (mMaxChooseCount < 1) {
            mMaxChooseCount = 1;
        }

        mSelectedImages = getIntent().getStringArrayListExtra(EXTRA_SELECTED_IMAGES);
        mPreviewImages = getIntent().getStringArrayListExtra(EXTRA_PREVIEW_IMAGES);
        if (TextUtils.isEmpty(mPreviewImages.get(0))) {
            // 从MQPhotoPickerActivity跳转过来时，如果有开启拍照功能，则第0项为""
            mPreviewImages.remove(0);
        }

        // 处理是否是拍完照后跳转过来
        mIsFromTakePhoto = getIntent().getBooleanExtra(EXTRA_IS_FROM_TAKE_PHOTO, false);
        if (mIsFromTakePhoto) {
            // 如果是拍完照后跳转过来，一直隐藏底部选择栏
            mChooseRl.setVisibility(View.INVISIBLE);
        }

        // 获取右上角按钮文本
        mTopRightBtnText = getIntent().getStringExtra(EXTRA_TOP_RIGHT_BTN_TEXT);

        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);
        mContentHvp.setAdapter(new ImagePageAdapter());
        mContentHvp.setCurrentItem(currentPosition);

        // 处理第一次进来时指示数字
        handlePageSelectedStatus();
        renderTopRightBtn();

        // 过2秒隐藏标题栏和底部选择栏
        mTitleRl.postDelayed(new Runnable() {
            @Override
            public void run() {
                hiddenTitlebarAndChoosebar();
            }
        }, 2000);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_iv) {
            onBackPressed();
        } else if (v.getId() == R.id.submit_tv) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, mSelectedImages);
            intent.putExtra(EXTRA_IS_FROM_TAKE_PHOTO, mIsFromTakePhoto);
            setResult(RESULT_OK, intent);
            finish();
        } else if (v.getId() == R.id.choose_tv) {
            String currentImage = mPreviewImages.get(mContentHvp.getCurrentItem());
            if (mSelectedImages.contains(currentImage)) {
                mSelectedImages.remove(currentImage);
                mChooseTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mq_ic_cb_normal, 0, 0, 0);
                renderTopRightBtn();
            } else {
                if (mMaxChooseCount == 1) {
                    // 单选

                    mSelectedImages.clear();
                    mSelectedImages.add(currentImage);
                    mChooseTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mq_ic_cb_checked, 0, 0, 0);
                    renderTopRightBtn();
                } else {
                    // 多选

                    if (mMaxChooseCount == mSelectedImages.size()) {
                        MQUtils.show(this, getString(R.string.mq_toast_photo_picker_max, mMaxChooseCount));
                    } else {
                        mSelectedImages.add(currentImage);
                        mChooseTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mq_ic_cb_checked, 0, 0, 0);
                        renderTopRightBtn();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, mSelectedImages);
        intent.putExtra(EXTRA_IS_FROM_TAKE_PHOTO, mIsFromTakePhoto);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void handlePageSelectedStatus() {
        mTitleTv.setText((mContentHvp.getCurrentItem() + 1) + "/" + mPreviewImages.size());
        if (mSelectedImages.contains(mPreviewImages.get(mContentHvp.getCurrentItem()))) {
            mChooseTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mq_ic_cb_checked, 0, 0, 0);
        } else {
            mChooseTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mq_ic_cb_normal, 0, 0, 0);
        }
    }

    /**
     * 渲染右上角按钮
     */
    private void renderTopRightBtn() {
        if (mIsFromTakePhoto) {
            mSubmitTv.setEnabled(true);
            mSubmitTv.setText(mTopRightBtnText);
        } else if (mSelectedImages.size() == 0) {
            mSubmitTv.setEnabled(false);
            mSubmitTv.setText(mTopRightBtnText);
        } else {
            mSubmitTv.setEnabled(true);
            mSubmitTv.setText(mTopRightBtnText + "(" + mSelectedImages.size() + "/" + mMaxChooseCount + ")");
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        if (System.currentTimeMillis() - mLastShowHiddenTime > 500) {
            mLastShowHiddenTime = System.currentTimeMillis();
            if (mIsHidden) {
                showTitlebarAndChoosebar();
            } else {
                hiddenTitlebarAndChoosebar();
            }
        }
    }

    private void showTitlebarAndChoosebar() {
        ViewCompat.animate(mTitleRl).translationY(0).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(View view) {
                mIsHidden = false;
            }
        }).start();

        if (!mIsFromTakePhoto) {
            mChooseRl.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(mChooseRl, 0);
            ViewCompat.animate(mChooseRl).alpha(1).setInterpolator(new DecelerateInterpolator(2)).start();
        }
    }

    private void hiddenTitlebarAndChoosebar() {
        ViewCompat.animate(mTitleRl).translationY(-mTitleRl.getHeight()).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(View view) {
                mIsHidden = true;
                mChooseRl.setVisibility(View.INVISIBLE);
            }
        }).start();

        if (!mIsFromTakePhoto) {
            ViewCompat.animate(mChooseRl).alpha(0).setInterpolator(new DecelerateInterpolator(2)).start();
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
            photoViewAttacher.setOnViewTapListener(MQPhotoPickerPreviewActivity.this);

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

            MQImage.displayImage(MQPhotoPickerPreviewActivity.this, imageView, mPreviewImages.get(position), R.drawable.mq_ic_holder_dark, R.drawable.mq_ic_holder_dark, MQUtils.getScreenWidth(MQPhotoPickerPreviewActivity.this), MQUtils.getScreenHeight(MQPhotoPickerPreviewActivity.this), null);

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
