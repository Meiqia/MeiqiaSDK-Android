package com.meiqia.meiqiasdk.controller;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.meiqia.core.callback.SimpleCallback;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.VoiceMessage;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSink;
import okio.Okio;

public class MediaRecordFunc {

    public static String VOICE_STORE_PATH;

    public final static int SUCCESS = 1000;
    public final static int E_NOSDCARD = 1001;
    public final static int E_STATE_RECODING = 1002;
    public final static int E_UNKOWN = 1003;
    public final static int CANCEL = 1004;

    private boolean isRecord = false;
    private String tempVoiceFileName;

    private MediaRecorder mMediaRecorder;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;
    private List<String> downloadTaskList;
    private Context context;

    private MediaRecordFunc(Context context) {
        this.context = context;
        mHandler = new Handler();
        mOkHttpClient = new OkHttpClient();
        downloadTaskList = new ArrayList<>();
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            VOICE_STORE_PATH = externalCacheDir.getAbsolutePath();
        }
    }

    private static MediaRecordFunc mInstance;

    public synchronized static MediaRecordFunc getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MediaRecordFunc(context);

        }
        return mInstance;
    }

    public int startRecordAndFile() {
        //判断是否有外部存储设备sdcard
        if (MQUtils.isSdcardAvailable() && !TextUtils.isEmpty(VOICE_STORE_PATH)) {
            if (isRecord) {
                return E_STATE_RECODING;
            } else {
                if (mMediaRecorder == null)
                    createMediaRecord();
                try {
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                    // 让录制状态为true
                    isRecord = true;
                    return SUCCESS;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return E_UNKOWN;
                }
            }

        } else {
            return E_NOSDCARD;
        }
    }


    public String stopRecordAndFile() {
        return close();
    }

    private void createMediaRecord() {
         /* ①Initial：实例化MediaRecorder对象 */
        mMediaRecorder = new MediaRecorder();
        /* setAudioSource/setVedioSource*/
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风
        /* 设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
         * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
         */
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
         /* 设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
         /* 设置输出文件的路径 */
        tempVoiceFileName = System.currentTimeMillis() + ".amr";
        File file = new File(VOICE_STORE_PATH + "/" + tempVoiceFileName);
        if (file.exists()) {
            file.delete();
        }
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
    }


    private String close() {
        if (mMediaRecorder != null) {
            isRecord = false;
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
            } catch (Exception e) {
                Log.e("meiqia", "MediaRecordFunc close error = " + e.toString());
                return null;
            } finally {
                mMediaRecorder = null;
            }
            return VOICE_STORE_PATH + "/" + tempVoiceFileName;
        }
        return null;
    }

    private PopupWindow voicePop;
    private ImageView popVoiceMicIv;
    private TextView popVoiceTipTv;
    private boolean isCancelRecord;
    private int countDownCnt = -1;
    private boolean isCountDown;
    private OnCountDownListener onCountDownListener;

    public void setOnCountDownListener(OnCountDownListener onCountDownListener) {
        this.onCountDownListener = onCountDownListener;
    }

    /**
     * 开启倒计时
     */
    private Runnable countDown = new Runnable() {
        @Override
        public void run() {
            isCountDown = true;
            isCancelRecord = false;
            // 初始化计数
            if (countDownCnt == -1) {
                countDownCnt = 11;
            }
            countDownCnt--;
            if (!isCancelRecord || isCountDown) {
                popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_0);
                String countDownStr = context.getString(R.string.mq_record_count_down);
                popVoiceTipTv.setText(String.format(countDownStr, countDownCnt));
            }
            mHandler.postDelayed(countDown, 1000);

            // 时间到，强制停止
            if (countDownCnt == 0) {
                if (onCountDownListener != null) {
                    onCountDownListener.timeUp();
                    mHandler.removeCallbacks(countDown);
                }
            }
        }
    };

    public void showContent(Activity activity, View popParentView) {
        if (voicePop == null) {
            View popContent = LayoutInflater.from(activity).inflate(R.layout.mq_voice_pop, null);
            popVoiceMicIv = (ImageView) popContent.findViewById(R.id.mc_voice_pop_iv);
            popVoiceTipTv = (TextView) popContent.findViewById(R.id.mc_voice_pop_tv);
            voicePop = new PopupWindow(popContent, MQUtils.dip2px(activity, 180), MQUtils.dip2px(activity, 190));
            voicePop.setAnimationStyle(android.R.style.Animation_Dialog);
        }

        if (voicePop != null && !voicePop.isShowing()) {
            popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_0);
            popVoiceTipTv.setText(R.string.mq_record_up_and_cancel);
            voicePop.showAtLocation(popParentView, Gravity.CENTER, 0, 0);
            isCancelRecord = false;
            isCountDown = false;
            countDownCnt = -1;
            updateMicStatus();
            mHandler.postDelayed(countDown, 50000);
        }
    }

    public void showUpThenCancelContent() {
        if (voicePop != null && popVoiceTipTv != null && !isCountDown) {
            popVoiceTipTv.setText(R.string.mq_record_up_and_cancel);
            this.isCancelRecord = false;
        }
    }

    public void showCancelContent() {
        if (voicePop != null && popVoiceMicIv != null && popVoiceTipTv != null && !isCancelRecord) {
            popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_cancel);
            popVoiceTipTv.setText(R.string.mq_record_cancel);
            this.isCancelRecord = true;
        }
    }

    public void dismissContent() {
        if (voicePop != null && voicePop.isShowing()) {
            voicePop.dismiss();
        }
        mHandler.removeCallbacks(countDown);
    }

    /**
     * 更新话筒状态 分贝是也就是相对响度 分贝的计算公式K=20lg(Vo/Vi) Vo当前振幅值 Vi基准值为600：我是怎么制定基准值的呢？
     * 当20 * Math.log10(mMediaRecorder.getMaxAmplitude() /
     * Vi)==0的时候vi就是我所需要的基准值
     * 当我不对着麦克风说任何话的时候，测试获得的mMediaRecorder.getMaxAmplitude()值即为基准值。
     * Log.i("mic_", "麦克风的基准值：" +
     * mMediaRecorder.getMaxAmplitude());前提时不对麦克风说任何话
     */
    private int BASE = 500;
    private int SPACE = 200;// 间隔取样时间

    private void updateMicStatus() {
        if (mMediaRecorder != null && popVoiceMicIv != null) {
            // int vuSize = 10 * mMediaRecorder.getMaxAmplitude() /
            // 32768;
            int ratio = mMediaRecorder.getMaxAmplitude() / BASE;
            int db = 0;// 分贝
            if (ratio > 1) {
                db = (int) (20 * Math.log10(ratio));
            }
            if (!isCancelRecord) {
                switch (db / 4) {
                    case 0:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_0);
                        break;
                    case 1:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_1);
                        break;
                    case 2:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_2);
                        break;
                    case 3:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_3);
                        break;
                    case 4:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_4);
                        break;
                    case 5:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_5);
                        break;
                    case 6:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_6);
                        break;
                    case 7:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_7);
                        break;
                    case 8:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_8);
                        break;
                    default:
                        popVoiceMicIv.setBackgroundResource(R.drawable.mq_ic_voice_pop_mic_8);
                        break;
                }
            }
            if (voicePop.isShowing()) {
                popVoiceMicIv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateMicStatus();
                    }
                }, SPACE);
            }
        }
    }

    public static int getDuration(Context context, String path) {
        MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
        if (mp == null) {
            return VoiceMessage.NO_DURATION;
        }
        int duration = mp.getDuration() / 1000;
        // 修正
        if (duration == 0) {
            duration = 1;
        }
        return duration;
    }

    public static boolean isVoiceFileAvailable(Context context, String voiceFilePath) {
        MediaPlayer mp = MediaPlayer.create(context, Uri.parse(voiceFilePath));
        return mp != null;
    }

    public static void renameVoiceFile(VoiceMessage message, long id) {
        String localPath = message.getLocalPath();
        try {
            File file = new File(localPath);
            boolean isSuc = file.renameTo(new File(VOICE_STORE_PATH + "/" + id + ".amr"));
            if (isSuc) {
                message.setLocalPath(VOICE_STORE_PATH + "/" + id + ".amr");
            }
            Log.e("debug", "");
        } catch (Exception e) {

        }
    }

    /**
     * 下载文件
     *
     * @param url            文件地址
     * @param filePath       保存路径
     * @param filename       文件名
     * @param simpleCallback 回调
     */
    public void downloadVoice(String url, String filePath, final String filename, final SimpleCallback simpleCallback) {
        if (TextUtils.isEmpty(url)) {
            if (simpleCallback != null) simpleCallback.onFailure(0, "url is null");
            return;
        }
        if (!downloadTaskList.contains(filename)) {
            //避免重复下载
            downloadTaskList.add(filename);

            final File downloadedFile = new File(filePath, filename);
            if (downloadedFile.exists()) {
                try {
                    downloadedFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Request request = new Request.Builder().url(url).build();
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    if (simpleCallback != null) simpleCallback.onFailure(0, "download failed");
                    downloadTaskList.remove(filename);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                        sink.writeAll(response.body().source());
                        sink.close();
                        if (simpleCallback != null) simpleCallback.onSuccess();
                    } else {
                        if (simpleCallback != null) simpleCallback.onFailure(0, "download failed");
                    }
                    downloadTaskList.remove(filename);
                }
            });
        }
    }

    public interface OnCountDownListener {
        void timeUp();
    }

}