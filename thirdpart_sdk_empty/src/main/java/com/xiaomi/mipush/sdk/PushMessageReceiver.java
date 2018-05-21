package com.xiaomi.mipush.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by newsdog on 21/2/17.
 */

public class PushMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {

    }

    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {

    }

    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {

    }

    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage){}

    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message){}
}
