package com.example.ldh12;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

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

public class MainActivity extends BaseActivity {
    private static final String TAG_SERVICE = "LDH21";
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
    private ImageView DaoState;
    private int x;
    private int y;
    private int h;
    private int BHeight;
    private TextView alertText;
    private Button pindi;
    private Button caidian;
    private Button shouzidong;
    private Bitmap baseBitmap;
    private Canvas canvasHandler;
    private Paint pain;
    private long sumHeight;
    private boolean isBHset = false;
    private locationData Item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deltaText = findViewById(R.id.textView16);

        centerCover = findViewById(R.id.imageView);
        JState = findViewById(R.id.textView2);
        curHeight = findViewById(R.id.textView9);
        baseHeight = findViewById(R.id.textView10);
        pindao = findViewById(R.id.textView6);
        DaoState = findViewById(R.id.imageView4);
        alertText = findViewById(R.id.textView11);
        pindi = findViewById(R.id.button4);
        caidian = findViewById(R.id.button5);
        shouzidong = findViewById(R.id.button8);


        upBtn = findViewById(R.id.button6);
        upBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        heartData.heart[2] |= 0x04;
                        break;
                    case MotionEvent.ACTION_MOVE:

                        break;
                    case MotionEvent.ACTION_UP:
                        heartData.heart[2] &= 0xfb;
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
                        break;
                    case MotionEvent.ACTION_UP:
                        heartData.heart[2] &= 0xf7;
                        break;

                }
                return false;
            }
        });
        pain = new Paint();
        pain.setColor(Color.RED);
        pain.setStrokeWidth(1.0f);
        pain.setStyle(Paint.Style.FILL);

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
        Item = new locationData();
    }
    private void initSetting(){
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
                            h = (int) (Double.parseDouble(strArr[3]) * 100);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                curHeight.setText(String.format("%dcm", h));
                                if (!isTask) {

                                    baseHeight.setText(String.format("%dcm", h));
                                }
                                else
                                {
                                    baseHeight.setText(String.format("%dcm", BHeight));
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

                        Item.x = x;
                        Item.y = y;
                        Item.height = h;
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
                                sumHeight = h;
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
                                    curP.x = x;
                                    curP.y = y;
                                    sumHeight+=h;
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertText.setText(String.format("正在采点中，当前数量：%d",data.size()));
                                }
                            });
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
                        pindao.setText(String.format("%d",pindao1));
                    }
                });
                break;

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){			switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle("系统提示").setMessage("确定要退出吗？");
            build.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            unbindService(coon);
                           // mSerialPortManager.closeSerialPort();
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
        if (!isAuto){
            heartData.heart[2]|=0x01;
            shouzidong.setBackgroundResource(R.drawable.autonative);
            isAuto = true;
        }
        else {
            isAuto = false;
            heartData.heart[2] &= 0xfe;
            shouzidong.setBackgroundResource(R.drawable.motive);
        }

    }

    public void startTask(View view) {
        if(isCollect)
        {
            Toast.makeText(MainActivity.this, "正在采点中...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isTask) {
            heartData.heart[2] &= 0xfd;
            pindi.setBackgroundResource(R.drawable.home_collect);
            isTask = false;
            alertText.setText(null);
        }
        else {
            isTask = true;
            heartData.heart[2] |= 0x02;
            if (isBHset)
                setBH();
            else
            {
                BHeight = Item.height;
                setBH();
            }
            alertText.setText("正在平地...");
        }
    }

    public void startCollect(View view) {
        if(isTask)
        {
            Toast.makeText(MainActivity.this, "正在平地中...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isCollect) {
            alertText.setText("正在生成等高图...");
            isCollect = false;
            if (data.size()>0)
                sumHeight/=data.size();
            BHeight = (int) sumHeight;
            isBHset = true;
            curP.x = 0;
            curP.y = 0;
            data = new ArrayList();
            readData();
            handlData();
            caidian.setBackgroundResource(R.drawable.home_start);
            alertText.setText("等高图生成完成！");
        }
        else {
            isCollect = true;
            isBHset = false;
            caidian.setBackgroundResource(R.drawable.endcollect);
        }
    }
    private void savaData(){
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
    private void readData(){
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
                if (i==0)
                {
                    max_x = item.x;
                    max_y = item.y;
                    max_h = item.height;
                    min_x = item.x;
                    min_y = item.y;
                    min_h = item.height;
                }
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


    }
    private void setPY(){
        if (Delta < 0) {
            heartData.heart[7] = (byte) ((Delta % 256-1) & 0xff);

        } else {
            heartData.heart[7] = (byte) ((Delta % 256) & 0xff);

        }
    }
    private void setBH(){
        heartData.heart[6] = (byte) ((BHeight % 256) & 0xff);
        heartData.heart[5] = (byte) ((BHeight/256) & 0xff);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        initSetting();
    }
    public int getcolor(int i){
        int color = Color.argb(255,255,255,255);
        switch (i)
        {
            case 0:
                color = Color.argb(255,136,180,241);
                break;
            case 1:
                color = Color.argb(255,188,146,210);
                break;
            case 2:
                color = Color.argb(255,226,194,243);
                break;
            case 3:
                color = Color.argb(255,187,239,250);
                break;
            case 4:
                color = Color.argb(255,247,253,239);
                break;
            case 5:
                color = Color.argb(255,0,0,255);
                break;
            case 6:
                color = Color.argb(255,127,0,0);
                break;
            case 7:
                color = Color.argb(255,127,127,0);
                break;
            case 8:
                color = Color.argb(255,127,0,127);
                break;
            case 9:
                color = Color.argb(255,0,127,0);
                break;
            case 10:
                color = Color.argb(255,0,0,127);
                break;
            case 11:
                color = Color.argb(255,0,127,127);
                break;
        }
        return color;
    }
    public int caculateValue(int x,int y,double[][] pic,int xlength,int ylength){
        double sum=0;
        double sum1=0;

        for(int i=0;i<xlength;i++)
        {
            for (int j=0;j<ylength;j++) {
                if (pic[i][j]>-1) {
                    double result;
                    result = 1.0f / ((i - x) * (i - x) + (j - y) * (j - y));
                    sum += result;
                    sum1 += (result * pic[i][j]);
                }
            }
        }
        int aa = (int) (sum1/sum);
        return aa;
    }
    public void isoband(double[][] data, double[] thArray, int xtotal,int ytotal,int xlength,int ylength) {
        int x, y, k;
        int count = 0;
        int squareWidth = xtotal / (xlength - 1);
        int squareHeight = ytotal / (ylength - 1);
        Path[] dstPolygonVec = new Path[thArray.length - 1];

        // 等值线的每一个阈值
        for (k = 0; k < thArray.length - 1; k++) {

            // 3值化
            int[][] stateMat = new int[xlength][ylength];
            for (y = 0; y < ylength; y++) {
                for (x = 0; x < xlength; x++) {
                    if (data[x][y] < thArray[k]) {
                        stateMat[x][y] = 0;
                    } else {
                        if (data[x][y] < thArray[k + 1]) {
                            stateMat[x][y] = 1;
                        } else {
                            stateMat[x][y] = 2;
                        }
                    }
                }
            }

            int x1, y1, x2, y2, x3, y3, x4, y4;
            Path polygon = new Path();
            for (y = 0; y < ylength - 1; y++) {

                int ymin = ytotal*y / (ylength - 1);
                int ymax = ytotal*(y + 1) / (ylength - 1);

                for (x = 0; x < xlength - 1; x++) {

                    int xmin = xtotal*x / (xlength - 1);
                    int xmax = xtotal*(x + 1) / (xlength - 1) ;

                    // square 四角坐标
                    Point p7 = new Point(xmin, ymin);
                    Point p9 = new Point(xmax, ymin);
                    Point p3 = new Point(xmax, ymax);
                    Point p1 = new Point(xmin, ymax);

                    // square 四角数值
                    int d7 = (int) data[x][y];
                    int d9 = (int) data[x + 1][y];
                    int d3 = (int) data[x + 1][y + 1];
                    int d1 = (int) data[x][y + 1];
                    int mid;

                    // isoband的顶点坐标
                    Point pt1 = null;
                    Point pt2 = null;
                    Point pt3 = null;
                    Point pt4 = null;
                    Point pt5 = null;
                    Point pt6 = null;
                    Point pt7 = null;
                    Point pt8 = null;

                    String squareState = getSquareState(stateMat, x, y);
                    switch (squareState)        // total 81 cases
                    {
                        // no color
                        case "2222":
                        case "0000":
                            break;
                        // square
                        case "1111":
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        // triangle                8 cases
                        case "2221":
                            x1 = (int) (p1.x + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (p1.y - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, p1.y);
                            pt2 = new Point(p7.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2212":
                            y1 = (int) (p3.y - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (p3.x - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(p9.x, y1);
                            pt2 = new Point(x2, p1.y);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2122":
                            x1 = (int) (p9.x - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            y2 = (int) (p9.y + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            pt1 = new Point(x1, p7.y);
                            pt2 = new Point(p9.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1222":
                            x1 = (int) (p7.x + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y2 = (int) (p7.y + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, p7.y);
                            pt2 = new Point(p7.x, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "0001":
                            x1 = (int) (p3.x - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (p7.y + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, p1.y);
                            pt2 = new Point(p7.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0010":
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            x2 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(x2, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0100":
                            x1 = (int) (p7.x + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            y2 = (int) (p3.y - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            pt1 = new Point(x1, p9.y);
                            pt2 = new Point(p9.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1000":
                            x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        // trapezoid        8 cases
                        case "2220":
                            x1 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x2 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y2 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, p1.y);
                            pt2 = new Point(x2, p1.y);
                            pt3 = new Point(p7.x, y1);
                            pt4 = new Point(p7.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();

                            break;
                        case "2202":
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x1 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x2 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(xmax, y2);
                            pt3 = new Point(x1, ymax);
                            pt4 = new Point(x2, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2022":
                            x1 = (int) (p9.x - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (p9.x - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (p9.y + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (p9.y + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            pt1 = new Point(x1, p9.y);
                            pt2 = new Point(x2, p9.y);
                            pt3 = new Point(p9.x, y1);
                            pt4 = new Point(p9.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0222":
                            x1 = (int) (p7.x + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (p7.x + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (p7.y + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y2 = (int) (p7.y + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, p7.y);
                            pt2 = new Point(x2, p7.y);
                            pt3 = new Point(p7.x, y1);
                            pt4 = new Point(p7.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0002":
                            x1 = (int) (p3.x - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x2 = (int) (p3.x - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y1 = (int) (p7.y + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y2 = (int) (p7.y + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, p1.y);
                            pt2 = new Point(x2, p1.y);
                            pt3 = new Point(p1.x, y1);
                            pt4 = new Point(p1.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0020":
                            y1 = (int) (p9.y + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (p9.y + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x1 = (int) (p1.x + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x2 = (int) (p1.x + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(p3.x, y1);
                            pt2 = new Point(p3.x, y2);
                            pt3 = new Point(x1, p3.y);
                            pt4 = new Point(x2, p3.y);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0200":
                            x1 = (int) (p7.x + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (p7.x + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (p3.y - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (p3.y - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            pt1 = new Point(x1, p9.y);
                            pt2 = new Point(x2, p9.y);
                            pt3 = new Point(p9.x, y1);
                            pt4 = new Point(p9.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2000":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y2 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmin, y1);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        // rectangle 12 cases
                        case "0011":
                            y1 = (int) (p9.y + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (p7.y + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(p9.x, y1);
                            pt2 = new Point(p7.x, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0110":
                            x1 = (int) (p7.x + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (p1.x + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(x1, p7.y);
                            pt2 = new Point(x2, p1.y);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1100":
                            y1 = (int) (p3.y - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (p1.y - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(p9.x, y1);
                            pt2 = new Point(p7.x, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1001":
                            x1 = (int) (p9.x - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (p3.x - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(x1, p7.y);
                            pt2 = new Point(x2, p1.y);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "2211":
                            y1 = (int) (p3.y - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (p1.y - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2112":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1122":
                            y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareWidth);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareWidth);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1221":
                            x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymax);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "2200":
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            y3 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y4 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(xmax, y2);
                            pt3 = new Point(xmin, y3);
                            pt4 = new Point(xmin, y4);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2002":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            x3 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x4 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(x3, ymax);
                            pt4 = new Point(x4, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0022":
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            y3 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y4 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(xmax, y2);
                            pt3 = new Point(xmin, y3);
                            pt4 = new Point(xmin, y4);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0220":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            x3 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x4 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(x3, ymax);
                            pt4 = new Point(x4, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        // hexagon 12 cases
                        case "0211":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2110":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y2 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymax);
                            pt3 = new Point(xmin, y1);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();

                            break;
                        case "1102":
                            y1 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x1 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x2 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(x1, ymax);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1021":
                            x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmax, y2);
                            pt4 = new Point(x2, ymax);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "2011":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0112":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y1 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y2 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymax);
                            pt3 = new Point(xmin, y1);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1120":
                            y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x1 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x2 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(x1, ymax);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1201":
                            x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmax, y2);
                            pt4 = new Point(x2, ymax);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "2101":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0121":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1012":
                            x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            x2 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1210":
                            x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        // pentagon        24 cases
                        case "1211":
                            x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y2 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "2111":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            y2 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1112":
                            x1 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymax);
                            pt2 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1121":
                            y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(x2, ymax);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1011":
                            x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y2 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "0111":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            y2 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmin, y2);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1110":
                            x1 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymax);
                            pt2 = new Point(xmin, y2);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1101":
                            y1 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(x2, ymax);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "1200":
                            x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            y3 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmax, y2);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "0120":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x3 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(x3, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0012":
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            x1 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y3 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(x1, ymax);
                            pt3 = new Point(xmin, y2);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2001":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            x3 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(x3, ymax);
                            pt4 = new Point(xmin, y1);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1022":
                            x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            y3 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmax, y2);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "2102":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x3 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(x3, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2210":
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            x1 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y3 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(x1, ymax);
                            pt3 = new Point(xmin, y2);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0221":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            x3 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(x3, ymax);
                            pt4 = new Point(xmin, y1);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1002":
                            x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x3 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y1 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymax);
                            pt3 = new Point(x3, ymax);
                            pt4 = new Point(xmin, y1);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "2100":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y3 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmin, y2);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0210":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            x3 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(x3, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "0021":
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x1 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            y3 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(xmax, y2);
                            pt3 = new Point(x1, ymax);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "1220":
                            x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x3 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymax);
                            pt3 = new Point(x3, ymax);
                            pt4 = new Point(xmin, y1);
                            polygon.moveTo(p7.x, p7.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(p7.x, p7.y);
                            polygon.close();
                            break;
                        case "0122":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y3 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmin, y2);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(p9.x, p9.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2012":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            x3 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(x3, ymax);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p3.x, p3.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        case "2201":
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x1 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            y3 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(xmax, y1);
                            pt2 = new Point(xmax, y2);
                            pt3 = new Point(x1, ymax);
                            pt4 = new Point(xmin, y3);
                            polygon.moveTo(pt1.x, pt1.y);
                            polygon.lineTo(pt2.x, pt2.y);
                            polygon.lineTo(pt3.x, pt3.y);
                            polygon.lineTo(p1.x, p1.y);
                            polygon.lineTo(pt4.x, pt4.y);
                            polygon.lineTo(pt1.x, pt1.y);
                            polygon.close();
                            break;
                        // saddles - 8 sided        2 cases
                        case "2020":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x3 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x4 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y3 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y4 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(xmax, y2);
                            pt5 = new Point(x3, ymax);
                            pt6 = new Point(x4, ymax);
                            pt7 = new Point(xmin, y3);
                            pt8 = new Point(xmin, y4);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt7.x, pt7.y);
                                polygon.lineTo(pt8.x, pt8.y);
                                polygon.lineTo(pt1.x, pt1.y);


                                polygon.moveTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.close();
                            } else if (mid < thArray[k + 1]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt7.x, pt7.y);
                                polygon.lineTo(pt8.x, pt8.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                polygon.moveTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt7.x, pt7.y);
                                polygon.lineTo(pt8.x, pt8.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.close();
                            }
                            break;
                        case "0202":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x3 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x4 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y3 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y4 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(xmax, y2);
                            pt5 = new Point(x3, ymax);
                            pt6 = new Point(x4, ymax);
                            pt7 = new Point(xmin, y3);
                            pt8 = new Point(xmin, y4);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                polygon.moveTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt7.x, pt7.y);
                                polygon.lineTo(pt8.x, pt8.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.close();
                            } else if (mid < thArray[k + 1]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt7.x, pt7.y);
                                polygon.lineTo(pt8.x, pt8.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt7.x, pt7.y);
                                polygon.lineTo(pt8.x, pt8.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                polygon.moveTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.close();
                            }
                            break;
                        // saddles:        6 sided                4 cases
                        case "0101":
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                x1 = (int) (p7.x + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                                y2 = (int) (p3.y - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                                pt1 = new Point(x1, p9.y);
                                pt2 = new Point(p9.x, y2);
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                x1 = (int) (p3.x - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                                y2 = (int) (p7.y + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                                pt1 = new Point(x1, p1.y);
                                pt2 = new Point(p7.x, y2);
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {

                                x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                                y1 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                                x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                                y2 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                                pt1 = new Point(x1, ymin);
                                pt2 = new Point(xmax, y1);
                                pt3 = new Point(x2, ymax);
                                pt4 = new Point(xmin, y2);
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            }
                            break;
                        case "1010":
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                                y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                                pt1 = new Point(x1, ymin);
                                pt2 = new Point(xmin, y2);
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(p7.x, p7.y);

                                y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                                x2 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                                pt1 = new Point(p9.x, y1);
                                pt2 = new Point(x2, p1.y);
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {
                                x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                                y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                                x2 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                                y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                                pt1 = new Point(x1, ymin);
                                pt2 = new Point(xmax, y1);
                                pt3 = new Point(x2, ymax);
                                pt4 = new Point(xmin, y2);
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(p7.x, p7.y);
                                polygon.close();
                            }
                            break;
                        case "2121":
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k + 1]) {
                                x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                                y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                                x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                                y2 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                                pt1 = new Point(x1, ymin);
                                pt2 = new Point(xmax, y1);
                                pt3 = new Point(x2, ymax);
                                pt4 = new Point(xmin, y2);

                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {
                                x1 = (int) (p9.x - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                                y2 = (int) (p9.y + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                                pt1 = new Point(x1, p7.y);
                                pt2 = new Point(p9.x, y2);
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);


                                x1 = (int) (p1.x + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                                y2 = (int) (p1.y - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                                pt1 = new Point(x1, p1.y);
                                pt2 = new Point(p7.x, y2);
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            }
                            break;
                        case "1212":
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k + 1]) {
                                x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                                y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                                x2 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                                y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                                pt1 = new Point(x1, ymin);
                                pt2 = new Point(xmax, y1);
                                pt3 = new Point(x2, ymax);
                                pt4 = new Point(xmin, y2);
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(p7.x, p7.y);
                                polygon.close();
                            } else {
                                x1 = (int) (p7.x + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                                y2 = (int) (p7.y + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                                pt1 = new Point(x1, p7.y);
                                pt2 = new Point(p7.x, y2);
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(p7.x, p7.y);

                                y1 = (int) (p3.y - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                                x2 = (int) (p3.x - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                                pt1 = new Point(p9.x, y1);
                                pt2 = new Point(x2, p1.y);
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            }
                            break;
                        // saddles 7 sided                8 cases
                        case "2120":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x3 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y3 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(x3, ymax);
                            pt5 = new Point(xmin, y2);
                            pt6 = new Point(xmin, y3);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k + 1]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                polygon.moveTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.close();
                            }
                            break;
                        case "2021":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x3 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            y3 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(xmax, y2);
                            pt5 = new Point(x3, ymax);
                            pt6 = new Point(xmin, y3);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k + 1]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                polygon.moveTo(pt5.x, pt5.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.close();
                            }
                            break;
                        case "1202":
                            x1 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x3 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y3 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmax, y2);
                            pt4 = new Point(x2, ymax);
                            pt5 = new Point(x3, ymax);
                            pt6 = new Point(xmin, y3);

                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k + 1]) {
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(p7.x, p7.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(p7.x, p7.y);

                                polygon.moveTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.close();
                            }
                            break;
                        case "0212":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            x3 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y3 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(x3, ymax);
                            pt5 = new Point(xmin, y2);
                            pt6 = new Point(xmin, y3);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k + 1]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.moveTo(pt3.x, pt3.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.close();
                            }
                            break;
                        case "0102":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x2 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            x3 = (int) (xmax - (thArray[k + 1] - d3) / (d1 - d3) * squareWidth);
                            y2 = (int) (ymin + (thArray[k + 1] - d7) / (d1 - d7) * squareHeight);
                            y3 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(x2, ymax);
                            pt4 = new Point(x3, ymax);
                            pt5 = new Point(xmin, y2);
                            pt6 = new Point(xmin, y3);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.moveTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(p9.x, p9.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            }
                            break;
                        case "0201":
                            x1 = (int) (xmin + (thArray[k] - d7) / (d9 - d7) * squareWidth);
                            x2 = (int) (xmin + (thArray[k + 1] - d7) / (d9 - d7) * squareWidth);
                            y1 = (int) (ymax - (thArray[k + 1] - d3) / (d9 - d3) * squareHeight);
                            y2 = (int) (ymax - (thArray[k] - d3) / (d9 - d3) * squareHeight);
                            x3 = (int) (xmax - (thArray[k] - d3) / (d1 - d3) * squareWidth);
                            y3 = (int) (ymin + (thArray[k] - d7) / (d1 - d7) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(xmax, y2);
                            pt5 = new Point(x3, ymax);
                            pt6 = new Point(xmin, y3);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                polygon.moveTo(pt5.x, pt5.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.close();
                            } else {

                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.moveTo(pt5.x, pt5.y);
                                polygon.lineTo(p1.x, p1.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            }
                            break;
                        case "1020":
                            x1 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            y2 = (int) (ymin + (thArray[k + 1] - d9) / (d3 - d9) * squareHeight);
                            x2 = (int) (xmin + (thArray[k + 1] - d1) / (d3 - d1) * squareWidth);
                            x3 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y3 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(xmax, y1);
                            pt3 = new Point(xmax, y2);
                            pt4 = new Point(x2, ymax);
                            pt5 = new Point(x3, ymax);
                            pt6 = new Point(xmin, y3);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(p7.x, p7.y);

                                polygon.moveTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.close();
                            } else {
                                polygon.moveTo(p7.x, p7.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(p7.x, p7.y);
                                polygon.close();
                            }
                            break;
                        case "2010":
                            x1 = (int) (xmax - (thArray[k + 1] - d9) / (d7 - d9) * squareWidth);
                            x2 = (int) (xmax - (thArray[k] - d9) / (d7 - d9) * squareWidth);
                            y1 = (int) (ymin + (thArray[k] - d9) / (d3 - d9) * squareHeight);
                            x3 = (int) (xmin + (thArray[k] - d1) / (d3 - d1) * squareWidth);
                            y2 = (int) (ymax - (thArray[k] - d1) / (d7 - d1) * squareHeight);
                            y3 = (int) (ymax - (thArray[k + 1] - d1) / (d7 - d1) * squareHeight);
                            pt1 = new Point(x1, ymin);
                            pt2 = new Point(x2, ymin);
                            pt3 = new Point(xmax, y1);
                            pt4 = new Point(x3, ymax);
                            pt5 = new Point(xmin, y2);
                            pt6 = new Point(xmin, y3);
                            mid = (d7 + d9 + d3 + d1) / 4;
                            if (mid < thArray[k]) {
                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);

                                polygon.moveTo(pt3.x, pt3.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.close();
                            } else {

                                polygon.moveTo(pt1.x, pt1.y);
                                polygon.lineTo(pt2.x, pt2.y);
                                polygon.lineTo(pt3.x, pt3.y);
                                polygon.lineTo(p3.x, p3.y);
                                polygon.lineTo(pt4.x, pt4.y);
                                polygon.lineTo(pt5.x, pt5.y);
                                polygon.lineTo(pt6.x, pt6.y);
                                polygon.lineTo(pt1.x, pt1.y);
                                polygon.close();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            dstPolygonVec[k] = polygon;
            pain.setColor(getcolor(k));
            canvasHandler.drawPath(polygon, pain);

        }
        centerCover.setImageBitmap(baseBitmap);
    }
    public String getSquareState(int[][] mat,int x,int y) {
        String str = new String();
        str = "";
        str += mat[x][y];
        str += mat[x+1][y];
        str += mat[x+1][y+1];
        str += mat[x][y+1];
        return str;
    }
    public void handlData() {
        int x_length = (max_x-min_x)/10;
        int y_length = (max_y-min_y)/10;
        int h_length = max_h-min_h;
        double[][] data_mW =new double[x_length+1][y_length+1];
        for (int i=0;i<x_length+1;i++) {
            for (int j=0;j<y_length+1;j++)
                data_mW[i][j] = -1;
        }
        double[] levels_mW = new double[5];
        for (int i=0;i<5;i++)
            levels_mW[i] = min_h+i*h_length/4;
        for (int i=0;i<data.size();i++)
        {
            locationData item = (locationData) data.get(i);
            int x = (max_x-item.x)/10;
            int y = (item.y-min_y)/10;
            int h = item.height-min_h;
            data_mW[x][y] = item.height;
        }
        for (int i=0;i<x_length+1;i++) {
            for (int j=0;j<y_length+1;j++) {
                if (data_mW[i][j] == -1) {
                    data_mW[i][j] = caculateValue(i, j,data_mW,x_length+1,y_length+1);

                }

            }
        }

        int pic_width=0;
        int pic_height = 0;
        if ((double)(x_length/y_length)>744/461)
        {
            pic_width = 744;
            pic_height = 744*y_length/x_length;
        }
        else
        {
            pic_height = 461;
            pic_width = 461*x_length/y_length;
        }

        baseBitmap = Bitmap.createBitmap(744,461, Bitmap.Config.ARGB_8888);
        canvasHandler = new Canvas(baseBitmap);
        isoband(data_mW, levels_mW, 744, 461, x_length, y_length);
    }
}



