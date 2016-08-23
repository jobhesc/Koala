package com.hesc.koala.model;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by hesc on 16/8/10.
 * <p>下载文件数据详情</p>
 */
public class KoalaDownloadSpecialData implements Serializable, Cloneable{

    private long mTotalBytes = -1;
    private long mDownloadBytes;
    private String mLocalPath;
    private String mUrl;

    public KoalaDownloadSpecialData(){}

    public KoalaDownloadSpecialData(String url, String localPath, long totalBytes, long downloadBytes){
        this.mUrl = url;
        this.mLocalPath = localPath;
        this.mTotalBytes = totalBytes;
        this.mDownloadBytes = downloadBytes;
    }

    /**
     * 获取下载文件总的大小(单位：字节)
     */
    public long getTotalBytes() {
        return mTotalBytes;
    }

    /**
     * 获取目前已经下载的大小(单位：字节)
     */
    public long getDownloadBytes() {
        return mDownloadBytes;
    }

    /**
     * 获取下载文件本地路径
     */
    public String getLocalPath() {
        return mLocalPath;
    }

    /**
     * 获取下载文件url地址
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 设置下载文件总的大小(单位：字节)
     */
    public void setTotalBytes(long totalBytes) {
        mTotalBytes = totalBytes;
    }

    /**
     * 设置目前已经下载的大小(单位：字节)
     */
    public void setDownloadBytes(long downloadBytes) {
        mDownloadBytes = downloadBytes;
    }

    /**
     * 设置下载文件本地路径
     */
    public void setLocalPath(String localPath) {
        mLocalPath = localPath;
    }

    /**
     * 设置下载文件url地址
     */
    public void setUrl(String url) {
        mUrl = url;
    }

    public KoalaDownloadSpecialData clone(){
        try {
            return (KoalaDownloadSpecialData)super.clone();
        } catch (CloneNotSupportedException e) {
            return new KoalaDownloadSpecialData();
        }
    }

    public boolean isDownloadComplete(){
        return mTotalBytes == mDownloadBytes;
    }

    public void checkValid(){
        if(TextUtils.isEmpty(mUrl)){
            throw new IllegalArgumentException("check data: download url is null!");
        }

        if(TextUtils.isEmpty(mLocalPath)){
            throw new IllegalArgumentException("check data: local path is null!");
        }

    }
}
