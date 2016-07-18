package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.model.ImageFolderModel;
import com.meiqia.meiqiasdk.pw.MQPhotoFolderPw;
import com.meiqia.meiqiasdk.util.MQAsyncTask;
import com.meiqia.meiqiasdk.util.MQImageCaptureManager;
import com.meiqia.meiqiasdk.util.MQLoadPhotoTask;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 下午5:36
 * 描述:图片选择界面
 */
public class MQPhotoPickerActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, MQAsyncTask.Callback<ArrayList<ImageFolderModel>> {
    private static final String EXTRA_IMAGE_DIR = "EXTRA_IMAGE_DIR";
    private static final String EXTRA_SELECTED_IMAGES = "EXTRA_SELECTED_IMAGES";
    private static final String EXTRA_MAX_CHOOSE_COUNT = "EXTRA_MAX_CHOOSE_COUNT";
    private static final String EXTRA_TOP_RIGHT_BTN_TEXT = "EXTRA_TOP_RIGHT_BTN_TEXT";

    /**
     * 拍照的请求码
     */
    private static final int REQUEST_CODE_TAKE_PHOTO = 1;
    /**
     * 预览照片的请求码
     */
    private static final int REQUEST_CODE_PREVIEW = 2;

    private RelativeLayout mTitleRl;
    private TextView mTitleTv;
    private ImageView mArrowIv;
    private TextView mSubmitTv;
    private GridView mContentGv;

    private ImageFolderModel mCurrentImageFolderModel;

    /**
     * 是否可以拍照
     */
    private boolean mTakePhotoEnabled;
    /**
     * 最多选择多少张图片，默认等于1，为单选
     */
    private int mMaxChooseCount = 1;
    /**
     * 右上角按钮文本
     */
    private String mTopRightBtnText;
    /**
     * 图片目录数据集合
     */
    private ArrayList<ImageFolderModel> mImageFolderModels;

    private PicAdapter mPicAdapter;

    private MQImageCaptureManager mImageCaptureManager;

    private MQPhotoFolderPw mPhotoFolderPw;
    /**
     * 上一次显示图片目录的时间戳，防止短时间内重复点击图片目录菜单时界面错乱
     */
    private long mLastShowPhotoFolderTime;
    private MQLoadPhotoTask mLoadPhotoTask;
    private Dialog mLoadingDialog;

    /**
     * @param context         应用程序上下文
     * @param imageDir        拍照后图片保存的目录。如果传null表示没有拍照功能，如果不为null则具有拍照功能，
     * @param maxChooseCount  图片选择张数的最大值
     * @param selectedImages  当前已选中的图片路径集合，可以传null
     * @param topRightBtnText 右上角按钮的文本
     * @return
     */
    public static Intent newIntent(Context context, File imageDir, int maxChooseCount, ArrayList<String> selectedImages, String topRightBtnText) {
        Intent intent = new Intent(context, MQPhotoPickerActivity.class);
        intent.putExtra(EXTRA_IMAGE_DIR, imageDir);
        intent.putExtra(EXTRA_MAX_CHOOSE_COUNT, maxChooseCount);
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, selectedImages);
        intent.putExtra(EXTRA_TOP_RIGHT_BTN_TEXT, topRightBtnText);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    private void initView() {
        setContentView(R.layout.mq_activity_photo_picker);
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mArrowIv = (ImageView) findViewById(R.id.arrow_iv);
        mSubmitTv = (TextView) findViewById(R.id.submit_tv);
        mContentGv = (GridView) findViewById(R.id.content_gv);
    }

    private void initListener() {
        findViewById(R.id.back_iv).setOnClickListener(this);
        findViewById(R.id.folder_ll).setOnClickListener(this);
        mSubmitTv.setOnClickListener(this);
        mContentGv.setOnItemClickListener(this);
    }

