package com.meiqia.meiqiasdk.model;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;

import java.util.ArrayList;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 下午6:28
 * 描述:
 */
public class ImageFolderModel {
    public String name;
    public String coverPath;
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<Uri> mImageUri = new ArrayList<>();
    private boolean mTakePhotoEnabled;

    public ImageFolderModel(boolean takePhotoEnabled) {
        mTakePhotoEnabled = takePhotoEnabled;
        if (takePhotoEnabled) {
            // 拍照
            mImages.add("");
        }
    }

    public ImageFolderModel(String name, String coverPath) {
        this.name = name;
        this.coverPath = coverPath;
    }

    public boolean isTakePhotoEnabled() {
        return mTakePhotoEnabled;
    }

    public void addLastImage(String imagePath) {
        mImages.add(imagePath);
    }

    public void addLastImageUri(Uri uri) {
        mImageUri.add(uri);
    }

    public ArrayList<String> getImages() {
        return mImages;
    }

    @TargetApi(Build.VERSION_CODES.Q)
    public ArrayList<Uri> getImageUri() {
        return mImageUri;
    }

    public int getCount() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return mImageUri.size();
        }
        return mImages.size();
    }
}