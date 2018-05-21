package com.newsdog.facebook.deeplink;

import android.net.Uri;

/**
 * Created by objectlife on 12/7/16.
 */

public interface OnDeferredLinkListener {

    void fetchDeferredAppLinkData(Uri targetUri);

}
