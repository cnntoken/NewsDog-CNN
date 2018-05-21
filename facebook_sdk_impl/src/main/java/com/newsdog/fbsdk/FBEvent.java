package com.newsdog.fbsdk;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.appevents.AppEventsLogger;

/**
 * facebook app event
 * Created by ly on 2016/11/28.
 */
public class FBEvent {

    private static final String REFRESH = "refresh";
    private static final String ARTICLE_READ = "article_read";
    private static final String GCM_ARTICLE_READ = "gcm_article_read";
    private static final String SHARE = "share";
    private static final String COMMENT = "comment";
    private static final String VIDEO = "video_read";
    private static final String ATLAS = "atlas_read";
    private static final String AD = "advertisement_click";
    private static final String CATEGORY = "category";

    /**
     * 统计刷新事件
     *
     * @param context
     */
    public static void refreshEvent(Context context, String languageName, String category) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        if (!TextUtils.isEmpty(category)) {
            Bundle bundle = new Bundle();
            bundle.putString(CATEGORY, category);
            logger.logEvent(getEventName(REFRESH, languageName), bundle);
        } else {
            logger.logEvent(getEventName(REFRESH, languageName));
        }
    }

    /**
     * 统计阅读文章
     *
     * @param context
     */
    public static void articleReadEvent(Context context, String languageName ) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(ARTICLE_READ, languageName));
    }

    /**
     * 统计阅读各个分类的文章
     *
     * @param context
     * @param category
     */
    public static void categoryArticleReadEvent(Context context, String category,  String languageName) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(category + "_" + ARTICLE_READ, languageName));
    }

    /**
     * 统计阅读gcm push
     *
     * @param context
     */
    public static void gcmReadEvent(Context context, String languageName) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(GCM_ARTICLE_READ, languageName));
    }

    /**
     * 分享统计
     *
     * @param context
     */
    public static void shareEvent(Context context, String languageName) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(SHARE, languageName));
    }

    /**
     * 评论统计
     *
     * @param context
     */
    public static void commentEvent(Context context, String languageName) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(COMMENT, languageName));
    }


    /**
     * 视频阅读统计
     *
     * @param context
     */
    public static void videoEvent(Context context, String languageName) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(VIDEO, languageName));
    }

    /**
     * 图集阅读统计
     *
     * @param context
     */
    public static void atlasReadEvent(Context context, String languageName) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(ATLAS, languageName));
    }

    /**
     * 统计广告点击
     *
     * @param context
     */
    public static void adClickEvent(Context context, String languageName) {
        AppEventsLogger logger = AppEventsLogger.newLogger(context.getApplicationContext());
        logger.logEvent(getEventName(AD, languageName));
    }

    private static String getEventName(String action, String languageName) {
        if (TextUtils.isEmpty(languageName)) {
            return "en" + "_" + action;
        } else {
            return languageName + "_" + action;
        }
    }
}
