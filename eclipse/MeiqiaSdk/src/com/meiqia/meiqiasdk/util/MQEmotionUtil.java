package com.meiqia.meiqiasdk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.meiqia.meiqiasdk.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MQEmotionUtil {
    public static final String REGEX_EMOJI = ":[\u4e00-\u9fa5\\w]+:";
    public static final String REGEX_GROUP = "(" + REGEX_EMOJI + ")";

    private MQEmotionUtil() {
    }

    public static final Map<String, Integer> sEmotionMap;

    public static final String[] sEmotionKeyArr = new String[]{
            ":smile:",
            ":smiley:",
            ":grinning:",
            ":blush:",
            ":relaxed:",
            ":wink:",
            ":heart_eyes:",
            ":kissing_heart:",
            ":kissing_closed_eyes:",
            ":kissing:",
            ":kissing_smiling_eyes:",
            ":stuck_out_tongue_winking_eye:",
            ":stuck_out_tongue_closed_eyes:",
            ":stuck_out_tongue:",
            ":flushed:",
            ":grin:",
            ":pensive:",
            ":relieved:",
            ":unamused:",
            ":disappointed:",
            ":persevere:",
            ":cry:",
            ":joy:",
            ":sob:",
            ":sleepy:",
            ":disappointed_relieved:",
            ":cold_sweat:",
            ":sweat_smile:",
            ":sweat:",
            ":weary:",
            ":tired_face:",
            ":fearful:",
            ":scream:",
            ":angry:",
            ":rage:",
            ":dog:",
    };

    public static final int[] sEmotionValueArr = new int[]{
            R.drawable.mq_emoji_1,
            R.drawable.mq_emoji_2,
            R.drawable.mq_emoji_3,
            R.drawable.mq_emoji_4,
            R.drawable.mq_emoji_5,
            R.drawable.mq_emoji_6,
            R.drawable.mq_emoji_7,
            R.drawable.mq_emoji_8,
            R.drawable.mq_emoji_9,
            R.drawable.mq_emoji_10,
            R.drawable.mq_emoji_11,
            R.drawable.mq_emoji_12,
            R.drawable.mq_emoji_13,
            R.drawable.mq_emoji_14,
            R.drawable.mq_emoji_15,
            R.drawable.mq_emoji_16,
            R.drawable.mq_emoji_17,
            R.drawable.mq_emoji_18,
            R.drawable.mq_emoji_19,
            R.drawable.mq_emoji_20,
            R.drawable.mq_emoji_21,
            R.drawable.mq_emoji_22,
            R.drawable.mq_emoji_23,
            R.drawable.mq_emoji_24,
            R.drawable.mq_emoji_25,
            R.drawable.mq_emoji_26,
            R.drawable.mq_emoji_27,
            R.drawable.mq_emoji_28,
            R.drawable.mq_emoji_29,
            R.drawable.mq_emoji_30,
            R.drawable.mq_emoji_31,
            R.drawable.mq_emoji_32,
            R.drawable.mq_emoji_33,
            R.drawable.mq_emoji_34,
            R.drawable.mq_emoji_35,
            R.drawable.mq_emoji_36
    };

    static {
        sEmotionMap = new HashMap<>();
        int count = sEmotionKeyArr.length;
        for (int i = 0; i < count; i++) {
            sEmotionMap.put(sEmotionKeyArr[i], sEmotionValueArr[i]);
        }
    }

    public static int getImgByName(String imgName) {
        Integer integer = sEmotionMap.get(imgName);
        return integer == null ? -1 : integer;
    }

    public static SpannableString getEmotionText(Context context, String source, int emotionSizeDp) {
        SpannableString spannableString = new SpannableString(source);
        Pattern pattern = Pattern.compile(REGEX_GROUP);
        Matcher matcher = pattern.matcher(spannableString);
        if (matcher.find()) {
            matcher.reset();
        }

        while (matcher.find()) {
            String emojiStr = matcher.group(1);
            // 处理emoji表情
            if (emojiStr != null) {
                ImageSpan imageSpan = getImageSpan(context, emojiStr, emotionSizeDp);
                if (imageSpan != null) {
                    int start = matcher.start(1);
                    spannableString.setSpan(imageSpan, start, start + emojiStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        return spannableString;
    }

    public static ImageSpan getImageSpan(Context context, String emojiStr, int emotionSizeDp) {
        ImageSpan imageSpan = null;
        int imgRes = getImgByName(emojiStr);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgRes);
        if (bitmap != null) {
            int size = MQUtils.dip2px(context, emotionSizeDp);
            bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
            imageSpan = new ImageSpan(context, bitmap);
        }
        return imageSpan;
    }
}