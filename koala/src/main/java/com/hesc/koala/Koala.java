package com.hesc.koala;

import com.hesc.koala.internal.KoalaDownloadProxy;
import com.hesc.koala.intf.IKoalaDownloadDashboard;
import com.hesc.koala.intf.IKoalaDownloadListener;
import com.hesc.koala.model.KoalaDownloadProfile;

/**
 * Created by hesc on 16/8/10.
 * <p>文件下载入口类</p>
 */
public class Koala implements IKoalaDownloadDashboard{

    private static final String DEFAULT_KEY = "koala";
    //全局配置文件
    private static KoalaConfig GLOBAL_CONFIG;

    private IKoalaDownloadDashboard mDownloadProxy;

    private Koala(String key, KoalaConfig config){
        if(config == null){
            throw new IllegalArgumentException("parameter config is null! ");
        }

        mDownloadProxy = new KoalaDownloadProxy(key, config.clone());
    }

    /**
     * 获取全局下载配置类信息
     */
    public static KoalaConfig getGlobalConfig(){
        return GLOBAL_CONFIG;
    }

    /**
     * 全局进行初始化方法
     */
    public static void initialize(KoalaConfig config){
        GLOBAL_CONFIG = config;
    }

    /**
     * 创建下载实例
     * @param key 下载标识，主要用于区分多次不同任务下载
     * @param config 当config为null时，按照全局配置信息(global config)来执行下载任务
     * @return 下载实例
     */
    public static Koala create(String key, KoalaConfig config){
        return new Koala(key, config);
    }

    /**
     * 按照全局配置信息(global config)来执行下载任务
     * @param key 下载标识，主要用于区分多次不同任务下载
     * @return 下载实例
     */
    public static Koala create(String key){
        return create(key, GLOBAL_CONFIG);
    }

    public static Koala create(){
        return create(DEFAULT_KEY);
    }

    public static Koala create(KoalaConfig config){
        return create(DEFAULT_KEY, config);
    }

    @Override
    public void start(String url, IKoalaDownloadListener listener) {
        mDownloadProxy.start(url, listener);
    }

    @Override
    public void start(String[] urls, IKoalaDownloadListener listener) {
        mDownloadProxy.start(urls, listener);
    }

    @Override
    public void stop(int downloadId) {
        mDownloadProxy.stop(downloadId);
    }

    @Override
    public void stopAll() {
        mDownloadProxy.stopAll();
    }

    @Override
    public void pause(int downloadId) {
        mDownloadProxy.pause(downloadId);
    }

    @Override
    public void pauseAll() {
        mDownloadProxy.pauseAll();
    }

    @Override
    public void resume(int downloadId) {
        mDownloadProxy.resume(downloadId);
    }

    @Override
    public void resumeAll() {
        mDownloadProxy.resumeAll();
    }

    @Override
    public void remove(int downloadId) {
        mDownloadProxy.remove(downloadId);
    }

    @Override
    public void removeAll() {
        mDownloadProxy.removeAll();
    }

    @Override
    public void bindAll(IKoalaDownloadListener listener) {
        mDownloadProxy.bindAll(listener);
    }

    @Override
    public void bind(int downloadId, IKoalaDownloadListener listener) {
        mDownloadProxy.bind(downloadId, listener);
    }

    @Override
    public void unbind(int downloadId, IKoalaDownloadListener listener) {
        mDownloadProxy.unbind(downloadId, listener);
    }

    @Override
    public void unbindAll(IKoalaDownloadListener listener) {
        mDownloadProxy.unbindAll(listener);
    }

    @Override
    public KoalaDownloadProfile getProfile(int downloadId) {
        return mDownloadProxy.getProfile(downloadId);
    }

    @Override
    public boolean hasRunningTask() {
        return mDownloadProxy.hasRunningTask();
    }

    @Override
    public boolean isRunning(int downloadId) {
        return mDownloadProxy.isRunning(downloadId);
    }

    @Override
    public void destroy() {
        mDownloadProxy.destroy();
    }
}
