package com.meiqia.meiqiasdk.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.SimpleCallback;
import com.meiqia.meiqiasdk.dialog.MQLoadingDialog;
import com.meiqia.meiqiasdk.model.MessageFormInputModel;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQImageView;
import com.meiqia.meiqiasdk.widget.MQMessageFormInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/23 上午10:44
 * 描述:留言表单界面
 */
public class MQMessageFormActivity extends Activity implements View.OnClickListener {
    public static final String EXTRA_IS_MIXED_MODE = "EXTRA_IS_MIXED_MODE";
    /**
     * 最大的图片张数
     */
    private static final int MAX_PHOTO_COUNT = 3;
    /**
     * 选择照片的请求码
     */
    private static final int REQUEST_CODE_PHOTO = 1;

    /**
     * 请求外部存储权限的请求码
     */
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 500;

    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private TextView mSubmitTv;

    private TextView mMessageTipTv;
    private LinearLayout mInputContainerLl;

    private MQImageView mPictureOneSiv;
    private ImageView mDeleteOneIv;
    private RelativeLayout mPictureTwoRl;
    private MQImageView mPictureTwoSiv;
    private ImageView mDeleteTwoIv;
    private RelativeLayout mPictureThreeRl;
    private MQImageView mPictureThreeSiv;
    private ImageView mDeleteThreeIv;

    private ArrayList<MessageFormInputModel> mMessageFormInputModels = new ArrayList<>();
    private ArrayList<MQMessageFormInputLayout> mMessageFormInputLayouts = new ArrayList<>();

    private ArrayList<String> mPictures = new ArrayList<>();

    private MQLoadingDialog mLoadingDialog;

    private int mImageSize;

    private boolean mIsMixedMode = false;

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

        mPictureOneSiv = (MQImageView) findViewById(R.id.picture_one_siv);
        mDeleteOneIv = (ImageView) findViewById(R.id.delete_one_iv);

        mPictureTwoRl = (RelativeLayout) findViewById(R.id.picture_two_rl);
        mPictureTwoSiv = (MQImageView) findViewById(R.id.picture_two_siv);
        mDeleteTwoIv = (ImageView) findViewById(R.id.delete_two_iv);

        mPictureThreeRl = (RelativeLayout) findViewById(R.id.picture_three_rl);
        mPictureThreeSiv = (MQImageView) findViewById(R.id.picture_three_siv);
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
        mIsMixedMode = getIntent().getBooleanExtra(EXTRA_IS_MIXED_MODE, false);
        mImageSize = MQUtils.getScreenWidth(getApplicationContext()) / 10;

        applyCustomUIConfig();

        addFormInputLayouts();

        handlePictureCount();

