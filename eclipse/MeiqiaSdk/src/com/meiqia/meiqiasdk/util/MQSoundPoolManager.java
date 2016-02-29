package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RawRes;

import java.util.HashMap;
import java.util.Map;

public class MQSoundPoolManager {

    private static final int STREAMS_COUNT = 1;
    private SoundPool mSoundPool;
    private AudioManager mAudioManager;
    private Map<Integer, Integer> mSoundSourceMap;
    private Context context;

    public static MQSoundPoolManager getInstance(Context context) {
        return new MQSoundPoolManager(context);
    }

    private MQSoundPoolManager(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(STREAMS_COUNT).build();
        } else {
            mSoundPool = new SoundPool(STREAMS_COUNT, AudioManager.STREAM_MUSIC, 0);
        }
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSoundSourceMap = new HashMap<>();
    }

    public void playSound(@RawRes final int resId) {
        if (mSoundSourceMap == null) {
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
            mSoundPool.load(context.getApplicationContext(), resId, 1);
        } else {
            soundId = mSoundSourceMap.get(resId);
            play(soundId);
        }
    }

    private void play(int soundId) {
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            mSoundPool.stop(soundId);
            mSoundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        }
    }

    public void release() {
        mSoundPool.release();
        mSoundPool = null;
        mAudioManager = null;
        context = null;
        mSoundSourceMap = null;
    }
}