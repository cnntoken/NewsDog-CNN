package com.newsdog.fbsdk;

import android.content.Context;

/**
 * facebook app event
 * Created by ly on 2016/11/28.
 */
public class FBEvent {
    /**
     * 统计刷新事件
     *
     * @param context
     */
    public static void refreshEvent(Context context, String languageName, String category) {
    }

    /**
     * 统计阅读文章
     *
     * @param context
     */
    public static void articleReadEvent(Context context, String languageName ) {
    }

    /**
     * 统计阅读各个分类的文章
     *
     * @param context
     * @param category
     */
    public static void categoryArticleReadEvent(Context context, String category,  String languageName) {
    }

    /**
     * 统计阅读gcm push
     *
     * @param context
     */
    public static void gcmReadEvent(Context context, String languageName) {
    }

    /**
     * 分享统计
     *
     * @param context
     */
    public static void shareEvent(Context context, String languageName) {
    }

    /**
     * 评论统计
     *
     * @param context
     */
    public static void commentEvent(Context context, String languageName) {
    }


    /**
     * 视频阅读统计
     *
     * @param context
     */
    public static void videoEvent(Context context, String languageName) {
    }

    /**
     * 图集阅读统计
     *
     * @param context
     */
    public static void atlasReadEvent(Context context, String languageName) {
    }

    /**
     * 统计广告点击
     *
     * @param context
     */
    public static void adClickEvent(Context context, String languageName) {
    }
}