        handleLeaveMessageIntro();
    }

    /**
     * 处理引导文案
     */
    private void handleLeaveMessageIntro() {
        if (TextUtils.isEmpty(MQConfig.leaveMessageIntro)) {
            refreshLeaveMessageIntro();
            MQConfig.getController(this).refreshEnterpriseConfig(new SimpleCallback() {
                @Override
                public void onFailure(int code, String message) {
                }

                @Override
                public void onSuccess() {
                    refreshLeaveMessageIntro();
                }
            });
        } else {
            mMessageTipTv.setText(MQConfig.leaveMessageIntro);
            mMessageTipTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 刷新引导文案
     */
    private void refreshLeaveMessageIntro() {
        String leaveMessageIntro = MQConfig.getController(this).getLeaveMessageIntro();
        if (TextUtils.isEmpty(leaveMessageIntro)) {
            mMessageTipTv.setVisibility(View.GONE);
        } else {
            mMessageTipTv.setText(leaveMessageIntro);
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

    private void addFormInputLayouts() {
        MessageFormInputModel messageMfim = new MessageFormInputModel();
        messageMfim.tip = getString(R.string.mq_leave_msg);
        messageMfim.required = true;
        messageMfim.hint = getString(R.string.mq_leave_msg_hint);
        messageMfim.inputType = InputType.TYPE_CLASS_TEXT;
        messageMfim.singleLine = false;
        mMessageFormInputModels.add(messageMfim);

        if (MQConfig.messageFormInputModels != null && MQConfig.messageFormInputModels.size() > 0) {
            mMessageFormInputModels.addAll(MQConfig.messageFormInputModels);
        } else {
            MessageFormInputModel emailMfim = new MessageFormInputModel();
            emailMfim.tip = getString(R.string.mq_email);
            emailMfim.key = "email";
            emailMfim.required = true;
            emailMfim.hint = getString(R.string.mq_email_hint);
            emailMfim.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            mMessageFormInputModels.add(emailMfim);

            MessageFormInputModel phoneMfim = new MessageFormInputModel();
            phoneMfim.tip = getString(R.string.mq_phone);
            phoneMfim.key = "tel";
            phoneMfim.required = false;
            phoneMfim.hint = getString(R.string.mq_phone_hint);
            phoneMfim.inputType = InputType.TYPE_CLASS_PHONE;
            mMessageFormInputModels.add(phoneMfim);
        }

        for (MessageFormInputModel messageFormInputModel : mMessageFormInputModels) {
            MQMessageFormInputLayout formInputLayout = new MQMessageFormInputLayout(this, messageFormInputModel);
            mInputContainerLl.addView(formInputLayout);
            mMessageFormInputLayouts.add(formInputLayout);
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
                choosePictureWrapper();
            } else {
                startActivity(MQPhotoPreviewActivity.newIntent(this, null, mPictures, 0));
            }
        } else if (v.getId() == R.id.picture_two_siv) {
            if (mPictures.size() == 1) {
                choosePictureWrapper();
            } else {
                startActivity(MQPhotoPreviewActivity.newIntent(this, null, mPictures, 1));
            }
        } else if (v.getId() == R.id.picture_three_siv) {
            if (mPictures.size() == 2) {
                choosePictureWrapper();
            } else {
                startActivity(MQPhotoPreviewActivity.newIntent(this, null, mPictures, 2));
            }
        }
    }

    private void choosePictureWrapper() {
        if (checkStoragePermission()) {
            choosePicture();
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

        MQConfig.getImageLoader(this).displayImage(mPictureOneSiv, mPictures.get(0), R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageSize, mImageSize, null);
        mPictureTwoSiv.setImageResource(R.drawable.mq_ic_add_img);
    }

    private void changeToTwoPicture() {
        mPictureTwoRl.setVisibility(View.VISIBLE);
        mPictureThreeRl.setVisibility(View.VISIBLE);

        mDeleteOneIv.setVisibility(View.VISIBLE);
        mDeleteTwoIv.setVisibility(View.VISIBLE);
        mDeleteThreeIv.setVisibility(View.INVISIBLE);

        MQConfig.getImageLoader(this).displayImage(mPictureOneSiv, mPictures.get(0), R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageSize, mImageSize, null);
        MQConfig.getImageLoader(this).displayImage(mPictureTwoSiv, mPictures.get(1), R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageSize, mImageSize, null);
        mPictureThreeSiv.setImageResource(R.drawable.mq_ic_add_img);
    }

    private void changeToThreePicture() {
        mPictureTwoRl.setVisibility(View.VISIBLE);
        mPictureThreeRl.setVisibility(View.VISIBLE);

        mDeleteOneIv.setVisibility(View.VISIBLE);
        mDeleteTwoIv.setVisibility(View.VISIBLE);
        mDeleteThreeIv.setVisibility(View.VISIBLE);

        MQConfig.getImageLoader(this).displayImage(mPictureOneSiv, mPictures.get(0), R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageSize, mImageSize, null);
        MQConfig.getImageLoader(this).displayImage(mPictureTwoSiv, mPictures.get(1), R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageSize, mImageSize, null);
        MQConfig.getImageLoader(this).displayImage(mPictureThreeSiv, mPictures.get(2), R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageSize, mImageSize, null);
    }

    private void submit() {
        String message = mMessageFormInputLayouts.get(0).getText();
        if (TextUtils.isEmpty(message)) {
            MQUtils.show(this, getString(R.string.mq_param_not_allow_empty, getString(R.string.mq_leave_msg)));
            return;
        }

        Map<String, String> formInputModelMap = new HashMap();
        int len = mMessageFormInputModels.size();
        MessageFormInputModel messageFormInputModel;
        for (int i = 1; i < len; i++) {
            messageFormInputModel = mMessageFormInputModels.get(i);
            String value = mMessageFormInputLayouts.get(i).getText();
            if (messageFormInputModel.required && TextUtils.isEmpty(value)) {
                MQUtils.show(this, getString(R.string.mq_param_not_allow_empty, messageFormInputModel.tip));
                return;
            }
            formInputModelMap.put(messageFormInputModel.key, value);
        }

        final long submitTimeMillis = System.currentTimeMillis();

        showLoadingDialog();

        MQConfig.getController(this).submitMessageForm(message, mPictures, formInputModelMap, new SimpleCallback() {
            @Override
            public void onFailure(final int code, final String message) {
                if (System.currentTimeMillis() - submitTimeMillis < 1500) {
                    MQUtils.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingDialog();
                            if (com.meiqia.meiqiasdk.util.ErrorCode.BLACKLIST == code) {
                                // 产品需求，提交留言表单时，如果用户被拉黑了依然提示提交成功
                                MQUtils.show(getApplicationContext(), R.string.mq_submit_leave_msg_success);
                                finish();
                            } else {
                                MQUtils.show(getApplicationContext(), message);
                            }
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

    /**
     * 检查存储权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePicture();
                } else {
                    MQUtils.show(this, com.meiqia.meiqiasdk.R.string.mq_sdcard_no_permission);
                }
                break;
            }
        }
    }
}
