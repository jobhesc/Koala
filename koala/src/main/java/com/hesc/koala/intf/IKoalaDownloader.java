package com.hesc.koala.intf;

import com.hesc.koala.exception.KoalaDownloadSizeException;

/**
 * Created by hesc on 16/8/10.
 */
public interface IKoalaDownloader {

    void setConfig(Config config);
    void setDownloadData(int downloadId, String url, String localPath);
    long getTotalBytes() throws KoalaDownloadSizeException;
    void run(Callback callback);
    void cancel();
    boolean isCanceled();
    boolean isRunning();

    class Config{
        /**
         * 最大失败自动重连次数
         */
        public int maxAutoRetryTimes;
        /**
         * 连接超时时间，单位：毫秒
         */
        public int connectTimeoutMillis;
        /**
         * 读取网络数据超时时间，单位：毫秒
         */
        public int readTimeoutMillis;
        /**
         * 缓冲区大小，单位：字节
         */
        public int bufferSize;
    }

    interface Callback {
        void onConnect(int downloadId, String url);
        void onProgress(int downloadId, String url, long totalBytes, long downloadBytes);
        void onComplete(int downloadId, String url);
        void onError(int downloadId, String url, Throwable e);
        void onCancel(int downloadId, String url);
    }
}
