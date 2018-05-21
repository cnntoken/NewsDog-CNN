package com.codemonkeylabs.fpslibrary;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Choreographer;
import android.view.Display;
import android.view.WindowManager;

import com.codemonkeylabs.fpslibrary.bg.Foreground;
import com.codemonkeylabs.fpslibrary.callback.ActivityCallback;
import com.codemonkeylabs.fpslibrary.callback.DoFrameCallback;
import com.codemonkeylabs.fpslibrary.callback.FPSFrameCallback;
import com.codemonkeylabs.fpslibrary.callback.FPSFrameCoachCallback;
import com.codemonkeylabs.fpslibrary.dump.FpsDump;
import com.codemonkeylabs.fpslibrary.ui.TinyCoach;

/**
 * Created by brianplummer on 8/29/15.
 */
public class TinyDancer {
    private FPSConfig fpsConfig = new FPSConfig();
    private TinyCoach tinyCoach;
    private Foreground.Listener foregroundListener = new Foreground.Listener() {
        @Override
        public void onBecameForeground() {
            if (tinyCoach != null) {
                tinyCoach.show();
            }
        }

        @Override
        public void onBecameBackground() {
            if (tinyCoach != null) {
                tinyCoach.hide(false);
            }
        }
    };

    private FPSFrameCallback frameCallback;
    private String activityName = "";
    private Context context;

    /**
     * create a TinyDancer instance
     *
     * @param context
     * @return
     */
    public static TinyDancer create(Context context) {
        return create(context, "");
    }

    /**
     * create a TinyDancer for specified activity
     *
     * @param context
     * @param activityName
     * @return
     */
    public static TinyDancer create(Context context, String activityName) {
        TinyDancer instance = new TinyDancer();
        instance.context = context.getApplicationContext();
        instance.activityName = activityName;
        // set device's frame rate info into the config
        instance.setFrameRate();
        return instance;
    }


    /**
     * configures the fpsConfig to the device's hardware
     * refreshRate ex. 60fps and deviceRefreshRateInMs ex. 16.6ms
     */
    private void setFrameRate() {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        fpsConfig.deviceRefreshRateInMs = 1000f / display.getRefreshRate();
        fpsConfig.refreshRate = display.getRefreshRate();
    }

    public TinyDancer setFpsConfig(FPSConfig fpsConfig) {
        this.fpsConfig = fpsConfig ;
        setFrameRate();
        return this;
    }

    /**
     * stops the frame callback and foreground listener
     * nulls out static variables
     * called from FPSLibrary in a static context
     */
    public void hide() {
        if (tinyCoach == null) {
            return;
        }
        destroy();
    }

    // PUBLIC BUILDER METHODS

    /**
     * show fps meter, this regisers the frame callback that
     * collects the fps info and pushes it to the ui
     */
    public void show() {
        if (overlayPermRequest(context)) {
            //once permission is granted then you must call show() again
            return;
        }

        //are we running?  if so, call tinyCoach.show() and return
        if (tinyCoach != null) {
            tinyCoach.show();
            return;
        }

        // create the presenter that updates the view
        tinyCoach = new TinyCoach(context.getApplicationContext(), fpsConfig);
        // create our choreographer callback and register it
        startFpsMonitor(context, new FPSFrameCoachCallback(fpsConfig, tinyCoach));
    }


    /**
     * show fps meter, this regisers the frame callback that
     * collects the fps info only .
     */
    public void start() {
        startFpsMonitor(context, new FPSFrameCallback(fpsConfig));
    }


    private void startFpsMonitor(Context context, FPSFrameCallback callback) {
        frameCallback = callback;
        // create our choreographer callback and register it
        Choreographer.getInstance().postFrameCallback(callback);
        //set activity background/foreground listener
        Foreground.init((Application) context.getApplicationContext()).addListener(foregroundListener);
    }


    /**
     * @return
     */
    public FpsData getFpsData() {
        FpsData fpsData;
        if (frameCallback == null) {
            fpsData = new FpsData();
        } else {
            fpsData = new FpsData(frameCallback.getFpsDataSet());
        }
        fpsData.setActivityName(activityName);
        return fpsData;
    }


    /**
     * auto start fps monitor
     */
    public static void install(Application application) {
        if ( application != null ) {
            application.registerActivityLifecycleCallbacks(new ActivityCallback());
        }
    }


    /**
     * auto start fps monitor
     */
    public static void install(Application application, FPSConfig config) {
        if ( application != null ) {
            application.registerActivityLifecycleCallbacks(new ActivityCallback());
        }
    }


    /**
     * this adds a frame callback that the library will invoke on the
     * each time the choreographer calls us, we will send you the frame times
     * and number of dropped frames.
     *
     * @param callback
     * @return
     */
    public TinyDancer addFrameDataCallback(DoFrameCallback callback) {
        fpsConfig.frameDataCallback = callback;
        return this;
    }

    /**
     * set red flag percent, default is 20%
     *
     * @param percentage
     * @return
     */
    public TinyDancer redFlagPercentage(float percentage) {
        fpsConfig.redFlagPercentage = percentage;
        return this;
    }

    /**
     * set red flag percent, default is 5%
     *
     * @param percentage
     * @return
     */
    public TinyDancer yellowFlagPercentage(float percentage) {
        fpsConfig.yellowFlagPercentage = percentage;
        return this;
    }

    /**
     * starting x position of fps meter default is 200px
     *
     * @param xPosition
     * @return
     */
    public TinyDancer startingXPosition(int xPosition) {
        fpsConfig.startingXPosition = xPosition;
        return this;
    }

    /**
     * starting y positon of fps meter default is 600px
     *
     * @param yPosition
     * @return
     */
    public TinyDancer startingYPosition(int yPosition) {
        fpsConfig.startingYPosition = yPosition;
        return this;
    }

    /**
     * starting gravity of fps meter default is Gravity.TOP | Gravity.START;
     *
     * @param gravity
     * @return
     */
    public TinyDancer startingGravity(int gravity) {
        fpsConfig.startingGravity = gravity;
        return this;
    }


    /**
     * should dump fps data to local file. The default value is false.
     *
     * @param isDump
     * @return
     */
    public TinyDancer dumpFps(boolean isDump) {
        fpsConfig.dumpFps = isDump;
        return this;
    }

    /**
     * request overlay permission when api >= 23
     *
     * @param context
     * @return
     */
    private boolean overlayPermRequest(Context context) {
        boolean permNeeded = false;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context
                        .getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                permNeeded = true;
            }
        }
        return permNeeded;
    }


    public void stop() {
        if (frameCallback != null) {
            frameCallback.stop();
        }
    }

    public void resume() {
        if (frameCallback != null) {
            frameCallback.resume();
        }
    }

    public void destroy() {
        if (frameCallback != null) {
            if (fpsConfig != null && fpsConfig.dumpFps) {
                FpsDump.dump(context.getApplicationContext(), getFpsData());
            }
            // tell callback to stop registering itself
            frameCallback.destroy();
        }

        if (tinyCoach != null) {
            // remove the view from the window
            tinyCoach.destroy();
        }

        Foreground.get(context).removeListener(foregroundListener);
        // null it all out
        tinyCoach = null;
        frameCallback = null;
        fpsConfig = null;
    }

}
