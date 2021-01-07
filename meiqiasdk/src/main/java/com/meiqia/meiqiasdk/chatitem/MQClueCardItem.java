package com.meiqia.meiqiasdk.chatitem;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.meiqia.core.MQManager;
import com.meiqia.core.callback.SuccessCallback;
import com.meiqia.meiqiasdk.R;
import com.meiqia.meiqiasdk.dialog.MQInputDialog;
import com.meiqia.meiqiasdk.imageloader.MQImage;
import com.meiqia.meiqiasdk.model.ClueCardMessage;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQSimpleTextWatcher;
import com.meiqia.meiqiasdk.util.MQTimeUtils;
import com.meiqia.meiqiasdk.util.MQUtils;
import com.meiqia.meiqiasdk.widget.MQImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;

public class MQClueCardItem extends MQBaseBubbleItem {

    private MQImageView mAvatarIv;
    private LinearLayout mContainerLl;
    private ClueCardMessage mClueCardMessage;
    private TextView mSendButton;

    private int mPadding;
    private int mTextSize;

    public MQClueCardItem(Context context, Callback callback) {
        super(context, callback);
    }

    public void setMessage(ClueCardMessage clueCardMessage, Activity activity) {
        mContainerLl.removeAllViews();
        mClueCardMessage = clueCardMessage;
        MQImage.displayImage(activity, mAvatarIv, clueCardMessage.getAvatar(), R.drawable.mq_ic_holder_avatar, R.drawable.mq_ic_holder_avatar, 100, 100, null);
        fillContentLl(mClueCardMessage.getContent());
    }

    private void fillContentLl(String content) {
        try {
            JSONArray contentArray = new JSONArray(content);
            boolean isUnknownType = false;
            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                String type = item.getString("type");
                switch (type) {
                    case "string":
                    case "text":
                        addInputEdit(item, EditorInfo.TYPE_CLASS_TEXT);
                        break;
                    case "number":
                        addInputEdit(item, EditorInfo.TYPE_CLASS_NUMBER);
                        break;
                    case "radio":
                        addRadioGroup(item);
                        break;
                    case "check":
                    case "checkbox":
                        addCheckBox(item);
                        break;
                    case "date":
                    case "datetime":
                        addDatePick(item);
                        break;
                    default:
                        isUnknownType = true;
                        addNormalOrRichTextView(getContext().getString(R.string.mq_unknown_msg_tip));
                        break;
                }
            }
            if (isUnknownType && contentArray.length() == 1) {
                return;
            }
            addSendButton();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSendButton() {
        mSendButton = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.mq_item_clue_card_send, null);
        setSendButtonEnableState(mClueCardMessage.isAllEnable());
        mContainerLl.addView(mSendButton, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mClueCardMessage.isAllEnable()) {
                    return;
                }
                boolean isInputValid = true;
                // 这几个字段特殊处理，要验证
                Iterator<String> keys = mClueCardMessage.getAttrs().keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = mClueCardMessage.getAttrs().optString(key);
                    if (TextUtils.equals(key, "qq")) {
                        if (!MQUtils.isQQ(value)) {
                            isInputValid = false;
                            mClueCardMessage.getAttrs().remove(key);
                            break;
                        }
                    } else if (TextUtils.equals(key, "tel")) {
                        if (!MQUtils.isPhone(value)) {
                            isInputValid = false;
                            mClueCardMessage.getAttrs().remove(key);
                            break;
                        }
                    } else if (TextUtils.equals(key, "email")) {
                        if (!MQUtils.isEmailValid(value)) {
                            isInputValid = false;
                            mClueCardMessage.getAttrs().remove(key);
                            break;
                        }
                    }
                }
                if (!isInputValid) {
                    mContainerLl.removeAllViews();
                    fillContentLl(mClueCardMessage.getContent());
                    return;
                }

