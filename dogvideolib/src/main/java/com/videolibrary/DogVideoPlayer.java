package com.videolibrary;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.videolibrary.cache.VideoDiskCache;
import com.videolibrary.task.GifDownloadTask;
import com.videolibrary.utils.FileUtils;
import com.videolibrary.widget.TextureVideoView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>视频播放器，库的外面所有使用的接口也在这里</p>
 * <p>Jiecao video player，all outside the library interface is here</p>
 */
public class DogVideoPlayer implements View.OnClickListener, TextureVideoView.MediaPlayerCallback {

    //控件
    public ImageView ivStart;
    TextureVideoView videoTextureView;
    public ImageView ivCover;
    ProgressBar pbLoading;

    //属性
    private String videoUrl;

    public int mVideoState = -1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PREPARE = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSE = 3;

    private View mRootView;
    private Context mContext;

    private GifDownloadTask mVideoTask;
    private VideoDiskCache mCache;

    private boolean isControlViewShow = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    /**
     * 播放相关事件
     */
    private PlayListener mPlayListener;
    boolean isGif = false ;

    /**
     * 状态是否改变
     */
    private boolean mStatusChanged = false;

    public DogVideoPlayer(Context context, View rootView) {
        this.mRootView = rootView;
        mContext = context;
        init();
    }


    <T extends View> T findViewById(int id) {
        return (T) mRootView.findViewById(id);
    }

    private void init() {
        ivStart = findViewById(R.id.start);
        ivStart.setOnClickListener(this);
        pbLoading = findViewById(R.id.loading);
        ivCover = findViewById(R.id.cover);
        videoTextureView = findViewById(R.id.texture_view);
        videoTextureView.setMediaPlayerCallback(this);
        videoTextureView.setOnClickListener(this);
    }


    public Context getContext() {
        return mContext;
    }

    TextureClickStrategy mClickStrategy ;

    /**
     * <p>配置要播放的内容</p>
     * <p>Configuring the Content to Play</p>
     *
     * @param url 视频地址 | Video address
     */
    public void setUp(String url, boolean isGif) {
        this.videoUrl = url;
        reset();
        this.isGif = isGif ;
        mClickStrategy = isGif ? new GifClickStrategy() : new VideoClickStrategy();
        updateStartImage();
    }

    /**
     * 重置video 状态
     */
    public void reset() {
        mVideoState = STATE_NORMAL;
        changeUiToNormal();
        mStatusChanged = false;
        if (videoTextureView != null) {
            videoTextureView.stop();
        }
    }

