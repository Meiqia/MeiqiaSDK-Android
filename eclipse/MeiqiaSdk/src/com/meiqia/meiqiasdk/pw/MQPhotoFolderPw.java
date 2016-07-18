package com.meiqia.meiqiasdk.pw;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.model.ImageFolderModel;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/26 下午4:00
 * 描述:图片选择界面中的图片目录选择窗口
 */
public class MQPhotoFolderPw extends MQBasePopupWindow implements AdapterView.OnItemClickListener {
    public static final int ANIM_DURATION = 300;
    private LinearLayout mRootLl;
    private ListView mContentLv;
    private FolderAdapter mFolderAdapter;
    private Callback mCallback;
    private int mCurrentPosition;

    public MQPhotoFolderPw(Activity activity, View anchorView, Callback callback) {
        super(activity, R.layout.mq_pw_photo_folder, anchorView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mCallback = callback;
    }

    @Override
    protected void initView() {
        mRootLl = getViewById(R.id.root_ll);
        mContentLv = getViewById(R.id.content_lv);
    }

    @Override
    protected void setListener() {
        mRootLl.setOnClickListener(this);
        mContentLv.setOnItemClickListener(this);
    }

    @Override
    protected void processLogic() {
        setAnimationStyle(android.R.style.Animation);
        setBackgroundDrawable(new ColorDrawable(0x90000000));

        mFolderAdapter = new FolderAdapter();
        mContentLv.setAdapter(mFolderAdapter);
    }

    /**
     * 设置目录数据集合
     *
     * @param datas
     */
    public void setDatas(ArrayList<ImageFolderModel> datas) {
        mFolderAdapter.setDatas(datas);
    }

    @Override
    public void show() {
        showAsDropDown(mAnchorView);
        ViewCompat.animate(mContentLv).translationY(-mWindowRootView.getHeight()).setDuration(0).start();
        ViewCompat.animate(mContentLv).translationY(0).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(ANIM_DURATION).start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mCallback != null && mCurrentPosition != position) {
            mCallback.onSelectedFolder(position);
        }
        mCurrentPosition = position;
        dismiss();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.root_ll) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        ViewCompat.animate(mContentLv).translationY(-mWindowRootView.getHeight()).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(ANIM_DURATION).start();

        if (mCallback != null) {
            mCallback.executeDismissAnim();
        }

        mContentLv.postDelayed(new Runnable() {
            @Override
            public void run() {
                MQPhotoFolderPw.super.dismiss();
            }
        }, ANIM_DURATION);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    private class FolderAdapter extends BaseAdapter {
        private List<ImageFolderModel> mDatas;
        private int mImageWidth;
        private int mImageHeight;

        public FolderAdapter() {
            mDatas = new ArrayList<>();
            mImageWidth = MQUtils.getScreenWidth(mActivity) / 10;
            mImageHeight = mImageWidth;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public ImageFolderModel getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FolderViewHolder folderViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mq_item_photo_folder, parent, false);
                folderViewHolder = new FolderViewHolder();
                folderViewHolder.photoIv = (MQImageView) convertView.findViewById(R.id.photo_iv);
                folderViewHolder.nameTv = (TextView) convertView.findViewById(R.id.name_tv);
                folderViewHolder.countTv = (TextView) convertView.findViewById(R.id.count_tv);
                convertView.setTag(folderViewHolder);
            } else {
                folderViewHolder = (FolderViewHolder) convertView.getTag();
            }

            ImageFolderModel imageFolderModel = getItem(position);
            folderViewHolder.nameTv.setText(imageFolderModel.name);
            folderViewHolder.countTv.setText(String.valueOf(imageFolderModel.getCount()));
            MQImage.displayImage(mActivity, folderViewHolder.photoIv, imageFolderModel.coverPath, R.drawable.mq_ic_holder_light, R.drawable.mq_ic_holder_light, mImageWidth, mImageHeight, null);

            return convertView;
        }

        public void setDatas(ArrayList<ImageFolderModel> datas) {
            if (datas != null) {
                mDatas = datas;
            } else {
                mDatas.clear();
            }
            notifyDataSetChanged();
        }
    }

    private class FolderViewHolder {
        public MQImageView photoIv;
        public TextView nameTv;
        public TextView countTv;
    }

    public interface Callback {
        void onSelectedFolder(int position);

        void executeDismissAnim();
    }
}