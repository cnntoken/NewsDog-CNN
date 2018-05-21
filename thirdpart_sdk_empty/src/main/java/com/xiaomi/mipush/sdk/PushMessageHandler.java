package com.xiaomi.mipush.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by newsdog on 25/2/17.
 */

public class PushMessageHandler extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
