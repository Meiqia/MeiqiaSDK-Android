package com.meiqia.meiqiasdk.util;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.activity.MQPhotoPreviewActivity;
import com.meiqia.meiqiasdk.model.AgentChangeMessage;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.EvaluateMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.widget.MQCircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.List;

public class MQChatAdapter extends BaseAdapter {

    private static final String TAG = MQChatAdapter.class.getSimpleName();

    private MQConversationActivity mqConversationActivity;
    private List<BaseMessage> mcMessageList;
    private ListView listView;

    // ImageLoader
    private ImageLoader imageLoader;

    private static final int NO_POSITION = -1;
    private int mCurrentPlayingItemPosition = NO_POSITION;
    private int mCurrentDownloadingItemPosition = NO_POSITION;
    private int mMinItemWidth;
    private int mMaxItemWidth;
    private ImageSize mImageSize;

    private Runnable mNotifyDataSetChangedRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public MQChatAdapter(MQConversationActivity mqConversationActivity, List<BaseMessage> mcMessageList, ListView listView) {
        this.mqConversationActivity = mqConversationActivity;
        this.mcMessageList = mcMessageList;
        this.listView = listView;
        this.imageLoader = ImageLoader.getInstance();
        int screenWidth = MQUtils.getScreenWidth(listView.getContext());
        mMaxItemWidth = (int) (screenWidth * 0.5f);
        mMinItemWidth = (int) (screenWidth * 0.18f);
        int size = MQUtils.getScreenWidth(mqConversationActivity) / 4;
        mImageSize = new ImageSize(size, size);
    }

    public void addMQMessage(BaseMessage baseMessage) {
        mcMessageList.add(baseMessage);
        notifyDataSetChanged();
    }

    public void addMQMessage(BaseMessage baseMessage, int location) {
        mcMessageList.add(location, baseMessage);
        notifyDataSetChanged();
    }

    public void loadMoreMessage(List<BaseMessage> baseMessages) {
        mcMessageList.addAll(0, baseMessages);
        notifyDataSetChanged();
        downloadAndNotifyDataSetChanged(baseMessages);
    }

    @Override
    public int getItemViewType(int position) {
        return mcMessageList.get(position).getItemViewType();
    }

    @Override
    public int getViewTypeCount() {
        return BaseMessage.MAX_TYPE;
    }

