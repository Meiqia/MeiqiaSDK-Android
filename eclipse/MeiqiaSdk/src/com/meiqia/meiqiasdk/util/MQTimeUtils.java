package com.meiqia.meiqiasdk.util;

import android.content.Context;

import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.BaseMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MQTimeUtils {
    // 2分钟
    private static final long TIME_INTERNAL_LIMIT = 120000;
    private static final String MONTH_DAY = "M-d";
    private static final String HOURS_MINUTE = "H:mm";
    private static String TODAY = "today";
    private static String YESTERDAY = "yesterday";

    /**
     * 必须先初始化，因为需要国际化处理
     *
     * @param context context
     */
    public static void init(Context context) {
        //动态读取时间轴上的文字，方便国际化
        TODAY = context.getResources().getString(R.string.mq_timeline_today);
        YESTERDAY = context.getResources().getString(R.string.mq_timeline_yesterday);
    }

    public static void refreshMQTimeItem(List<BaseMessage> mcMessageList) {
        // 从底部开始删除
        for (int i = mcMessageList.size() - 1; i >= 0; i--) {
            if (mcMessageList.get(i).getItemViewType() == BaseMessage.TYPE_TIME) {
                mcMessageList.remove(i);
            }
        }
        addMQTimeItem(mcMessageList);
    }

    private static void addMQTimeItem(List<BaseMessage> mcMessageList) {
        // 从底部开始插入
        for (int i = mcMessageList.size() - 1; i >= 0; i--) {
            // 不是第一条消息
            if (i != 0) {
                long currentMsgTime = mcMessageList.get(i).getCreatedOn();
                long previousMsgTime = mcMessageList.get(i - 1).getCreatedOn();
                long difTime = currentMsgTime - previousMsgTime;
                // 如果时间前面是 BaseMessage.TYPE_TIME，不添加时间
                if (difTime > TIME_INTERNAL_LIMIT && mcMessageList.get(i).getItemViewType() != BaseMessage.TYPE_TIME) {
                    // 添加TimeItem
                    BaseMessage timeItem = new BaseMessage();
                    timeItem.setItemViewType(BaseMessage.TYPE_TIME);
                    // 设置Item类型
                    timeItem.setCreatedOn(currentMsgTime);
                    mcMessageList.add(i, timeItem);
                }
            }
        }
    }

    public static String parseTime(long time) {
        String timeStr;
        Date curDates = new Date(time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(HOURS_MINUTE, Locale.getDefault());
        timeStr = simpleDateFormat.format(curDates);

        // 今天
        if (time > getTodayZeroTime()) {
            timeStr = TODAY + " " + timeStr;
        }
        // 昨天
        else if (time > getYesterdayZeroTime() && time < getTodayZeroTime()) {
            timeStr = YESTERDAY + " " + timeStr;
        }
        // 昨天以前
        else {
            SimpleDateFormat formatters2 = new SimpleDateFormat(MONTH_DAY, Locale.getDefault());
            timeStr = formatters2.format(curDates) + " " + timeStr;
        }
        return timeStr;
    }

    private static long getTodayZeroTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static long getYesterdayZeroTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, -24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long parseTimeToLong(String time) {
        if (null == time) return System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        java.util.Date d;
        try {
            d = sdf.parse(time);
            return d.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }
}