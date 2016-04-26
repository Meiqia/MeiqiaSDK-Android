package com.meiqia.meiqiasdk.model;

import com.meiqia.core.bean.MQMessage;

/**
 * OnePiece
 * Created by xukq on 3/30/16.
 */
public class FileMessage extends BaseMessage {

    public static final int FILE_STATE_FINISH = 0;
    public static final int FILE_STATE_DOWNLOADING = 1;
    public static final int FILE_STATE_NOT_EXIST = 2;
    public static final int FILE_STATE_FAILED = 3;
    public static final int FILE_STATE_EXPIRED = 4;

    private String localPath;
    private int fileState;
    private int progress;
    private String url;
    private String extra;

    public FileMessage() {
        setItemViewType(TYPE_CLIENT);
        setContentType(MQMessage.TYPE_CONTENT_FILE);
        this.fileState = FILE_STATE_NOT_EXIST;
    }

    public FileMessage(String url) {
        this();
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getFileState() {
        return fileState;
    }

    public void setFileState(int fileState) {
        this.fileState = fileState;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
