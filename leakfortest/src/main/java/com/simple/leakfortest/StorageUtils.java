package com.simple.leakfortest;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by newsdog on 9/3/17.
 */

public final class StorageUtils {

    private static DateFormat sFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private static boolean isClear = false;

    static void saveResult(String leakInfo) {
        BufferedWriter bos = null;
        try {
            String fileName = generateLogFileName();
            Log.e("", "### leak file name : " + fileName);

            bos = new BufferedWriter(new FileWriter(fileName, true));
            bos.append(leakInfo);
            bos.append("\n\n");
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 删除目录
     *
     * @param directory
     * @return
     */
    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    /**
     *
     * @return
     */
    private static boolean isExistSdCard() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

}
