package com.codemonkeylabs.fpslibrary;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class CalculationsTest extends TestCase {

    // 16.6ms
    private long oneFrameNS = TimeUnit.NANOSECONDS.convert(16600, TimeUnit.MICROSECONDS);

    @Test
    public void testBaseCase() {
        FPSConfig fpsConfig = new FPSConfig();
        List<Long> dataSet = new ArrayList<>();
        dataSet.add(0L);
        dataSet.add(TimeUnit.NANOSECONDS.convert(50, TimeUnit.MILLISECONDS));
        List<Integer> droppedSet = FpsCalculator.getDroppedSet(fpsConfig, dataSet);
        assertThat(droppedSet.size()).isEqualTo(1);
        assertThat(droppedSet.get(0)).isEqualTo(2);
    }

    @Test
    public void testBaseGetAmountOfFramesInSet() {
        FPSConfig fpsConfig = new FPSConfig();
        assertThat(FpsCalculator.getNumberOfFramesInSet(oneFrameNS, fpsConfig)).isEqualTo(1);
        assertThat(FpsCalculator.getNumberOfFramesInSet(oneFrameNS * 5, fpsConfig)).isEqualTo(5);
        assertThat(FpsCalculator.getNumberOfFramesInSet(oneFrameNS * 58, fpsConfig)).isEqualTo(58);
    }

    @Test
    public void testCalculateMetric() {
        FPSConfig fpsConfig = new FPSConfig();
        long start = 0;
        long end = oneFrameNS * 100;
        assertThat(FpsCalculator.getNumberOfFramesInSet(end, fpsConfig)).isEqualTo(100);

        List<Long> dataSet = new ArrayList<>();
        dataSet.add(start);
        dataSet.add(end);

        List<Integer> droppedSet = new ArrayList<>();

        droppedSet.add(4);
        assertThat(FpsCalculator.calculateMetric(fpsConfig, dataSet, droppedSet).getKey())
                .isEqualTo(FpsCalculator.Metric.GOOD);

        droppedSet.add(6);
        assertThat(FpsCalculator.calculateMetric(fpsConfig, dataSet, droppedSet).getKey())
                .isEqualTo(FpsCalculator.Metric.MEDIUM);

        droppedSet.add(10);
        assertThat(FpsCalculator.calculateMetric(fpsConfig, dataSet, droppedSet).getKey())
                .isEqualTo(FpsCalculator.Metric.BAD);
    }

    @Test
    public void testSb() throws Exception {
        StringBuilder stringBuilder = new StringBuilder("12,45,") ;
        int index = stringBuilder.lastIndexOf(",");
        assertEquals("12,45", stringBuilder.delete(index, stringBuilder.length()).toString()) ;

    }
}
