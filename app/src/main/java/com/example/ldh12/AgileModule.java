package com.example.ldh12;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AgileModule extends BaseActivity {
    private Toolbar toolbar;
    private FileAccess fileAccess;
    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agile_module);
        toolbar = findViewById(R.id.toolbar3);
        editText = findViewById(R.id.editText4);
        fileAccess = new FileAccess();
        fileAccess.getData();
        editText.setText(String.format("%d", fileAccess.item.agile));
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
        int freqency = Integer.parseInt(editText.getText().toString()) ;
        fileAccess.item.agile = freqency;
        fileAccess.saveData();
        Toast.makeText(AgileModule.this, "设置成功！",Toast.LENGTH_SHORT);

    }
}
