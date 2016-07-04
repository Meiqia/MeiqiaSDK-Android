package com.meiqia.meiqiasdk.chatitem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.callback.OnDownloadFileCallback;
import com.meiqia.meiqiasdk.model.FileMessage;
import com.meiqia.meiqiasdk.util.ErrorCode;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQTimeUtils;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.CircularProgressBar;
import com.meiqia.meiqiasdk.widget.MQBaseCustomCompositeView;

import org.json.JSONObject;

import java.io.File;

/**
 * OnePiece
 * Created by xukq on 3/30/16.
 */
public class MQChatFileItem extends MQBaseCustomCompositeView implements View.OnTouchListener {

    private CircularProgressBar mProgressBar;
    private TextView mTitleTv;
    private TextView mSubTitleTv;
    private View mRightIv;
    private View root;
    private FileMessage mFileMessage;
    private Callback mCallback;

    private boolean isCancel;

    public MQChatFileItem(Context context) {
        super(context);
    }

    public MQChatFileItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MQChatFileItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_file_layout;
    }

    @Override
    protected void initView() {
        root = findViewById(R.id.root);
        mProgressBar = (CircularProgressBar) findViewById(R.id.progressbar);
        mTitleTv = (TextView) findViewById(R.id.mq_file_title_tv);
        mSubTitleTv = (TextView) findViewById(R.id.mq_file_sub_title_tv);
        mRightIv = findViewById(R.id.mq_right_iv);
    }

    @Override
    protected void setListener() {
        root.setOnClickListener(this);
        mRightIv.setOnClickListener(this);
        mProgressBar.setOnTouchListener(this);
    }

    @Override
    protected void processLogic() {
    }

    @Override
    public void onClick(View view) {
        if (mFileMessage == null) {
            return;
        }

        int id = view.getId();
        if (id == R.id.mq_right_iv) {
            root.performClick();
        } else if (id == R.id.progressbar) {
            cancelDownloading();
        } else if (id == R.id.root) {
            switch (mFileMessage.getFileState()) {
                case FileMessage.FILE_STATE_NOT_EXIST:
                    isCancel = false;
                    mFileMessage.setFileState(FileMessage.FILE_STATE_DOWNLOADING);
                    downloadingState();
                    MQConfig.getController(getContext()).downloadFile(mFileMessage, new OnDownloadFileCallback() {
                        @Override
                        public void onSuccess(File file) {
                            // 取消请求到真正取消请求有一个延迟
                            if (isCancel) {
                                return;
                            }
                            mFileMessage.setFileState(FileMessage.FILE_STATE_FINISH);
                            mCallback.notifyDataSetChanged();
                        }

                        @Override
                        public void onProgress(int progress) {
                            mFileMessage.setProgress(progress);
                            mCallback.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(int code, String message) {
                            if (code == ErrorCode.DOWNLOAD_IS_CANCEL) {
                                // 取消下载 do nothing
                                return;
                            }

                            mFileMessage.setFileState(FileMessage.FILE_STATE_FAILED);
                            downloadFailedState();
                            // 下载失败，删除文件
                            cancelDownloading();
                            mCallback.onFileMessageDownloadFailure(mFileMessage, code, message);
                        }
                    });
                    break;
                case FileMessage.FILE_STATE_FINISH:
                    openFile();
                    break;
                case FileMessage.FILE_STATE_FAILED:
                    mFileMessage.setFileState(FileMessage.FILE_STATE_NOT_EXIST);
                    root.performClick();
                    break;
                case FileMessage.FILE_STATE_EXPIRED:
                    mCallback.onFileMessageExpired(mFileMessage);
                    break;
            }
        }
    }

    public void initFileItem(Callback callback, FileMessage fileMessage) {
        mCallback = callback;
        mFileMessage = fileMessage;
        downloadInitState();
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type = getExtraStringValue("type");
        Uri uri = Uri.fromFile(new File(MQUtils.getFileMessageFilePath(mFileMessage)));
        intent.setDataAndType(uri, type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.mq_no_app_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelDownloading() {
        isCancel = true;
        mFileMessage.setFileState(FileMessage.FILE_STATE_NOT_EXIST);
        MQConfig.getController(getContext()).cancelDownload(mFileMessage.getUrl());
        String filePath = MQUtils.getFileMessageFilePath(mFileMessage);
        MQUtils.delFile(filePath);
        mCallback.notifyDataSetChanged();
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public void downloadInitState() {
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(GONE);
        displayFileInfo();
    }

    public void downloadSuccessState() {
        displayFileInfo();
        mProgressBar.setVisibility(GONE);
        setProgress(100);
        mRightIv.setVisibility(GONE);
    }

    public void downloadFailedState() {
        mProgressBar.setVisibility(GONE);
    }

    public void downloadingState() {
        mSubTitleTv.setText(String.format("%s%s", getSubTitlePrefix(), getResources().getString(R.string.mq_downloading)));
        mRightIv.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);
    }

    private void displayFileInfo() {
        mTitleTv.setText(getExtraStringValue("filename"));
        String endStr;
        String filePath = MQUtils.getFileMessageFilePath(mFileMessage);
        boolean isFileExist = MQUtils.isFileExist(filePath);
        if (isFileExist) {
            endStr = getResources().getString(R.string.mq_download_complete);
            mRightIv.setVisibility(GONE);
        } else {
            String expire_at = getExtraStringValue("expire_at");
            long expireTimeLong = MQTimeUtils.parseTimeToLong(expire_at);
            long diffTime = expireTimeLong - System.currentTimeMillis();
            if (diffTime <= 0) {
                endStr = getResources().getString(R.string.mq_expired);
                mRightIv.setVisibility(GONE);
                mFileMessage.setFileState(FileMessage.FILE_STATE_EXPIRED);
            } else {
                float leaveHours = diffTime / 3600000f;
                String leaveHoursStr = new java.text.DecimalFormat("#.0").format(leaveHours);
                endStr = getContext().getString(R.string.mq_expire_after, leaveHoursStr);
                mRightIv.setVisibility(VISIBLE);
            }
        }
        String subTitle = getSubTitlePrefix() + endStr;
        mSubTitleTv.setText(subTitle);
        mProgressBar.setVisibility(GONE);
    }

    private String getExtraStringValue(String key) {
        String value = "";
        try {
            JSONObject extraJsonObj = new JSONObject(mFileMessage.getExtra());
            value = extraJsonObj.optString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private String getSubTitlePrefix() {
        String size = Formatter.formatShortFileSize(getContext(), getExtraLongValue("size"));
        return size + " · ";
    }

    private long getExtraLongValue(String key) {
        long value = 0;
        try {
            JSONObject extraJsonObj = new JSONObject(mFileMessage.getExtra());
            value = extraJsonObj.optLong(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
            cancelDownloading();
        }
        return false;
    }

    public interface Callback {
        void notifyDataSetChanged();

        void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message);

        void onFileMessageExpired(FileMessage fileMessage);
    }
}
