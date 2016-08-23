package com.hesc.koala.internal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaDownloadService extends Service {

    private KoalaDownloadBinder mBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new KoalaDownloadBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.i("kkk", "onDestroy");
        mBinder.onDestroy();
        super.onDestroy();
    }
}