                setSendButtonEnableState(false);
                MQManager.getInstance(getContext()).replyClueCard(mClueCardMessage.getAttrs(), new SuccessCallback() {
                    @Override
                    public void onSuccess() {
                        mCallback.onClueCardMessageSendSuccess(mClueCardMessage);
                    }

                    @Override
                    public void onFailure(int code, String message) {
                        setSendButtonEnableState(mClueCardMessage.isAllEnable());
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setSendButtonEnableState(boolean enable) {
        if (mSendButton != null) {
            mSendButton.setEnabled(enable);
            mSendButton.setAlpha(enable ? 1f : 0.3f);
        }
    }

    private void addInputEdit(final JSONObject item, final int inputType) {
        if (item != null) {
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.mq_item_clue_card_input_edit, null);
            final TextView titleTv = customView.findViewById(R.id.mq_title_tv);
            final EditText editText = customView.findViewById(R.id.mq_input_et);

            final String name = item.optString("name");
            final String displayName = getName(name);
            String title = String.format(getResources().getString(R.string.mq_item_clue_card_input), displayName);
            titleTv.setText(title);
            editText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MQInputDialog(getContext(), displayName, editText.getText().toString(), "", inputType, new MQInputDialog.OnContentChangeListener() {
                        @Override
                        public void onContentChange(String content) {
                            editText.setText(content);
                            notifyDataSetChanged();
                        }
                    }).show();
                }
            });
            editText.setInputType(inputType);
            editText.setText(mClueCardMessage.getAttrs().optString(name, ""));
            editText.setSelection(editText.getText().length());
            mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
            setSendButtonEnableState(mClueCardMessage.isAllEnable());
            mContainerLl.addView(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));

