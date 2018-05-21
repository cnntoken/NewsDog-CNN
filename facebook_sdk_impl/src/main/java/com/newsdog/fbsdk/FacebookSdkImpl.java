package com.newsdog.fbsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.appcompat.BuildConfig;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.newsdog.facebook.FacebookSocial;
import com.newsdog.facebook.LoginListener;
import com.newsdog.facebook.ShareContent;
import com.newsdog.facebook.ShareListener;
import com.newsdog.facebook.User;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * facebook登录、分享功能的实现, 集成了facebook sdk, 引用该模块会增加apk大小. 参考 README.md 中关于facebook 模块的说明.
 * Created by newsdog on 6/12/16.
 */

public class FacebookSdkImpl implements FacebookSocial {
    /**
     * activity
     */
    private Activity mActivity = null;

    private DefaultAudience defaultAudience = DefaultAudience.FRIENDS;

    private LoginBehavior loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK;

    private List<String> mPermissions = new ArrayList<>();

    private CallbackManager mCallbackManager;

    private ShareDialog shareDialog = null;

    public static final int SUCCESS = 200;
    public static final int CANCEL = 40000;
    public static final int ERROR = 40001;
    // 社交网络平台字段
    public static final String PLATFORM_FB = "fb";

    public FacebookSdkImpl(Activity activity) {
        this.mActivity = activity;
        mCallbackManager = CallbackManager.Factory.create();
        mPermissions.add("public_profile");

        LoginManager.getInstance().setDefaultAudience(defaultAudience);
        LoginManager.getInstance().setLoginBehavior(loginBehavior);
    }


    /**
     * 初始化 facebook
     * @param context
     */
    public static void initFacebook(Context context) {
        Log.e("", "### facebook real initFacebook");
        FacebookSdk.sdkInitialize(context);
        AppEventsLogger.activateApp(context);
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        }
    }


    @Override
    public void login(final LoginListener listener) {
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult result) {
                        parseAuthData(result, listener);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        listener.onComplete(ERROR, null);
                    }

                    @Override
                    public void onCancel() {
                        listener.onComplete(CANCEL, null);
                    }
                });

        // 如果当前已经是登陆状态，首先退出登录
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
        // execute login operation
        LoginManager.getInstance().logInWithReadPermissions(mActivity, mPermissions);
    }

    private void parseAuthData(LoginResult result, final LoginListener listener) {
        final AccessToken accessToken = result.getAccessToken();
        if (accessToken == null && listener != null) {
            listener.onComplete(-1, new User());
        }
        final Profile profile = Profile.getCurrentProfile();
        // 在web的情况下授权之后Profile为null
        if (profile == null) {
            Utility.getGraphMeRequestWithCacheAsync(accessToken.getToken(),
                    new Utility.GraphMeRequestWithCacheCallback() {
                        @Override
                        public void onSuccess(JSONObject userInfo) {
                            String id = userInfo.optString("id");
                            if (id == null) {
                                return;
                            }
                            String link = userInfo.optString("link");
                            Profile profile = new Profile(
                                    id,
                                    userInfo.optString("first_name"),
                                    userInfo.optString("middle_name"),
                                    userInfo.optString("last_name"),
                                    userInfo.optString("name"),
                                    link != null ? Uri.parse(link) : null
                            );
                            Profile.setCurrentProfile(profile);
                            listener.onComplete( 200, convertToUser(accessToken, profile));
                        }

                        @Override
                        public void onFailure(FacebookException error) {
                            listener.onComplete( -1, new User());
                        }
                    });
        } else {
            if (listener != null) {
                listener.onComplete(200, convertToUser(accessToken, profile));
            }
        }
    }

    private User convertToUser(AccessToken accessToken, Profile profile) {
        if ( profile == null ) {
            return  new User() ;
        }
        final User loginUser = new User();
        loginUser.token = accessToken.getToken();
        loginUser.id = profile.getId();
        loginUser.name = profile.getName();
        loginUser.portraitUrl = profile.getProfilePictureUri(200, 200).toString();
        loginUser.source = PLATFORM_FB;
        return loginUser;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void logout() {
        LoginManager.getInstance().logOut();
    }

    @Override
    public void share(Context context, ShareContent content, ShareListener listener) {
        ShareLinkContent linkContent = prepareShareContent(content);
        if (content != null && ShareDialog.canShow(linkContent.getClass())) {
            shareDialog = new ShareDialog(mActivity);
            registerShareListener(shareDialog, listener);
            shareDialog.show(linkContent);
        } else {
            listener.onComplete(ERROR);
        }
    }

    @Override
    public void share(Context context, File file, ShareListener listener) {
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(decodeBitmapFromFile(file.getPath(), 1))
                .build();
        SharePhotoContent photoContent = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        if (ShareDialog.canShow(photoContent.getClass())) {
            shareDialog = new ShareDialog(mActivity);
            registerShareListener(shareDialog, listener);
            shareDialog.show(photoContent);
        } else {
            listener.onComplete(ERROR);
        }
    }

    public static Bitmap decodeBitmapFromFile(String filePath, int inSampleSize) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // Calculate inSampleSize
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeFile(filePath, options);
    }

    @Override
    public boolean useIntentShare() {
        return false;
    }


    private ShareLinkContent prepareShareContent(ShareContent shareContent) {
        ShareLinkContent.Builder builder = new ShareLinkContent.Builder();
        builder.setContentDescription(shareContent.text);
        if (TextUtils.isEmpty(shareContent.title)) {
            builder.setContentTitle(shareContent.title);
        }
        if (!TextUtils.isEmpty(shareContent.targetUrl)) {
            Uri contentUrl = Uri.parse(shareContent.targetUrl);
            builder.setContentUrl(contentUrl);
        } else {
            Log.w("", "###please set target url");
        }

        if (!TextUtils.isEmpty(shareContent.imageUrl)) {
            Uri imageUri = Uri.parse(shareContent.imageUrl);
            builder.setImageUrl(imageUri);
        }

        return builder.build();
    }


    private void registerShareListener(ShareDialog shareDialog,
                                       final ShareListener listener) {
        shareDialog.registerCallback(mCallbackManager,
                new FacebookCallback<Sharer.Result>() {

                    @Override
                    public void onSuccess(Sharer.Result result) {
                        listener.onComplete(SUCCESS);
                    }

                    @Override
                    public void onCancel() {
                        listener.onComplete(CANCEL);
                    }

                    @Override
                    public void onError(FacebookException error) {
                        listener.onComplete(CANCEL);
                        if (error != null) {
                            error.printStackTrace();
                        }
                        listener.onComplete(ERROR);
                    }
                });
    }

    public static boolean hasFacebookSdk() {
        return true;
    }

    @Override
    public void detach() {
        // 注销回调  todo: 修改此方法是为了解决内存泄露问题 此方法在新版facebook sdk中是否需要修改 待验证
//        ((CallbackManagerImpl) mCallbackManager).unregisterCallback(CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode());
//        LoginManager.getInstance().detach();
    }
}
