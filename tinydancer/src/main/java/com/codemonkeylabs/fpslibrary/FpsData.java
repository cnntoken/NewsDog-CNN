package com.codemonkeylabs.fpslibrary;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * fps data object
 * Created by newsdog on 12/12/16.
 */
public class FpsData {
    private List<Long> mDataSet = new LinkedList<>();
    private long mMax;
    private long mMin;
    private long mAverage;
    /**
     * Activity name
     */
    private String mActivityName ;

    public FpsData() {
    }

    public FpsData(List<Long> dataSet) {
        mDataSet.addAll(dataSet);
        if ( mDataSet.size() > 0 ) {
            mMax = Collections.max(mDataSet);
            mMin = Collections.min(mDataSet);
            long sum = 0;
            for (Long value : mDataSet) {
                sum += value;
            }
            mAverage = (int) (sum / mDataSet.size());
        }
    }

    public String getActivityName() {
        return mActivityName;
    }

    public void setActivityName(String mActivityName) {
        this.mActivityName = mActivityName;
    }

    public List<Long> getDataSet() {
        return mDataSet;
    }

    public long getMax() {
        return mMax;
    }

    public long getMin() {
        return mMin;
    }

    public long getAverage() {
        return mAverage;
    }

}