            editText.addTextChangedListener(new MQSimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        mClueCardMessage.getAttrs().put(name, s);
                        boolean isEnable = !TextUtils.isEmpty(s);
                        mClueCardMessage.setEnable(name, isEnable);
                        titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
                        setSendButtonEnableState(mClueCardMessage.isAllEnable());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            editText.setFocusable(false);
        }
    }

    private String getName(String name) {
        return MQUtils.keyToName(name, getContext());
    }

    private void addRadioGroup(final JSONObject item) {
        if (item != null) {
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.mq_item_clue_card_radio, null);
            final TextView titleTv = customView.findViewById(R.id.mq_title_tv);
            final RadioGroup radioGroup = customView.findViewById(R.id.radio_group);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton radioButton = group.findViewById(checkedId);
                    if (radioButton == null) {
                        return;
                    }
                    String value = (String) radioButton.getTag();
                    try {
                        String name = item.optString("name");
                        mClueCardMessage.getAttrs().put(name, value);
                        mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
                        titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
                        setSendButtonEnableState(mClueCardMessage.isAllEnable());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mCallback.notifyDataSetChanged();
                }
            });
            try {
                String name = item.optString("name");
                String displayName = getName(name);
                String title = String.format(getResources().getString(R.string.mq_item_clue_card_select), displayName);
                titleTv.setText(title);
                radioGroup.clearCheck();
                JSONArray choiceArray = item.optJSONArray("metainfo");
                for (int i = 0; i < choiceArray.length(); i++) {
                    RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.mq_item_form_radio_btn_left, null);
                    radioButton.setText(choiceArray.optJSONObject(i).optString("name"));
                    String value = choiceArray.optJSONObject(i).optString("value");
                    if (TextUtils.equals(value, mClueCardMessage.getAttrs().optString(name, ""))) {
                        radioButton.setChecked(true);
                    } else {
                        radioButton.setChecked(false);
                    }
                    radioButton.setTag(value);
                    radioButton.setId(View.NO_ID);
                    MQUtils.tintCompoundButton(radioButton, R.drawable.mq_radio_btn_uncheck, R.drawable.mq_radio_btn_checked);
                    radioGroup.addView(radioButton, LinearLayout.LayoutParams.MATCH_PARENT, MQUtils.dip2px(getContext(), 32));
                }

                mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
                titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
                setSendButtonEnableState(mClueCardMessage.isAllEnable());

                mContainerLl.addView(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            } catch (Exception e) {

            }
        }
    }

    private void addCheckBox(final JSONObject item) {
        if (item != null) {
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.mq_item_clue_card_checkbox, null);
            final TextView titleTv = customView.findViewById(R.id.mq_title_tv);
            final LinearLayout checkboxContainerLl = customView.findViewById(R.id.checkbox_container);
            try {
                final String name = item.optString("name");
                String displayName = getName(name);
                String title = String.format(getResources().getString(R.string.mq_item_clue_card_select), displayName);
                titleTv.setText(title);
                JSONArray choiceArray = item.optJSONArray("metainfo");
                CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        try {
                            JSONArray values = new JSONArray();
                            for (int i = 0; i < checkboxContainerLl.getChildCount(); i++) {
                                CompoundButton compoundButton = (CompoundButton) checkboxContainerLl.getChildAt(i);
                                if (compoundButton.isChecked()) {
                                    values.put((compoundButton.getTag()));
                                }
                            }
                            mClueCardMessage.getAttrs().put(name, values);
                            mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
                            titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
                            setSendButtonEnableState(mClueCardMessage.isAllEnable());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                JSONArray selectItemArrays = mClueCardMessage.getAttrs().optJSONArray(item.optString("name"));
                for (int i = 0; i < choiceArray.length(); i++) {
                    String value = choiceArray.optJSONObject(i).optString("value");
                    CheckBox checkBox = (CheckBox) LayoutInflater.from(getContext()).inflate(R.layout.mq_item_form_checkbox, null);
                    checkBox.setChecked(false);
                    checkBox.setText(choiceArray.optJSONObject(i).optString("name"));
                    checkBox.setSingleLine();
                    if (selectItemArrays != null) {
                        for (int j = 0; j < selectItemArrays.length(); j++) {
                            String selectValue = selectItemArrays.getString(j);
                            if (TextUtils.equals(selectValue, value)) {
                                checkBox.setChecked(true);
                                break;
                            }
                        }
                    }
                    checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
                    checkBox.setTag(value);
                    MQUtils.tintCompoundButton(checkBox, R.drawable.mq_checkbox_uncheck, R.drawable.mq_checkbox_unchecked);
                    checkboxContainerLl.addView(checkBox, LinearLayout.LayoutParams.MATCH_PARENT, MQUtils.dip2px(getContext(), 32));
                }

                mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
                titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
                setSendButtonEnableState(mClueCardMessage.isAllEnable());

                mContainerLl.addView(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            } catch (Exception ignore) {
            }
        }
    }

    private void addDatePick(JSONObject item) {
        if (item != null) {
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.mq_item_clue_card_time_picker, null);
            final TextView titleTv = customView.findViewById(R.id.mq_title_tv);
            final TextView editText = customView.findViewById(R.id.mq_input_tv);

            final String name = item.optString("name");
            String displayName = getName(name);
            String title = String.format(getResources().getString(R.string.mq_item_clue_card_input), displayName);
            titleTv.setText(title);
            mContainerLl.addView(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            String cacheTime = mClueCardMessage.getAttrs().optString(name, "");
            if (!TextUtils.isEmpty(cacheTime)) {
                editText.setText(MQTimeUtils.partLongToTime(MQTimeUtils.parseTimeToLong(cacheTime)));
            }
            mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
            titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
            setSendButtonEnableState(mClueCardMessage.isAllEnable());
            editText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar cal = Calendar.getInstance();
                    new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, month);
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            String time = MQTimeUtils.partLongToTime(cal.getTimeInMillis());
                            try {
                                mClueCardMessage.getAttrs().put(name, MQTimeUtils.partLongToTime(cal.getTimeInMillis()));
                                mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
                                titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
                                setSendButtonEnableState(mClueCardMessage.isAllEnable());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            editText.setText(time);
                            new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.YEAR, year);
                                    cal.set(Calendar.MONTH, month);
                                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    cal.set(Calendar.MINUTE, minute);
                                    String time = MQTimeUtils.partLongToTime(cal.getTimeInMillis());
                                    editText.setText(time);
                                    try {
                                        mClueCardMessage.getAttrs().put(name, MQTimeUtils.partLongToTime(cal.getTimeInMillis()));
                                        mClueCardMessage.setEnable(name, !TextUtils.isEmpty(mClueCardMessage.getAttrs().optString(name, "")));
                                        titleTv.setTextColor(getResources().getColor(mClueCardMessage.isEnable(name) ? R.color.mq_chat_event_gray : R.color.mq_error));
                                        setSendButtonEnableState(mClueCardMessage.isAllEnable());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
                        }
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
        }
    }

    /**
     * 添加普通的文本内容
     *
     * @param text
     */
    private void addNormalOrRichTextView(String text) {
        if (!TextUtils.isEmpty(text)) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            textView.setTextColor(getResources().getColor(R.color.mq_chat_left_textColor));
            textView.setPadding(mPadding, mPadding, mPadding, mPadding);
            MQUtils.applyCustomUITextAndImageColor(R.color.mq_chat_left_textColor, MQConfig.ui.leftChatTextColorResId, null, textView);
            mContainerLl.addView(textView);
        }
    }

    @Override
    protected void initView() {
        mAvatarIv = getViewById(R.id.iv_robot_avatar);
        mContainerLl = getViewById(R.id.ll_container);
    }

    @Override
    protected void processLogic() {
        MQUtils.applyCustomUITintDrawable(mContainerLl, R.color.mq_chat_left_bubble_final, R.color.mq_chat_left_bubble, MQConfig.ui.leftChatBubbleColorResId);

        mPadding = getResources().getDimensionPixelSize(R.dimen.mq_size_level2);
        mTextSize = getResources().getDimensionPixelSize(R.dimen.mq_textSize_level2);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mq_item_clue_card;
    }

    @Override
    protected void setListener() {

    }

}
