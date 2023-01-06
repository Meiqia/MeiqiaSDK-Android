package com.meiqia.meiqiasdk.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meiqia.core.bean.MQAgent;
import com.meiqia.core.bean.MQMessage;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.model.Agent;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.ClueCardMessage;
import com.meiqia.meiqiasdk.model.FileMessage;
import com.meiqia.meiqiasdk.model.HybridMessage;
import com.meiqia.meiqiasdk.model.PhotoMessage;
import com.meiqia.meiqiasdk.model.RobotMessage;
import com.meiqia.meiqiasdk.model.TextMessage;
import com.meiqia.meiqiasdk.model.VideoMessage;
import com.meiqia.meiqiasdk.model.VoiceMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MQUtils {
    /**
     * 键盘切换延时时间
     */
    public static final int KEYBOARD_CHANGE_DELAY = 300;
    // Android Q 的文件下载路径
    public static String DOWNLOAD_DIR;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void runInThread(Runnable task) {
        new Thread(task).start();
    }

    public static void runInUIThread(Runnable task) {
        sHandler.post(task);
    }

    public static void runInUIThread(Runnable task, long delayMillis) {
        sHandler.postDelayed(task, delayMillis);
    }

    public static BaseMessage parseMQMessageIntoBaseMessage(MQMessage message, BaseMessage baseMessage) {
        baseMessage.setStatus(message.getStatus());
        baseMessage.setItemViewType(getItemType(message));
        baseMessage.setContent(message.getContent());
        baseMessage.setContentType(message.getContent_type());
        baseMessage.setStatus(message.getStatus());
        baseMessage.setId(message.getId());
        baseMessage.setType(message.getType());
        baseMessage.setConversationId(message.getConversation_id());
        baseMessage.setAgentNickname(message.getAgent_nickname());
        baseMessage.setCreatedOn(message.getCreated_on());
        baseMessage.setAvatar(message.getAvatar());
        baseMessage.setIsRead(message.is_read());
        baseMessage.setFromType(message.getFrom_type());
        if (MQMessage.TYPE_CONTENT_PHOTO.equals(message.getContent_type())) {
            ((PhotoMessage) baseMessage).setUrl(message.getMedia_url());
        } else if (MQMessage.TYPE_CONTENT_VOICE.equals(message.getContent_type())) {
            ((VoiceMessage) baseMessage).setUrl(message.getMedia_url());
        } else if (MQMessage.TYPE_CONTENT_VIDEO.equals(message.getContent_type())) {
            if (!TextUtils.isEmpty(message.getExtra())) {
                try {
                    JSONObject extraObj = new JSONObject(message.getExtra());
                    ((VideoMessage) baseMessage).setThumbUrl(extraObj.optString("thumb_url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ((VideoMessage) baseMessage).setUrl(message.getMedia_url());
        } else if (MQMessage.TYPE_CONTENT_FILE.equals(message.getContent_type())) {
            FileMessage fileMessage = ((FileMessage) baseMessage);
            fileMessage.setUrl(message.getMedia_url());
            fileMessage.setExtra(message.getExtra());
            updateFileState(fileMessage);
        } else if (MQMessage.TYPE_CONTENT_TEXT.endsWith(message.getContent_type())) {
            try {
                if (!TextUtils.isEmpty(message.getExtra())) {
                    JSONObject extraObj = new JSONObject(message.getExtra());
                    boolean isContainsSensitiveWords = extraObj.optBoolean("contains_sensitive_words", false);
                    String replacedContent = extraObj.optString("replaced_content");
                    TextMessage textMessage = (TextMessage) baseMessage;
                    textMessage.setContainsSensitiveWords(isContainsSensitiveWords);
                    textMessage.setReplaceContent(replacedContent);
                }
            } catch (Exception e) {
                Log.e("meiqia_log", e.toString());
            }
        }
        return baseMessage;
    }

    private static int getItemType(MQMessage message) {
        // 如果不是机器人，也不是客户时，默认是客服
        int itemType = BaseMessage.TYPE_AGENT;
        if (TextUtils.equals(MQMessage.TYPE_FROM_ROBOT, message.getFrom_type())) {
            itemType = BaseMessage.TYPE_ROBOT;
            if (TextUtils.equals(BaseMessage.TYPE_CONTENT_HYBRID, message.getContent_type())) {
                itemType = BaseMessage.TYPE_HYBRID;
            }
        } else if (TextUtils.equals(BaseMessage.TYPE_CONTENT_HYBRID, message.getContent_type())) {
            itemType = BaseMessage.TYPE_HYBRID;
        } else if (MQMessage.TYPE_FROM_CLIENT.equals(message.getFrom_type())) {
            itemType = BaseMessage.TYPE_CLIENT;
        } else if (MQMessage.TYPE_CONTENT_RICH_TEXT.equals(message.getContent_type())) {
            itemType = BaseMessage.TYPE_RICH_TEXT;
        }
        return itemType;
    }

    public static BaseMessage parseMQMessageToBaseMessage(MQMessage message) {
        BaseMessage baseMessage;

        if (TextUtils.equals(MQMessage.TYPE_FROM_ROBOT, message.getFrom_type())) {
            if (TextUtils.equals(message.getContent_type(), BaseMessage.TYPE_CONTENT_HYBRID)) {
                baseMessage = new HybridMessage();
                baseMessage.setContent(message.getContent());
                ((HybridMessage) baseMessage).setExtra(message.getExtra());
            } else {
                RobotMessage robotMessage = new RobotMessage();
                robotMessage.setContentRobot(message.getContent_robot());
                robotMessage.setContent(message.getContent());
                robotMessage.setSubType(message.getSub_type());
                robotMessage.setQuestionId(message.getQuestion_id());
                robotMessage.setFeedbackUseful(message.getFeedbackUseful());
                robotMessage.setExtra(message.getExtra());
                baseMessage = robotMessage;
            }
        } else if (TextUtils.equals(message.getContent_type(), BaseMessage.TYPE_CONTENT_HYBRID)) {
            baseMessage = new HybridMessage();
            baseMessage.setContent(message.getContent());
        } else if (MQMessage.TYPE_CONTENT_TEXT.equals(message.getContent_type())) {
            baseMessage = new TextMessage(message.getContent());
            baseMessage.setContent(message.getContent());
            try {
                if (!TextUtils.isEmpty(message.getExtra())) {
                    JSONObject extraObj = new JSONObject(message.getExtra());
                    boolean isContainsSensitiveWords = extraObj.optBoolean("contains_sensitive_words", false);
                    String replacedContent = extraObj.optString("replaced_content");
                    TextMessage textMessage = (TextMessage) baseMessage;
                    textMessage.setContainsSensitiveWords(isContainsSensitiveWords);
                    textMessage.setReplaceContent(replacedContent);
                    JSONArray operatorMsg = extraObj.optJSONArray("operator_msg");
                    if (operatorMsg != null && operatorMsg.length() != 0) {
                        // 渲染成富文本的的 hybrid 消息
                        JSONArray contentArray = new JSONArray();
                        JSONObject contentObj = new JSONObject();
                        try {
                            contentObj.put("type", "rich_text");
                            contentObj.put("body", message.getContent());
                            contentArray.put(contentObj);
                        } catch (Exception ignore) {
                            ignore.printStackTrace();
                        }
                        baseMessage = new HybridMessage();
                        baseMessage.setContent(contentArray.toString());
                        ((HybridMessage) baseMessage).setExtra(message.getExtra());

                        message.setContent_type(MQMessage.TYPE_CONTENT_RICH_TEXT);
                    }
                }
            } catch (Exception e) {
                Log.e("meiqia_log", e.toString());
            }
        } else if (MQMessage.TYPE_CONTENT_PHOTO.equals(message.getContent_type())) {
            // message.getMedia_url() 可能是本地路径
            baseMessage = new PhotoMessage();
            if (isLocalPath(message.getMedia_url())) {
                ((PhotoMessage) baseMessage).setLocalPath(message.getMedia_url());
            } else {
                ((PhotoMessage) baseMessage).setUrl(message.getMedia_url());
            }
            baseMessage.setContent("[photo]");
        } else if (MQMessage.TYPE_CONTENT_VOICE.equals(message.getContent_type())) {
            baseMessage = new VoiceMessage(message.getMedia_url());
            // message.getMedia_url() 可能是本地路径
            if (isLocalPath(message.getMedia_url())) {
                ((VoiceMessage) baseMessage).setLocalPath(message.getMedia_url());
            } else {
                ((VoiceMessage) baseMessage).setUrl(message.getMedia_url());
            }
            baseMessage.setContent("[voice]");
        } else if (MQMessage.TYPE_CONTENT_VIDEO.equals(message.getContent_type())) {
            baseMessage = new VideoMessage(message.getMedia_url());
            if (!TextUtils.isEmpty(message.getExtra())) {
                try {
                    JSONObject extraObj = new JSONObject(message.getExtra());
                    ((VideoMessage) baseMessage).setThumbUrl(extraObj.optString("thumb_url"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // message.getMedia_url() 可能是本地路径
            if (isLocalPath(message.getMedia_url())) {
                ((VideoMessage) baseMessage).setLocalPath(message.getMedia_url());
            } else {
                ((VideoMessage) baseMessage).setUrl(message.getMedia_url());
            }
            baseMessage.setContent("[video]");
        } else if (MQMessage.TYPE_CONTENT_FILE.equals(message.getContent_type())) {
            baseMessage = new FileMessage(message.getMedia_url());
            if (isLocalPath(message.getMedia_url())) {
                ((FileMessage) baseMessage).setLocalPath(message.getMedia_url());
            } else {
                ((FileMessage) baseMessage).setUrl(message.getMedia_url());
            }
            ((FileMessage) baseMessage).setExtra(message.getExtra());
            baseMessage.setContent("[file]");
            baseMessage.setId(message.getId());
            updateFileState(((FileMessage) baseMessage));
        } else if (MQMessage.TYPE_CONTENT_RICH_TEXT.equals(message.getContent_type())) {
            // 渲染成富文本的的 hybrid 消息
            JSONArray contentArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            try {
                contentObj.put("type", "rich_text");
                // 工单消息，富文本内容在 content
                if (TextUtils.equals(message.getType(), MQMessage.TYPE_SDK)) {
                    contentObj.put("body", message.getContent());
                } else {
                    JSONObject extra = new JSONObject(message.getExtra());
                    contentObj.put("body", extra.opt("content"));
                }
                contentArray.put(contentObj);
            } catch (Exception ignore) {

            }
            baseMessage = new HybridMessage();
            baseMessage.setContent(contentArray.toString());
            ((HybridMessage) baseMessage).setExtra(message.getExtra());
        } else {
            // TYPE 设置 unknown,在 adapter 渲染内容
            baseMessage = new TextMessage(message.getContent());
            baseMessage.setContentType(BaseMessage.TYPE_CONTENT_UNKNOWN);
        }

        baseMessage.setConversationId(message.getConversation_id());
        baseMessage.setStatus(message.getStatus());
        // 注意 type
        baseMessage.setItemViewType(getItemType(message));
        baseMessage.setContentType(message.getContent_type());
        baseMessage.setType(message.getType());
        baseMessage.setStatus(message.getStatus());
        baseMessage.setId(message.getId());
        baseMessage.setAgentNickname(message.getAgent_nickname());
        baseMessage.setCreatedOn(message.getCreated_on());
        baseMessage.setAvatar(message.getAvatar());
        baseMessage.setIsRead(message.is_read());
        baseMessage.setFromType(message.getFrom_type());
        baseMessage.setConvId(message.getConversation_id());
        return baseMessage;
    }

    public static MQMessage parseBaseMessageToMQMessage(BaseMessage baseMessage) {
        MQMessage message = new MQMessage(baseMessage.getContentType());
        message.setConversation_id(baseMessage.getConversationId());
        message.setStatus(baseMessage.getStatus());
        message.setContent_type(baseMessage.getContentType());
        message.setType(baseMessage.getType());
        message.setStatus(baseMessage.getStatus());
        message.setId(baseMessage.getId());
        message.setAgent_nickname(baseMessage.getAgentNickname());
        message.setCreated_on(baseMessage.getCreatedOn());
        message.setAvatar(baseMessage.getAvatar());
        message.setFrom_type(baseMessage.getFromType());
        if (baseMessage instanceof FileMessage) {
            message.setExtra(((FileMessage) baseMessage).getExtra());
            message.setMedia_url(((FileMessage) baseMessage).getUrl());
        }
        return message;
    }

    public static BaseMessage parseMQMessageToClueCardMessage(MQMessage message) {
        BaseMessage baseMessage = new ClueCardMessage();
        baseMessage.setStatus(message.getStatus());
        baseMessage.setContent(message.getContent());
        baseMessage.setContentType(message.getContent_type());
        baseMessage.setStatus(message.getStatus());
        baseMessage.setId(message.getId());
        baseMessage.setType(message.getType());
        baseMessage.setConversationId(message.getConversation_id());
        baseMessage.setAgentNickname(message.getAgent_nickname());
        baseMessage.setCreatedOn(message.getCreated_on());
        baseMessage.setAvatar(message.getAvatar());
        baseMessage.setFromType(message.getFrom_type());
        return baseMessage;
    }

    /**
     * 将 MQMessage 转换为 BaseMessage
     *
     * @param mqMessageList 待转换的消息
     * @return 转换后的消息
     */
    public static List<BaseMessage> parseMQMessageToChatBaseList(List<MQMessage> mqMessageList) {
        List<BaseMessage> baseMessages = new ArrayList<BaseMessage>();
        for (MQMessage mqMessage : mqMessageList) {
            baseMessages.add(parseMQMessageToBaseMessage(mqMessage));
        }
        return baseMessages;
    }

    public static Agent parseMQAgentToAgent(MQAgent mqAgent) {
        if (mqAgent == null) return null;
        Agent agent = new Agent();
        agent.setId(mqAgent.getId());
        agent.setNickname(mqAgent.getNickname());
        agent.setStatus(mqAgent.getStatus());
        agent.setIsOnline(mqAgent.isOnLine());
        agent.setPrivilege(mqAgent.getPrivilege());
        agent.setAvatar(mqAgent.getAvatar());
        agent.setSignature(mqAgent.getSignature());
        return agent;
    }

    private static boolean isLocalPath(String path) {
        return !TextUtils.isEmpty(path) && !path.startsWith("http");
    }

    private static long lastClickTime;

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 处理自定义图片和文字颜色
     *
     * @param resourceResId 通过资源文件id的形式自定义的id
     * @param codeResId     通过java代码的方式自定义的id
     * @param iconIv        要改变tint颜色的图片控件
     * @param textViews     要改变文字颜色的文本控件
     */
    public static void applyCustomUITextAndImageColor(int resourceResId, int codeResId, ImageView iconIv, TextView... textViews) {
        Context context = null;
        if (iconIv != null) {
            context = iconIv.getContext();
        }
        if (textViews != null && textViews.length > 0) {
            context = textViews[0].getContext();
        }

        if (context != null) {
            if (MQConfig.DEFAULT != codeResId) {
                resourceResId = codeResId;
            }

            int color = context.getResources().getColor(resourceResId);
            if (iconIv != null) {
                iconIv.setColorFilter(color);
            }
            if (textViews != null) {
                for (TextView textView : textViews) {
                    textView.setTextColor(color);
                }
            }
        }
    }

    /**
     * 处理自定义标题文本对其方式
     *
     * @param backTv  返回文本控件
     * @param titleTv 标题文本控件
     */
    public static void applyCustomUITitleGravity(TextView backTv, TextView titleTv) {
        if (MQConfig.ui.MQTitleGravity.LEFT == MQConfig.ui.titleGravity) {
            RelativeLayout.LayoutParams titleTvParams = (RelativeLayout.LayoutParams) titleTv.getLayoutParams();
            titleTvParams.addRule(RelativeLayout.RIGHT_OF, R.id.back_rl);
            titleTv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            if (backTv != null) {
                backTv.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 处理自定义图片背景色
     *
     * @param view          包含背景图片的控件
     * @param finalResId    默认颜色的资源id
     * @param resourceResId 通过资源文件id的形式自定义的id
     * @param codeResId     通过java代码的方式自定义的id
     */
    public static void applyCustomUITintDrawable(View view, int finalResId, int resourceResId, int codeResId) {
        Context context = view.getContext();
        if (MQConfig.DEFAULT != codeResId) {
            resourceResId = codeResId;
        }
        if (context.getResources().getColor(resourceResId) != context.getResources().getColor(finalResId)) {
            Drawable tintDrawable = tintDrawable(context, view.getBackground(), resourceResId);
            setBackground(view, tintDrawable);
        }
    }

    public static Drawable tintDrawable(Context context, Drawable drawable, @ColorRes int color) {
        if (drawable == null) {
            return null;
        }

        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(color));
        return wrappedDrawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            bitmap = null;
        }
        return bitmap;
    }

    public static void setBackground(View v, Drawable bgDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(bgDrawable);
        } else {
            v.setBackgroundDrawable(bgDrawable);
        }
    }

    public static void tintPressedIndicator(ImageView imageView, @DrawableRes int normalResId, @DrawableRes int pressedResId) {
        Drawable normal = imageView.getResources().getDrawable(normalResId);
        Drawable pressed = imageView.getResources().getDrawable(pressedResId);
        pressed = MQUtils.tintDrawable(imageView.getContext(), pressed, R.color.mq_indicator_selected);
        imageView.setImageDrawable(getPressedSelectorDrawable(normal, pressed));
    }

    public static void tintCompoundButton(CompoundButton compoundButton, @DrawableRes int normalResId, @DrawableRes int pressedResId) {
        Drawable normal = compoundButton.getResources().getDrawable(normalResId);
        normal = MQUtils.tintDrawable(compoundButton.getContext(), normal, R.color.mq_form_et_bg_normal);
        Drawable pressed = compoundButton.getResources().getDrawable(pressedResId);
        pressed = MQUtils.tintDrawable(compoundButton.getContext(), pressed, R.color.mq_indicator_selected);
        compoundButton.setCompoundDrawablesWithIntrinsicBounds(getCheckedSelectorDrawable(normal, pressed), null, null, null);
    }

    /**
     * 得到点击改变状态的Selector,一般给setBackgroundDrawable使用
     *
     * @param normal
     * @param pressed
     * @return
     */
    public static StateListDrawable getPressedSelectorDrawable(Drawable normal, Drawable pressed) {
        StateListDrawable bg = new StateListDrawable();
        bg.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, pressed);
        bg.addState(new int[]{android.R.attr.state_enabled}, normal);
        bg.addState(new int[]{}, normal);
        return bg;
    }

    public static StateListDrawable getCheckedSelectorDrawable(Drawable normal, Drawable pressed) {
        StateListDrawable bg = new StateListDrawable();
        bg.addState(new int[]{android.R.attr.state_checked, android.R.attr.state_enabled}, pressed);
        bg.addState(new int[]{android.R.attr.state_enabled}, normal);
        bg.addState(new int[]{}, normal);
        return bg;
    }

    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    public static boolean isSdcardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean isQQ(String qq) {
        if (TextUtils.isEmpty(qq)) {
            return false;
        }

        if (qq.length() < 5 || qq.length() > 11) {
            return false;
        }

        if (qq.startsWith("0")) {
            return false;
        }

        for (char c : qq.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    public static boolean isPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }

        if (phone.length() < 10 || phone.length() > 18) {
            return false;
        }

        String expression = "[-0-9]";
        CharSequence inputStr = phone;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        String result = matcher.replaceAll("");
        return TextUtils.isEmpty(result);
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean isFileExist(String filePath) {
        boolean isFileExist;
        try {
            File file = new File(filePath);
            isFileExist = file.exists();
        } catch (Exception e) {
            isFileExist = false;
        }
        return isFileExist;
    }

    public static boolean updateFileState(FileMessage fileMessage) {
        boolean isExist = isFileExist(getFileMessageFilePath(fileMessage));
        if (isExist) {
            fileMessage.setFileState(FileMessage.FILE_STATE_FINISH);
        }
        return isExist;
    }

    public static void delFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignore) {

        }
    }

    public static String getFileMessageFilePath(FileMessage fileMessage) {
        String path = null;
        try {
            JSONObject extraJsonObj = new JSONObject(fileMessage.getExtra());
            // 命名规则：文件名后面加上消息 id
            String destFileName = extraJsonObj.optString("filename");
            int lastIndexOf = destFileName.lastIndexOf(".");
            String prefix = destFileName.substring(0, lastIndexOf);
            String suffix = destFileName.substring(lastIndexOf, destFileName.length());
            destFileName = prefix + fileMessage.getId() + suffix;
            String destFileDir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                destFileDir = DOWNLOAD_DIR;
            } else {
                destFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            }
            path = destFileDir + "/" + destFileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public static String getPicStorePath(Context ctx) {
        File file = ctx.getExternalFilesDir(null);
        if (file == null) {
            file = ctx.getFilesDir();
        }
        if (!file.exists()) {
            file.mkdir();
        }
        File imageStoreFile = new File(file.getAbsolutePath() + "/mq");
        if (!imageStoreFile.exists()) {
            imageStoreFile.mkdir();
        }
        return imageStoreFile.getAbsolutePath();
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 显示吐司
     *
     * @param context
     * @param text
     */
    public static void show(Context context, CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            if (text.length() < 10) {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 显示吐司
     *
     * @param context
     * @param resId
     */
    public static void show(Context context, @StringRes int resId) {
        show(context, context.getResources().getString(resId));
    }

    /**
     * 在子线程中显示吐司时使用该方法
     *
     * @param context
     * @param text
     */
    public static void showSafe(final Context context, final CharSequence text) {
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                show(context, text);
            }
        });
    }

    /**
     * 在子线程中显示吐司时使用该方法
     *
     * @param context
     * @param resId
     */
    public static void showSafe(Context context, @StringRes int resId) {
        showSafe(context, context.getResources().getString(resId));
    }

    /**
     * 拷贝文档到黏贴板
     *
     * @param text
     */
    public static void clip(Context context, String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(text);
        } else {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("mq_content", text));
        }
    }

    /**
     * 关闭activity中打开的键盘
     *
     * @param activity
     */
    public static void closeKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }

        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 关闭dialog中打开的键盘
     *
     * @param dialog
     */
    public static void closeKeyboard(Dialog dialog) {
        View view = dialog.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) dialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 打开键盘
     *
     * @param editText
     */
    public static void openKeyboard(final EditText editText) {
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                editText.setSelection(editText.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        }, 300);
    }

    /**
     * 滚动ListView到底部
     *
     * @param absListView
     */
    public static void scrollListViewToBottom(final AbsListView absListView) {
        if (absListView != null) {
            if (absListView.getAdapter() != null && absListView.getAdapter().getCount() > 0) {
                absListView.post(new Runnable() {
                    @Override
                    public void run() {
                        absListView.setSelection(absListView.getAdapter().getCount() - 1);
                    }
                });
            }
        }
    }

    /**
     * 根据Uri获取文件的真实路径
     *
     * @param uri
     * @param context
     * @return
     */
    public static String getRealPathByUri(Context context, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        }

        try {
            ContentResolver resolver = context.getContentResolver();
            String[] proj = new String[]{MediaStore.Images.Media.DATA};
            Cursor cursor = MediaStore.Images.Media.query(resolver, uri, proj);
            String realPath = null;
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    realPath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            return realPath;
        } catch (Exception e) {
            return uri.getPath();
        }
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 获取取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }


    public static File getImageDir(Context context) {
        File imageDir = null;
        if (isExternalStorageWritable()) {
            String appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
            imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
        } else {
            MQUtils.showSafe(context, R.string.mq_no_sdcard);
        }
        return imageDir;
    }

    public static void saveBitmap(Context context, String url, Bitmap bm) {
        if (!isExternalStorageWritable()) {
            return;
        }
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            return;
        }
        String md5 = stringToMD5(url);
        if (TextUtils.isEmpty(md5)) {
            return;
        }

        File f = new File(cacheDir, md5);
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Drawable getDrawableFromFile(Context context, String url) {
        if (!isExternalStorageWritable()) {
            return null;
        }
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            return null;
        }
        String md5 = stringToMD5(url);
        if (TextUtils.isEmpty(md5)) {
            return null;
        }
        File file = new File(cacheDir.getAbsolutePath() + "/" + md5);
        if (!file.exists()) {
            return null;
        }
        return Drawable.createFromPath(file.getAbsolutePath());
    }

    /**
     * 判断外存储是否可写
     *
     * @return
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.isConnected();
    }

    /**
     * 设置未发送的文本消息
     *
     * @param context
     * @param text
     */
    public static void setUnSendTextMessage(Context context, String clientId, String text) {
        putString(context, "mq_un_send_text_msg" + clientId, text);
    }

    /**
     * 获取未发送的文本消息
     *
     * @param context
     * @return
     */
    public static String getUnSendTextMessage(Context context, String clientId) {
        return getString(context, "mq_un_send_text_msg" + clientId, "");
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MeiqiaSDK", Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String def) {
        return context.getSharedPreferences("MeiqiaSDK", Context.MODE_PRIVATE).getString(key, def);
    }

    public static String getRealFilePath(Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static Uri getImageContentUri(Context context, String filePath) {
        if (!TextUtils.isEmpty(filePath) && filePath.startsWith("http")) {
            return Uri.parse(filePath);
        }
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                File imageFile = new File(filePath);
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    public static void copyToDownloadAndroidQ(Context context, String sourcePath, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "*/*");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();

        Uri insertUri = resolver.insert(external, values);
        if (insertUri == null) {
            return;
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            os = resolver.openOutputStream(insertUri);
            if (os == null) {
                return;
            }
            int read;
            File sourceFile = new File(sourcePath);
            if (sourceFile.exists()) { // 文件存在时
                is = new FileInputStream(sourceFile); // 读入原文件
                byte[] buffer = new byte[4096];
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void copyToPictureAndroidQ(Context context, String sourcePath, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "image/*");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();

        Uri insertUri = resolver.insert(external, values);
        if (insertUri == null) {
            return;
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            os = resolver.openOutputStream(insertUri);
            if (os == null) {
                return;
            }
            int read;
            File sourceFile = new File(sourcePath);
            if (sourceFile.exists()) { // 文件存在时
                is = new FileInputStream(sourceFile); // 读入原文件
                byte[] buffer = new byte[1444];
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 通过MediaStore保存，兼容AndroidQ，保存成功自动添加到相册数据库，无需再发送广播告诉系统插入相册
     *
     * @param context      context
     * @param sourceFile   源文件
     * @param saveFileName 保存的文件名
     * @return 成功或者失败
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean saveImageWithAndroidQ(Context context, File sourceFile, String saveFileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is an image");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, saveFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
        values.put(MediaStore.Images.Media.TITLE, "image");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures");

        Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();

        Uri insertUri = resolver.insert(external, values);
        BufferedInputStream inputStream = null;
        OutputStream os = null;
        boolean result = false;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            if (insertUri != null) {
                os = resolver.openOutputStream(insertUri);
            }
            if (os != null) {
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
            result = true;
        } catch (IOException e) {
            result = false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param file
     */
    public static String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex).toLowerCase();
        if (end.equals("")) return type;
        for (String[] strings : MIME_MapTable) {
            if (end.equals(strings[0]))
                type = strings[1];
        }
        return type;
    }

    private static final String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    public static String keyToName(String name, Context context) {
        switch (name) {
            case "name":
                name = context.getResources().getString(R.string.mq_name);
                break;
            case "contact":
                name = context.getResources().getString(R.string.mq_contact);
                break;
            case "gender":
                name = context.getResources().getString(R.string.mq_gender);
                break;
            case "age":
                name = context.getResources().getString(R.string.mq_age);
                break;
            case "tel":
                name = context.getResources().getString(R.string.mq_phone);
                break;
            case "qq":
                name = context.getResources().getString(R.string.mq_qq);
                break;
            case "weixin":
                name = context.getResources().getString(R.string.mq_wechat);
                break;
            case "weibo":
                name = context.getResources().getString(R.string.mq_weibo);
                break;
            case "address":
                name = context.getResources().getString(R.string.mq_address);
                break;
            case "email":
                name = context.getResources().getString(R.string.mq_email);
                break;
            case "comment":
                name = context.getResources().getString(R.string.mq_comment);
                break;
            default:
                break;
        }
        return name;
    }

    public static JSONObject mapToJson(Map<?, ?> data) {
        JSONObject object = new JSONObject();
        for (Map.Entry<?, ?> entry : data.entrySet()) {
            /*
             * Deviate from the original by checking that keys are non-null and
             * of the proper type. (We still defer validating the values).
             */
            String key = (String) entry.getKey();
            if (key == null) {
                throw new NullPointerException("key == null");
            }
            try {
                object.put(key, wrap(entry.getValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return object;
    }

    private static Object wrap(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return collectionToJson((Collection) o);
            } else if (o.getClass().isArray()) {
                return arrayToJson(o);
            }
            if (o instanceof Map) {
                return mapToJson((Map) o);
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static JSONArray arrayToJson(Object data) throws JSONException {
        if (!data.getClass().isArray()) {
            throw new JSONException("Not a primitive data: " + data.getClass());
        }
        final int length = Array.getLength(data);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < length; ++i) {
            jsonArray.put(wrap(Array.get(data, i)));
        }

        return jsonArray;
    }

    public static JSONArray collectionToJson(Collection data) {
        JSONArray jsonArray = new JSONArray();
        if (data != null) {
            for (Object aData : data) {
                jsonArray.put(wrap(aData));
            }
        }
        return jsonArray;
    }

    public static void copyIntentExtra(Intent preIntent, Intent toIntent) {
        if (preIntent != null) {
            toIntent.putExtras(preIntent);
        }
    }

}
