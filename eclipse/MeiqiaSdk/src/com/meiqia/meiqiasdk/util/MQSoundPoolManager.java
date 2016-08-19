package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RawRes;

import java.util.HashMap;
import java.util.Map;

public class MQSoundPoolManager {

    private static final int SOUND_INTERNAL_TIME = 500;

    private static final int STREAMS_COUNT = 1;
    private SoundPool mSoundPool;
    private AudioManager mAudioManager;
    private Map<Integer, Integer> mSoundSourceMap;
    private Context mContext;

    public static MQSoundPoolManager getInstance(Context context) {
        return new MQSoundPoolManager(context.getApplicationContext());
    }

    private MQSoundPoolManager(Context context) {
        this.mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(STREAMS_COUNT).build();
        } else {
            mSoundPool = new SoundPool(STREAMS_COUNT, AudioManager.STREAM_MUSIC, 0);
        }
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSoundSourceMap = new HashMap<>();
    }

    public void playSound(@RawRes final int resId) {
        if (mSoundSourceMap == null || isSilentMode()) {
            return;
        }
        int soundId;
        if (!mSoundSourceMap.containsKey(resId)) {
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    // 状态成功
                    if (status == 0) {
                        mSoundSourceMap.put(resId, sampleId);
                        play(sampleId);
                    }
                }
            });
            mSoundPool.load(mContext.getApplicationContext(), resId, 1);
        } else {
            soundId = mSoundSourceMap.get(resId);
            play(soundId);
        }
    }

    private void play(int soundId) {
        if (isPlaying()) {
            return;
        }
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            mSoundPool.stop(soundId);
            mSoundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void release() {
        mSoundPool.release();
        mSoundPool = null;
        mAudioManager = null;
        mContext = null;
        mSoundSourceMap = null;
    }

    private long mPrePlayTime = 0;

    private boolean isPlaying() {
        boolean isPlaying;
        if (System.currentTimeMillis() - mPrePlayTime > SOUND_INTERNAL_TIME) {
            mPrePlayTime = System.currentTimeMillis();
            isPlaying = false;
        } else {
            isPlaying = true;
        }
        return isPlaying;
    }

    private boolean isSilentMode() {
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
    }
}