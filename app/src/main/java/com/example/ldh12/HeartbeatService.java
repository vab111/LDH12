package com.example.ldh12;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.android.guard.CommunicationService;

public class HeartbeatService extends Service implements Runnable {
    private CommunicationService mService;
    private Thread mThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        heartData.heart[0] = 0x05;
        while (true)
        {
            try
            {
                mService = CommunicationService.getInstance(this);
                byte[] order = new byte[4];
                order[0] = -32;
                order[1] = 64;
                order[2] = 0x00;
                order[3] = 0x00;
                mService.sendCan(order, heartData.heart);
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onCreate()
    {
        super.onCreate();
        mThread = new Thread(this);
        mThread.start();
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
