package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.ImageFolderModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/7/10 下午9:46
 * 描述:
 */
public class MQLoadPhotoTask extends MQAsyncTask<Void, ArrayList<ImageFolderModel>> {
    private Context mContext;
    private boolean mTakePhotoEnabled;

    public MQLoadPhotoTask(Callback<ArrayList<ImageFolderModel>> callback, Context context, boolean takePhotoEnabled) {
        super(callback);
        mContext = context.getApplicationContext();
        mTakePhotoEnabled = takePhotoEnabled;
    }

    private static boolean isImageFile(String path) {
        // 获取图片的宽和高，但不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
//        return options.outWidth > 0 && options.outHeight > 0;
        return options.outMimeType != null;
    }

    @Override
    protected ArrayList<ImageFolderModel> doInBackground(Void... voids) {
        ArrayList<ImageFolderModel> imageFolderModels = new ArrayList();
        ImageFolderModel allImageFolderModel = new ImageFolderModel(mTakePhotoEnabled);
        HashMap<String, ImageFolderModel> imageFolderModelMap = new HashMap<>();

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            ImageFolderModel otherImageFolderModel;
            if (cursor != null && cursor.getCount() > 0) {
                boolean firstInto = true;
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    if (!TextUtils.isEmpty(imagePath) && isImageFile(imagePath)) {
                        if (firstInto) {
                            allImageFolderModel.name = mContext.getString(R.string.mq_all_image);
                            allImageFolderModel.coverPath = imagePath;
                            firstInto = false;
                        }
                        // 所有图片目录每次都添加
                        allImageFolderModel.addLastImage(imagePath);

                        String folderPath = null;
                        // 其他图片目录
                        File folder = new File(imagePath).getParentFile();
                        if (folder != null) {
                            folderPath = folder.getAbsolutePath();
                        }

                        if (TextUtils.isEmpty(folderPath)) {
                            int end = imagePath.lastIndexOf(File.separator);
                            if (end != -1) {
                                folderPath = imagePath.substring(0, end);
                            }
                        }

                        if (!TextUtils.isEmpty(folderPath)) {
                            if (imageFolderModelMap.containsKey(folderPath)) {
                                otherImageFolderModel = imageFolderModelMap.get(folderPath);
                            } else {
                                String folderName = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);
                                if (TextUtils.isEmpty(folderName)) {
                                    folderName = "/";
                                }
                                otherImageFolderModel = new ImageFolderModel(folderName, imagePath);
                                imageFolderModelMap.put(folderPath, otherImageFolderModel);
                            }
                            otherImageFolderModel.addLastImage(imagePath);
                        }
                    }
                }

                // 添加所有图片目录
                imageFolderModels.add(allImageFolderModel);

                // 添加其他图片目录
                Iterator<Map.Entry<String, ImageFolderModel>> iterator = imageFolderModelMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    imageFolderModels.add(iterator.next().getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imageFolderModels;
    }

    public MQLoadPhotoTask perform() {
        if (Build.VERSION.SDK_INT >= 11) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            execute();
        }
        return this;
    }
}