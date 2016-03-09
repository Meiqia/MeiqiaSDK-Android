package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.SimpleCallback;
import com.meiqia.meiqiasdk.dialog.MQLoadingDialog;
import com.meiqia.meiqiasdk.model.CustomInfoModel;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQFormInputLayout;
import com.meiqia.meiqiasdk.widget.MQSquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/23 上午10:44
 * 描述:留言表单界面
 */
public class MQMessageFormActivity extends Activity implements View.OnClickListener {
    /**
     * 最大的图片张数
     */
    private static final int MAX_PHOTO_COUNT = 3;
    /**
     * 选择照片的请求码
     */
    private static final int REQUEST_CODE_PHOTO = 1;

    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private TextView mSubmitTv;

    private TextView mMessageTipTv;
    private LinearLayout mInputContainerLl;
    private MQFormInputLayout mMessageFil;

    private MQSquareImageView mPictureOneSiv;
    private ImageView mDeleteOneIv;
    private RelativeLayout mPictureTwoRl;
    private MQSquareImageView mPictureTwoSiv;
    private ImageView mDeleteTwoIv;
    private RelativeLayout mPictureThreeRl;
    private MQSquareImageView mPictureThreeSiv;
    private ImageView mDeleteThreeIv;

    private ArrayList<MQFormInputLayout> mFormInputLayouts;

    private ArrayList<String> mPictures = new ArrayList<>();

    private ImageSize mImageSize;

