package com.hesc.koala;

import android.content.Context;
import android.text.TextUtils;

import com.hesc.koala.impl.KoalaDownloader;
import com.hesc.koala.impl.KoalaPersistence;
import com.hesc.koala.intf.IKoalaCustomAction;
import com.hesc.koala.intf.IKoalaDownloadLocalPathBuilder;
import com.hesc.koala.intf.IKoalaDownloader;
import com.hesc.koala.intf.IKoalaDownloaderFactory;
import com.hesc.koala.intf.IKoalaPersistence;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by hesc on 16/8/10.
 * <p>下载配置类</p>
 */
public class KoalaConfig implements Cloneable{

    private static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    private Context mContext;
    private boolean mIsEnableAvoidDropFrame = true;

    private int mMaxAutoRetryTimes = 3;
    private int mConnectTimeoutMillis = 3000;
    private int mReadTimeoutMillis = 5000;
    private int mBufferSize = 1024 * 4;

    private long mMinAvailableSpace = 0;
    private String mDownloadLocalRootPath;
    private Executor mDownloadExecutor;
    private IKoalaPersistence mPersistence;
    private IKoalaDownloaderFactory mDownloaderFactory;
    private IKoalaDownloadLocalPathBuilder mLocalPathBuilder;
    private List<IKoalaCustomAction> mCustomActionList = new ArrayList<>();

    private KoalaConfig(){ }

    /**
     * 获取上下文
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 获取是否开启了避免掉帧功能
     */
    public boolean isEnableAvoidDropFrame() {
        return mIsEnableAvoidDropFrame;
    }

    /**
     * 获取最大的失败自动重连次数
     */
    public int getMaxAutoRetryTimes() {
        return mMaxAutoRetryTimes;
    }

    /**
     * 获取下载缓冲池大小，单位：字节
     */
    public int getBufferSize() {
        return mBufferSize;
    }

    /**
     * 获取网络连接超时时间，单位：毫秒
     */
    public int getConnectTimeoutMillis() {
        return mConnectTimeoutMillis;
    }

    /**
     * 获取读取网络数据超时时间，单位：毫秒
     */
    public int getReadTimeoutMillis() {
        return mReadTimeoutMillis;
    }

    /**
     * 获取最小的可用空间，即下载后的剩余空间下限值
     */
    public long getMinAvailableSpace() {
        return mMinAvailableSpace;
    }

    /**
     * 获取下载文件本地根目录路径
     */
    public String getDownloadLocalRootPath() {
        return mDownloadLocalRootPath;
    }

    /**
     * 获取下载线程执行器
     */
    public Executor getDownloadExecutor() {
        return mDownloadExecutor;
    }

    /**
     * 获取下载数据状态的持久化接口
     */
    public IKoalaPersistence getPersistence() {
        return mPersistence;
    }

    /**
     * 获取创建实际执行下载任务的工厂类
     */
    public IKoalaDownloaderFactory getDownloaderFactory() {
        return mDownloaderFactory;
    }

    /**
     * 获取本地文件名构造类
     */
    public IKoalaDownloadLocalPathBuilder getLocalPathBuilder(){
        return mLocalPathBuilder;
    }

    /**
     * 获取自定义动作列表
     */
    public List<IKoalaCustomAction> getCustomActionList(){
        return mCustomActionList;
    }

