package com.codemonkeylabs.fpslibrary.callback;

import android.util.Log;
import android.view.Choreographer;

import com.codemonkeylabs.fpslibrary.FPSConfig;
import com.codemonkeylabs.fpslibrary.FpsCalculator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brianplummer on 8/29/15.
 */
public class FPSFrameCallback implements Choreographer.FrameCallback {
    protected FPSConfig fpsConfig;
    protected List<Long> frameTimeDataSet; //holds the frame times of the sample set
    private long startSampleTimeInNs = 0;
    private boolean isStop = false;
    protected final List<Long> mFpsDataSet = new LinkedList<>();

    public FPSFrameCallback(FPSConfig fpsConfig) {
        this.fpsConfig = fpsConfig;
        frameTimeDataSet = new ArrayList<>();
    }


    @Override
    public final void doFrame(long frameTimeNanos) {
        if (isStop) {
            Log.e("", "### stop detect fps. ");
            //we need to register for the next frame callback.   from API 16.
            Choreographer.getInstance().postFrameCallback(this);
            return;
        }
        if ( fpsConfig == null ) {
            return;
        }
        //initial case
        if (startSampleTimeInNs == 0) {
            startSampleTimeInNs = frameTimeNanos;
        }
        // only invoked for callbacks....
        else if (fpsConfig.frameDataCallback != null) {
            long start = frameTimeDataSet.get(frameTimeDataSet.size() - 1);
            int droppedCount = FpsCalculator.droppedCount(start, frameTimeNanos, fpsConfig.deviceRefreshRateInMs);
            fpsConfig.frameDataCallback.doFrame(start, frameTimeNanos, droppedCount);
        }

        //we have exceeded the sample length ~700ms worth of data...we should push results and save current
        //frame time in new list
        if (isFinishedWithSample(frameTimeNanos)) {
            collectSampleAndSend(frameTimeNanos);
        }

//        Log.e("", "### do frame : frameTimeNanos = " + frameTimeNanos);
        // add current frame time to our list
        frameTimeDataSet.add(frameTimeNanos);

        //we need to register for the next frame callback
        Choreographer.getInstance().postFrameCallback(this);
    }


    protected void collectSampleAndSend(long frameTimeNanos) {
        calculateFps(new LinkedList<>(frameTimeDataSet));
        // clear data
        frameTimeDataSet.clear();
        //reset sample timer to last frame
        startSampleTimeInNs = frameTimeNanos;
    }


    public void calculateFps(List<Long> dataSet) {
        List<Integer> droppedSet = FpsCalculator.getDroppedSet(fpsConfig, dataSet);
        AbstractMap.SimpleEntry<FpsCalculator.Metric, Long> answer = FpsCalculator.calculateMetric(fpsConfig,
                dataSet, droppedSet);
        // cache fps values
        mFpsDataSet.add(answer.getValue());
//        Log.e("", "####  tiny dancer fps : " + answer.getValue());
    }


    public List<Long> getFpsDataSet() {
        return mFpsDataSet;
    }

    /**
     * returns true when sample length is exceed
     *
     * @param frameTimeNanos current frame time in NS
     * @return
     */
    private boolean isFinishedWithSample(long frameTimeNanos) {
        return frameTimeNanos - startSampleTimeInNs > fpsConfig.getSampleTimeInNs();
    }


    public void stop() {
        isStop = true;
    }

    public void resume() {
        isStop = false;
    }


    public void destroy() {
        frameTimeDataSet.clear();
        mFpsDataSet.clear();
        fpsConfig = null;
    }

}
