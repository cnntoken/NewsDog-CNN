package com.newsdog.facebook.deeplink;

import android.content.Context;

/**
 * Created by objectlife on 12/7/16.
 */

public interface FbDeepLinkInter {
    void fetchDeferredAppLinkData(Context context, OnDeferredLinkListener linkListener);
}
