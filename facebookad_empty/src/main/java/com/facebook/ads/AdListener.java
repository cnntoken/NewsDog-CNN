package com.facebook.ads;

/**
 * Created by objectlife on 5/5/17.
 */

public interface AdListener {
    void onError(Ad var1, AdError var2);

    void onAdLoaded(Ad var1);

    void onAdClicked(Ad var1);
}
