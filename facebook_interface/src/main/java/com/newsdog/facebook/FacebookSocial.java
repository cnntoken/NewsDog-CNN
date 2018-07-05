package com.newsdog.facebook;

import android.content.Context;
import android.content.Intent;

import java.io.File;

/**
 * Created by newsdog on 6/12/16.
 */
public interface FacebookSocial {
    void login(LoginListener listener);

    void logout();

    void share(Context context, ShareContent content, ShareListener listener);

    void share(Context context, File file, ShareListener listener);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    boolean useIntentShare();

    void detach();

    void onLog();
}
