package com.example.ldh12;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

public class FileAccess {
    static public SettingData item = new SettingData();
    public void getData()
    {
        ArrayList fileList = new ArrayList();
        File appDir = new File(Environment.getExternalStorageDirectory()+"/LDH21");   //自定义的目录
        if (!appDir.exists()) {
            boolean isSuccess = appDir.mkdir();
            Log.d("MsgId:" ,"----------0------------------"+isSuccess);
        }
        else
            Log.d("MsgId:" ,"----------0------------------目录已经存在:"+Environment.getExternalStorageDirectory()+"/LDH21");

        File fs = new File(Environment.getExternalStorageDirectory()+"/LDH21/Record.json");
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
                fileList = gson.fromJson(result, new TypeToken<List<SettingData>>() {
                }.getType());

                SettingData data = (SettingData) fileList.get(0);
                item.freqency = data.freqency;
                item.agile = data.agile;
                item.accurent = data.accurent;
            }

        }
        else {
            try {
                FileOutputStream outputStream =new FileOutputStream(fs);
                OutputStreamWriter outStream = new OutputStreamWriter(outputStream);

                item.accurent = 1.0f;
                item.agile = 1;
                item.freqency = 8;
                fileList.add(item);
                Gson gson = new Gson();
                String jsonString = gson.toJson(fileList);
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

    }
    public void saveData()
    {
        ArrayList fileList = new ArrayList();
        File fs = new File(Environment.getExternalStorageDirectory()+"/LDH21/Record.json");
        try {
            FileOutputStream outputStream =new FileOutputStream(fs);
            OutputStreamWriter outStream = new OutputStreamWriter(outputStream);

            fileList.add(item);
            Gson gson = new Gson();
            String jsonString = gson.toJson(fileList);
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
}


