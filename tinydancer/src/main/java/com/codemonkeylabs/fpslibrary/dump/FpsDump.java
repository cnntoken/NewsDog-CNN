package com.codemonkeylabs.fpslibrary.dump;

/**
 * Created by newsdog on 14/3/17.
 */

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.codemonkeylabs.fpslibrary.FpsData;

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

public final class FpsDump {
    // 用于格式化日期,作为日志文件名的一部分
    private static DateFormat sFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static boolean isClear = false;

    /**
     * dump fps values
     * @param context
     * @param fpsData
     */
    public static void dump(final Context context, final FpsData fpsData) {
        if ( context == null || fpsData == null || fpsData.getDataSet().size() == 0 ) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                dumpToFile(context, fpsData);
            }
        }.start();
    }


    private static void dumpToFile(final Context context, final FpsData fpsData) {
        BufferedWriter bos = null;
        try {
            String fileName = generateFpsFileName(context);
            Log.e("", "### fps file name : " + fileName);

            bos = new BufferedWriter(new FileWriter(fileName, true));
            bos.append("activity : " + fpsData.getActivityName()) ;
            bos.append("\n") ;
            StringBuilder stringBuilder = new StringBuilder() ;
            for (Long fpsValue : fpsData.getDataSet()) {
                stringBuilder.append(String.valueOf(fpsValue)).append(",");
            }

            int index = stringBuilder.lastIndexOf(",");
            stringBuilder = stringBuilder.delete(index, stringBuilder.length()) ;
            bos.append(stringBuilder.toString()) ;
            bos.append("\n");
            bos.append("Max : " + fpsData.getMax()) ;
            bos.append("\n") ;
            bos.append("Min : " + fpsData.getMin()) ;
            bos.append("\n") ;
            bos.append("Avg : " + fpsData.getAverage()) ;
            bos.append("\n");
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


    private static String generateFpsFileName(Context context) {
        String fileName = sFormatter.format(new Date()) + "_fps.txt";
        if (isExistSdCard()) {
            String path = Environment.getExternalStorageDirectory().getPath();
            //获取跟目录
            path = path + File.separator + context.getPackageName() + "/fps/";
            File dir = new File(path);
            // 第一次写入时清空缓存, 避免与上次的日志混淆
            if (!isClear) {
                isClear = true;
                deleteDirectory(dir);
            }
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fileName = path + fileName;
        }
        return fileName;
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
     * 判断sd卡是否存在
     *
     * @return
     */
    private static boolean isExistSdCard() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

}
