package com.facebook.ads;

import android.view.View;

/**
 * Created by objectlife on 5/5/17.
 */

public class NativeAd {


    public String getAdTitle() {
        return null;
    }

    public String getAdSubtitle() {
        return null;
    }

    public String getAdBody() {
        return null;
    }

    public String getAdCallToAction() {
        return null;
    }

    public String getAdSocialContext() {
        return null;
    }

    public NativeAd.Image getAdIcon() {
        return null;
    }

    public NativeAd.Image getAdCoverImage() {
        return null;
    }

    public static class Image {

        public String getUrl() {
            return null;
        }

        public int getWidth() {
            return 0;
        }

        public int getHeight() {
            return 0;
        }
    }

    public void setMediaViewAutoplay(boolean var1) {
    }

    public void setAdListener(com.facebook.ads.AdListener var1) {
    }

    public void registerViewForInteraction(View var1) {

    }

    public void unregisterView() {

    }

    public void destroy() {

    }

}
