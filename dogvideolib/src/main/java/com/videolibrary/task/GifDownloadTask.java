package com.videolibrary.task;

import android.content.Context;
import android.util.Log;

import com.videolibrary.cache.VideoDiskCache;
import com.videolibrary.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * 视频下载任务,如果有缓存则从缓存获取
 * Created by mrsimple on 22/4/16.
 */
public class GifDownloadTask extends DownloadTask<Void, Void, File> {
    public static final int MP4_TYPE = 1;
    public static final int GIF_TYPE = 2;
    public boolean isWaitToSave = false;
    private File mCacheFile = null;
    private final WeakReference<Context> mContextRef ;

    public GifDownloadTask(Context context, String url) {
        this(context, url, MP4_TYPE);
    }

    public GifDownloadTask(Context context,String url, int type) {
        super(url);
        mContextRef = new WeakReference<>(context) ;
    }


    @Override
    protected File doInBackground(Void... params) {
        if ( mContextRef.get() != null ) {
            mCacheFile = VideoDiskCache.getInstance(mContextRef.get()).get(mTargetUrl) ;
        }
        // 使用缓存
        if (mCacheFile != null && mCacheFile.exists()) {
            Log.e("", "#### 使用缓存的视频 : " + mCacheFile.getName());
            return mCacheFile;
        }
        return super.doInBackground(params);
    }


    @Override
    protected File parseResult(InputStream inputStream) {
        try {
            // 存储视频文件
            mCacheFile = VideoDiskCache.getInstance(mContextRef.get()).save(mTargetUrl, inputStream) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mCacheFile;
    }


    public String getGifUrl() {
        return mTargetUrl;
    }

    public void deleteGifFile() {
        FileUtils.deleteGifFile(mCacheFile, mTargetUrl);
    }

}
