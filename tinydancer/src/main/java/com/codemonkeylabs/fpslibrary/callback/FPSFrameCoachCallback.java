package com.codemonkeylabs.fpslibrary.callback;

import com.codemonkeylabs.fpslibrary.FPSConfig;
import com.codemonkeylabs.fpslibrary.ui.TinyCoach;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brianplummer on 8/29/15.
 */
public class FPSFrameCoachCallback extends FPSFrameCallback {
    private TinyCoach tinyCoach;

    public FPSFrameCoachCallback(FPSConfig fpsConfig, TinyCoach tinyCoach) {
        super(fpsConfig);
        this.tinyCoach = tinyCoach;
    }


    @Override
    public void calculateFps(List<Long> dataSet) {
        long fps = tinyCoach.showFps(fpsConfig, new ArrayList<>(dataSet));
        mFpsDataSet.add(fps);
    }
}
