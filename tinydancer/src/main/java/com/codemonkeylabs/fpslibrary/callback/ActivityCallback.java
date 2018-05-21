package com.codemonkeylabs.fpslibrary.callback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.codemonkeylabs.fpslibrary.FPSConfig;
import com.codemonkeylabs.fpslibrary.TinyDancer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by newsdog on 14/3/17.
 */

public class ActivityCallback implements Application.ActivityLifecycleCallbacks {

    private static final Map<Integer, ActivityDancer> ACTIVITY_MAP = new HashMap<>() ;

    private FPSConfig mConfig ;

    public ActivityCallback() {
    }

    public ActivityCallback(FPSConfig mConfig) {
        this.mConfig = mConfig;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ActivityDancer dancer = ActivityDancer.create(activity) ;
        if ( mConfig != null ) {
            dancer.mDancer.setFpsConfig(mConfig) ;
        }
        ACTIVITY_MAP.put(activity.hashCode(), dancer) ;
        Log.e("", "### ===> onActivity Created dancer : " + dancer) ;
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ActivityDancer dancer = ACTIVITY_MAP.remove(activity.hashCode()) ;
        Log.e("", "### ---> onActivity  Destroyed  : " + activity + ", dancer : " + dancer) ;
        if ( dancer != null ) {
            dancer.mDancer.destroy();
        }
    }

    /**
     *
     */
    public static class ActivityDancer {
        WeakReference<Activity> mActivityRef ;
        TinyDancer mDancer ;

        public static ActivityDancer create(Activity activity) {
            ActivityDancer dancer = new ActivityDancer();
            dancer.mActivityRef = new WeakReference<>(activity) ;
            dancer.mDancer = TinyDancer.create(activity, activity.getClass().getName()) ;
            dancer.mDancer.start();
            return dancer;
        }

        @Override
        public String toString() {
            return "ActivityDancer{" +
                    "mActivityRef=" + mActivityRef.get() != null ? mActivityRef.get().getClass().getSimpleName() : "" +
                    ", mDancer=" + mDancer +
                    '}';
        }
    }
}
