package com.meiqia.meiqiasdk.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;

import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.imageloader.MQImageLoader;

import org.xml.sax.XMLReader;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * OnePiece
 * Created by xukq on 9/2/16.
 */
public class RichText {

    private String richTextStr;
    private TextView textView;
    private Html.ImageGetter imageGetter;
    private Html.TagHandler tagHandler;
    private OnImageClickListener onImageClickListener;
    private static Map<String, SoftReference<Drawable>> sHtmlDrawableCache = new HashMap<>();

    public RichText() {
        imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(final String source) {
                if (TextUtils.isEmpty(source)) {
                    return null;
                }
                if (sHtmlDrawableCache.get(source) != null && sHtmlDrawableCache.get(source).get() != null) {
                    return sHtmlDrawableCache.get(source).get();
                }

                Drawable drawableFromFile = MQUtils.getDrawableFromFile(textView.getContext(), source);
                if (drawableFromFile != null) {
                    resizeDrawable(drawableFromFile);
                    sHtmlDrawableCache.put(source, new SoftReference<>(drawableFromFile));
                    return drawableFromFile;
                }

                final URLDrawable urlDrawable = new URLDrawable();
                MQImage.downloadImage(textView.getContext(), source, new MQImageLoader.MQDownloadImageListener() {
                    @Override
                    public void onSuccess(String url, final Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(bitmap);
                        resizeDrawable(drawable);
                        sHtmlDrawableCache.put(url, new SoftReference<>(drawable));
                        urlDrawable.drawable = drawable;
                        fromHtml(richTextStr).setOnImageClickListener(onImageClickListener).into(textView);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MQUtils.saveBitmap(textView.getContext(), source, bitmap);
                            }
                        }).start();
                    }

                    @Override
                    public void onFailed(String url) {

                    }
                });
                return urlDrawable;
            }
        };
        tagHandler = new MyTagHandler();
    }

    /**
     * 调整到合适大小
     *
     * @param drawable
     */
    private void resizeDrawable(Drawable drawable) {
        float density = textView.getResources().getDisplayMetrics().density;
        if (density > 2) {
            density = (density / 2) + (0.25f * density);
        }
        float intrinsicWidth = (drawable.getIntrinsicWidth());
        float intrinsicHeight = (drawable.getIntrinsicHeight());
        int right = (int) (intrinsicWidth * density);
        int bottom = (int) (intrinsicHeight * density);
        // 气泡最大宽度为 240 dp
        int limitWidth = MQUtils.dip2px(textView.getContext(), 205);
        if (right > limitWidth) {
            float scale = intrinsicWidth / limitWidth;
            right = limitWidth;
            bottom = (int) (intrinsicHeight / scale);
        }
        drawable.setBounds(0, 0, right, bottom);
    }

    public RichText fromHtml(String richTextStr) {
        this.richTextStr = richTextStr;
        return this;
    }

    public RichText setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
        return this;
    }

    public void into(final TextView textView) {
        this.textView = textView;
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Spanned spanned = Html.fromHtml(richTextStr, imageGetter, tagHandler);
        // 结尾会多出两个换行，在这里去掉
        CharSequence charSequence = spanned;
        try {
            if (spanned.length() >= 2
                    && spanned.charAt(spanned.length() - 1) == '\n'
                    && spanned.charAt(spanned.length() - 2) == '\n') {
                charSequence = spanned.subSequence(0, spanned.length() - 2);
            }
        } catch (Exception e) {
            // 有异常，就直接显示
        }
        RichText.this.textView.setText(charSequence);
        RichText.this.textView.setVisibility(View.VISIBLE);
        RichText.this.textView.invalidate();
    }

    private class URLDrawable extends BitmapDrawable {
        // the drawable that you need to set, you could set the initial drawing
        // with the loading image if you need to
        protected Drawable drawable;

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }

    public interface OnImageClickListener {
        void onImageClicked(String url, String imgLink);
    }

    public class MyTagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

            // 处理标签<img>
            if (tag.toLowerCase(Locale.getDefault()).equals("img")) {
                // 获取长度
                int len = output.length();
                // 获取图片地址
                ImageSpan[] images = output.getSpans(len - 1, len, ImageSpan.class);
                String imgURL = images[0].getSource();

                // 使图片可点击并监听点击事件
                output.setSpan(new ClickableImage(imgURL), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        private class ClickableImage extends ClickableSpan {

            private String url;

            public ClickableImage(String url) {
                this.url = url;
            }

            @Override
            public void onClick(View widget) {
                // 进行图片点击之后的处理
                if (onImageClickListener != null) {
                    String imgLink = getImgLink(richTextStr, url);
                    onImageClickListener.onImageClicked(url, imgLink);
                }
            }
        }
    }

    private String getImgLink(String content, String url) {
        if (TextUtils.isEmpty(content) || !content.contains("href")) {
            return null;
        }
        int imgLinkStartIndex = content.indexOf(url);
        String imgLinkLeftPartContent = content.substring(0, imgLinkStartIndex);
        int startATagBeforeLinkIndex = imgLinkLeftPartContent.lastIndexOf("<a");
        if (startATagBeforeLinkIndex == -1) {
            return null;
        }
        int endATagBeforeLinkIndex = imgLinkLeftPartContent.lastIndexOf("a>");
        // img url 前面包含了完整的 a 标签
        if (endATagBeforeLinkIndex > startATagBeforeLinkIndex && endATagBeforeLinkIndex != -1) {
            return null;
        }

        int linkRightPartStartIndex = imgLinkStartIndex + url.length();
        String imgLinkRightPartContent = content.subSequence(linkRightPartStartIndex, content.length()).toString();
        int endATagAfterLinkIndex = imgLinkRightPartContent.indexOf("a>");
        if (endATagAfterLinkIndex == -1) {
            return null;
        }
        int startATagAfterLinkIndex = imgLinkRightPartContent.indexOf("<a");
        // img url 后面包含了完整的 a 标签
        if (endATagAfterLinkIndex > startATagAfterLinkIndex && startATagAfterLinkIndex != -1) {
            return null;
        }

        String aContentWithImgUrl = content.substring(startATagBeforeLinkIndex, linkRightPartStartIndex + endATagAfterLinkIndex);
        if (aContentWithImgUrl.contains("href")) {
            int start = aContentWithImgUrl.indexOf("href=");
            String startContent = aContentWithImgUrl.substring(start + "href=".length());
            int first = startContent.indexOf("\"");
            String sub = startContent.substring(first+1);
            int end = sub.indexOf("\"");
            String result = sub.substring(0, end);
            return result;
        }

        return null;
    }
}
