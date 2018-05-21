package com.xiaomi.mipush.sdk;

import android.content.Context;
import android.util.Log;

/**
 * Created by newsdog on 21/2/17.
 */

public class MiPushClient {
    public static final String COMMAND_REGISTER = "register";

    public static void reportMessageClicked(Context context, String miMsgId) {
        Log.e("", "### MiPushClient fake reportMessageClicked !!!! ");
    }

    public static void clearNotification(Context context) {

    }

    public static void registerPush(Context context, String string, String string1) {
        Log.e("", "### MiPushClient fake registerPush !!!! ");
    }
}