    /**
     * 播放video 超时
     */
    private void sendTimeoutPrepareMsg() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mVideoState == STATE_PREPARE) {
                    mVideoState = STATE_NORMAL;
                    videoTextureView.stop();
                    changeUiToNormal();
                    Log.e("", "### play gif timeout") ;
                }
            }
        }, 45 * 1000);
    }

    /**
     * 播放按钮/TextureView 点击事件
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.start) {
            startLoadVideo();
        } else if (i == R.id.texture_view) {
            mClickStrategy.click();
        } else {
            reset();
        }
    }

    /**
     * 隐藏控制控件
     */
    private void showControlView() {
        isControlViewShow = true;
        ivStart.setVisibility(View.VISIBLE);
    }

    /**
     * 显示控制控件
     */
    private void dismissControlView() {
        isControlViewShow = false;
        ivStart.setVisibility(View.GONE);
    }

    /**
     * 自动隐藏控制控件
     */
    Runnable controlViewThread = new Runnable() {
        @Override
        public void run() {
            dismissControlView();
        }
    };

    /**
     * 点击播放按钮
     */
    private void startLoadVideo() {
        if (mPlayListener != null && mPlayListener.onIntercept()) {
            return;
        }
        if (TextUtils.isEmpty(videoUrl)) {
            Toast.makeText(mContext, R.string.empty_video_url, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isOnline(mContext)) {
            Toast.makeText(mContext, "Network is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        switchVideoState();
    }

    /**
     * 切换视频状态
     */
    private void switchVideoState() {
        switch (mVideoState) {
            case STATE_NORMAL:
                if (mPlayListener != null) {
                    mPlayListener.onPreLoad();
                }
                prepareVideoToPlay(false, false);
                break;
            case STATE_PLAYING:
                playToPause();
                break;
            case STATE_PAUSE:
                pauseToPlay();
                break;
        }
    }

    /**
     * 播放video
     *
     * @param auto todo 该参数用于区分视频是自动加载还是手动点击播放按钮加载(用于记录flurry 事件)
     * @param restart 失败导致重新播放
     */
    private void prepareVideoToPlay(boolean auto, boolean restart) {
        if ( TextUtils.isEmpty(videoUrl) ) {
            return;
        }

        mVideoState = STATE_PREPARE;
        changeUiToShowUiPrepare();
        mStatusChanged = true;

        if (mCache == null) {
            mCache = VideoDiskCache.getInstance(mContext.getApplicationContext());
        }
        preparePlayVideo(videoUrl, auto, restart);
        sendTimeoutPrepareMsg();
        if (mPlayListener != null) {
            mPlayListener.onLoadStarted(auto);
        }
    }

    private void preparePlayVideo(final String url, final boolean auto, boolean restart) {
        if (restart) {
            Log.e("", "### restart restart : " + url) ;
            startPlayVideo(url);
            return;
        }
        // 取消上个任务, todo : 不取消,但是可以设置一个标识位, 下载完成之后不播放.
        if (mVideoTask != null && mVideoTask.getStatus() != AsyncTask.Status.FINISHED) {
            mVideoTask.cancel(true);
            // delete gif after cancel
            mVideoTask.deleteGifFile();
            sTaskList.remove(mVideoTask);
        }
        cancelAllTask();
        isReleased = false ;

        mVideoTask = new GifFileDownloadTask(this, url, auto);
        mVideoTask.executeOnExecutor(VIDEO_TASK);
        sTaskList.add(mVideoTask) ;
    }

    private static final ExecutorService VIDEO_TASK = Executors.newCachedThreadPool() ;

    /**
     * gif 下载任务
     */
    private static class GifFileDownloadTask extends GifDownloadTask {

        WeakReference<DogVideoPlayer> mPlayRef ;
        boolean isAutoPlay = false ;

        public GifFileDownloadTask(DogVideoPlayer player, String url, boolean auto) {
            super(player.getContext(), url);
            mPlayRef = new WeakReference<>(player) ;
            isAutoPlay = auto ;
        }

        @Override
        protected void onPostExecute(File file) {
            DogVideoPlayer player = mPlayRef.get() ;
            if (player == null || player.isReleased || isCancelled()) {
                return;
            }
            player.playVideo(file, mTargetUrl, isAutoPlay);
            sTaskList.remove(this);
            super.onPostExecute(file);
        }

        @Override
        protected void onCancelled() {
            this.deleteGifFile();
            sTaskList.remove(this);
        }
    }

    private void playVideo(File gifFile, String url, boolean autoPlay) {
        if (mPlayListener != null) {
            mPlayListener.onPlayStarted(autoPlay);
        }
        if ( isCacheValid(gifFile ) ) {
            startPlayVideo(gifFile.getPath());
            if ( mPlayListener != null ) {
                mPlayListener.onDownload(gifFile);
            }
        } else {
            startPlayVideo(url);
        }
//        Log.e("", "### video path : " + videoTextureView.getVideoURI().toString() + ", url : " + url) ;
    }


    private static boolean isCacheValid(File file) {
        return file != null && file.exists() ;
    }


    /**
     * 播放视频
     * @param videoUrl
     */
    private void startPlayVideo(String videoUrl) {
        videoTextureView.setVisibility(View.VISIBLE);
        videoTextureView.setVideoPath(videoUrl);
        videoTextureView.start();
    }

    public static void cancelAllTask() {
        if (sTaskList.size() == 0) {
            return;
        }
        for (AsyncTask task : sTaskList) {
            task.cancel(true) ;
        }
        sTaskList.clear();
    }

    private static List<AsyncTask> sTaskList = new LinkedList<>() ;


    private void playToPause() {
        mVideoState = STATE_PAUSE;
        changeUiToShowUiPause();
        videoTextureView.pause();
    }

    private void pauseToPlay() {
        mVideoState = STATE_PLAYING;
        changeUiToShowUiPlaying();
        videoTextureView.resume();
        isControlViewShow = false;
    }

    //Unified management Ui
    private void changeUiToNormal() {
        ivStart.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.GONE);
        videoTextureView.setVisibility(View.GONE);
        ivCover.setVisibility(View.VISIBLE);
        updateStartImage();
//        Log.e("", "### changeUiToNormal") ;
    }

    private void changeUiToShowUiPrepare() {
        ivStart.setVisibility(View.GONE);
        pbLoading.setVisibility(View.VISIBLE);
        ivCover.setVisibility(View.VISIBLE);

//        Log.e("", "### changeUiToShowUiPrepare") ;
    }

    private void changeUiToShowUiPlaying() {
        ivStart.setVisibility(View.GONE);
        pbLoading.setVisibility(View.GONE);
        ivCover.setVisibility(View.GONE);
        updateStartImage();
        // 清空超时消息
        mHandler.removeCallbacksAndMessages(null);

//        Log.e("", "### changeUiToShowUiPlaying") ;
    }

    private void changeUiToShowUiPause() {
        ivStart.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.GONE);
        ivCover.setVisibility(View.GONE);
        updateStartImage();
    }


    private void updateStartImage() {
        if (mVideoState == STATE_PLAYING) {
            ivStart.setImageResource(R.drawable.click_video_pause_selector);
        } else {
            if ( isGif ) {
                ivStart.setImageResource(R.drawable.gif_icon);
            } else {
                ivStart.setImageResource(R.drawable.click_video_play_selector);
            }
        }
    }

    /**
     * 列表滚动触发播放
     */
    public void setActive() {
        if (!videoTextureView.isPlaying()) {
            prepareVideoToPlay(true, false);
        }
    }

    /**
     * 列表滚动停止播放
     */
    public void deactivate() {
        reset();
    }

    public void release() {
        videoTextureView.release();
        mVideoState = STATE_NORMAL;
        changeUiToNormal();
        mHandler.removeCallbacksAndMessages(null);
        isReleased = true ;
    }

    private boolean isReleased = false ;

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mVideoState = STATE_PLAYING;
            changeUiToShowUiPlaying();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            mVideoState = STATE_PLAYING;
            changeUiToShowUiPlaying();
            return true;
        }
