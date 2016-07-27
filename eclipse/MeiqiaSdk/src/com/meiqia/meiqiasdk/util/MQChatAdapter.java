package com.meiqia.meiqiasdk.util;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.meiqia.meiqiasdk.activity.MQConversationActivity;
import com.meiqia.meiqiasdk.activity.MQPhotoPreviewActivity;
import com.meiqia.meiqiasdk.chatitem.MQRichTextItem;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.EvaluateMessage;
import com.meiqia.meiqiasdk.model.FileMessage;
import com.meiqia.meiqiasdk.model.RedirectQueueMessage;
import com.meiqia.meiqiasdk.model.RobotMessage;
import com.meiqia.meiqiasdk.model.InitiativeRedirectMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.widget.MQRedirectQueueItem;
import com.meiqia.meiqiasdk.model.RichTextMessage;
import com.meiqia.meiqiasdk.chatitem.MQAgentItem;
import com.meiqia.meiqiasdk.chatitem.MQBaseBubbleItem;
import com.meiqia.meiqiasdk.chatitem.MQClientItem;
import com.meiqia.meiqiasdk.chatitem.MQEvaluateItem;
import com.meiqia.meiqiasdk.chatitem.MQNoAgentItem;
import com.meiqia.meiqiasdk.chatitem.MQRobotItem;
import com.meiqia.meiqiasdk.chatitem.MQTimeItem;
import com.meiqia.meiqiasdk.chatitem.MQTipItem;
import com.meiqia.meiqiasdk.chatitem.MQInitiativeRedirectItem;

import java.io.File;
import java.util.List;

public class MQChatAdapter extends BaseAdapter implements MQBaseBubbleItem.Callback {
    private static final String TAG = MQChatAdapter.class.getSimpleName();

    private MQConversationActivity mConversationActivity;
    private List<BaseMessage> mMessageList;
    private ListView mListView;

    private static final int NO_POSITION = -1;
    private int mCurrentPlayingItemPosition = NO_POSITION;
    private int mCurrentDownloadingItemPosition = NO_POSITION;

    private Runnable mNotifyDataSetChangedRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public MQChatAdapter(MQConversationActivity conversationActivity, List<BaseMessage> messageList, ListView listView) {
        mConversationActivity = conversationActivity;
        mMessageList = messageList;
        mListView = listView;
    }

    public void addMQMessage(BaseMessage baseMessage) {
        mMessageList.add(baseMessage);
        notifyDataSetChanged();
    }

    public void addMQMessage(BaseMessage baseMessage, int location) {
        mMessageList.add(location, baseMessage);
        notifyDataSetChanged();
    }

    public void loadMoreMessage(List<BaseMessage> baseMessages) {
        mMessageList.addAll(0, baseMessages);
        notifyDataSetChanged();
        downloadAndNotifyDataSetChanged(baseMessages);
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getItemViewType();
    }

    @Override
    public int getViewTypeCount() {
        return BaseMessage.MAX_TYPE;
    }

