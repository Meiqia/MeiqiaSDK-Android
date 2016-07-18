package com.meiqia.meiqiasdk.callback;

import android.os.Bundle;

import com.meiqia.meiqiasdk.activity.MQConversationActivity;

/**
 * OnePiece
 * Created by xukq on 7/13/16.
 */
public interface MQActivityLifecycleCallback {

    void onActivityCreated(MQConversationActivity activity, Bundle savedInstanceState);

    void onActivityStarted(MQConversationActivity activity);

    void onActivityResumed(MQConversationActivity activity);

    void onActivityPaused(MQConversationActivity activity);

    void onActivityStopped(MQConversationActivity activity);

    void onActivitySaveInstanceState(MQConversationActivity activity, Bundle outState);

    void onActivityDestroyed(MQConversationActivity activity);

}
