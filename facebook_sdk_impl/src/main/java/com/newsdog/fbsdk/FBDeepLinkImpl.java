package com.newsdog.fbsdk;

import android.content.Context;
import android.net.Uri;

import com.facebook.applinks.AppLinkData;
import com.newsdog.facebook.deeplink.FbDeepLinkInter;
import com.newsdog.facebook.deeplink.OnDeferredLinkListener;

/**
 * Created by objectlife on 12/7/16.
 */

public class FBDeepLinkImpl implements FbDeepLinkInter {
    @Override
    public void fetchDeferredAppLinkData(Context context, final OnDeferredLinkListener linkListener) {
        AppLinkData.CompletionHandler handler = new AppLinkData.CompletionHandler() {
            @Override
            public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                Uri targetUri = null;
                if (appLinkData != null) {
                    targetUri = appLinkData.getTargetUri();
                }
                if (linkListener != null) {
                    linkListener.fetchDeferredAppLinkData(targetUri);
                }
            }
        };
        AppLinkData.fetchDeferredAppLinkData(context, handler);
    }
}
