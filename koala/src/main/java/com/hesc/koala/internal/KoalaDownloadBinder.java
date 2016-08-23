package com.hesc.koala.internal;

import android.os.Binder;

import com.hesc.koala.KoalaConfig;
import com.hesc.koala.intf.IKoalaDownloadDashboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaDownloadBinder extends Binder {

    private Map<String, IKoalaDownloadDashboard> mDashboardMap;

    public KoalaDownloadBinder(){
        mDashboardMap = new HashMap<>();
    }

    public void onDestroy(){
        for(String key: mDashboardMap.keySet()){
            mDashboardMap.get(key).destroy();
        }
        mDashboardMap.clear();
        mDashboardMap = null;
    }

    public IKoalaDownloadDashboard getDashboard(String key, KoalaConfig config) {
        if(mDashboardMap.containsKey(key)){
            return mDashboardMap.get(key);
        }

        IKoalaDownloadDashboard dashboard = new KoalaDownloadContainer(key, config);
        mDashboardMap.put(key, dashboard);
        return dashboard;
    }

    public void releaseDashboard(String key){
        mDashboardMap.remove(key);
    }
}
