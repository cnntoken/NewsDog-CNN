package com.videolibrary.task;

import android.os.AsyncTask;
import android.util.Log;

import com.videolibrary.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载任务
 * Created by mrsimple on 22/4/16.
 */
public abstract class DownloadTask<Params, Progress, R> extends AsyncTask<Params, Progress, R> {
    protected String mTargetUrl = "";
    protected int mTimeOut = 60 * 1000;  // 一分钟
    protected int mContentLength ;

    public DownloadTask(String url) {
        mTargetUrl = url;
    }

    protected HttpURLConnection createUrlConnection() throws IOException {
        // 网络请求
        URL url = new URL(mTargetUrl);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setDoInput(true);
        // 默认设置1分钟链接超时
        urlConn.setConnectTimeout(mTimeOut);
        return urlConn;
    }

    @Override
    protected R doInBackground(Params... params) {
        R result = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = createUrlConnection();
            // 在执行请求执行的处理函数
            beforeExecuteRequest(connection);
            // 获取response code
            connection.getResponseCode();
            mContentLength = connection.getContentLength();
            // 请求成功
            if (connection.getResponseCode() == 200) {
                inputStream = connection.getInputStream();
                // 下载的数据合法
                result = parseResult(inputStream);
            } else {
                inputStream = connection.getErrorStream();
                // 错误处理
                parseErrorStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            StreamUtils.closeSilently(inputStream);
        }
        return result;
    }

    protected void beforeExecuteRequest(HttpURLConnection connection) {

    }

    /**
     * @param inputStream
     * @return
     */
    protected abstract R parseResult(InputStream inputStream);

    /**
     * @param errStream
     * @throws IOException
     */
    protected void parseErrorStream(InputStream errStream) throws IOException {
        Log.e("", " download 请求失败 : " + StreamUtils.streamToString(errStream));
    }
}
