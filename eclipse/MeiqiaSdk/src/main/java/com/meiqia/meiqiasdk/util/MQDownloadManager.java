package com.meiqia.meiqiasdk.util;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/1/29 下午2:55
 * 描述:
 */
public class MQDownloadManager {
    private static MQDownloadManager sInstance;
    private OkHttpClient mOkHttpClient;
    private Context mContext;

    private MQDownloadManager(Context context) {
        mOkHttpClient = new OkHttpClient();
        mContext = context;
    }

    public static MQDownloadManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (MQDownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new MQDownloadManager(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public void downloadVoice(final String url, final Callback callback) {
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (callback != null) {
                    callback.onFailure();
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        File voiceFile = MQAudioRecorderManager.getCachedVoiceFileByUrl(mContext, url);
                        BufferedSink sink = Okio.buffer(Okio.sink(voiceFile));
                        sink.writeAll(response.body().source());
                        sink.close();
                        if (callback != null) {
                            callback.onSuccess(voiceFile);
                        }
                    } catch (IOException e) {
                        if (callback != null) {
                            callback.onFailure();
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onFailure();
                    }
                }
            }
        });
    }

    public interface Callback {
        void onSuccess(File file);

        void onFailure();
    }
}