package com.meiqia.meiqiasdk.util;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
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

import com.meiqia.core.MQManager;
import com.meiqia.core.bean.MQMessage;
import com.meiqia.core.callback.SimpleCallback;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.controller.MediaRecordFunc;
import com.meiqia.meiqiasdk.model.AgentChangeMessage;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.widget.CircleImageView;
import com.meiqia.meiqiasdk.widget.RoundProgressBar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

public class MQChatAdapter extends BaseAdapter {

    private static final String TAG = MQChatAdapter.class.getSimpleName();

    private MQConversationActivity mqConversationActivity;
    private List<BaseMessage> mcMessageList;
    private ListView listView;

    // ImageLoader
    private ImageLoader imageLoader;
    private MediaRecordFunc mediaRecordFunc;

    private final MediaPlayer mediaPlayer;
    private int onClickPosition;
    private int playingPosition;

    public MQChatAdapter(MQConversationActivity mqConversationActivity, List<BaseMessage> mcMessageList, ListView listView) {
        this.mqConversationActivity = mqConversationActivity;
        this.mcMessageList = mcMessageList;
        this.listView = listView;
        this.imageLoader = ImageLoader.getInstance();
        this.mediaPlayer = new MediaPlayer();
        this.mediaRecordFunc = MediaRecordFunc.getInstance(mqConversationActivity);
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

        //根据 type 创建不同的 ViewHolder，并缓存
        if (convertView == null) {
            switch (getItemViewType(position)) {
                case BaseMessage.TYPE_AGENT:
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(mqConversationActivity).inflate(R.layout.mq_item_chat_left, null);
                    viewHolder.contentText = (TextView) convertView.findViewById(R.id.content_text);
                    viewHolder.contentImage = (ImageView) convertView.findViewById(R.id.content_pic);
                    viewHolder.contentImageRl = convertView.findViewById(R.id.content_pic_rl);
                    viewHolder.contentVoice = (TextView) convertView.findViewById(R.id.content_voice);
                    viewHolder.voiceImage = (ImageView) convertView.findViewById(R.id.pic_voice);
                    viewHolder.voiceRl = convertView.findViewById(R.id.content_voice_rl);
                    viewHolder.playProgressbar = (RoundProgressBar) convertView.findViewById(R.id.mc_play_progressbar);
                    viewHolder.usAvatar = (CircleImageView) convertView.findViewById(R.id.us_avatar_iv);
                    viewHolder.unreadCircle = convertView.findViewById(R.id.unread_view);
                    convertView.setTag(viewHolder);
                    break;
                case BaseMessage.TYPE_CLIENT:
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(mqConversationActivity).inflate(R.layout.mq_item_chat_right, null);
                    viewHolder.contentText = (TextView) convertView.findViewById(R.id.content_text);
                    viewHolder.contentImage = (ImageView) convertView.findViewById(R.id.content_pic);
                    viewHolder.contentImageRl = convertView.findViewById(R.id.content_pic_rl);
                    viewHolder.contentVoice = (TextView) convertView.findViewById(R.id.content_voice);
                    viewHolder.voiceImage = (ImageView) convertView.findViewById(R.id.pic_voice);
                    viewHolder.voiceRl = convertView.findViewById(R.id.content_voice_rl);
                    viewHolder.playProgressbar = (RoundProgressBar) convertView.findViewById(R.id.mc_play_progressbar);
                    viewHolder.sendingProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
                    viewHolder.sendState = (ImageView) convertView.findViewById(R.id.send_state);
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
        //显示消息：文字、图片、语音
        else if (getItemViewType(position) == BaseMessage.TYPE_AGENT || getItemViewType(position) == BaseMessage.TYPE_CLIENT) {
            // 文字
            if (MQMessage.TYPE_CONTENT_TEXT.equals(mcMessage.getContentType())) {
                viewHolder.contentText.setVisibility(View.VISIBLE);
                viewHolder.contentImageRl.setVisibility(View.GONE);
                viewHolder.voiceRl.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(mcMessage.getContent())) {
                    viewHolder.contentText.setText(MQEmotionUtil.getEmotionText(mqConversationActivity, mcMessage.getContent()));
                }

            }
            // 图片
            else if (MQMessage.TYPE_CONTENT_PHOTO.equals(mcMessage.getContentType())) {
                viewHolder.contentText.setVisibility(View.GONE);
                viewHolder.voiceRl.setVisibility(View.GONE);

                String path = ((PhotoMessage) mcMessage).getLocalPath();
                boolean isLocalImageExist = MQUtils.isFileExist(path);

                String url;
                if (isLocalImageExist) {
                    url = "file://" + ((PhotoMessage) mcMessage).getLocalPath();
                } else {
                    url = ((PhotoMessage) mcMessage).getUrl();
                }
                imageLoader.displayImage(url, viewHolder.contentImage, new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
                        if (loadedImage != null) {
                            if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
                                listView.setSelection(getCount() - 1);
                            }

                            view.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    mqConversationActivity.displayPhoto(imageUri);
                                }
                            });
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
                viewHolder.contentImageRl.setVisibility(View.VISIBLE);

            }
            //语音
            else if (MQMessage.TYPE_CONTENT_VOICE.equals(mcMessage.getContentType())) {
                viewHolder.contentText.setVisibility(View.GONE);
                viewHolder.contentImageRl.setVisibility(View.GONE);
                viewHolder.voiceRl.setVisibility(View.VISIBLE);

                final VoiceMessage voiceMessage = (VoiceMessage) mcMessage;
                // 语音未读显示 小红点
                if (viewHolder.unreadCircle != null) {
                    if (!voiceMessage.isRead()) {
                        viewHolder.unreadCircle.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.unreadCircle.setVisibility(View.GONE);
                    }
                }
                String path = voiceMessage.getLocalPath();
                // 本地拼接
                if (TextUtils.isEmpty(path)) {
                    path = MediaRecordFunc.VOICE_STORE_PATH + "/" + voiceMessage.getId() + ".amr";
                    voiceMessage.setLocalPath(path);
                }
                boolean isLocalVoiceFileExist = MQUtils.isFileExist(path);

                // 填补缺失的duration
                if (voiceMessage.getDuration() == VoiceMessage.NO_DURATION && isLocalVoiceFileExist) {
                    int duration = MediaRecordFunc.getDuration(mqConversationActivity, voiceMessage.getLocalPath());
                    voiceMessage.setDuration(duration);
                }

                // 如果没有设置录音时间，则显示空
                String voiceDuration = voiceMessage.getDuration() == VoiceMessage.NO_DURATION ? "  " : "" + voiceMessage.getDuration();
                if (mcMessage.getItemViewType() == BaseMessage.TYPE_AGENT) {
                    viewHolder.contentVoice.setText(voiceDuration + "\"" + "         ");
                } else {
                    viewHolder.contentVoice.setText("         " + voiceDuration + "\"");
                }

                // 刷新录音播放状态
                if (onClickPosition != position) {
                    viewHolder.voiceImage.setBackgroundResource(R.drawable.mq_ic_voice_play);
                    viewHolder.playProgressbar.stop();
                }

                viewHolder.voiceRl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onClickPosition = position;
                        playOrStopVoice(v, voiceMessage);
                    }
                });
            }
            //显示客服头像
            if (getItemViewType(position) == BaseMessage.TYPE_AGENT) {
                imageLoader.displayImage(mcMessage.getAvatar(), viewHolder.usAvatar);
            }
            //显示发送状态：发送中、发送失败
            else if (getItemViewType(position) == BaseMessage.TYPE_CLIENT) {
                if (viewHolder.sendingProgressBar != null) {
                    if (MQMessage.STATE_SENDING.equals(mcMessage.getStatus())) {
                        viewHolder.sendingProgressBar.setVisibility(View.VISIBLE);
                        viewHolder.sendState.setVisibility(View.GONE);
                    } else if (MQMessage.STATE_ARRIVE.equals(mcMessage.getStatus())) {
                        viewHolder.sendingProgressBar.setVisibility(View.GONE);
                        viewHolder.sendState.setVisibility(View.GONE);
                    } else if (MQMessage.STATE_FAILED.equals(mcMessage.getStatus())) {
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
            style.setSpan(new ForegroundColorSpan(tipTv.getResources().getColor(R.color.mq_direct_agent_nickname_color)), start, start + agentNickName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            tipTv.setText(style);
        }
    }

    static class ViewHolder {
        TextView contentText;
        ImageView contentImage;
        View contentImageRl;
        TextView contentVoice;
        ImageView voiceImage;
        View voiceRl;
        RoundProgressBar playProgressbar;
        ProgressBar sendingProgressBar;
        ImageView sendState;
        CircleImageView usAvatar;
        View unreadCircle;
    }

    static class TimeViewHolder {
        TextView timeTv;
    }

    static class TipViewHolder {
        TextView contentTv;
    }

    private class FailedMessageOnClickListener implements OnClickListener {

        private BaseMessage failedMessage;

        public FailedMessageOnClickListener(BaseMessage failedMessage) {
            this.failedMessage = failedMessage;
        }

        @Override
        public void onClick(View v) {
            mqConversationActivity.sendMessage(failedMessage);
        }

    }

    private void playOrStopVoice(View v, final VoiceMessage voiceMessage) {
        // 设置已读状态
        voiceMessage.setIsRead(true);
        MQManager.getInstance(mqConversationActivity).updateMessage(voiceMessage.getId(), true);

        final ImageView voiceImage = (ImageView) v.findViewById(R.id.pic_voice);
        final RoundProgressBar playProgressBar = (RoundProgressBar) v.findViewById(R.id.mc_play_progressbar);
        boolean isVoiceFileAvailable = MQUtils.isFileExist(voiceMessage.getLocalPath());
        // 如果本地文件不可用，则从网络获取
        if (!isVoiceFileAvailable) {
            final String fileName = voiceMessage.getId() + ".amr";
            final String filePath = MediaRecordFunc.VOICE_STORE_PATH;
            MediaRecordFunc.getInstance(mqConversationActivity).downloadVoice(voiceMessage.getUrl(), filePath, fileName, new SimpleCallback() {
                @Override
                public void onSuccess() {
                    // 主线程回调
                    mqConversationActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            voiceMessage.setLocalPath(filePath + "/" + fileName);
                            notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onFailure(int code, String message) {

                }
            });
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                voiceImage.setBackgroundResource(R.drawable.mq_ic_voice_play);
                playProgressBar.stop();
            }
        });
        try {
            String localPath = voiceMessage.getLocalPath();
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(localPath);
                mediaPlayer.prepare();// 缓冲
                mediaPlayer.start();// 开始或恢复播放
                voiceImage.setBackgroundResource(R.drawable.mq_ic_voice_stop);
                playProgressBar.setMax(voiceMessage.getDuration() * 10);
                playProgressBar.start();
                playingPosition = onClickPosition;
            } else {
                // 再次点击，停止变为可播放按钮状态
                if (playingPosition == onClickPosition) {
                    mediaPlayer.stop();
                    voiceImage.setBackgroundResource(R.drawable.mq_ic_voice_play);
                    playProgressBar.stop();
                    playingPosition = -1;
                }
                // 其它地方点击，播放，变为可停止状态
                else {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(localPath);
                    mediaPlayer.prepare();// 缓冲
                    mediaPlayer.start();// 开始或恢复播放
                    voiceImage.setBackgroundResource(R.drawable.mq_ic_voice_stop);
                    playProgressBar.start();
                    playingPosition = onClickPosition;
                }
            }
        } catch (Exception e) {
            playingPosition = -1;
        }

        notifyDataSetChanged();
    }

}