    private void processLogic(Bundle savedInstanceState) {
        // 获取拍照图片保存目录
        File imageDir = (File) getIntent().getSerializableExtra(EXTRA_IMAGE_DIR);
        if (imageDir != null) {
            mTakePhotoEnabled = true;
            mImageCaptureManager = new MQImageCaptureManager(this, imageDir);
        }
        // 获取图片选择的最大张数
        mMaxChooseCount = getIntent().getIntExtra(EXTRA_MAX_CHOOSE_COUNT, 1);
        if (mMaxChooseCount < 1) {
            mMaxChooseCount = 1;
        }

        // 获取右上角按钮文本
        mTopRightBtnText = getIntent().getStringExtra(EXTRA_TOP_RIGHT_BTN_TEXT);

        mPicAdapter = new PicAdapter();
        mPicAdapter.setSelectedImages(getIntent().getStringArrayListExtra(EXTRA_SELECTED_IMAGES));
        mContentGv.setAdapter(mPicAdapter);

        renderTopRightBtn();

        mTitleTv.setText(R.string.mq_all_image);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLoadingDialog();
        mLoadPhotoTask = new MQLoadPhotoTask(this, this, mTakePhotoEnabled).perform();
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new Dialog(this, R.style.MQDialog);
            mLoadingDialog.setContentView(R.layout.mq_dialog_loading_photopicker);
            mLoadingDialog.setCancelable(false);
        }
        mLoadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_iv) {
            onBackPressed();
        } else if (v.getId() == R.id.folder_ll && System.currentTimeMillis() - mLastShowPhotoFolderTime > MQPhotoFolderPw.ANIM_DURATION) {
            showPhotoFolderPw();
            mLastShowPhotoFolderTime = System.currentTimeMillis();
        } else if (v.getId() == R.id.submit_tv) {
            returnSelectedImages(mPicAdapter.getSelectedImages());
        }
    }

    /**
     * 返回已选中的图片集合
     *
     * @param selectedImages
     */
    private void returnSelectedImages(ArrayList<String> selectedImages) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, selectedImages);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showPhotoFolderPw() {
        if (mPhotoFolderPw == null) {
            mPhotoFolderPw = new MQPhotoFolderPw(this, mTitleRl, new MQPhotoFolderPw.Callback() {
                @Override
                public void onSelectedFolder(int position) {
                    reloadPhotos(position);
                }

                @Override
                public void executeDismissAnim() {
                    ViewCompat.animate(mArrowIv).setDuration(MQPhotoFolderPw.ANIM_DURATION).rotation(0).start();
                }
            });
        }
        mPhotoFolderPw.setDatas(mImageFolderModels);
        mPhotoFolderPw.show();

        ViewCompat.animate(mArrowIv).setDuration(MQPhotoFolderPw.ANIM_DURATION).rotation(-180).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mMaxChooseCount == 1) {
            // 单选

            if (mCurrentImageFolderModel.isTakePhotoEnabled() && position == 0) {
                takePhoto();
            } else {
                changeToPreview(position);
            }
        } else {
            // 多选

            if (mCurrentImageFolderModel.isTakePhotoEnabled() && position == 0) {
                if (mPicAdapter.getSelectedCount() == mMaxChooseCount) {
                    toastMaxCountTip();
                } else {
                    takePhoto();
                }
            } else {
                changeToPreview(position);
            }
        }
    }

    /**
     * 跳转到图片选择预览界面
     *
     * @param position 当前点击的item的索引位置
     */
    private void changeToPreview(int position) {
        int currentPosition = position;
        if (mCurrentImageFolderModel.isTakePhotoEnabled()) {
            currentPosition--;
        }
        startActivityForResult(MQPhotoPickerPreviewActivity.newIntent(this, mMaxChooseCount, mPicAdapter.getSelectedImages(), mPicAdapter.getData(), currentPosition, mTopRightBtnText, false), REQUEST_CODE_PREVIEW);
    }

    /**
     * 显示只能选择 mMaxChooseCount 张图的提示
     */
    private void toastMaxCountTip() {
        MQUtils.show(this, getString(R.string.mq_toast_photo_picker_max, mMaxChooseCount));
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        try {
            startActivityForResult(mImageCaptureManager.getTakePictureIntent(), REQUEST_CODE_TAKE_PHOTO);
        } catch (Exception e) {
            MQUtils.show(this, R.string.mq_photo_not_support);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                ArrayList<String> photos = new ArrayList<>();
                photos.add(mImageCaptureManager.getCurrentPhotoPath());
                startActivityForResult(MQPhotoPickerPreviewActivity.newIntent(this, 1, photos, photos, 0, mTopRightBtnText, true), REQUEST_CODE_PREVIEW);
            } else if (requestCode == REQUEST_CODE_PREVIEW) {
                if (MQPhotoPickerPreviewActivity.getIsFromTakePhoto(data)) {
                    // 从拍照预览界面返回，刷新图库
                    mImageCaptureManager.refreshGallery();
                }

                returnSelectedImages(MQPhotoPickerPreviewActivity.getSelectedImages(data));
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_CODE_PREVIEW) {
            if (MQPhotoPickerPreviewActivity.getIsFromTakePhoto(data)) {
                // 从拍照预览界面返回，删除之前拍的照片
                mImageCaptureManager.deletePhotoFile();
            } else {
                mPicAdapter.setSelectedImages(MQPhotoPickerPreviewActivity.getSelectedImages(data));
                renderTopRightBtn();
            }
        }
    }

    /**
     * 渲染右上角按钮
     */
    private void renderTopRightBtn() {
        if (mPicAdapter.getSelectedCount() == 0) {
            mSubmitTv.setEnabled(false);
            mSubmitTv.setText(mTopRightBtnText);
        } else {
            mSubmitTv.setEnabled(true);
            mSubmitTv.setText(mTopRightBtnText + "(" + mPicAdapter.getSelectedCount() + "/" + mMaxChooseCount + ")");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTakePhotoEnabled) {
            mImageCaptureManager.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mTakePhotoEnabled) {
            mImageCaptureManager.onRestoreInstanceState(savedInstanceState);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void reloadPhotos(int position) {
        if (position < mImageFolderModels.size()) {
            mCurrentImageFolderModel = mImageFolderModels.get(position);
            mTitleTv.setText(mCurrentImageFolderModel.name);
            mPicAdapter.setData(mCurrentImageFolderModel.getImages());
        }
    }

    @Override
    public void onPostExecute(ArrayList<ImageFolderModel> imageFolderModels) {
        dismissLoadingDialog();
        mLoadPhotoTask = null;
        mImageFolderModels = imageFolderModels;
        reloadPhotos(mPhotoFolderPw == null ? 0 : mPhotoFolderPw.getCurrentPosition());
    }

    @Override
    public void onTaskCancelled() {
        dismissLoadingDialog();
        mLoadPhotoTask = null;
    }

    private void cancelLoadPhotoTask() {
        if (mLoadPhotoTask != null) {
            mLoadPhotoTask.cancelTask();
            mLoadPhotoTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        cancelLoadPhotoTask();

        super.onDestroy();
    }

    private class PicAdapter extends BaseAdapter {
        private ArrayList<String> mSelectedImages = new ArrayList<>();
        private ArrayList<String> mDatas;
        private int mImageWidth;
        private int mImageHeight;

        public PicAdapter() {
            mDatas = new ArrayList<>();
            mImageWidth = MQUtils.getScreenWidth(getApplicationContext()) / 10;
            mImageHeight = mImageWidth;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public String getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // 重用ViewHolder
            PicViewHolder picViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mq_item_square_image, parent, false);
                picViewHolder = new PicViewHolder();
                picViewHolder.photoIv = (MQImageView) convertView.findViewById(R.id.photo_iv);
                picViewHolder.tipTv = (TextView) convertView.findViewById(R.id.tip_tv);
                picViewHolder.flagIv = (ImageView) convertView.findViewById(R.id.flag_iv);
                convertView.setTag(picViewHolder);
            } else {
                picViewHolder = (PicViewHolder) convertView.getTag();
            }

            String imagePath = getItem(position);
            if (mCurrentImageFolderModel.isTakePhotoEnabled() && position == 0) {
                picViewHolder.tipTv.setVisibility(View.VISIBLE);
                picViewHolder.photoIv.setScaleType(ImageView.ScaleType.CENTER);
                picViewHolder.photoIv.setImageResource(R.drawable.mq_ic_gallery_camera);

                picViewHolder.flagIv.setVisibility(View.INVISIBLE);

                picViewHolder.photoIv.setColorFilter(null);
            } else {
                picViewHolder.tipTv.setVisibility(View.INVISIBLE);
                picViewHolder.photoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                MQImage.displayImage(MQPhotoPickerActivity.this, picViewHolder.photoIv, imagePath, R.drawable.mq_ic_holder_dark, R.drawable.mq_ic_holder_dark, mImageWidth, mImageHeight, null);

                picViewHolder.flagIv.setVisibility(View.VISIBLE);

                if (mSelectedImages.contains(imagePath)) {
                    picViewHolder.flagIv.setImageResource(R.drawable.mq_ic_cb_checked);
                    picViewHolder.photoIv.setColorFilter(getResources().getColor(R.color.mq_photo_selected_color));
                } else {
                    picViewHolder.flagIv.setImageResource(R.drawable.mq_ic_cb_normal);
                    picViewHolder.photoIv.setColorFilter(null);
                }

                setFlagClickListener(picViewHolder.flagIv, position);
            }

            return convertView;
        }

        private void setFlagClickListener(ImageView flagIv, final int position) {
            flagIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String currentImage = mPicAdapter.getItem(position);
                    if (mMaxChooseCount == 1) {
                        // 单选

                        if (mPicAdapter.getSelectedCount() > 0) {
                            String selectedImage = mPicAdapter.getSelectedImages().remove(0);
                            if (!TextUtils.equals(selectedImage, currentImage)) {
                                mPicAdapter.getSelectedImages().add(currentImage);
                            }
                        } else {
                            mPicAdapter.getSelectedImages().add(currentImage);
                        }
                        notifyDataSetChanged();
                        renderTopRightBtn();
                    } else {
                        // 多选

                        if (!mPicAdapter.getSelectedImages().contains(currentImage) && mPicAdapter.getSelectedCount() == mMaxChooseCount) {
                            toastMaxCountTip();
                        } else {
                            if (mPicAdapter.getSelectedImages().contains(currentImage)) {
                                mPicAdapter.getSelectedImages().remove(currentImage);
                            } else {
                                mPicAdapter.getSelectedImages().add(currentImage);
                            }
                            notifyDataSetChanged();

                            renderTopRightBtn();
                        }
                    }
                }
            });
        }

        public void setData(ArrayList<String> datas) {
            if (datas != null) {
                mDatas = datas;
            } else {
                mDatas.clear();
            }
            notifyDataSetChanged();
        }

        public ArrayList<String> getData() {
            return mDatas;
        }

        public void setSelectedImages(ArrayList<String> selectedImages) {
            if (selectedImages != null) {
                mSelectedImages = selectedImages;
            }
            notifyDataSetChanged();
        }

        public ArrayList<String> getSelectedImages() {
            return mSelectedImages;
        }

        public int getSelectedCount() {
            return mSelectedImages.size();
        }
    }

    private class PicViewHolder {
        public MQImageView photoIv;
        public TextView tipTv;
        public ImageView flagIv;
    }
}