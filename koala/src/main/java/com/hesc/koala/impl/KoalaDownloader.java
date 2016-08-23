package com.hesc.koala.impl;

import android.os.Build;
import android.text.TextUtils;

import com.hesc.koala.KoalaUtils;
import com.hesc.koala.exception.KoalaDownloadAccessException;
import com.hesc.koala.exception.KoalaDownloadInvalidException;
import com.hesc.koala.exception.KoalaDownloadSizeException;
import com.hesc.koala.intf.IKoalaDownloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hesc on 16/8/10.
 * <p>默认的下载器</p>
 */
public class KoalaDownloader implements IKoalaDownloader {
    protected Config mConfig;
    protected int mDownloadId;
    protected String mUrl;
    protected String mLocalPath;
    protected boolean mRunning = false;
    protected boolean mCancel = false;
    protected long mTotalBytes = 0;

    @Override
    public void setConfig(Config config) {
        mConfig = config;
    }

    @Override
    public void setDownloadData(int downloadId, String url, String localPath) {
        if(mRunning){
            throw new KoalaDownloadAccessException("the download is running, don't set download data");
        }
        checkDownloadData(url, localPath);
        mDownloadId = downloadId;
        mUrl = url;
        mLocalPath = localPath;
        mTotalBytes = 0;
        mCancel = false;
    }

    @Override
    public long getTotalBytes() throws KoalaDownloadSizeException {
        checkDownloadData(mUrl, mLocalPath);

        if(mTotalBytes>0){
            return mTotalBytes;
        }

        long totalBytes = 0;
        int retryTime = 0;
        do {
            try {
                HttpURLConnection connection = createConnection(mUrl);
                connection.setRequestMethod("HEAD");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    totalBytes = Long.parseLong(connection.getHeaderField("Content-Length"));
                }
                connection.disconnect();
                break;
            } catch (Throwable e){
                e.printStackTrace();
            }
            retryTime++;
        } while(retryTime<mConfig.maxAutoRetryTimes);

        if(totalBytes <= 0){
            throw new KoalaDownloadSizeException(KoalaUtils.formatString(
                    "can't know the size of the download file(%s)!", mUrl));
        }

        mTotalBytes = totalBytes;
        return totalBytes;
    }

    private long getDownloadBytes(){
        File file = new File(mLocalPath);
        return file.exists()?file.length():0;
    }

    protected HttpURLConnection createConnection(String url) throws IOException {
        checkConfig(mConfig);

        URL requestURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
        connection.setConnectTimeout(mConfig.connectTimeoutMillis);
        connection.setReadTimeout(mConfig.readTimeoutMillis);
        connection.setChunkedStreamingMode(0);
        addHeader(connection);
        return connection;
    }

    protected void addHeader(HttpURLConnection connection){
        connection.setRequestProperty("User-Agent", "Android "+ Build.VERSION.RELEASE);
        //要求服务器不使用gzip传输数据，否则content-length获取到的数据可能比实际文件小
        connection.setRequestProperty("Accept-Encoding", "identity");
    }

    protected void resumeBrokenDownloads(HttpURLConnection connection, long downloadBytes){
        //断点续传
        if(downloadBytes>0) {
            connection.setRequestProperty("Range", "bytes=" + downloadBytes + "-");
        }
    }

    private boolean isServerSupportResume(HttpURLConnection connection) {
        String ranges = connection.getHeaderField("Accept-Ranges");
        if (ranges != null) {
            return ranges.contains("bytes");
        }
        ranges = connection.getHeaderField("Content-Range");
        return ranges != null && ranges.contains("bytes");
    }

    private void checkConfig(Config config){
        if(config == null){
            throw new IllegalArgumentException("config is null! ");
        }
    }

    private void checkDownloadData(String url, String localPath){
        if(TextUtils.isEmpty(url)){
            throw new IllegalArgumentException("url is empty!");
        }

        if(TextUtils.isEmpty(localPath)){
            throw new IllegalArgumentException("localPath is empty!");
        }
    }

    @Override
    public void run(Callback callback) {
        if(mRunning){
            throw new KoalaDownloadAccessException("the download is running, don't run again");
        }

        checkDownloadData(mUrl, mLocalPath);
        mRunning = true;
        mCancel = false;
        try{
            loop(callback);
        } finally {
            mRunning = false;
        }
    }

    private void loop(Callback callback){
        HttpURLConnection connection;
        int retryTime = 0;
        Throwable lastException = null;
        do {
            try {
                long totalBytes = getTotalBytes();
                long downloadBytes = getDownloadBytes();

                connection = createConnection(mUrl);
                connection.setRequestMethod("GET");
                resumeBrokenDownloads(connection, downloadBytes);
                connection.connect();

                if(mCancel){
                    if(callback != null){
                        callback.onCancel(mDownloadId, mUrl);
                    }
                    break;
                }

                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK ||
                        responseCode == HttpURLConnection.HTTP_PARTIAL) {

                    if (callback != null) {
                        callback.onConnect(mDownloadId, mUrl);
                    }

                    //判断服务端是否支持断点续传
                    boolean isAutoResume = isServerSupportResume(connection);
                    FileOutputStream out = new FileOutputStream(mLocalPath, isAutoResume);
                    if(!isAutoResume) downloadBytes = 0;

                    writeToFile(connection.getInputStream(), out, totalBytes, downloadBytes, callback);
                    lastException = null;
                    break;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                lastException = e;
            }
            retryTime++;
        } while(retryTime<mConfig.maxAutoRetryTimes);

        if(lastException != null){
            if(callback != null){
                callback.onError(mDownloadId, mUrl, lastException);
            }
        }
    }

    protected void writeToFile(InputStream inputStream, OutputStream outputStream,
                               long totalBytes, long downloadBytes,
                               Callback callback) throws Throwable {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            out =new BufferedOutputStream(outputStream);
            in = new BufferedInputStream(inputStream);

            int len;
            byte[] buffer = new byte[mConfig.bufferSize];
            while ((len = in.read(buffer)) > 0) {
                if(mCancel) {
                    if(callback != null){
                        callback.onCancel(mDownloadId, mUrl);
                    }
                    return;
                }

                out.write(buffer, 0, len);

                downloadBytes += len;
                if (callback != null) {
                    callback.onProgress(mDownloadId, mUrl, totalBytes, downloadBytes);
                }
            }
            out.flush();

            //完整性校验
            if(downloadBytes != totalBytes){
                throw new KoalaDownloadInvalidException(KoalaUtils.formatString(
                        "download bytes[%d] not equal total[%d] in the download file(%s)",
                        downloadBytes, totalBytes, mUrl));
            }

            if (callback != null) {
                callback.onComplete(mDownloadId, mUrl);
            }
        } finally {
            if(out != null)
                out.close();
            if(in != null)
                in.close();
        }
    }

    @Override
    public void cancel() {
        mCancel = true;
    }

    @Override
    public boolean isCanceled() {
        return mCancel;
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }
}
