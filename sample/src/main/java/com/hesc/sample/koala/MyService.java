package com.hesc.sample.koala;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    private MyBinder mBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new MyBinder();
        Log.e("kkk", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("kkk", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("kkk", "onBind");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.e("kkk", "onDestroy");
        mBinder = null;
        super.onDestroy();
    }

    public static class MyBinder extends Binder{
        public void doSomeThing(){
            Log.e("kkk", "doSomeThing");
        }
    }
}
