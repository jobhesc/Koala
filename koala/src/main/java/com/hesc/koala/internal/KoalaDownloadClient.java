package com.hesc.koala.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.hesc.koala.KoalaConfig;
import com.hesc.koala.intf.IKoalaDownloadDashboard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaDownloadClient implements ServiceConnection {
    private KoalaDownloadBinder mBinder;
    private List<ActionHolder> mActionList = new ArrayList<>();

    private final static class HolderClass {
        private final static KoalaDownloadClient INSTANCE = new KoalaDownloadClient();
    }

    public static KoalaDownloadClient getInstance(){
        return HolderClass.INSTANCE;
    }

    private KoalaDownloadClient(){ }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        this.mBinder = (KoalaDownloadBinder)service;
        for(int i=mActionList.size()-1; i>=0; i--){
            ActionHolder holder = mActionList.remove(i);
            Action action = holder.action.get();
            if(action != null) {
                action.execute(getDashboard(holder.key, holder.config));
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.mBinder = null;
    }

    public IKoalaDownloadDashboard getDashboard(String key, KoalaConfig config){
        if(this.mBinder == null){
            throw new IllegalArgumentException("binder is null!");
        }
        return this.mBinder.getDashboard(key, config);
    }

    public void releaseDashboard(String key, KoalaConfig config){
        if(this.mBinder == null) return;
        getDashboard(key, config).destroy();
        this.mBinder.releaseDashboard(key);
    }

    public boolean isConnected(){
        return this.mBinder != null;
    }

    public void bindService(Context context){
        Intent intent = new Intent(context, KoalaDownloadService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        context.startService(intent);
    }

    public void unbindService(Context context){
        Intent intent = new Intent(context, KoalaDownloadService.class);
        context.unbindService(this);
        context.stopService(intent);
    }

    public void run(String key, KoalaConfig config, WeakReference<Action> action){
        if(isConnected()){
            if(action.get() != null) {
                action.get().execute(getDashboard(key, config));
            }
        } else {
            bindService(config.getContext());
            mActionList.add(new ActionHolder(key, config, action));
        }
    }

    public interface Action {
        void execute(IKoalaDownloadDashboard dashboard);
    }

    private static class ActionHolder {
        public String key;
        public KoalaConfig config;
        public WeakReference<Action> action;

        public ActionHolder(String key, KoalaConfig config, WeakReference<Action> action){
            this.key = key;
            this.config = config;
            this.action = action;
        }
    }
}
