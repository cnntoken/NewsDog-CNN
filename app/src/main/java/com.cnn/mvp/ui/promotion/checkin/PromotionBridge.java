package com.newsdog.mvp.ui.promotion.checkin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;

import com.newsdog.app.NewsDogApp;
import com.newsdog.config.PermissionConfig;
import com.newsdog.config.PromotionConfig;
import com.newsdog.constants.Constants;
import com.newsdog.dialog.share.SystemShareDialog;
import com.newsdog.facebook.ShareContent;
import com.newsdog.facebook.ShareListener;
import com.newsdog.mvp.ui.main.MainActivity;
import com.newsdog.mvp.ui.main.newslist.presenter.SocialPresenter;
import com.newsdog.mvp.ui.main.newslist.presenter.action.SharePresenter;
import com.newsdog.mvp.ui.promotion.ads.InterstitialAdLoaderPoolWrap;
import com.newsdog.mvp.ui.promotion.bridge.BaseBridge;
import com.newsdog.mvp.ui.promotion.share.SmsShareActivity;
import com.newsdog.mvp.ui.promotion.share.SmsSharePresenter;
import com.newsdog.mvp.ui.promotion.toast.PromotionToast;
import com.newsdog.mvp.ui.promotion.utils.PromImgCache;
import com.newsdog.mvp.ui.promotion.utils.PromotionGAEvent;
import com.newsdog.mvp.ui.promotion.widgets.HourlyRewardsLayout;
import com.newsdog.net.protocol.ErrorCode;
import com.newsdog.thirdpart.events.PromotionEvent;
import com.newsdog.ui.R;
import com.newsdog.utils.DeviceUtils;
import com.newsdog.utils.LogUtils;
import com.newsdog.utils.face2face.ApkFinder;
import com.newsdog.utils.ui.ToastUtils;
import com.simple.jsbridge.JsCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class PromotionBridge extends BaseBridge {
    protected SocialPresenter mSocialPresenter;
    private SmsSharePresenter mSharePresenter;
    private JsCallback mFlauntCallback;
    private boolean mHasRequestContact = false;
    private InterstitialAdLoaderPoolWrap mInterstitialAdLoader;
    protected JsCallback mLoginCallback ;

    private boolean refreshPageWhenClickShareFlag = false; 

    public PromotionBridge(String injectName, WebView webview, SocialPresenter presenter) {
        super(injectName, webview);
        mSocialPresenter = presenter ;
    }

    public void attach(Activity activity) {
        super.attach(activity);
        mSharePresenter = new SmsSharePresenter();
        mSharePresenter.attach(activity, null);

        mInterstitialAdLoader = new InterstitialAdLoaderPoolWrap(mActivityRef) ;
        EventBus.getDefault().register(this);
    }

    @Override
    public void detach() {
        super.detach();
        EventBus.getDefault().unregister(this);
        if ( mInterstitialAdLoader != null ) {
            mInterstitialAdLoader.destroy();
        }
        if ( mSocialPresenter != null ) {
            mSocialPresenter.detach();
        }
    }


    public void share(final JSONObject param, final JsCallback callback) {
        if (isDetached()) {
            return;
        }
        if (!DeviceUtils.isOnline(getContext())) {
            ToastUtils.shortToast(getContext(), R.string.no_network);
            if (callback != null ) {
                callback.apply(createCallbackJson(-1, "error"));
            }
            return;
        }

        String platform = param.optString("platform") ;

        boolean hasImageCoordinate = !TextUtils.isEmpty(param.optString("image_coordinate")) ;
        if ( "fb".equalsIgnoreCase(platform) ) {
            shareToFacebook(param, callback);
        } else if (hasImageCoordinate) {
            FlauntPresenter.startCaptureBitmap(getWebView(), param);
            mFlauntCallback = callback ;
        } else  {
            shareToWhatsAppOrSms(param, callback);
        }
        sendShareLog(platform, param.optString("from"));
    }

    private void shareToWhatsAppOrSms(JSONObject param, final JsCallback callback) {
        String platform = param.optString("platform") ;
        String title = param.optString("title");
        String text = param.optString("text") + param.optString("url");
        boolean hasImageCoordinate = !TextUtils.isEmpty(param.optString("image_coordinate")) ;
        boolean haveImg = param.optBoolean("have_img") ;
        Uri imageUri = null;
        if ( haveImg ) {
            imageUri = hasImageCoordinate ? FlauntPresenter.getImageUri() : PromImgCache.getPromotionImgUri() ;
        }
        if ("whatsapp".equalsIgnoreCase(platform)) {
            mSocialPresenter.shareTo(title, text , Constants.WHATSAPP_PACKAGE_NAME, imageUri);
            setRefreshPageWhenClickShareFlag(true); 
            callbackSuccess(callback);
        } else if ("sms".equalsIgnoreCase(platform)) {
            if (hasImageCoordinate) {
                if (Build.VERSION.SDK_INT >= 19 ) {
                    mSocialPresenter.shareTo(title, text , Telephony.Sms.getDefaultSmsPackage(getContext()), imageUri);
                } else {
                    mSocialPresenter.shareTo(title, text , null, imageUri);
                }
            } else {
                SmsShareActivity.openSmsShare(mActivityRef.get(), text);
            }
            callbackSuccess(callback);
        } else if ("other".equals(platform)){
            final SystemShareDialog shareDialog = new SystemShareDialog(mActivityRef.get()) ;
            shareDialog.addFilterPackage(Constants.WHATSAPP_PACKAGE_NAME) ;
            shareDialog.addFilterPackage(Constants.FACEBOOK_PACKAGE_NAME) ;
            shareDialog.addFilterPackage("com.tencent.mobileqq") ;
            shareDialog.addFilterPackage("com.lenovo.anyshare.gps") ;
            shareDialog.addFilterPackage("cn.xender") ;
            if (Build.VERSION.SDK_INT >= 19 ) {
                shareDialog.addFilterPackage(Telephony.Sms.getDefaultSmsPackage(getContext()));
            }
            shareDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    callbackSuccess(callback);
                }
            });
            shareDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!shareDialog.isClickedItem && callback != null ) {
                        callback.apply(createCallbackJson(-1, "error"));
                    }
                    mFlauntCallback = null;
                }
            });
            Intent intent = SharePresenter.createShareIntent(title, text, imageUri) ;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareDialog.setShareIntent(intent);
            shareDialog.show();
        }
    }

    public void isAppInstalled(final JSONObject param, final JsCallback callback)  {
        if (isDetached()) {
            return;
        }
        if (callback != null && param != null ) {
            callback.apply(ApkFinder.makeAppsInstalledResponse(param.optString("packages")));
        }
    }


    public void shareApp(final JSONObject param, final JsCallback callback) {
        if (isDetached()) {
            return;
        }
        String targetPlatform = param.optString("platform");
        String shareApkPkg = param.optString("s_apk", NewsDogApp.getAppContext().getPackageName()) ;
        File apkFile = ApkFinder.copyAndGetApkFile(shareApkPkg, param.optString("name", ApkFinder.APP_NAME)) ;
        if ( !TextUtils.isEmpty(targetPlatform)
                && !TextUtils.isEmpty(shareApkPkg)
                && ApkFinder.isValidApkFile(apkFile)) {
            mSocialPresenter.shareApp(targetPlatform , apkFile,
                    ApkFinder.json2Bundle(param.optJSONObject("params")));
            callbackSuccess(callback);
            sendShareLog(targetPlatform, param.optString("from", "h5"));
        } else {
            ToastUtils.longToast(getContext(), R.string.failed);
            callback.apply(createCallbackJson(-1, "error"));
        }
    }

    public void loadInterstitialAd(final JSONObject param, final JsCallback callback) {
        if (isDetached() || mInterstitialAdLoader == null  ) {
            return;
        }
        Log.e("", "### loadInterstitialAd bridge") ;
        mInterstitialAdLoader.load();
        fireCallback(callback);
    }

    public void showInterstitialAd(final JSONObject param, final JsCallback callback) {
        if (isDetached() || mInterstitialAdLoader == null ) {
            return;
        }
        Log.e("", "### showInterstitialAd bridge ") ;
        mInterstitialAdLoader.show();
        fireCallback(callback);
    }


    private void sendShareLog(String platform, String from) {
        Map<String, String> params = new HashMap<>() ;
        params.put("platform", platform) ;
        params.put("from", from) ;
        PromotionGAEvent.logEvent(PromotionGAEvent.SHARE, params);
    }


    public void onEventMainThread(PromotionEvent event) {
        if ( event.type == PromotionEvent.FLAUNT && event.extra != null ) {
            try {
                JSONObject jsonObject = new JSONObject(event.extra.getString("data"));
                shareToWhatsAppOrSms(jsonObject, mFlauntCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void shareToFacebook(JSONObject param, final JsCallback callback) {
        if (!DeviceUtils.isOnline(getContext())) {
            ToastUtils.shortToast(getContext(), R.string.no_network);
            if (callback != null ) {
                callback.apply(createCallbackJson(-1, "error"));
            }
            return;
        }
        String url = param.optString("url") ;
        ShareContent shareContent = new ShareContent();
        shareContent.title = param.optString("title");
        shareContent.text = param.optString("text") + url;
        shareContent.imageUrl = param.optString("image");
        shareContent.targetUrl = url;
        shareContent.original = url;
        shareContent.shareUrl = url ;
        mSocialPresenter.shareToFacebook(shareContent, new ShareListener() {

            @Override
            public void onComplete(int code) {
                if (code != ErrorCode.SUCCESS || shouldNotCallback(callback)) {
                    return;
                }
                callbackSuccess(callback);
            }
        });
    }

    public void onJsShare(final JSONObject param, final JsCallback callback) {
        if (isActivityDetach() || isDetached() ) {
            return;
        }
        PromotionToast.showToast(mActivityRef.get(), R.string.share_success, param.optInt("coin"));
        PromotionGAEvent.logEvent(PromotionGAEvent.SHARE_SUCCESS);
        fireCallback(callback);
    }



    /**
     * @param param
     * @param callback
     */
    public void requirePermissions(final JSONObject param, final JsCallback callback) {
        if (isDetached() || isActivityDetach()) {
            return;
        }
        if (mHasRequestContact || PermissionConfig.getConfig().isContactsRequested()) {
            return;
        }
        if (mSharePresenter != null) {
            mHasRequestContact = true;
            mSharePresenter.setRequestTips(param != null ? param.optString("text") : null);
            mSharePresenter.requestPermissionContact();
        }
        fireCallback(callback);
    }


    public void login(final JSONObject param, final JsCallback callback) {
        if (isDetached() || isActivityDetach() ) {
            return;
        }
        EventBus.getDefault().post(PromotionEvent.createLoginEvent());
        fireCallback(callback);
    }


    public void getLastTimerCoinsTs(final JSONObject param, final JsCallback callback) {
        if (isDetached() || isActivityDetach()) {
            return;
        }
        JSONObject result = new JSONObject();
        try {
            result.put("ts", PromotionConfig.getCoinsTime());
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            fireCallback(callback, result);
        }
    }

    @Deprecated
    public void onTimerCoinsSuccess(final JSONObject param, final JsCallback callback) {
        if (isDetached() || isActivityDetach()) {
            return;
        }
        if (param != null) {
            PromotionConfig.saveCoinsTime(param.optLong("ts"));
            EventBus.getDefault().post(new HourlyRewardsLayout.RewardsEvent());
        }
        fireCallback(callback, new JSONObject());
    }


    protected void callbackSuccess(JsCallback callback) {
        if ( callback != null ) {
            callback.apply(createCallbackJson(ErrorCode.SUCCESS, "ok"));
        }
    }

    protected static JSONObject createCallbackJson(int code, String msg) {
        JSONObject object = new JSONObject();
        try {
            object.putOpt("code", code);
            object.putOpt("msg", msg);
            return object;
        } catch (JSONException var3) {
            var3.printStackTrace();
            return object;
        }
    }

    public void setRefreshPageWhenClickShareFlag(boolean refresh){
        refreshPageWhenClickShareFlag = refresh;
    }

    public boolean getRefreshPageWhenClickShareFlag(){
        return refreshPageWhenClickShareFlag;
    }
}