    @Override
    public int getCount() {
        return mMessageList.size();
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
        final BaseMessage mcMessage = mMessageList.get(position);

        if (convertView == null) {
            switch (getItemViewType(position)) {
                case BaseMessage.TYPE_AGENT:
                    convertView = new MQAgentItem(mConversationActivity, this);
                    break;
                case BaseMessage.TYPE_CLIENT:
                    convertView = new MQClientItem(mConversationActivity, this);
                    break;
                case BaseMessage.TYPE_TIME:
                    convertView = new MQTimeItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_TIP:
                    convertView = new MQTipItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_EVALUATE:
                    convertView = new MQEvaluateItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_ROBOT:
                    convertView = new MQRobotItem(mConversationActivity, mConversationActivity);
                    break;
                case BaseMessage.TYPE_NO_AGENT_TIP:
                    convertView = new MQNoAgentItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_INITIATIVE_REDIRECT_TIP:
                    convertView = new MQInitiativeRedirectItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_QUEUE_TIP:
                    convertView = new MQRedirectQueueItem(mConversationActivity, mConversationActivity);
                    break;
                case BaseMessage.TYPE_RICH_TEXT:
                    convertView = new MQRichTextItem(mConversationActivity);
                    break;
            }
        }

        if (getItemViewType(position) == BaseMessage.TYPE_AGENT) {
            ((MQAgentItem) convertView).setMessage(mcMessage, position, mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_CLIENT) {
            ((MQClientItem) convertView).setMessage(mcMessage, position, mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_NO_AGENT_TIP) {
            ((MQNoAgentItem) convertView).setCallback(mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_ROBOT) {
            ((MQRobotItem) convertView).setMessage((RobotMessage) mcMessage, mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_INITIATIVE_REDIRECT_TIP) {
            ((MQInitiativeRedirectItem) convertView).setMessage((InitiativeRedirectMessage) mcMessage, mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_TIME) {
            ((MQTimeItem) convertView).setMessage(mcMessage);
        } else if (getItemViewType(position) == BaseMessage.TYPE_TIP) {
            ((MQTipItem) convertView).setMessage(mcMessage);
        } else if (getItemViewType(position) == BaseMessage.TYPE_EVALUATE) {
            ((MQEvaluateItem) convertView).setMessage((EvaluateMessage) mcMessage);
        } else if (getItemViewType(position) == BaseMessage.TYPE_QUEUE_TIP) {
            ((MQRedirectQueueItem) convertView).setMessage((RedirectQueueMessage) mcMessage);
        } else if (getItemViewType(position) == BaseMessage.TYPE_RICH_TEXT) {
            ((MQRichTextItem) convertView).setMessage((RichTextMessage) mcMessage, mConversationActivity);
        }

        return convertView;
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
                    voiceFile = MQAudioRecorderManager.getCachedVoiceFileByUrl(mConversationActivity, voiceMessage.getUrl());
                }

                // 如果声音文件已经存在则不下载
                if (voiceFile != null && voiceFile.exists()) {
                    setVoiceMessageDuration(voiceMessage, voiceFile.getAbsolutePath());
                    notifyDataSetChanged();
                } else {
                    MQDownloadManager.getInstance(mConversationActivity).downloadVoice(voiceMessage.getUrl(), new MQDownloadManager.Callback() {
                        @Override
                        public void onSuccess(File file) {
                            setVoiceMessageDuration(voiceMessage, file.getAbsolutePath());
                            mListView.post(mNotifyDataSetChangedRunnable);
                        }

                        @Override
                        public void onFailure() {
                        }
                    });
                }
            }
        }
    }

    @Override
    public void setVoiceMessageDuration(VoiceMessage voiceMessage, String audioFilePath) {
        voiceMessage.setLocalPath(audioFilePath);
        voiceMessage.setDuration(MQAudioPlayerManager.getDurationByFilePath(mConversationActivity, audioFilePath));
    }

    @Override
    public void scrollContentToBottom() {
        mConversationActivity.scrollContentToBottom();
    }

    @Override
    public boolean isLastItemAndVisible(int position) {
        return position == mListView.getLastVisiblePosition() && mListView.getLastVisiblePosition() == getCount() - 1;
    }

    @Override
    public void photoPreview(String url) {
        mConversationActivity.startActivity(MQPhotoPreviewActivity.newIntent(mConversationActivity, MQUtils.getImageDir(mConversationActivity), url));
    }

    @Override
    public void startPlayVoiceAndRefreshList(VoiceMessage voiceMessage, int position) {
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
        MQConfig.getController(mConversationActivity).updateMessage(voiceMessage.getId(), true);

        mCurrentPlayingItemPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public void stopPlayVoice() {
        MQAudioPlayerManager.stop();
        mCurrentPlayingItemPosition = NO_POSITION;
        notifyDataSetChanged();
    }

    @Override
    public void setCurrentDownloadingItemPosition(int currentPlayingItemPosition) {
        mCurrentPlayingItemPosition = currentPlayingItemPosition;
    }

    @Override
    public int getCurrentDownloadingItemPosition() {
        return mCurrentDownloadingItemPosition;
    }

    @Override
    public int getCurrentPlayingItemPosition() {
        return mCurrentPlayingItemPosition;
    }

    @Override
    public void resendFailedMessage(BaseMessage failedMessage) {
        notifyDataSetInvalidated();
        mConversationActivity.resendMessage(failedMessage);
    }

    @Override
    public void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message) {
        mConversationActivity.onFileMessageDownloadFailure(fileMessage, code, message);
    }

    @Override
    public void onFileMessageExpired(FileMessage fileMessage) {
        mConversationActivity.onFileMessageExpired(fileMessage);
    }
}
