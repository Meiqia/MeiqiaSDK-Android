package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
            if (callback != null) {
                callback.onFailure();
            }
            return;
        }

        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure();
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        File file = MQAudioRecorderManager.getCachedVoiceFileByUrl(mContext, url);
                        BufferedSink sink = Okio.buffer(Okio.sink(file));
                        sink.writeAll(response.body().source());
                        sink.close();
                        if (callback != null) {
                            callback.onSuccess(file);
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