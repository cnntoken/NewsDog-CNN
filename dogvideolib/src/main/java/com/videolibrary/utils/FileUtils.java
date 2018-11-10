package com.videolibrary.utils;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by mrsimple on 10/5/17.
 */

public final class FileUtils {

    private FileUtils() {
    }

    public static byte[] file2byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    public static void deleteGifFile(File cacheFile, String url) {
        if (!TextUtils.isEmpty(url)) {
//            File file = new File(mCacheDir + "/" + mGifPath);
            if (cacheFile != null && cacheFile.exists()) {
                cacheFile.delete();
            }
        }
    }

    /**
     * 删除缓存文件
     * @param filePath
     */
    public static void deleteCacheFile(String filePath){
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        final File file = new File(filePath);
        if (file != null && file.exists()) {
            new Thread() {
                @Override
                public void run() {
                    file.delete();
                }
            }.start();
        }
    }
}
