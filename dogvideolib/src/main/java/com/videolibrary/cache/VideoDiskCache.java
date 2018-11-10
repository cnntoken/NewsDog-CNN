package com.videolibrary.cache;

import android.content.Context;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 视频缓存类
 * Created by mrsimple on 22/4/16.
 */
public class VideoDiskCache {
    private static final long MB = 1024 * 1024;
    private Context mContext;
    private LruDiskCache mDiskLruCache;
    private Md5FileNameGenerator mFileNameGenerator = new Md5FileNameGenerator();
    private File mCacheDir;
    boolean initFailed = false;
    private static VideoDiskCache sInstance;

    private VideoDiskCache(Context context) {
        mContext = context;
        initCache();
    }

    public static VideoDiskCache getInstance(Context context) {
        if (sInstance == null) {
            synchronized (VideoDiskCache.class) {
                if ( sInstance == null && context != null ) {
                    sInstance = new VideoDiskCache(context);
                }
            }
        }
        return sInstance;
    }

    private void initCache() {
        if (mDiskLruCache == null) {
            try {
                mCacheDir = new File(StorageUtils.getCacheDirectory(mContext) + File.separator + "gif");
                if (!mCacheDir.exists()) {
                    mCacheDir.mkdir();
                }
                Log.e("", "### gif cache dir : " + mCacheDir.getAbsolutePath()) ;
                mDiskLruCache = new LruDiskCacheV2(mCacheDir, mFileNameGenerator, 50 * MB);
            } catch (Exception e) {
                e.printStackTrace();
                initFailed = true;
            } finally {
                if (initFailed && mDiskLruCache != null) {
                    Log.e("", "### 关闭 lru 缓存");
                    mDiskLruCache.close();
                }
            }
        }
    }

    private void checkCacheDir() {
        if (mCacheDir != null && !mCacheDir.exists()) {
            close();
            mDiskLruCache = null;
            initCache();
        }
    }

    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * 获取缓存文件
     *
     * @param videoUrl
     * @return
     */
    public File get(String videoUrl) {
        return mDiskLruCache != null ? mDiskLruCache.get(videoUrl) : null;
    }

    /**
     * 保存数据到缓存目录中
     *
     * @param videoUrl
     * @param inputStream
     * @throws IOException
     */
    public File save(String videoUrl, InputStream inputStream) throws IOException {
        checkCacheDir();
        if (mDiskLruCache != null) {
            mDiskLruCache.save(videoUrl, inputStream, null);
            return mDiskLruCache.get(videoUrl);
        }
        return null;
    }

    public void close() {
        if (mDiskLruCache != null) {
            mDiskLruCache.close();
        }
    }

}