    private MQLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        processLogic(savedInstanceState);
    }

    private void initView() {
        setContentView(R.layout.mq_activity_message_form);
        mTitleRl = (RelativeLayout) findViewById(R.id.title_rl);
        mBackRl = (RelativeLayout) findViewById(R.id.back_rl);
        mBackTv = (TextView) findViewById(R.id.back_tv);
        mBackIv = (ImageView) findViewById(R.id.back_iv);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mSubmitTv = (TextView) findViewById(R.id.submit_tv);

        mMessageTipTv = (TextView) findViewById(R.id.message_tip_tv);
        mInputContainerLl = (LinearLayout) findViewById(R.id.input_container_ll);
        mMessageFil = (MQFormInputLayout) findViewById(R.id.message_fil);

        mPictureOneSiv = (MQSquareImageView) findViewById(R.id.picture_one_siv);
        mDeleteOneIv = (ImageView) findViewById(R.id.delete_one_iv);

        mPictureTwoRl = (RelativeLayout) findViewById(R.id.picture_two_rl);
        mPictureTwoSiv = (MQSquareImageView) findViewById(R.id.picture_two_siv);
        mDeleteTwoIv = (ImageView) findViewById(R.id.delete_two_iv);

        mPictureThreeRl = (RelativeLayout) findViewById(R.id.picture_three_rl);
        mPictureThreeSiv = (MQSquareImageView) findViewById(R.id.picture_three_siv);
        mDeleteThreeIv = (ImageView) findViewById(R.id.delete_three_iv);
    }

    private void initListener() {
        mBackRl.setOnClickListener(this);
        mSubmitTv.setOnClickListener(this);

        mPictureOneSiv.setOnClickListener(this);
        mPictureTwoSiv.setOnClickListener(this);
        mPictureThreeSiv.setOnClickListener(this);

        mDeleteOneIv.setOnClickListener(this);
        mDeleteTwoIv.setOnClickListener(this);
        mDeleteThreeIv.setOnClickListener(this);
    }

    private void processLogic(Bundle savedInstanceState) {
        int size = MQUtils.getScreenWidth(getApplicationContext()) / 10;
        mImageSize = new ImageSize(size, size);

        applyCustomUIConfig();

        addCustomInfoFil();

        MQUtils.initImageLoader(this);

        handlePictureCount();

        handleLeaveMessageTemplete();
    }

    /**
     * 刷新离线消息模板
     */
    private void handleLeaveMessageTemplete() {
        refreshLeaveMessageTemplete();
        MQConfig.getController(this).refreshEnterpriseConfig(new SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
            }

            @Override
            public void onSuccess() {
                refreshLeaveMessageTemplete();
            }
        });
    }

    /**
     * 刷新离线消息模板
     */
    private void refreshLeaveMessageTemplete() {
        String leaveMessageTemplete = MQConfig.getController(this).getLeaveMessageTemplete();
        if (TextUtils.isEmpty(leaveMessageTemplete)) {
            mMessageTipTv.setVisibility(View.GONE);
        } else {
            mMessageTipTv.setText(leaveMessageTemplete);
            mMessageTipTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 如果配置了界面相关的 config，在这里应用
     */
    private void applyCustomUIConfig() {
        if (MQConfig.DEFAULT != MQConfig.ui.backArrowIconResId) {
            mBackIv.setImageResource(MQConfig.ui.backArrowIconResId);
        }

        // 处理标题栏背景色
        MQUtils.applyCustomUITintDrawable(mTitleRl, android.R.color.white, R.color.mq_activity_title_bg, MQConfig.ui.titleBackgroundResId);

        // 处理标题、返回、返回箭头颜色
        MQUtils.applyCustomUITextAndImageColor(R.color.mq_activity_title_textColor, MQConfig.ui.titleTextColorResId, mBackIv, mBackTv, mTitleTv, mSubmitTv);

        // 处理标题文本的对其方式
        MQUtils.applyCustomUITitleGravity(mBackTv, mTitleTv);
    }

    private void addCustomInfoFil() {
        mFormInputLayouts = new ArrayList<>();
        if (MQConfig.customInfoModels != null) {
            for (CustomInfoModel customInfoModel : MQConfig.customInfoModels) {
                MQFormInputLayout formInputLayout = new MQFormInputLayout(this);

                formInputLayout.setTip(customInfoModel.tip);
                formInputLayout.setHint(customInfoModel.hint);
                if (customInfoModel.inputType != 0) {
                    formInputLayout.setInputType(customInfoModel.inputType);
                }
                if (customInfoModel.required) {
                    formInputLayout.setRequired();
                }
                if (customInfoModel.singleLine) {
                    formInputLayout.setSingleLine();
                }

                mInputContainerLl.addView(formInputLayout, mInputContainerLl.getChildCount() - 2);
                mFormInputLayouts.add(formInputLayout);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_rl) {
            finish();
        } else if (v.getId() == R.id.submit_tv) {
            submit();
        } else if (v.getId() == R.id.delete_one_iv) {
            mPictures.remove(0);
            handlePictureCount();
        } else if (v.getId() == R.id.delete_two_iv) {
            mPictures.remove(1);
            handlePictureCount();
        } else if (v.getId() == R.id.delete_three_iv) {
            mPictures.remove(2);
            handlePictureCount();
        } else if (v.getId() == R.id.picture_one_siv) {
            if (mPictures.size() == 0) {
                choosePicture();
            } else {
                startActivity(MQPhotoPreviewActivity.newIntent(this, null, mPictures, 0));
            }
        } else if (v.getId() == R.id.picture_two_siv) {
            if (mPictures.size() == 1) {
                choosePicture();
            } else {
                startActivity(MQPhotoPreviewActivity.newIntent(this, null, mPictures, 1));
            }
        } else if (v.getId() == R.id.picture_three_siv) {
            if (mPictures.size() == 2) {
                choosePicture();
            } else {
                startActivity(MQPhotoPreviewActivity.newIntent(this, null, mPictures, 2));
            }
        }
    }

    private void choosePicture() {
        startActivityForResult(MQPhotoPickerActivity.newIntent(this, MQUtils.getImageDir(this), MAX_PHOTO_COUNT, mPictures, getString(R.string.mq_confirm)), REQUEST_CODE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PHOTO) {
                // 从 相册 获取的图片
                mPictures = MQPhotoPickerActivity.getSelectedImages(data);
                handlePictureCount();
            }
        }
    }

    private void handlePictureCount() {
        if (mPictures.size() == 0) {
            changeToZeroPicture();
        } else if (mPictures.size() == 1) {
            changeToOnePicture();
        } else if (mPictures.size() == 2) {
            changeToTwoPicture();
        } else if (mPictures.size() == 3) {
            changeToThreePicture();
        }
    }

    private void changeToZeroPicture() {
        mPictureTwoRl.setVisibility(View.INVISIBLE);
        mPictureThreeRl.setVisibility(View.INVISIBLE);

        mDeleteOneIv.setVisibility(View.INVISIBLE);
        mDeleteTwoIv.setVisibility(View.INVISIBLE);
        mDeleteThreeIv.setVisibility(View.INVISIBLE);

        mPictureOneSiv.setImageResource(R.drawable.mq_ic_add_img);
    }

    private void changeToOnePicture() {
        mPictureTwoRl.setVisibility(View.VISIBLE);
        mPictureThreeRl.setVisibility(View.INVISIBLE);

        mDeleteOneIv.setVisibility(View.VISIBLE);
        mDeleteTwoIv.setVisibility(View.INVISIBLE);
        mDeleteThreeIv.setVisibility(View.INVISIBLE);

        ImageLoader.getInstance().displayImage("file://" + mPictures.get(0), new ImageViewAware(mPictureOneSiv), null, mImageSize, null, null);
        mPictureTwoSiv.setImageResource(R.drawable.mq_ic_add_img);
    }

    private void changeToTwoPicture() {
        mPictureTwoRl.setVisibility(View.VISIBLE);
        mPictureThreeRl.setVisibility(View.VISIBLE);

        mDeleteOneIv.setVisibility(View.VISIBLE);
        mDeleteTwoIv.setVisibility(View.VISIBLE);
        mDeleteThreeIv.setVisibility(View.INVISIBLE);

        ImageLoader.getInstance().displayImage("file://" + mPictures.get(0), new ImageViewAware(mPictureOneSiv), null, mImageSize, null, null);
        ImageLoader.getInstance().displayImage("file://" + mPictures.get(1), new ImageViewAware(mPictureTwoSiv), null, mImageSize, null, null);
        mPictureThreeSiv.setImageResource(R.drawable.mq_ic_add_img);
    }

    private void changeToThreePicture() {
        mPictureTwoRl.setVisibility(View.VISIBLE);
        mPictureThreeRl.setVisibility(View.VISIBLE);

        mDeleteOneIv.setVisibility(View.VISIBLE);
        mDeleteTwoIv.setVisibility(View.VISIBLE);
        mDeleteThreeIv.setVisibility(View.VISIBLE);

        ImageLoader.getInstance().displayImage("file://" + mPictures.get(0), new ImageViewAware(mPictureOneSiv), null, mImageSize, null, null);
        ImageLoader.getInstance().displayImage("file://" + mPictures.get(1), new ImageViewAware(mPictureTwoSiv), null, mImageSize, null, null);
        ImageLoader.getInstance().displayImage("file://" + mPictures.get(2), new ImageViewAware(mPictureThreeSiv), null, mImageSize, null, null);
    }

    private void submit() {
        String message = mMessageFil.getText();
        if (TextUtils.isEmpty(message)) {
            MQUtils.show(this, getString(R.string.mq_param_not_allow_empty, getString(R.string.mq_title_leave_msg)));
            return;
        }

        Map<String, String> customInfoMap = new HashMap();
        if (MQConfig.customInfoModels != null) {
            int len = MQConfig.customInfoModels.size();
            CustomInfoModel customInfoModel;
            for (int i = 0; i < len; i++) {
                customInfoModel = MQConfig.customInfoModels.get(i);
                String value = mFormInputLayouts.get(i).getText();
                if (customInfoModel.required && TextUtils.isEmpty(value)) {
                    MQUtils.show(this, getString(R.string.mq_param_not_allow_empty, customInfoModel.tip));
                    return;
                }
                customInfoMap.put(customInfoModel.key, value);
            }
        }

        final long submitTimeMillis = System.currentTimeMillis();

        showLoadingDialog();

        MQConfig.getController(this).submitMessageForm(message, mPictures, customInfoMap, new SimpleCallback() {
            @Override
            public void onFailure(int code, final String message) {
                if (System.currentTimeMillis() - submitTimeMillis < 1500) {
                    MQUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            MQUtils.show(getApplicationContext(), message);
                        }
                    }, System.currentTimeMillis() - submitTimeMillis);
                } else {
                    dismissLoadingDialog();
                    MQUtils.show(getApplicationContext(), message);
                }
            }

            @Override
            public void onSuccess() {
                if (System.currentTimeMillis() - submitTimeMillis < 1500) {
                    MQUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            MQUtils.show(getApplicationContext(), R.string.mq_submit_leave_msg_success);
                            finish();
                        }
                    }, System.currentTimeMillis() - submitTimeMillis);
                } else {
                    dismissLoadingDialog();
                    MQUtils.show(getApplicationContext(), R.string.mq_submit_leave_msg_success);
                    finish();
                }
            }
        });
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new MQLoadingDialog(this);
            mLoadingDialog.setCancelable(false);
        }
        mLoadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
