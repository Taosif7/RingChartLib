package com.taosif7.android.ringchart;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.taosif7.android.ringchartlib.RingChart;
import com.taosif7.android.ringchartlib.models.RingChartData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, Float> dataPoints = new HashMap<>();
    private RingChart chart_concentric;
    private RingChart chart_overlap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find Views
        chart_concentric = findViewById(R.id.Chart_concentric);
        chart_overlap = findViewById(R.id.Chart_overlap);
        MaterialButton BTN_animStart = findViewById(R.id.animationStartBtn);
        MaterialButton BTN_animStop = findViewById(R.id.animationStopBtn);
        final ViewSwitcher VS_chartHolder = findViewById(R.id.chart_holder);

        // Prepare Data
        dataPoints.put("red", (float) Math.random());
        dataPoints.put("blue", (float) Math.random());
        dataPoints.put("green", (float) Math.random());
        dataPoints.put("yellow", (float) Math.random());

        final Map<String, Integer> colors = new HashMap<>();
        colors.put("red", ContextCompat.getColor(this, R.color.red));
        colors.put("blue", ContextCompat.getColor(this, R.color.blue));
        colors.put("green", ContextCompat.getColor(this, R.color.green));
        colors.put("yellow", ContextCompat.getColor(this, R.color.yellow));

        final List<RingChartData> data = new ArrayList<RingChartData>() {{
            for (String label : dataPoints.keySet()) {
                add(new RingChartData(dataPoints.get(label), colors.get(label), label));
            }
        }};

        // Set View Properties
        chart_concentric.setLayoutMode(RingChart.renderMode.MODE_CONCENTRIC);
        chart_concentric.setData(data);
        chart_concentric.startAnimateLoading();

        chart_overlap.setLayoutMode(RingChart.renderMode.MODE_OVERLAP);
        chart_overlap.setData(data);
        chart_overlap.startAnimateLoading();

        VS_chartHolder.setInAnimation(this, R.anim.anim_in);
        VS_chartHolder.setOutAnimation(this, R.anim.anim_out);
        VS_chartHolder.setAnimateFirstView(true);

        updateValueViews();


        // Set listeners
        BTN_animStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chart_concentric.startAnimateLoading();
                chart_overlap.startAnimateLoading();
            }
        });

        BTN_animStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chart_concentric.stopAnimateLoading();
                chart_overlap.stopAnimateLoading();
            }
        });

        VS_chartHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VS_chartHolder.showNext();
            }
        });
    }

    public void changeValue(View view) throws Exception {
        String dataName = (String) view.getTag();
        switch (((MaterialButton) view).getText().toString()) {
            case "+1%":
                dataPoints.put(dataName, Math.abs(dataPoints.get(dataName) + 0.01f) % 1.01f);
                break;
            case "+5%":
                dataPoints.put(dataName, Math.abs(dataPoints.get(dataName) + 0.05f) % 1.01f);
                break;
            case "-1%":
                dataPoints.put(dataName, Math.abs(dataPoints.get(dataName) - 0.01f) % 1.01f);
                break;
            case "-5%":
                dataPoints.put(dataName, Math.abs(dataPoints.get(dataName) - 0.05f) % 1.01f);
                break;
        }
        chart_concentric.updateItemByLabel(dataName, dataPoints.get(dataName));
        chart_overlap.updateItemByLabel(dataName, dataPoints.get(dataName));
        chart_concentric.stopAnimateLoading();
        chart_overlap.stopAnimateLoading();

        updateValueViews();
    }

    public void updateValueViews() {
        ((TextView) findViewById(R.id.label_red)).setText(String.format("%d", (int) (dataPoints.get("red") * 100)) + "%");
        ((TextView) findViewById(R.id.label_blue)).setText(String.format("%d", (int) (dataPoints.get("blue") * 100)) + "%");
        ((TextView) findViewById(R.id.label_green)).setText(String.format("%d", (int) (dataPoints.get("green") * 100)) + "%");
        ((TextView) findViewById(R.id.label_yellow)).setText(String.format("%d", (int) (dataPoints.get("yellow") * 100)) + "%");
    }
}