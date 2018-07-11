package com.newsdog.facebook;

/**
 * Created by newsdog on 6/12/16.
 */
public interface ShareListener {
    void onComplete(int code);
}


public interface PostListener {
    void onComplete(int code);
}
