package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewCompat;
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
import com.meiqia.meiqiasdk.model.ImageFolderModel;
import com.meiqia.meiqiasdk.pw.MQPhotoFolderPw;
import com.meiqia.meiqiasdk.util.MQImageCaptureManager;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 下午5:36
 * 描述:图片选择界面
 */
public class MQPhotoPickerActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
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


        MQUtils.initImageLoader(this);

        mPicAdapter = new PicAdapter();
        mPicAdapter.setSelectedImages(getIntent().getStringArrayListExtra(EXTRA_SELECTED_IMAGES));
        mContentGv.setAdapter(mPicAdapter);

        renderTopRightBtn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadDatas();
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
        if (mCurrentImageFolderModel.isTakePhotoEnabled() && position == 0) {
            if (mPicAdapter.getSelectedCount() == mMaxChooseCount) {
                toastMaxCountTip();
            } else {
                takePhoto();
            }
        } else {
            int currentPosition = position;
            if (mCurrentImageFolderModel.isTakePhotoEnabled()) {
                currentPosition--;
            }
            startActivityForResult(MQPhotoPickerPreviewActivity.newIntent(this, mMaxChooseCount, mPicAdapter.getSelectedImages(), mPicAdapter.getDatas(), currentPosition, mTopRightBtnText, false), REQUEST_CODE_PREVIEW);
        }
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
                // 更新图库
                mImageCaptureManager.refreshGallery();

                String photoPath = mImageCaptureManager.getCurrentPhotoPath();
                mPicAdapter.getSelectedImages().add(photoPath);
                mPicAdapter.getDatas().add(0, photoPath);
                renderTopRightBtn();

                startActivityForResult(MQPhotoPickerPreviewActivity.newIntent(this, mMaxChooseCount, mPicAdapter.getSelectedImages(), mPicAdapter.getDatas(), 0, mTopRightBtnText, true), REQUEST_CODE_PREVIEW);
            } else if (requestCode == REQUEST_CODE_PREVIEW) {
                returnSelectedImages(MQPhotoPickerPreviewActivity.getSelectedImages(data));
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_CODE_PREVIEW) {
            mPicAdapter.setSelectedImages(MQPhotoPickerPreviewActivity.getSelectedImages(data));
            renderTopRightBtn();
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

    private void loadDatas() {
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        ImageFolderModel allImageFolderModel = new ImageFolderModel(mTakePhotoEnabled);

        HashMap<String, ImageFolderModel> imageFolderModelMap = new HashMap<>();
        ImageFolderModel otherImageFolderModel = null;
        if (cursor != null && cursor.getCount() > 0) {
            boolean firstInto = true;
            while (cursor.moveToNext()) {
                String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                if (firstInto) {
                    allImageFolderModel.name = getString(R.string.mq_all_image);
                    allImageFolderModel.coverPath = imagePath;
                    firstInto = false;
                }
                // 所有图片目录每次都添加
                allImageFolderModel.addLastImage(imagePath);

                // 其他图片目录
                String folderName = new File(imagePath).getParentFile().getName();
                if (imageFolderModelMap.containsKey(folderName)) {
                    otherImageFolderModel = imageFolderModelMap.get(folderName);
                } else {
                    otherImageFolderModel = new ImageFolderModel(folderName, imagePath);
                    imageFolderModelMap.put(folderName, otherImageFolderModel);
                }
                otherImageFolderModel.addLastImage(imagePath);
            }
            cursor.close();
        }

        mImageFolderModels = new ArrayList();
        // 添加所有图片目录
        mImageFolderModels.add(allImageFolderModel);

        // 添加其他图片目录
        Iterator<Map.Entry<String, ImageFolderModel>> iterator = imageFolderModelMap.entrySet().iterator();
        while (iterator.hasNext()) {
            mImageFolderModels.add(iterator.next().getValue());
        }

        reloadPhotos(mPhotoFolderPw == null ? 0 : mPhotoFolderPw.getCurrentPosition());
    }

    private void reloadPhotos(int position) {
        mCurrentImageFolderModel = mImageFolderModels.get(position);
        mTitleTv.setText(mCurrentImageFolderModel.name);
        mPicAdapter.setDatas(mCurrentImageFolderModel.getImages());
    }

    private class PicAdapter extends BaseAdapter {
        private ArrayList<String> mSelectedImages = new ArrayList<>();
        private ArrayList<String> mDatas;
        private ImageSize mImageSize;

        public PicAdapter() {
            mDatas = new ArrayList<>();
            int size = MQUtils.getScreenWidth(getApplicationContext()) / 10;
            mImageSize = new ImageSize(size, size);
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
                picViewHolder.photoIv = (ImageView) convertView.findViewById(R.id.photo_iv);
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
                ImageLoader.getInstance().displayImage("drawable://" + R.drawable.mq_ic_gallery_camera, picViewHolder.photoIv);

                picViewHolder.flagIv.setVisibility(View.INVISIBLE);

                picViewHolder.photoIv.setColorFilter(null);
            } else {
                picViewHolder.tipTv.setVisibility(View.INVISIBLE);
                picViewHolder.photoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.getInstance().displayImage("file://" + imagePath, new ImageViewAware(picViewHolder.photoIv), null, mImageSize, null, null);

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
                    String image = getItem(position);
                    if (!mSelectedImages.contains(image) && getSelectedCount() == mMaxChooseCount) {
                        toastMaxCountTip();
                    } else {
                        if (mSelectedImages.contains(image)) {
                            mSelectedImages.remove(image);
                        } else {
                            mSelectedImages.add(image);
                        }
                        notifyDataSetChanged();

                        renderTopRightBtn();
                    }
                }
            });
        }

        public void setDatas(ArrayList<String> datas) {
            if (datas != null) {
                mDatas = datas;
            } else {
                mDatas.clear();
            }
            notifyDataSetChanged();
        }

        public ArrayList<String> getDatas() {
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
        public ImageView photoIv;
        public TextView tipTv;
        public ImageView flagIv;
    }
}