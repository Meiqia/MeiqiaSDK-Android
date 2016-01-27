package com.meiqia.meiqiasdk.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RawRes;

public class MQSoundPoolManager {
    private static final int STREAMS_COUNT = 1;
    private SoundPool mSoundPool;
    private int mSoundId;
    private AudioManager mAudioManager;

    public MQSoundPoolManager(Context context, @RawRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = createSoundPoolWithBuilder();
        } else {
            mSoundPool = createSoundPool();
        }
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSoundId = mSoundPool.load(context.getApplicationContext(), resId, 1);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private SoundPool createSoundPoolWithBuilder() {
        return new SoundPool.Builder().setMaxStreams(STREAMS_COUNT).build();
    }

    private SoundPool createSoundPool() {
        //noinspection deprecation
        return new SoundPool(STREAMS_COUNT, AudioManager.STREAM_MUSIC, 0);
    }

    public void playSound() {
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            mSoundPool.stop(mSoundId);
            mSoundPool.play(mSoundId, 1, 1, 0, 0, 1);
        }
    }

    public void release() {
        mSoundPool.stop(mSoundId);
        mSoundPool.release();
        mSoundPool = null;
        mAudioManager = null;
    }
}