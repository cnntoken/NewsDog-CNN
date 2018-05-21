package com.newsdog.fbsdk;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.newsdog.facebook.FacebookSocial;
import com.newsdog.facebook.LoginListener;
import com.newsdog.facebook.ShareContent;
import com.newsdog.facebook.ShareListener;

import java.io.File;

/**
 * facebook操作的空实现, 除了分享功能之外,其他功能都是空.  参考 README.md 中关于facebook 模块的说明.
 * Created by newsdog on 6/12/16.
 */

public class FacebookSdkImpl implements FacebookSocial {

    public FacebookSdkImpl(Activity activity) {
    }

    public static void initFacebook(Context context) {
        Log.e("", "### facebook nullobject initFacebook");
    }

    @Override
    public void login(LoginListener listener) {
        Log.e("", "### facebook nullobject login");
    }

    @Override
    public void logout() {
        Log.e("", "### facebook nullobject logout");
    }

    @Override
    public void share(Context context, ShareContent shareContent, ShareListener listener) {
        Log.e("", "### facebook nullobject share");
        shareToSocialPlatform(context, shareContent, FACEBOOK_PACKAGE_NAME);
    }

    @Override
    public void share(Context context, File file, ShareListener listener) {

    }

    @Override
    public boolean useIntentShare() {
        return true;
    }

    public static boolean hasFacebookSdk() {
        return false;
    }

    public static final String FACEBOOK_PACKAGE_NAME = "com.facebook.katana";

    /**
     * 分享到Twitter或者whatsapp
     *
     * @param packageName
     */
    private void shareToSocialPlatform(Context context, ShareContent shareContent, String packageName) {
        boolean isClientInstalled = isAppInstalled(context, packageName);
        try {
            if (!isClientInstalled) {
                jumpToGooglePlayStore(context, packageName);
                return;
            }
            Intent intent = createShareIntent(context, shareContent);
            intent.setPackage(packageName);
            // intent
            Intent chooserIntent = Intent.createChooser(intent, "Select app to share");
            if (chooserIntent == null) {
                return;
            }
            context.startActivity(chooserIntent);
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
            }
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Can't find share component to share", Toast.LENGTH_SHORT).show();
        }
    }

    private void jumpToGooglePlayStore(Context context, String packageNam) {
        if (context == null) {
            return;
        }
        String packageName = !TextUtils.isEmpty(packageNam) ? packageNam : context.getPackageName();
        try {
            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.setPackage("com.android.vending");
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent
                    .FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri appUri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
            context.startActivity(new Intent(Intent.ACTION_VIEW, appUri));
        }
    }

    /**
     * 指定包名的应用是否安装了
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName) || context == null) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager
                    .GET_META_DATA);
            return info.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private Intent createShareIntent(Context context, ShareContent shareContent) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, shareContent.title);
        intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
        intent.putExtra(Intent.EXTRA_TEXT, prepareShareText(context, shareContent));
        return intent;
    }


    public static final String APP_URL = "http://www.newsdog.today/app";

    /**
     * 构造分享内容
     *
     * @return
     */
    public String prepareShareText(Context context, ShareContent shareContent) {
        if (!TextUtils.isEmpty(shareContent.shareUrl)) {
            String shareText = shareContent.title;
            if (TextUtils.isEmpty(shareText) && !TextUtils.isEmpty(shareContent.text)) {
                shareText = shareContent.text;
            }
            return shareText + ", url: " + shareContent.shareUrl;
        }
        return shareContent.title + ", url: " + shareContent.original + ", " + context.getString(R.string
                .share_with_friends) + APP_URL;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void detach() {

    }
}
