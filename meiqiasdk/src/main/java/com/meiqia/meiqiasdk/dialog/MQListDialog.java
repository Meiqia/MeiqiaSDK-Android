package com.meiqia.meiqiasdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.meiqia.meiqiasdk.R;

import java.util.List;
import java.util.Map;

public class MQListDialog extends Dialog {
    private TextView mTitleTv;
    private ListView mListview;

    public MQListDialog(Activity activity, @StringRes int titleResId, List<Map<String, String>> dataList, AdapterView.OnItemClickListener onItemClickListener) {
        this(activity, activity.getString(titleResId), dataList, onItemClickListener);
    }

    public MQListDialog(Activity activity, String title, List<Map<String, String>> dataList, final AdapterView.OnItemClickListener onItemClickListener) {
        super(activity, R.style.MQDialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.mq_dialog_ticket_categry);
        mTitleTv = (TextView) findViewById(R.id.tv_comfirm_title);
        mListview = (ListView) findViewById(R.id.list_lv);

        setCanceledOnTouchOutside(true);
        setCancelable(true);

        mTitleTv.setText(title);

        mListview.setAdapter(new SimpleAdapter(activity, dataList,
                R.layout.mq_item_text_list, new String[]{"name"}, new int[]{android.R.id.text1}));
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(parent, view, position, id);
                }
                dismiss();
            }
        });
    }

}