package com.meiqia.meiqiasdk.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import im.delight.android.webview.AdvancedWebView;

/**
 * 为了更好地在 Android App 中集成聊天链接，需要按照 demo 方法替换原生 WebView
 * 步骤：
 * 1. 集成 AdvancedWebView 并替换原生 WebView。 参考：https://github.com/delight-im/Android-AdvancedWebView 集成 AdvancedWebView，记得添加混淆设置
 * 2.在布局中用 AdvancedWebView 替换 Android 自带的 WebView
 * 3.使用下方 webview 设置
 */
public class ActivityWebView extends Activity implements AdvancedWebView.Listener {

    private AdvancedWebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        final WebView webView = findViewById(R.id.webview);
        mWebView = (AdvancedWebView) webView;
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setJavaScriptEnabled(true);

        String link = getIntent().getStringExtra("link");

        webView.loadUrl(link);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        // 处理下载文件的情况
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        });

        // 处理无法复制部分聊天内容和保存图片的问题
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult result = webView.getHitTestResult();
                if (result == null) {
                    return false;
                }
                int resultType = result.getType();
                String resultExtra = result.getExtra();
                if (resultType == WebView.HitTestResult.IMAGE_TYPE
                        || resultType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    // 下载并保存到相册
                    downloadAndSave(resultExtra);
                }
                // 超链接、手机号、邮箱 自动复制
                else if (resultType == WebView.HitTestResult.SRC_ANCHOR_TYPE
                        || resultType == WebView.HitTestResult.PHONE_TYPE
                        || resultType == WebView.HitTestResult.EMAIL_TYPE) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", resultExtra);
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(ActivityWebView.this, "复制成功", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    private void downloadAndSave(final String picUrl) {
        if (!TextUtils.isEmpty(picUrl) && picUrl.startsWith("http")) {
            new AlertDialog.Builder(this)
                    .setMessage("是否保存当前图片？")
                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    URL url;
                                    InputStream connectionIs = null;
                                    OutputStream os = null;
                                    try {
                                        url = new URL(picUrl);
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        conn.setConnectTimeout(5000);
                                        conn.setRequestMethod("GET");
                                        if (conn.getResponseCode() == 200) {
                                            connectionIs = conn.getInputStream();
                                            Bitmap bitmap = BitmapFactory.decodeStream(connectionIs);
                                            // 保存
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                ContentValues values = new ContentValues();
                                                values.put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis() + ".png");
                                                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/*");
                                                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                                                values.put(MediaStore.Video.Media.IS_PENDING, 1);
                                                ContentResolver contentResolver = getContentResolver();

                                                Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                                os = contentResolver.openOutputStream(imageUri);
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                                                values.clear();
                                                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                                                contentResolver.update(imageUri, values, null, null);
                                                if (os == null) {
                                                    return;
                                                }
                                            } else {
                                                String filePath = getFilesDir() + File.separator + System.currentTimeMillis();
                                                os = new FileOutputStream(new File(filePath));
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                                                os.flush();
                                                // 通知图库更新
                                                MediaStore.Images.Media.insertImage(getContentResolver(), filePath, new File(filePath).getName(), null);
                                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(ActivityWebView.this, "图片保存成功", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } catch (Exception e) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                            if (!isDestroyed()) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(ActivityWebView.this, "保存失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (connectionIs != null) {
                                                connectionIs.close();
                                            }
                                            if (os != null) {
                                                os.close();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();
                        }
                    })
                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) {
            return;
        }
        // ...
        super.onBackPressed();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
    }

    @Override
    public void onPageFinished(String url) {
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
    }

    @Override
    public void onExternalPageRequest(String url) {
    }
}
