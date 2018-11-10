package com.videolibrary.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.videolibrary.task.GifDownloadTask.GIF_TYPE;

/**
 * Created by mrsimple on 10/5/17.
 */

public class GifNameGenerator {

    private static MessageDigest mDigest = null;

    public static String generateFileName(String url, int fileType) {
        String cacheName ;
        try {
            if ( mDigest == null ) {
                mDigest = MessageDigest.getInstance("MD5");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (fileType == GIF_TYPE) {
            cacheName = toMD5(url) + ".gif";
        } else {
            cacheName = toMD5(url) + ".mp4";
        }
        return cacheName;
    }


    /**
     * 对key进行MD5加密，如果无MD5加密算法，则直接使用key对应的hash值。</br>
     *
     * @param key
     * @return
     */
    public static String toMD5(String key) {
        String cacheKey;
        // 获取MD5算法失败时，直接使用key对应的hash值
        if (mDigest == null) {
            return String.valueOf(key.hashCode());
        }
        mDigest.update(key.getBytes());
        cacheKey = bytesToHexString(mDigest.digest());
        return cacheKey;
    }

    /**
     * @param bytes
     * @return
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
