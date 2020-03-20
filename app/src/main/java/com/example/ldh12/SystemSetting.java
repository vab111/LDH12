package com.example.ldh12;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SystemSetting extends BaseActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_setting);
        toolbar = findViewById(R.id.sandubar);
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

    public void sanduSetting(View view) {
        Intent intent = new Intent();
        intent.setClass(SystemSetting.this, FrequencyModule.class);
        startActivity(intent);
    }

    public void AccSetting(View view) {
        Intent intent = new Intent();
        intent.setClass(SystemSetting.this,AcurateModule.class);
        startActivity(intent);
    }

    public void AgileSetting(View view) {

        Intent intent = new Intent();
        intent.setClass(SystemSetting.this,AgileModule.class);
        startActivity(intent);
    }

    public void RegSetting(View view) {

        Intent intent = new Intent();
        intent.setClass(SystemSetting.this,RegisterModule.class);
        startActivity(intent);
    }
}
