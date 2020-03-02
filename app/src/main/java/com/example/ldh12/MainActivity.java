package com.example.ldh12;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothClass;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.TextView;

import java.util.Map;
import java.util.Random;

import ca.hss.heatmaplib.HeatMap;
import ca.hss.heatmaplib.HeatMapMarkerCallback;

public class MainActivity extends BaseActivity {
    private HeatMap heatMap;
    private int Delta=0;
    private TextView deltaText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deltaText = findViewById(R.id.textView16);
        heatMap = findViewById(R.id.heatmap);
        heatMap.setMinimum(0.0);
        heatMap.setMaximum(100.0);
        heatMap.setRadius(100);
        heatMap.setMaxDrawingWidth(400);
        Map<Float, Integer> colorStops = new ArrayMap<>();
        colorStops.put(0.0f, 0xffee42f4);
        colorStops.put(1.0f, 0xffeef442);
        heatMap.setColorStops(colorStops);
        Random rand = new Random();
        for (int i = 0; i < 2800; i++) {
            HeatMap.DataPoint point = new HeatMap.DataPoint(rand.nextFloat(), rand.nextFloat(), rand.nextDouble() * 100.0);
            heatMap.addData(point);
        }
        //heatMap.setMarkerCallback(new HeatMapMarkerCallback.CircleHeatMapMarker(0xff9400D3));
    }

    public void goSetting(View view) {
        startActivity(MainActivity.this, SystemSetting.class);
    }

    public void addDelta(View view) {
        Delta++;
        deltaText.setText(String.format("%dcm", Delta));
    }

    public void minusDelta(View view) {
        Delta--;
        deltaText.setText(String.format("%dcm", Delta));
    }
}



