package com.taosif7.android.ringchartlib.models;

import androidx.annotation.FloatRange;

public class RingChartData implements Comparable<RingChartData> {
    public float value;
    public int color;
    public String label;

    /**
     * Doughnut ring data item
     *
     * @param value absolute Integer value
     * @param color resolved int colour !!!NOT RESOURCE ID
     * @param label unique Label for this item
     */
    public RingChartData(@FloatRange(from = 0.0f, to = 1.0f) float value, int color, String label) {
        this.value = value;
        this.color = color;
        this.label = label;
    }

    @Override
    public int compareTo(RingChartData item) {
        // Not using Integer.compare method as it requires minimum api M
        return (int) ((value - item.value) * 100);
    }
}
