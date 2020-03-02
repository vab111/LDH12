package com.example.ldh12;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SanduSetting extends BaseActivity {

    private Toolbar toolbar;
    private Button light_low;
    private Button light_hight;
    private ProgressBar lightBar;
    private TextView lightProgress;
    private Button getup_low;
    private Button getup_hight;
    private ProgressBar getupBar;
    private TextView getupProgress;
    private Button getdown_low;
    private Button getdown_hight;
    private ProgressBar getdownBar;
    private TextView getdownProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandu_setting);
        toolbar = findViewById(R.id.toolbar2);
        light_low = findViewById(R.id.button12);
        lightProgress = findViewById(R.id.textView20);
        light_low.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:

                        lightBar.setProgress(lightBar.getProgress()-1);
                        lightProgress.setText(String.format("%d%%", lightBar.getProgress()));
                        break;
                }
                return false;
            }
        });
        light_hight = findViewById(R.id.button13);
        light_hight.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:

                        lightBar.setProgress(lightBar.getProgress()+1);
                        lightProgress.setText(String.format("%d%%", lightBar.getProgress()));
                        break;
                }
                return false;
            }

        });
        lightBar = findViewById(R.id.progressBar);
        getup_low = findViewById(R.id.button14);
        getupProgress = findViewById(R.id.textView21);
        getup_low.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:

                        getupBar.setProgress(getupBar.getProgress()-1);
                        getupProgress.setText(String.format("%d%%", getupBar.getProgress()));
                        break;
                }
                return false;
            }
        });
        getup_hight = findViewById(R.id.button15);
        getup_hight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:

                        getupBar.setProgress(getupBar.getProgress()+1);
                        getupProgress.setText(String.format("%d%%", getupBar.getProgress()));
                        break;
                }
                return false;
            }
        });
        getupBar = findViewById(R.id.progressBar2);
        getdown_low = findViewById(R.id.button16);
        getdownProgress = findViewById(R.id.textView22);
        getdown_low.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:

                        getdownBar.setProgress(getdownBar.getProgress()-1);
                        getdownProgress.setText(String.format("%d%%", getdownBar.getProgress()));
                        break;
                }
                return false;
            }
        });
        getdown_hight = findViewById(R.id.button17);
        getdown_hight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:

                        getdownBar.setProgress(getdownBar.getProgress()+1);
                        getdownProgress.setText(String.format("%d%%", getdownBar.getProgress()));
                        break;
                }
                return false;
            }
        });
        getdownBar = findViewById(R.id.progressBar3);
        setToolbar();
    }
    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);//设计隐藏标题

        //设置显示返回键
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish();
                finish();
            }
        });
    }
}