    public KoalaConfig clone() {
        try {
            return (KoalaConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new KoalaConfig();
        }
    }

    public static class Builder{

        private KoalaConfig mConfig;

        public Builder(Context context){
            mConfig = new KoalaConfig();
            mConfig.mContext = context.getApplicationContext();
        }

        public Builder(KoalaConfig config){
            mConfig = config.clone();
        }

        /**
         * 在下载过程中如果频繁通知UI线程刷新下载进度，有可能会出现掉帧的问题。
         * 这里设置是否开启避免掉帧功能，默认是开启的
         */
        public Builder setEnableAvoidDropFrame(boolean enableAvoidDropFrame) {
            mConfig.mIsEnableAvoidDropFrame = enableAvoidDropFrame;
            return this;
        }

        /**
         * 在下载过程中如果连接失败，提供失败自动重连功能，这里设置最大失败重连次数，默认值为3次，设置值的范围为[0,10]
         */
        public Builder setMaxAutoRetryTimes(int maxAutoRetryTimes) {
            if(maxAutoRetryTimes<0) return this;
            if(maxAutoRetryTimes>10) return this;

            mConfig.mMaxAutoRetryTimes = maxAutoRetryTimes;
            return this;
        }

        /**
         * 设置下载缓存池大小，单位：字节
         */
        public Builder setBufferSize(int bufferSize) {
            if(bufferSize<0) return this;

            mConfig.mBufferSize = bufferSize;
            return this;
        }

        /**
         * 设置网络连接超时时间，单位：毫秒
         */
        public Builder setConnectTimeoutMillis(int connectTimeoutMillis) {
            if(connectTimeoutMillis<0) return this;

            mConfig.mConnectTimeoutMillis = connectTimeoutMillis;
            return this;
        }

        /**
         * 设置读取网络数据超时时间，单位：毫秒
         */
        public Builder setReadTimeoutMillis(int readTimeoutMillis) {
            if(readTimeoutMillis<0) return this;

            mConfig.mReadTimeoutMillis = readTimeoutMillis;
            return this;
        }

        /**
         * 下载后需要保证最小的剩余空间，这里设置最小的剩余空间，单位为字节，默认值为0B
         */
        public Builder setMinAvailableSpace(long minAvailableSpace) {
            if(minAvailableSpace<0) return this;
            mConfig.mMinAvailableSpace = minAvailableSpace;
            return this;
        }

        /**
         * 设置下载文件后所存放的本地根目录路径
         */
        public Builder setDownloadLocalRootPath(String downloadLocalRootPath) {
            mConfig.mDownloadLocalRootPath = downloadLocalRootPath;
            return this;
        }

        /**
         * 设置下载线程执行器
         */
        public Builder setDownloadExecutor(Executor downloadExecutor) {
            mConfig.mDownloadExecutor = downloadExecutor;
            return this;
        }

        /**
         * 设置下载数据状态的持久化类
         */
        public Builder setPersistence(IKoalaPersistence persistence) {
            mConfig.mPersistence = persistence;
            return this;
        }

        /**
         * 设置创建实际执行下载任务的工厂类
         */
        public Builder setDownloaderFactory(IKoalaDownloaderFactory downloaderFactory) {
            mConfig.mDownloaderFactory = downloaderFactory;
            return this;
        }

        /**
         * 设置本地文件名构造类
         */
        public Builder setLocalPathBuilder(IKoalaDownloadLocalPathBuilder localPathBuilder){
            mConfig.mLocalPathBuilder = localPathBuilder;
            return this;
        }

        /**
         * 设置自定义动作列表
         */
        public Builder setCustomActionList(List<IKoalaCustomAction> customActionList){
            if(customActionList == null) return this;
            mConfig.mCustomActionList = new ArrayList<>(customActionList);
            return this;
        }

        /**
         * 添加自定义动作
         */
        public Builder addCustomAction(IKoalaCustomAction customAction){
            if(customAction == null) return this;
            mConfig.mCustomActionList.add(customAction);
            return this;
        }

        private String getDefaultDownloadLocalPath(Context context){
            String cacheFile = KoalaUtils.getCacheFilePath(context);
            return cacheFile + File.separator + "Koala" + File.separator + "download" + File.separator;
        }

        private IKoalaDownloaderFactory getDefaultDownloadFactory(){
            return new IKoalaDownloaderFactory() {
                @Override
                public IKoalaDownloader create() {
                    return new KoalaDownloader();
                }
            };
        }

        private IKoalaDownloadLocalPathBuilder getDefaultLocalPathBuilder(){
            return new IKoalaDownloadLocalPathBuilder() {
                @Override
                public String build(String url, String downloadRootPath) {
                    return downloadRootPath + File.separator + UUID.randomUUID().toString() + KoalaUtils.getFileExt(url);
                }
            };
        }

        public KoalaConfig build(){
            if(TextUtils.isEmpty(mConfig.mDownloadLocalRootPath)){
                mConfig.mDownloadLocalRootPath = getDefaultDownloadLocalPath(mConfig.mContext);
            }

            if(mConfig.mDownloadExecutor == null){
                mConfig.mDownloadExecutor = Executors.newFixedThreadPool(CPU_NUM);
            }

            if(mConfig.mDownloaderFactory == null){
                mConfig.mDownloaderFactory = getDefaultDownloadFactory();
            }

            if(mConfig.mPersistence == null){
                mConfig.mPersistence = new KoalaPersistence(mConfig.getContext());
            }

            if(mConfig.mLocalPathBuilder == null){
                mConfig.mLocalPathBuilder = getDefaultLocalPathBuilder();
            }

            return mConfig;
        }
    }
}
