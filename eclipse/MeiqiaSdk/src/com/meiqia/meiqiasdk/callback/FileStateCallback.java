package com.meiqia.meiqiasdk.callback;

import com.meiqia.meiqiasdk.model.FileMessage;

/**
 * OnePiece
 * Created by xukq on 4/21/16.
 */
public interface FileStateCallback {

    void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message);

    void onFileMessageExpired(FileMessage fileMessage);

}
