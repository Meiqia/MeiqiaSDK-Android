package com.meiqia.meiqiasdk.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OnePiece
 * Created by xukq on 7/8/16.
 */
public class HttpUtils {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static HttpUtils sInstance;
    private static OkHttpClient sOkHttpClient;

    public static HttpUtils getInstance() {
        if (sInstance == null) {
            sInstance = new HttpUtils();
        }
        return sInstance;
    }

    private HttpUtils() {
        sOkHttpClient = new OkHttpClient();
    }

    public JSONObject getAuthCode() throws IOException, JSONException {
        String baseUrl = "https://eco-api.meiqia.com/";
        RequestBody body = RequestBody.create(JSON, new byte[]{});
        Request request = new Request.Builder()
                .url(baseUrl + "/captchas")
                .post(body)
                .build();

        Response response = sOkHttpClient.newCall(request).execute();
        String responseStr = response.body().string();
        JSONObject result = new JSONObject(responseStr);
        String url = result.optString("captcha_image_url");
        result.put("captcha_image_url", baseUrl + url);
        return result;
    }

}