//        Log.e("",  "## onInfo ") ;
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mVideoState = STATE_NORMAL;
        changeUiToNormal();
        restartPlay();
        Log.e("",  "## play gif error, what : " + what + ", extra : " + extra) ;
        return true;
    }

    /**
     * 缓存播放失败后，使用线上地址重新播放视频
     */
    private void restartPlay() {
        final Uri videoUri = videoTextureView.getVideoURI();
        // 本地视频播放失败了,则使用线上视频播放.
        if (videoUri != null && !Patterns.WEB_URL.matcher(videoUri.toString()).matches() && !TextUtils.isEmpty(videoUrl)) {
            // 1.删除本地视频
            FileUtils.deleteCacheFile(videoUri.toString());
            // 2.重新播放
            prepareVideoToPlay(false, true);
        }
    }

    /**
     * <p>
     * True if the device is connected or connection to network.
     * </p>
     * 需要权限: <code>android.permission.ACCESS_NETWORK_STATE</code> </p>
     *
     * @param context
     * @return 如果当前有网络连接返回 true 如果网络状态访问权限或没网络连接返回false
     */
    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }

    public void setPlayListener(PlayListener listener) {
        this.mPlayListener = listener;
    }


    interface TextureClickStrategy {
        void click();
    }


    class VideoClickStrategy implements TextureClickStrategy {
        @Override
        public void click() {
            switch (mVideoState) {
                case STATE_NORMAL:
                    startLoadVideo();
                    break;
                case STATE_PLAYING:
                case STATE_PAUSE:
                    if (isControlViewShow) {
                        dismissControlView();
                        mHandler.removeCallbacks(controlViewThread);
                    } else {
                        showControlView();
                        mHandler.postDelayed(controlViewThread, 2500);
                    }
                    break;
            }
        }
    }

    /**
     * 点击播放gif的视图, 如果是播放状态, 那么点击后变为暂停; 如果是未播放状态则变为播放.
     */
    class GifClickStrategy implements TextureClickStrategy {
        @Override
        public void click() {
            if (mStatusChanged) { // 视频已经从初始状态改变，所以点击Texture只是用来显示或者隐藏控件。
                if (mVideoState == STATE_PREPARE) {
                    return;
                }
                // 显示或者隐藏 播放暂停 按钮。
                switchVideoState();
            } else {
                startLoadVideo();
            }
        }
    }

    /**
     * 播放监听器
     */
    public interface PlayListener {
        /**
         * 拦截点击事件事件,如果返回true,那么则不会执行点击操作,此时操作可以写在onIntercept函数中
         *
         * @return
         */
         boolean onIntercept();

        /**
         * 点击事件触发之前
         */
         void onPreLoad();

        /**
         * 开始加载
         *
         * @param auto 是否为点击
         */
         void onLoadStarted(boolean auto);

        /**
         * 开始播放
         *
         * @param auto
         */
         void onPlayStarted(boolean auto);

        /**
         * 视频下载完成
         * @param cache
         */
        void onDownload(File cache);
    }
}
