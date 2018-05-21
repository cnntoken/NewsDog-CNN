package com.newsdog.facebook;

/**
 * Created by newsdog on 6/12/16.
 */
public interface LoginListener {
    void onStart();
    void onComplete(int stCode, User aUser);
}
