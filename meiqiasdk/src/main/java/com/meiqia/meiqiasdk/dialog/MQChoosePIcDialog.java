package com.meiqia.meiqiasdk.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.util.MQUtils;

import java.io.File;

public class MQChoosePicDialog extends Dialog implements View.OnClickListener {
    private MQConversationActivity mConversationActivity;
    private String mCameraPicPath;

    public MQChoosePicDialog(MQConversationActivity conversationActivity) {
        super(conversationActivity, R.style.MQDialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.mq_dialog_choose_pic);
        findViewById(R.id.tv_choose_pic_camera).setOnClickListener(this);
        findViewById(R.id.tv_choose_pic_gallery).setOnClickListener(this);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
        mConversationActivity = conversationActivity;
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.tv_choose_pic_camera) {
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(MQUtils.getPicStorePath(mConversationActivity));
            file.mkdirs();
            String path = MQUtils.getPicStorePath(mConversationActivity) + "/" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(path);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            mCameraPicPath = path;
            try {
                mConversationActivity.startActivityForResult(camera, MQConversationActivity.REQUEST_CODE_CAMERA);
            } catch (Exception e) {
                MQUtils.show(mConversationActivity, R.string.mq_photo_not_support);
            }
        } else if (v.getId() == R.id.tv_choose_pic_gallery) {
            try {
                mConversationActivity.startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), MQConversationActivity.REQUEST_CODE_PHOTO);
            } catch (Exception e) {
                MQUtils.show(mConversationActivity, R.string.mq_photo_not_support);
            }
        }
    }

    public File getCameraPicFile() {
        String sdState = Environment.getExternalStorageState();
        if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        File imageFile = new File(mCameraPicPath);
        if (imageFile.exists()) {
            return imageFile;
        } else {
            return null;
        }
    }
}