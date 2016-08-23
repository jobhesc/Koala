package com.hesc.koala.internal;

import com.hesc.koala.KoalaConfig;
import com.hesc.koala.intf.IKoalaDownloadDashboard;
import com.hesc.koala.intf.IKoalaDownloadListener;
import com.hesc.koala.model.KoalaDownloadProfile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaDownloadProxy implements IKoalaDownloadDashboard {
    private String mKey;
    private KoalaConfig mConfig;
    private List<KoalaDownloadClient.Action> mActionList = new ArrayList<>();

    public KoalaDownloadProxy(String key, KoalaConfig config){
        mKey = key;
        mConfig = config;
    }

    private WeakReference<KoalaDownloadClient.Action> getWeakAction(KoalaDownloadClient.Action action){
        mActionList.add(action);  //防止被gc快速回收
        return new WeakReference<>(action);
    }

    @Override
    public void start(final String url, final IKoalaDownloadListener listener) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.start(url, listener);
                    }
                }));
    }

    @Override
    public void start(final String[] urls, final IKoalaDownloadListener listener) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.start(urls, listener);
                    }
                }));
    }

    @Override
    public void stop(final int downloadId) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.stop(downloadId);
                    }
                }));
    }

    @Override
    public void stopAll() {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.stopAll();
                    }
                }));
    }

    @Override
    public void pause(final int downloadId) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.pause(downloadId);
                    }
                }));
    }

    @Override
    public void pauseAll() {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.pauseAll();
                    }
                }));
    }

    @Override
    public void resume(final int downloadId) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.resume(downloadId);
                    }
                }));
    }

    @Override
    public void resumeAll() {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.resumeAll();
                    }
                }));
    }

    @Override
    public void remove(final int downloadId) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.remove(downloadId);
                    }
                }));
    }

    @Override
    public void removeAll() {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.removeAll();
                    }
                }));
    }

    @Override
    public void bindAll(final IKoalaDownloadListener listener) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.bindAll(listener);
                    }
                }));
    }

    @Override
    public void bind(final int downloadId, final IKoalaDownloadListener listener) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.bind(downloadId, listener);
                    }
                }));
    }

    @Override
    public void unbind(final int downloadId, final IKoalaDownloadListener listener) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.unbind(downloadId, listener);
                    }
                }));
    }

    @Override
    public void unbindAll(final IKoalaDownloadListener listener) {
        KoalaDownloadClient.getInstance().run(mKey, mConfig,
                getWeakAction(new KoalaDownloadClient.Action() {
                    @Override
                    public void execute(IKoalaDownloadDashboard dashboard) {
                        dashboard.unbindAll(listener);
                    }
                }));
    }

    @Override
    public KoalaDownloadProfile getProfile(int downloadId) {
        return KoalaDownloadClient.getInstance().isConnected()?
                KoalaDownloadClient.getInstance().getDashboard(mKey, mConfig).getProfile(downloadId):
                KoalaDownloadProfile.empty();
    }

    @Override
    public boolean hasRunningTask() {
        return KoalaDownloadClient.getInstance().isConnected() &&
                KoalaDownloadClient.getInstance().getDashboard(mKey, mConfig).hasRunningTask();
    }

    @Override
    public boolean isRunning(int downloadId) {
        return KoalaDownloadClient.getInstance().isConnected() &&
                KoalaDownloadClient.getInstance().getDashboard(mKey, mConfig).isRunning(downloadId);
    }

    @Override
    public void destroy() {
        if(KoalaDownloadClient.getInstance().isConnected()){
            KoalaDownloadClient.getInstance().releaseDashboard(mKey, mConfig);
        }
        mActionList.clear();
        mActionList = null;
    }
}
