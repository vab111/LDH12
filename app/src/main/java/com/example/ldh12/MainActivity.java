package com.example.ldh12;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.bluetooth.BluetoothClass;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.guard.CommunicationService;
import com.android.guard.DataType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortFinder;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ca.hss.heatmaplib.HeatMap;
import ca.hss.heatmaplib.HeatMapMarkerCallback;

public class MainActivity extends BaseActivity {
    private static final String TAG_SERVICE = "LDH21";
    private HeatMap heatMap;
    private int Delta=0;
    private TextView deltaText;
    private CommunicationService mService;
    private ServiceConnection coon;
    private boolean isAuto = false;
    private boolean isCollect = false;
    private Button upBtn;
    private Button downBtn;
    private boolean isTask = false;
    private ArrayList data = new ArrayList();
    private SerialPortManager mSerialPortManager;
    private File serialDevice;
    private String buffer = "";
    private FileAccess fileAccess;
    private Point curP = new Point(0,0);
    private ImageView centerCover;
    private int max_x;
    private int min_x;
    private int max_y;
    private int min_y;
    private int max_h;
    private int min_h;
    private int state = 0;
    private TextView JState;
    private TextView curHeight;
    private TextView baseHeight;
    private TextView pindao;
    private TextView pianYi;
    private ImageView DaoState;
    private int x;
    private int y;
    private int h;
    private int BHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deltaText = findViewById(R.id.textView16);
        heatMap = findViewById(R.id.heatmap);
        centerCover = findViewById(R.id.imageView);
        JState = findViewById(R.id.textView2);
        curHeight = findViewById(R.id.textView9);
        baseHeight = findViewById(R.id.textView10);
        pindao = findViewById(R.id.textView6);
        pianYi = findViewById(R.id.textView16);
        DaoState = findViewById(R.id.imageView4);
        heatMap.setMinimum(0.0);
        heatMap.setMaximum(100.0);
        heatMap.setRadius(100);
        heatMap.setMaxDrawingWidth(400);
        Map<Float, Integer> colorStops = new ArrayMap<>();
        colorStops.put(0.0f, 0xffee42f4);
        colorStops.put(1.0f, 0xffeef442);
        heatMap.setColorStops(colorStops);
        upBtn = findViewById(R.id.button6);
        upBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        heartData.heart[2] |= 0x04;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        heartData.heart[2] |= 0x04;
                        break;
                    case MotionEvent.ACTION_UP:
                        heartData.heart[2] &= 0xfc;
                        break;

                }
                return false;
            }
        });
        downBtn = findViewById(R.id.button);
        downBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        heartData.heart[2] |= 0x08;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        heartData.heart[2] |= 0x08;
                        break;
                    case MotionEvent.ACTION_UP:
                        heartData.heart[2] &= 0xf7;
                        break;

                }
                return false;
            }
        });
        createHeatmap();
        initCanService();
        initSerialPort();
        coon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        final Intent intent = new Intent(this,HeartbeatService.class);
        bindService(intent,coon, Service.BIND_AUTO_CREATE);
        checkPermission();
        fileAccess = new FileAccess();
        initSetting();
    }
    private void initSetting()
    {
        fileAccess.getData();
        int acc = (int) (fileAccess.item.accurent*10);
        heartData.heart[3] = (byte) ((acc%256) & 0xff);
        heartData.heart[4] = (byte) ((fileAccess.item.agile % 256) & 0xff);
        pindao.setText(String.format("%d", fileAccess.item.freqency));
    }
    private void checkPermission() {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        } else {
            // Toast.makeText(this, "授权成功！", Toast.LENGTH_SHORT).show();
            Log.e(TAG_SERVICE, "checkPermission: 已经授权！");
        }
    }
    private void createHeatmap()
    {

        Random rand = new Random();
        for (int i = 0; i < 38; i++) {
            HeatMap.DataPoint point = new HeatMap.DataPoint(rand.nextFloat(), rand.nextFloat(), rand.nextDouble() * 100.0);
            heatMap.addData(point);
        }
        //heatMap.setMarkerCallback(new HeatMapMarkerCallback.CircleHeatMapMarker(0xff9400D3));
    }
    public void goSetting(View view) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,SystemSetting.class);
        startActivityForResult(intent,RESULT_CANCELED);
    }

    public void addDelta(View view) {
        Delta++;
        deltaText.setText(String.format("%dcm", Delta));
        setPY();
    }

    public void minusDelta(View view) {
        Delta--;
        deltaText.setText(String.format("%dcm", Delta));
        setPY();
    }

    private void initCanService(){
        try {
            mService = CommunicationService.getInstance(this);
            mService.setShutdownCountTime(12);//setting shutdownCountTime
            mService.bind();
            mService.getData(new CommunicationService.IProcessData() {
                @Override
                public void process(byte[] bytes, DataType dataType) {
                    switch (dataType) {
                        //detail received data
                        case TDataCan:
                            handleCanData(bytes);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSerialPort(){
        mSerialPortManager = new SerialPortManager();
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        ArrayList<Device> devices = serialPortFinder.getDevices();
        for (int i=0;i<devices.size();i++)
        {
            Device item = devices.get(i);
            if (item.getName().equals("ttyS4")) {
                serialDevice = item.getFile();
                mSerialPortManager.openSerialPort(item.getFile(), 115200);
            }
        }
        heartData.heart[0] = 0x05;

        mSerialPortManager.setOnOpenSerialPortListener(new OnOpenSerialPortListener() {
            @Override
            public void onSuccess(File device) {

            }

            @Override
            public void onFail(File device, Status status) {

            }
        });
        mSerialPortManager.setOnSerialPortDataListener(new OnSerialPortDataListener() {
            @Override
            public void onDataReceived(byte[] bytes) {
                String s = new String(bytes);
                String[] subStringArr = s.split(",");
                if (subStringArr[0].equals("$KSXT")||subStringArr[0].equals("$GPYBM"))
                {


                    String[] strArr = buffer.split(",");
                    if(strArr.length == 14) {
                        Log.e("MainActivity", "收到数据！");

                        if (!strArr[5].equals(""))
                            x = (int) (Double.parseDouble(strArr[5]) * 10);
                        if (!strArr[6].equals(""))
                            y = (int) (Double.parseDouble(strArr[6]) * 10);
                        if (!strArr[3].equals(""))
                            h = (int) (Double.parseDouble(strArr[3]) * 10);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                curHeight.setText(String.format("%dcm", h));
                                if (!isAuto) {
                                    BHeight = h;
                                    baseHeight.setText(String.format("%dcm", h));
                                }
                                else
                                {
                                    pianYi.setText(String.format("%dcm", h-BHeight));
                                    if (h-BHeight>0)
                                    {
                                        //TODO 铲刀下
                                        DaoState.setImageResource(R.drawable.downarrow);
                                    }
                                    else
                                    {
                                        if (h-BHeight==0)
                                        {
                                            //TODO 铲刀居中
                                            DaoState.setImageResource(R.drawable.midstate);
                                        }
                                        else
                                        {
                                            //TODO 铲刀上
                                            DaoState.setImageResource(R.drawable.uparrow);

                                        }
                                    }
                                }
                            }
                        });
                        if (isCollect)
                        {
                            if (curP.x==0)
                            {
                                locationData item = new locationData();
                                item.x = x;
                                item.y = y;
                                item.height = h;
                                data.add(item);
                                curP.x = x;
                                curP.y = y;
                            }
                            else
                            {
                                if (((x-curP.x)*(x-curP.x)+(y-curP.y)*(y-curP.y))>100)
                                {
                                    locationData item = new locationData();
                                    item.x = x;
                                    item.y = y;
                                    item.height = h;
                                    data.add(item);
                                    if (x>max_x)
                                        max_x = x;
                                    if (x<min_x)
                                        min_x = x;
                                    if (y>max_y)
                                        max_y = y;
                                    if (y<min_y)
                                        min_y = y;
                                    if (h>max_h)
                                        max_h = h;
                                    if (h<min_h)
                                        min_h = h;
                                }
                            }
                        }
                        if (!strArr[9].equals(""))
                            x = (int) Double.parseDouble(strArr[9]);
                        if (subStringArr[0].equals("$KSXT"))
                        {
                            switch (x)
                            {
                                case 0:
                                    state = 0;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            JState.setText("未定位");
                                        }
                                    });
                                    break;
                                case 1:
                                    state = 0;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            JState.setText("单点解");
                                        }
                                    });
                                    break;
                                case 2:
                                    state = 0;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            JState.setText("浮点解");
                                        }
                                    });
                                    break;
                                case 3:
                                    state = 4;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            JState.setText("固定解");
                                        }
                                    });
                                    break;
                            }
                        }
                        else
                        {
                            if(subStringArr[0].equals("$GPYBM"))
                            {

                                switch (x)
                                {
                                    case 0:
                                        state = 0;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                JState.setText("未定位");
                                            }
                                        });
                                        break;
                                    case 1:
                                        state = 0;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                JState.setText("单点解");
                                            }
                                        });
                                        break;
                                    case 4:
                                        state = 4;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                JState.setText("固定解");
                                            }
                                        });
                                        break;
                                    case 5:
                                        state = 0;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                JState.setText("浮点解");
                                            }
                                        });
                                        break;
                                    case 6:
                                        state = 0;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                JState.setText("定位解");
                                            }
                                        });
                                        break;

                                }
                            }
                        }
                    }
                    buffer = s;
                }
                else {
                    buffer+=s;

                }



            }

            @Override
            public void onDataSent(byte[] bytes) {

            }
        });
    }
    private void handleCanData(byte[] bytes) {
        byte[] id = new byte[4];
        System.arraycopy(bytes, 1, id, 0, id.length);//ID
        byte[] data = null;
        int frameFormatType = (id[3] & 0x06);
        int frameFormat = 0;
        int frameType = 0;
        long extendid = 0;
        switch (frameFormatType) {
            case 0://标准数据
                frameFormat = 0;
                frameType = 0;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>21);//bit31-bit21: 标准ID
                int dataLength = bytes[5];
                data = new byte[dataLength];
                System.arraycopy(bytes, 6, data, 0, dataLength);
                handle((int) extendid, data);
                break;
            case 2://标准远程
                frameFormat = 0;
                frameType = 1;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>21);//bit31-bit21: 标准ID
                break;
            case 4://扩展数据
                frameFormat = 1;
                frameType = 0;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>3);//bit31-bit3: 扩展ID
                int dataLengthExtra = bytes[5];
                data = new byte[dataLengthExtra];
                System.arraycopy(bytes, 6, data, 0, dataLengthExtra);
                break;
            case 6://扩展远程
                frameFormat = 1;
                frameType = 1;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>3);//bit31-bit3: 扩展ID
                break;
        }

    }
    public void handle(int id,byte[] data) {
        switch (id) {
            case 1793:
                //0x701

                final int txxiuzheng = ((data[3] << 8) | (0xff&data[4]));
                final int pindao1 = data[7];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                break;

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {			switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle("系统提示").setMessage("确定要退出吗？");
            build.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            unbindService(coon);
                            System.exit(0);
                        }
                    });
            build.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();				break;
    }
        return super.onKeyDown(keyCode, event);
    }

    public void AutoMode(View view) {
        if (!isAuto)
            heartData.heart[2]|=0x01;
        else
            heartData.heart[2]&=0xfe;

    }

    public void startTask(View view) {
        if (isTask)
            heartData.heart[2]|=0x02;
        else
            heartData.heart[2]&=0xfd;
    }

    public void startCollect(View view) {
        if (isCollect) {
            isCollect = false;
            //savaData();
            setBH();
        }
        else {
            //isCollect = true;
            data = new ArrayList();
            readData();
        }
    }
    private void savaData()
    {
        File fs = new File(Environment.getExternalStorageDirectory()+"/LDH21/data.json");
        try {
            FileOutputStream outputStream =new FileOutputStream(fs);
            OutputStreamWriter outStream = new OutputStreamWriter(outputStream);


            Gson gson = new Gson();
            String jsonString = gson.toJson(data);
            outStream.write(jsonString);

            outputStream.flush();
            outStream.flush();
            outputStream.close();
            outputStream.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readData()
    {
        File fs = new File(Environment.getExternalStorageDirectory()+"/LDH21/data.json");
        if (fs.exists()) {
            String result = "";
            try {
                FileInputStream f = new FileInputStream(fs);
                BufferedReader bis = new BufferedReader(new InputStreamReader(f));
                String line = "";
                while ((line = bis.readLine()) != null) {
                    result += line;
                }
                bis.close();
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result.length()>0) {
                Gson gson = new Gson();
                data = gson.fromJson(result, new TypeToken<List<locationData>>() {
                }.getType());
            for (int i=0;i<data.size();i++)
            {
                locationData item = (locationData) data.get(i);
                if (item.x>max_x)
                    max_x = item.x;
                if (item.x<min_x)
                    min_x = item.x;
                if (item.y>max_y)
                    max_y = item.y;
                if (item.y<min_y)
                    min_y = item.y;
                if (item.height>max_h)
                    max_h = item.height;
                if (item.height<min_h)
                    min_h = item.height;
            }

            }

        }
        heatMap.clearData();
        float x_length = max_x-min_x;
        float y_length = max_y-min_y;
        double h_length = max_h-min_h;
        for (int i = 0; i < data.size(); i++) {
            locationData item = (locationData) data.get(i);
            float x = max_x-item.x;
            x/=x_length;
            float y = max_y-item.y;
            y/=y_length;
            float h = max_h-item.height;
            h/=h_length;
            HeatMap.DataPoint point = new HeatMap.DataPoint(x, y, h * 100.0);
            heatMap.addData(point);
        }
        heatMap.forceRefresh();
        centerCover.setVisibility(View.INVISIBLE);
    }
    private void setPY()
    {
        if (Delta < 0) {
            heartData.heart[7] = (byte) ((Delta % 256-1) & 0xff);

        } else {
            heartData.heart[7] = (byte) ((Delta % 256) & 0xff);

        }
    }
    private void setBH()
    {
        heartData.heart[6] = (byte) ((BHeight % 256) & 0xff);
        heartData.heart[5] = (byte) ((BHeight/256) & 0xff);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        initSetting();
    }
}



