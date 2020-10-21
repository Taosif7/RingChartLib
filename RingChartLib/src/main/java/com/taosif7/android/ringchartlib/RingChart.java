package com.taosif7.android.ringchartlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import androidx.annotation.FloatRange;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.taosif7.android.ringchartlib.models.RingChartData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RingChart extends ViewGroup {

    // Constants
    private static final long ANIM_DURATION_DEFAULT = 1000;
    private static final long ANIM_DURATION_LONG = 1000;
    final RectF oval = new RectF();
    // View Data
    List<ObjectAnimator> RingHeadAnimators = new ArrayList<>();
    List<ObjectAnimator> RingTailAnimators = new ArrayList<>();
    List<RingChartData> ChartData = new ArrayList<>();
    List<RingView> RingViews = new ArrayList<>();
    float width;
    int colorPrimary;
    int colorSecondary;
    // Properties
    private RingView backgroundRing;
    private renderMode mode = renderMode.MODE_OVERLAP;
    private boolean showLabels = true;

    public RingChart(Context ctx) {
        super(ctx);

        // Load default colors
        colorPrimary = ContextCompat.getColor(getContext(), R.color.primary);
        colorSecondary = ContextCompat.getColor(getContext(), R.color.secondary);

        init();
    }


    /*
     *
     *
     * CONSTRUCTOR AND INITIALISATION SECTION
     *
     *
     */

    public RingChart(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        TypedArray a = ctx.getTheme().obtainStyledAttributes(attrs, R.styleable.RingChart, 0, 0);

        // attempt to get any values from the xml
        try {
            colorPrimary = a.getColor(R.styleable.RingChart_RingChartPrimaryColor, ContextCompat.getColor(getContext(), R.color.primary));
            colorSecondary = a.getColor(R.styleable.RingChart_RingChartSecondaryColor, ContextCompat.getColor(getContext(), R.color.secondary));
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {

        // Set a minimum height for this
        setMinimumHeight(150);

        // Reset stuff
        RingViews.clear();
        RingHeadAnimators.clear();
        RingTailAnimators.clear();
        removeAllViews();

        // If no data is set, set random data
        if (ChartData.size() == 0) {
            setData((float) Math.random());
            startAnimateLoading();
            return;
        }

        // Add background ring if its overlap mode
        if (mode == renderMode.MODE_OVERLAP) {
            backgroundRing = new RingView(getContext(), new RingChartData(1.0f, colorSecondary, "BackgroundRing-Kwv0Tv1HrB"));
            backgroundRing.setShouldDrawLabel(false);
            addView(backgroundRing);
        }

        // Create rings for each value and set-up animators
        for (RingChartData item : ChartData) {

            // Create and add ring views
            final RingView ringView = new RingView(getContext(), item);
            addView(ringView);
            RingViews.add(ringView);

            // setup animators
            ObjectAnimator ringHeadAnimator = new ObjectAnimator();
            ringHeadAnimator.setTarget(ringView);
            ringHeadAnimator.setInterpolator(new LinearInterpolator());
            ringHeadAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ringView.invalidate();
                }
            });
            ringHeadAnimator.setProperty(new Property<RingView, Float>(Float.class, "Percent") {
                @Override
                public Float get(RingView ring) {
                    return ring.headAngle;
                }

                @Override
                public void set(RingView ring, Float value) {
                    ring.headAngle = (value / 100.f) * 360.f;
                }
            });
            ObjectAnimator ringTailAnimator = new ObjectAnimator();
            ringTailAnimator.setTarget(ringView);
            ringTailAnimator.setInterpolator(new LinearInterpolator());
            ringTailAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ringView.invalidate();
                }
            });
            ringTailAnimator.setProperty(new Property<RingView, Float>(Float.class, "Percent") {
                @Override
                public Float get(RingView ring) {
                    return ring.tailAngle;
                }

                @Override
                public void set(RingView ring, Float value) {
                    ring.tailAngle = (value / 100.f) * 360.f;
                }
            });

            RingHeadAnimators.add(ringHeadAnimator);
            RingTailAnimators.add(ringTailAnimator);
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
        // Nothing to be implemented here
        // Rings will be created in onSizeChanged() method
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 300;
        int desiredHeight = 300;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be smaller than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be smaller than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        int finalSize = Math.max(width, height);

        //MUST CALL THIS
        setMeasuredDimension(finalSize, finalSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // account for padding
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        // figure out how big the chart can be
        float ww = (float) w - xpad;
        float hh = (float) h - ypad;
        float diameter = Math.max(ww, hh);

        oval.set(0.f, 0.f, diameter, diameter);
        oval.offsetTo(getPaddingLeft(), getPaddingTop());

        // set stroke width..
        width = diameter / 15.f;

        // layout the child views that actually draws the rings
        if (mode == renderMode.MODE_OVERLAP) {
            for (RingView v : RingViews) {
                v.layout((int) oval.left, (int) oval.top, (int) oval.right, (int) oval.bottom);
            }
        } else {
            int spaceBetweenRings = 4;
            float base = diameter * 0.1f;
            float availableSpace = ((diameter - (spaceBetweenRings * (RingViews.size() - 1) * 2) - base) / 2);
            width = availableSpace / (float) Math.max(RingViews.size() + 2, 7);

            float left = oval.left;
            float right = oval.right;
            float top = oval.top;
            float bottom = oval.bottom;

            float basePadding = 0;

            for (RingView v : RingViews) {

                left = left + width + spaceBetweenRings - basePadding;
                right = right - width - spaceBetweenRings + basePadding;
                top = top + width + spaceBetweenRings - basePadding;
                bottom = bottom - width - spaceBetweenRings + basePadding;

                v.layout((int) left, (int) top, (int) right, (int) bottom);

            }
        }

        // Layout BG ring
        if (mode == renderMode.MODE_OVERLAP)
            backgroundRing.layout((int) oval.left, (int) oval.top, (int) oval.right, (int) oval.bottom);
    }

    /**
     * Method to set data to the chart via list of RingChartData items
     *
     * @param items items to set to chart
     */
    public void setData(List<RingChartData> items) {
        Collections.sort(items, Collections.<RingChartData>reverseOrder());

        this.ChartData.clear();
        this.ChartData.addAll(items);

        init();

        // Animate every ring to its final value
        for (int i = 0; i < RingViews.size(); i++) {
            RingView v = RingViews.get(i);
            animateRing(RingHeadAnimators.get(i), (v.headAngle / 360f) * 100f);
        }
    }

    /*
     *
     *
     *
     *
     * DATA SECTION
     *
     *
     *
     *
     */

    /**
     * Method to setup a chart of single ring with percent data
     *
     * @param percent value between 0.0 and 1.0
     */
    public void setData(@FloatRange(from = 0.0f, to = 1.0f) float percent) {
        List<RingChartData> dataPoints = new ArrayList<>();
        dataPoints.add(new RingChartData(percent, colorPrimary, "Loading"));
        showLabels(false);
        setLayoutMode(renderMode.MODE_OVERLAP);
        setData(dataPoints);
    }

    /**
     * Method to update the previously set data
     * Remember to use the exact list with updated data items
     * If items in the list are added or removed, this method throws exception
     * All the items are identified by label
     *
     * @param items Exact List of items that was set, but with updated colors or values
     * @throws Exception Exception thrown when any item in the list differ from previously set data
     */
    public void updateData(List<RingChartData> items) throws Exception {
        if (RingViews.size() != items.size()) {
            throw new Exception("Data is inconsistent");
        } else {

            if (mode == renderMode.MODE_OVERLAP)
                Collections.sort(items, Collections.<RingChartData>reverseOrder());

            for (int i = 0; i < items.size(); i++) {
                RingChartData d = items.get(i);

                boolean found = false;

                for (RingChartData c : ChartData) {
                    if (c.label.equals(d.label)) {

                        c.value = d.value;
                        c.color = d.color;

                        RingView ring = RingViews.get(i);
                        ring.updateData(c);

                        if (mode == renderMode.MODE_OVERLAP) {
                            removeView(ring);
                            addView(ring, 1 + i);
                        }

                        found = true;
                        break;
                    }

                }

                if (!found) throw new Exception("Data is inconsistent");
            }
        }
    }

    /**
     * A convenient method to update data value by its label
     *
     * @param label Label of the data item to update
     * @param value Value to update
     * @throws Exception Throws exception when no such label exists in chart
     */
    public void updateItemByLabel(String label, @FloatRange(from = 0.0, to = 1.0) float value) throws Exception {
        List<RingChartData> dataItems = new ArrayList<>(this.ChartData);

        for (RingChartData c : dataItems) {
            if (c.label.equals(label)) {
                c.value = value;
                break;
            }
        }
        updateData(dataItems);
    }

    /**
     * Method to set layout type of the chart
     * Remember to call this method before setData
     *
     * @param mode Type of chart layout
     */
    public void setLayoutMode(renderMode mode) {
        this.mode = mode;
    }

    public void showLabels(boolean showLabels) {
        this.showLabels = showLabels;
    }

    /**
     * Method to animate rings in indeterminate loading style
     */
    public void startAnimateLoading() {
        for (int i = 0; i < RingViews.size(); i++) {

            final int itemZ = RingHeadAnimators.size() - 1 - i;
            final RingView ring = RingViews.get(i);
            final ObjectAnimator head = RingHeadAnimators.get(i);
            final ObjectAnimator tail = RingTailAnimators.get(i);

            head.removeAllListeners();
            head.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    // Clear all listeners and start the animation loop
                    head.removeAllListeners();

                    // animate length of ring
                    if (mode == renderMode.MODE_OVERLAP)
                        head.setFloatValues(1f + itemZ, 10f + (itemZ * 5), 1f + itemZ);
                    else head.setFloatValues(1f, 25f, 1f);
                    head.setRepeatCount(Animation.INFINITE);
                    head.setDuration(ANIM_DURATION_LONG);
                    head.start();
                }
            });

            tail.removeAllListeners();
            tail.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    // Clear all listeners and start the animation loop
                    tail.removeAllListeners();

                    tail.setFloatValues(0f, 100f); // Displacement of ring
                    tail.setRepeatCount(Animation.INFINITE);
                    tail.setDuration(ANIM_DURATION_LONG);
                    tail.start();
                }
            });

            // Animate and shrink ring to 0 degree
            float currentHeadPercent = (ring.headAngle / 360f) * 100f;
            float currentTailPercent = ((ring.tailAngle / 360f) * 100f);
            animateRingSeamless(RingHeadAnimators.get(i), currentHeadPercent, 0, false);
            animateRingSeamless(RingTailAnimators.get(i), currentTailPercent, 100, true);
        }
    }

    /*
     *
     *
     *
     *
     * ANIMATION SECTION
     *
     *
     *
     */

    /**
     * Method to stop animation of rings and bring the rings to its actual data values
     */
    public void stopAnimateLoading() {
        for (int i = 0; i < RingViews.size(); i++) {
            final RingView v = RingViews.get(i);

            float currentHeadPercent = (v.headAngle / 360) * 100;
            float finalHeadPercent = v.data.value * 100;
            float currentTailPercent = (v.tailAngle / 360) * 100;
            float finalTailPercent = 0;

            RingHeadAnimators.get(i).removeAllListeners();
            RingTailAnimators.get(i).removeAllListeners();
            animateRingSeamless(RingHeadAnimators.get(i), currentHeadPercent, finalHeadPercent, true);
            animateRingSeamless(RingTailAnimators.get(i), currentTailPercent, 100 + finalTailPercent, true);
        }
    }

    /**
     * This method stops the animation and sets the ring at given percent
     * This method should be called when data is set via same method of setting the data by percent
     *
     * @param percent value between 0.0 and 1.0
     */
    public void stopAnimateLoading(@FloatRange(from = 0.0, to = 1.0) float percent) {
        try {
            updateItemByLabel("RingChartSingleRing", percent);
        } catch (Exception ignored) {
        }
        stopAnimateLoading();
    }

    private void animateRing(ObjectAnimator animator, float to) {
        animator.setFloatValues(0f, to);
        animator.setDuration(ANIM_DURATION_DEFAULT);
        animator.setRepeatCount(0);
        animator.setStartDelay(0);
        animator.start();
    }

    private void animateRingSeamless(ObjectAnimator animator, float from, float to, boolean seamless) {
        // Calculate duration of this animation -> for seamless animation switches
        // WARNING : Only works if its linear interpolator
        float durationDelta = (Math.abs(to - from) % 100) / 100;
        long duration = (long) (durationDelta * ANIM_DURATION_DEFAULT);

        animator.setFloatValues(from, to);
        animator.setDuration((seamless) ? duration : ANIM_DURATION_DEFAULT);
        animator.setRepeatCount(0);
        animator.setStartDelay(0);
        animator.start();
    }

    public enum renderMode {MODE_OVERLAP, MODE_CONCENTRIC}

    /**
     * An Individual Ring View that draws and maintains self ring of Ring chart
     */
    protected class RingView extends View {
        private final RectF RingOval = new RectF();
        private int labelAngleOffset;
        private float labelSize;
        private boolean shouldDrawLabel = true;
        private Path labelCirclePath;
        private int bgColor;
        private Paint colorPaint, labelPaint;
        RingChartData data;
        float headAngle, tailAngle = 0f;

        public RingView(Context ctx, RingChartData data) {
            super(ctx);
            this.data = data;
            this.colorPaint = new Paint();
            this.colorPaint.setAntiAlias(true);
            this.colorPaint.setColor(data.color);
            this.colorPaint.setStyle(Paint.Style.STROKE);
            this.colorPaint.setStrokeCap(Paint.Cap.ROUND);
            headAngle = (data.value * 360f);
            updateBgColor();
            updateLabelPaint();
        }

        public void updateData(RingChartData ringData) {
            this.data = ringData;
            this.colorPaint.setColor(data.color);
            updateBgColor();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mode == renderMode.MODE_CONCENTRIC) {
                colorPaint.setColor(bgColor);
                canvas.drawArc(RingOval, 270f, 360f, false, colorPaint);
                colorPaint.setColor(data.color);
            }

            canvas.drawArc(RingOval, 270f + tailAngle, headAngle, false, colorPaint);

            // Draw label
            if (showLabels && shouldDrawLabel) {
                if (mode == renderMode.MODE_CONCENTRIC) {
                    canvas.rotate(-92, RingOval.centerX(), RingOval.centerY());
                } else {
                    canvas.rotate((tailAngle + headAngle) - 90 - labelAngleOffset, RingOval.centerX(), RingOval.centerY());
                }
                canvas.drawTextOnPath(data.label, labelCirclePath, 0, (width * 0.4f) / 2, labelPaint);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            RingOval.set(width, width /*[SIC]*/, w - width, h - width);
            this.colorPaint.setStrokeWidth(width);
            updateLabelPaint();
        }

        private void updateBgColor() {
            this.bgColor = ColorUtils.blendARGB(data.color, Color.BLACK, 0.4f);
            bgColor = ColorUtils.setAlphaComponent(bgColor, 30);
        }

        private void updateLabelPaint() {
            labelPaint = new Paint();
            labelPaint.setColor(Color.WHITE);
            labelPaint.setTextSize(width * 0.7f);
            labelCirclePath = new Path();
            labelCirclePath.addCircle(RingOval.centerX(), RingOval.centerY(), RingOval.width() / 2f, Path.Direction.CW);
            labelSize = labelPaint.measureText(data.label);
            labelAngleOffset = (int) ((labelSize / (float) (Math.PI * RingOval.width())) * 360);
        }

        public void setShouldDrawLabel(boolean shouldDraw) {
            this.shouldDrawLabel = shouldDraw;
        }
    }

}
