package com.videolibrary.cache;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mrsimple on 28/12/17.
 */

public class LruDiskCacheV2 extends LruDiskCache {

    public LruDiskCacheV2(File cacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize) throws IOException {
        super(cacheDir, fileNameGenerator, cacheMaxSize);
    }

    public LruDiskCacheV2(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long
            cacheMaxSize, int cacheMaxFileCount) throws IOException {
        super(cacheDir, reserveCacheDir, fileNameGenerator, cacheMaxSize, cacheMaxFileCount);
    }

    @Override
    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        if ( cache == null ) {
            return false ;
        }
        return super.save(imageUri, bitmap);
    }

    @Override
    public boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException {
        if ( cache == null ) {
            return false ;
        }
        return super.save(imageUri, imageStream, listener);
    }

    @Override
    public File get(String imageUri) {
        if ( cache == null ) {
            return null ;
        }
        return super.get(imageUri);
    }

    @Override
    public boolean remove(String imageUri) {
        if ( cache == null ) {
            return false ;
        }
        return super.remove(imageUri);
    }

    @Override
    public void close() {
        if ( cache == null ) {
            return ;
        }
        super.close();
    }

    @Override
    public void clear() {
        if ( cache == null ) {
            return ;
        }
        super.clear();
    }
}