    @Override
    public int getCount() {
        return mcMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final BaseMessage mcMessage = mcMessageList.get(position);
        ViewHolder viewHolder = null;
        TimeViewHolder timeViewHolder = null;
        TipViewHolder tipViewHolder = null;
        EvaluateViewHolder evaluateViewHolder = null;

        //根据 type 创建不同的 ViewHolder，并缓存
        if (convertView == null) {
            switch (getItemViewType(position)) {
                case BaseMessage.TYPE_AGENT:
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(mqConversationActivity).inflate(R.layout.mq_item_chat_left, null);
                    viewHolder.contentText = (TextView) convertView.findViewById(R.id.content_text);
                    viewHolder.contentImage = (ImageView) convertView.findViewById(R.id.content_pic);
                    viewHolder.voiceContentTv = (TextView) convertView.findViewById(R.id.tv_voice_content);
                    viewHolder.voiceAnimIv = (ImageView) convertView.findViewById(R.id.iv_voice_anim);
                    viewHolder.voiceContainerRl = convertView.findViewById(R.id.rl_voice_container);
                    viewHolder.usAvatar = (MQCircleImageView) convertView.findViewById(R.id.us_avatar_iv);
                    viewHolder.unreadCircle = convertView.findViewById(R.id.unread_view);
                    // tint
                    configChatBubbleBg(viewHolder.contentText, true);
                    configChatBubbleBg(viewHolder.voiceContentTv, true);
                    configChatBubbleTextColor(viewHolder.contentText, true);
                    configChatBubbleTextColor(viewHolder.voiceContentTv, true);
                    convertView.setTag(viewHolder);
                    break;
                case BaseMessage.TYPE_CLIENT:
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(mqConversationActivity).inflate(R.layout.mq_item_chat_right, null);
                    viewHolder.contentText = (TextView) convertView.findViewById(R.id.content_text);
                    viewHolder.contentImage = (ImageView) convertView.findViewById(R.id.content_pic);
                    viewHolder.voiceContentTv = (TextView) convertView.findViewById(R.id.tv_voice_content);
                    viewHolder.voiceAnimIv = (ImageView) convertView.findViewById(R.id.iv_voice_anim);
                    viewHolder.voiceContainerRl = convertView.findViewById(R.id.rl_voice_container);
                    viewHolder.sendingProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
                    viewHolder.sendState = (ImageView) convertView.findViewById(R.id.send_state);
                    // tint
                    configChatBubbleBg(viewHolder.contentText, false);
                    configChatBubbleBg(viewHolder.voiceContentTv, false);
                    configChatBubbleTextColor(viewHolder.contentText, false);
                    configChatBubbleTextColor(viewHolder.voiceContentTv, false);
                    convertView.setTag(viewHolder);
                    break;
                case BaseMessage.TYPE_TIME:
                    timeViewHolder = new TimeViewHolder();
                    convertView = LayoutInflater.from(mqConversationActivity).inflate(R.layout.mq_item_chat_time, null);
                    timeViewHolder.timeTv = (TextView) convertView.findViewById(R.id.timeTv);
                    convertView.setTag(timeViewHolder);
                    break;
                case BaseMessage.TYPE_TIP:
                    tipViewHolder = new TipViewHolder();
                    convertView = LayoutInflater.from(mqConversationActivity).inflate(R.layout.mq_item_msg_tip, null, false);
                    tipViewHolder.contentTv = (TextView) convertView.findViewById(R.id.content_tv);
                    convertView.setTag(tipViewHolder);
                    break;
                case BaseMessage.TYPE_EVALUATE:
                    evaluateViewHolder = new EvaluateViewHolder();
                    convertView = LayoutInflater.from(mqConversationActivity).inflate(R.layout.mq_item_msg_evaluate, null, false);
                    evaluateViewHolder.levelTv = (TextView) convertView.findViewById(R.id.tv_msg_evaluate_level);
                    evaluateViewHolder.levelBg = convertView.findViewById(R.id.view_msg_evaluate_level);
                    evaluateViewHolder.levelImg = (ImageView) convertView.findViewById(R.id.ic_msg_evaluate_level);
                    evaluateViewHolder.contentTv = (TextView) convertView.findViewById(R.id.tv_msg_evaluate_content);
                    convertView.setTag(evaluateViewHolder);
                    break;
            }
        } else {
            switch (getItemViewType(position)) {
                case BaseMessage.TYPE_AGENT:
                    viewHolder = (ViewHolder) convertView.getTag();
                    break;
                case BaseMessage.TYPE_CLIENT:
                    viewHolder = (ViewHolder) convertView.getTag();
                    break;
                case BaseMessage.TYPE_TIME:
                    timeViewHolder = (TimeViewHolder) convertView.getTag();
                    break;
                case BaseMessage.TYPE_TIP:
                    tipViewHolder = (TipViewHolder) convertView.getTag();
                    break;
                case BaseMessage.TYPE_EVALUATE:
                    evaluateViewHolder = (EvaluateViewHolder) convertView.getTag();
                    break;
            }
        }

        //显示对话时间
        if (getItemViewType(position) == BaseMessage.TYPE_TIME) {
            timeViewHolder.timeTv.setText(MQTimeUtils.parseTime(mcMessage.getCreatedOn()));
        }
        //显示对话 Tip
        else if (getItemViewType(position) == BaseMessage.TYPE_TIP) {
            if (mcMessage instanceof AgentChangeMessage) {
                setDirectionMessageContent(mcMessage.getAgentNickname(), tipViewHolder.contentTv);
            } else {
                tipViewHolder.contentTv.setText(R.string.mq_leave_msg_tips);
            }
        }
        // 显示评价消息
        else if (getItemViewType(position) == BaseMessage.TYPE_EVALUATE) {
            handleBindEvaluateItem(evaluateViewHolder, (EvaluateMessage) mcMessage);
        }
        //显示消息：文字、图片、语音
        else if (getItemViewType(position) == BaseMessage.TYPE_AGENT || getItemViewType(position) == BaseMessage.TYPE_CLIENT) {
            // 文字
            if (BaseMessage.TYPE_CONTENT_TEXT.equals(mcMessage.getContentType())) {
                viewHolder.contentText.setVisibility(View.VISIBLE);
                viewHolder.contentImage.setVisibility(View.GONE);
                viewHolder.voiceContainerRl.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(mcMessage.getContent())) {
                    viewHolder.contentText.setText(MQEmotionUtil.getEmotionText(mqConversationActivity, mcMessage.getContent(), 20));
                }

            }
            // 图片
            else if (BaseMessage.TYPE_CONTENT_PHOTO.equals(mcMessage.getContentType())) {
                viewHolder.contentText.setVisibility(View.GONE);
                viewHolder.voiceContainerRl.setVisibility(View.GONE);

                String path = ((PhotoMessage) mcMessage).getLocalPath();
                boolean isLocalImageExist = MQUtils.isFileExist(path);

                String url;
                if (isLocalImageExist) {
                    url = "file://" + ((PhotoMessage) mcMessage).getLocalPath();
                } else {
                    url = ((PhotoMessage) mcMessage).getUrl();
                }
                final ViewHolder finalViewHolder = viewHolder;

                ImageLoader.getInstance().displayImage(url, new ImageViewAware(viewHolder.contentImage), null, mImageSize, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
                        if (loadedImage != null) {
                            if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
                                listView.setSelection(getCount() - 1);
                            }

                            view.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    mqConversationActivity.startActivity(MQPhotoPreviewActivity.newIntent(mqConversationActivity, MQUtils.getImageDir(mqConversationActivity), imageUri));
                                }
                            });
                            finalViewHolder.contentImage.setImageDrawable(MQUtils.getRoundedDrawable(mqConversationActivity, loadedImage, 8f));
                        }
                    }
                }, null);
                viewHolder.contentImage.setVisibility(View.VISIBLE);

            }
            //语音
            else if (BaseMessage.TYPE_CONTENT_VOICE.equals(mcMessage.getContentType())) {
                handleBindVoiceItem(viewHolder, (VoiceMessage) mcMessage, position);
            }
            //显示客服头像
            if (getItemViewType(position) == BaseMessage.TYPE_AGENT) {
                imageLoader.displayImage(mcMessage.getAvatar(), viewHolder.usAvatar);
            }
            //显示发送状态：发送中、发送失败
            else if (getItemViewType(position) == BaseMessage.TYPE_CLIENT) {
                if (viewHolder.sendingProgressBar != null) {
                    if (BaseMessage.STATE_SENDING.equals(mcMessage.getStatus())) {
                        viewHolder.sendingProgressBar.setVisibility(View.VISIBLE);
                        viewHolder.sendState.setVisibility(View.GONE);
                    } else if (BaseMessage.STATE_ARRIVE.equals(mcMessage.getStatus())) {
                        viewHolder.sendingProgressBar.setVisibility(View.GONE);
                        viewHolder.sendState.setVisibility(View.GONE);
                    } else if (BaseMessage.STATE_FAILED.equals(mcMessage.getStatus())) {
                        viewHolder.sendingProgressBar.setVisibility(View.GONE);
                        viewHolder.sendState.setVisibility(View.VISIBLE);
                        viewHolder.sendState.setBackgroundResource(R.drawable.mq_ic_msg_failed);
                        viewHolder.sendState.setOnClickListener(new FailedMessageOnClickListener(mcMessage));
                        viewHolder.sendState.setTag(mcMessage.getId());
                    }
                }
            }
        }

        return convertView;
    }

    private void setDirectionMessageContent(String agentNickName, TextView tipTv) {
        if (agentNickName != null) {
            String text = String.format(tipTv.getResources().getString(R.string.mq_direct_content), agentNickName);
            int start = text.indexOf(agentNickName);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.setSpan(new ForegroundColorSpan(tipTv.getResources().getColor(R.color.mq_chat_direct_agent_nickname_textColor)), start, start + agentNickName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            tipTv.setText(style);
        }
    }

    static class ViewHolder {
        TextView contentText;
        ImageView contentImage;
        TextView voiceContentTv;
        ImageView voiceAnimIv;
        View voiceContainerRl;
        ProgressBar sendingProgressBar;
        ImageView sendState;
        MQCircleImageView usAvatar;
        View unreadCircle;
    }

    static class TimeViewHolder {
        TextView timeTv;
    }

    static class TipViewHolder {
        TextView contentTv;
    }

    static class EvaluateViewHolder {
        TextView levelTv;
        ImageView levelImg;
        View levelBg;
        TextView contentTv;
    }

    private class FailedMessageOnClickListener implements OnClickListener {

        private BaseMessage failedMessage;

        public FailedMessageOnClickListener(BaseMessage failedMessage) {
            this.failedMessage = failedMessage;
        }

        @Override
        public void onClick(View v) {
            if (!MQUtils.isFastClick()) {
                failedMessage.setStatus(BaseMessage.STATE_SENDING);
                notifyDataSetChanged();
                mqConversationActivity.resendMessage(failedMessage);
            }
        }

    }

    /**
     * 处理绑定声音类型的数据item
     *
     * @param evaluateViewHolder
     * @param evaluateMessage
     */
    private void handleBindEvaluateItem(EvaluateViewHolder evaluateViewHolder, EvaluateMessage evaluateMessage) {
        switch (evaluateMessage.getLevel()) {
            case EvaluateMessage.EVALUATE_BAD:
                evaluateViewHolder.levelImg.setImageResource(R.drawable.mq_ic_angry_face);
                evaluateViewHolder.levelTv.setText(R.string.mq_evaluate_bad);
                evaluateViewHolder.levelBg.setBackgroundResource(R.drawable.mq_shape_evaluate_angry);
                break;
            case EvaluateMessage.EVALUATE_MEDIUM:
                evaluateViewHolder.levelImg.setImageResource(R.drawable.mq_ic_neutral_face);
                evaluateViewHolder.levelTv.setText(R.string.mq_evaluate_medium);
                evaluateViewHolder.levelBg.setBackgroundResource(R.drawable.mq_shape_evaluate_neutral);
                break;
            case EvaluateMessage.EVALUATE_GOOD:
                evaluateViewHolder.levelImg.setImageResource(R.drawable.mq_ic_smiling_face);
                evaluateViewHolder.levelTv.setText(R.string.mq_evaluate_good);
                evaluateViewHolder.levelBg.setBackgroundResource(R.drawable.mq_shape_evaluate_smiling);
                break;
        }
        final String context = evaluateMessage.getContent();
        if (!TextUtils.isEmpty(context)) {
            evaluateViewHolder.contentTv.setVisibility(View.VISIBLE);
            evaluateViewHolder.contentTv.setText(context);
        } else {
            evaluateViewHolder.contentTv.setVisibility(View.GONE);
        }


    }

    /**
     * 处理绑定声音类型的数据item
     *
     * @param viewHolder
     * @param voiceMessage
     * @param position
     */
    private void handleBindVoiceItem(ViewHolder viewHolder, final VoiceMessage voiceMessage, final int position) {
        viewHolder.contentText.setVisibility(View.GONE);
        viewHolder.contentImage.setVisibility(View.GONE);
        viewHolder.voiceContainerRl.setVisibility(View.VISIBLE);

        viewHolder.voiceContainerRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickVoiceBtn(voiceMessage, position);
            }
        });

        // 处理录音文本和控件长度
        String duration = voiceMessage.getDuration() == VoiceMessage.NO_DURATION ? "" : voiceMessage.getDuration() + "s";
        viewHolder.voiceContentTv.setText(duration);
        ViewGroup.LayoutParams layoutParams = viewHolder.voiceContainerRl.getLayoutParams();
        if (voiceMessage.getDuration() == VoiceMessage.NO_DURATION) {
            viewHolder.voiceContentTv.setText("");
            layoutParams.width = mMinItemWidth;
        } else {
            viewHolder.voiceContentTv.setText(voiceMessage.getDuration() + "\"");
            layoutParams.width = (int) (mMinItemWidth + (mMaxItemWidth / 60f * voiceMessage.getDuration()));
        }
        viewHolder.voiceContainerRl.setLayoutParams(layoutParams);


        // 刷新录音播放状态
        if (mCurrentPlayingItemPosition != position) {
            if (voiceMessage.getItemViewType() == BaseMessage.TYPE_AGENT) {
                viewHolder.voiceAnimIv.setImageResource(R.drawable.mq_voice_left_normal);
                viewHolder.voiceAnimIv.setColorFilter(mqConversationActivity.getResources().getColor(R.color.mq_chat_left_textColor));
            } else {
                viewHolder.voiceAnimIv.setImageResource(R.drawable.mq_voice_right_normal);
                viewHolder.voiceAnimIv.setColorFilter(mqConversationActivity.getResources().getColor(R.color.mq_chat_right_textColor));
            }
        } else {
            if (voiceMessage.getItemViewType() == BaseMessage.TYPE_AGENT) {
                viewHolder.voiceAnimIv.setImageResource(R.drawable.mq_anim_voice_left_playing);
            } else {
                viewHolder.voiceAnimIv.setImageResource(R.drawable.mq_anim_voice_right_playing);
            }
            AnimationDrawable animationDrawable = (AnimationDrawable) viewHolder.voiceAnimIv.getDrawable();
            animationDrawable.start();
        }

        // 语音未读显示 小红点
        if (viewHolder.unreadCircle != null) {
            if (!voiceMessage.isRead()) {
                viewHolder.unreadCircle.setVisibility(View.VISIBLE);
            } else {
                viewHolder.unreadCircle.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 处理播放录音按钮的点击事件
     *
     * @param voiceMessage
     * @param position
     */
    private void handleClickVoiceBtn(VoiceMessage voiceMessage, int position) {
        if (TextUtils.isEmpty(voiceMessage.getLocalPath())) {
            stopPlayVoice();

            downloadAndPlayVoice(voiceMessage, position);
            return;
        }

        if (MQAudioPlayerManager.isPlaying() && mCurrentPlayingItemPosition == position) {
            // 如果正在播放录音，并且当前正在播放录音的item是当前item，则停止播放录音

            stopPlayVoice();
        } else {
            startPlayVoiceAndRefreshList(voiceMessage, position);
        }
    }

    /**
     * 停止播放录音
     */
    public void stopPlayVoice() {
        MQAudioPlayerManager.stop();
        mCurrentPlayingItemPosition = NO_POSITION;
        notifyDataSetChanged();
    }

    /**
     * 开始播放录音，并更新数据列表
     *
     * @param voiceMessage
     * @param position
     */
    private void startPlayVoiceAndRefreshList(VoiceMessage voiceMessage, int position) {
        MQAudioPlayerManager.playSound(voiceMessage.getLocalPath(), new MQAudioPlayerManager.Callback() {
            @Override
            public void onError() {
                mCurrentPlayingItemPosition = NO_POSITION;
                notifyDataSetChanged();
            }

            @Override
            public void onCompletion() {
                mCurrentPlayingItemPosition = NO_POSITION;
                notifyDataSetChanged();
            }
        });

        // 设置已读状态
        voiceMessage.setIsRead(true);
        MQConfig.getController(mqConversationActivity).updateMessage(voiceMessage.getId(), true);

        mCurrentPlayingItemPosition = position;
        notifyDataSetChanged();
    }

    /**
     * 下载录音文件，并设置录音时长
     *
     * @param voiceMessage
     */
    private void downloadAndPlayVoice(final VoiceMessage voiceMessage, final int position) {
        mCurrentDownloadingItemPosition = position;
        MQDownloadManager.getInstance(mqConversationActivity).downloadVoice(voiceMessage.getUrl(), new MQDownloadManager.Callback() {
            @Override
            public void onSuccess(File file) {
                setVoiceMessageDuration(voiceMessage, file.getAbsolutePath());

                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        // 如果该文件对应的数据item的索引等于当前正在下载文件的索引，则播放录音
                        if (mCurrentDownloadingItemPosition == position) {
                            startPlayVoiceAndRefreshList(voiceMessage, position);
                        }
                    }
                });
            }

            @Override
            public void onFailure() {
                MQUtils.showSafe(mqConversationActivity, R.string.mq_download_audio_failure);
            }
        });
    }

    public void downloadAndNotifyDataSetChanged(List<BaseMessage> baseMessages) {
        for (BaseMessage baseMessage : baseMessages) {
            if (baseMessage instanceof VoiceMessage) {
                final VoiceMessage voiceMessage = (VoiceMessage) baseMessage;
                // 根据本地文件路径判断本地文件是否存在
                File localFile = null;
                if (!TextUtils.isEmpty(voiceMessage.getLocalPath())) {
                    localFile = new File(voiceMessage.getLocalPath());
                }

                // 如果本地文件存在则直接赋值，如果本地文件不存在则根据url获取文件
                File voiceFile;
                if (localFile != null && localFile.exists()) {
                    voiceFile = localFile;
                } else {
                    voiceFile = MQAudioRecorderManager.getCachedVoiceFileByUrl(mqConversationActivity, voiceMessage.getUrl());
                }

                // 如果声音文件已经存在则不下载
                if (voiceFile != null && voiceFile.exists()) {
                    setVoiceMessageDuration(voiceMessage, voiceFile.getAbsolutePath());
                    notifyDataSetChanged();
                } else {
                    MQDownloadManager.getInstance(mqConversationActivity).downloadVoice(voiceMessage.getUrl(), new MQDownloadManager.Callback() {
                        @Override
                        public void onSuccess(File file) {
                            setVoiceMessageDuration(voiceMessage, file.getAbsolutePath());
                            listView.post(mNotifyDataSetChangedRunnable);
                        }

                        @Override
                        public void onFailure() {
                        }
                    });
                }
            }
        }
    }

    /**
     * 设置录音本地文件地址和时长
     *
     * @param voiceMessage
     * @param audioFilePath
     */
    private void setVoiceMessageDuration(VoiceMessage voiceMessage, String audioFilePath) {
        voiceMessage.setLocalPath(audioFilePath);
        voiceMessage.setDuration(MQAudioPlayerManager.getDurationByFilePath(mqConversationActivity, audioFilePath));
    }

    /**
     * 如果开发者有配置气泡的颜色，改变气泡颜色
     *
     * @param view
     * @param isLeft
     */
    private void configChatBubbleBg(View view, boolean isLeft) {
        if (isLeft) {
            MQUtils.applyCustomUITintDrawable(view, R.color.mq_chat_left_bubble_final, R.color.mq_chat_left_bubble, MQConfig.ui.leftChatBubbleColorResId);
        } else {
            MQUtils.applyCustomUITintDrawable(view, R.color.mq_chat_right_bubble_final, R.color.mq_chat_right_bubble, MQConfig.ui.rightChatBubbleColorResId);
        }
    }

    /**
     * 如果开发者有配置气泡内文字的颜色，改变气泡文字的颜色
     */
    private void configChatBubbleTextColor(TextView textView, boolean isLeft) {
        if (isLeft) {
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
        } else {
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_right_textColor, MQConfig.ui.rightChatTextColorResId, null, textView);
        }
    }
}
