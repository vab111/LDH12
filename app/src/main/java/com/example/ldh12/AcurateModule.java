package com.example.ldh12;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

public class AcurateModule extends BaseActivity {
    private Toolbar toolbar;
    private FileAccess fileAccess;
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acurate_module);
        toolbar = findViewById(R.id.toolbar4);
        editText = findViewById(R.id.editText2);
        fileAccess = new FileAccess();
        fileAccess.getData();
        editText.setText(String.format("%.1f",fileAccess.item.accurent));
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

    public void confirm(View view) {
        float freqency = Float.parseFloat(editText.getText().toString()) ;
        fileAccess.item.accurent = freqency;
        int num = (int) (freqency*10);

        heartData.heart[3] = (byte) ((num%256)&0xff);
        fileAccess.saveData();
        Toast.makeText(AcurateModule.this, "设置成功！",Toast.LENGTH_SHORT);
    }
}
