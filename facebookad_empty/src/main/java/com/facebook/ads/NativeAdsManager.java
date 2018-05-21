package com.facebook.ads;

import android.content.Context;

/**
 * Created by objectlife on 5/5/17.
 */

public class NativeAdsManager {
    public NativeAdsManager(Context var1, String var2, int var3) {

    }

    public void setListener(NativeAdsManager.Listener var1) {
    }

    public void loadAds() {

    }

    public NativeAd nextNativeAd() {
        return null;
    }

    public interface Listener {
        void onAdsLoaded();

        void onAdError(AdError var1);
    }
}
