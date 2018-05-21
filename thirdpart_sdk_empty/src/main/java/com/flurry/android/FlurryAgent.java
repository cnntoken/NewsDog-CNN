package com.flurry.android;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

/**
 * Created by newsdog on 21/2/17.
 */

public class FlurryAgent {

    public static void logEvent(String event) {

    }

    public static void logEvent(String event, Map<String, String> params) {

    }

    public static void logEvent(String event, boolean params) {

    }

    public static void endTimedEvent(String mReadArticleEvent) {

    }

    public static void onEndSession(Context context) {

    }

    public static void logEvent(String detailDuration, Map<String, String> params, boolean b) {

    }

    public static void onStartSession(Context context) {

    }

    public static void setLogEvents(boolean b) {

    }

    public static void init(Context appContext, String key) {
        Log.e("", "### This is empty Flurry sdk ( thirdpart_sdk_empty module ) !!!! ");
        Toast.makeText(appContext, "请注意: 测试版 flurry sdk 初始化 !!!", Toast.LENGTH_LONG).show();
    }

    public static boolean isSessionActive() {
        return false;
    }

    public static void setFlurryAgentListener(FlurryAgentListener flurryAgentListener) {

    }
}
