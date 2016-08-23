package com.hesc.koala.model;

import android.text.TextUtils;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.intf.IKoalaDownloadLocalPathBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hesc on 16/8/10.
 * <p>下载文件数据实体</p>
 */
public class KoalaDownloadEntity implements Serializable, Cloneable{

    private int mId;
    private KoalaDownloadStatus mStatus;
    private long mTime;
    private KoalaDownloadSpecialData[] mSpecialDatas;

    /**
     * 获取下载id
     */
    public int getId() {
        return mId;
    }

    /**
     * 设置下载id
     */
    public void setId(int id) {
        mId = id;
    }

    /**
     * 获取当前下载状态
     */
    public KoalaDownloadStatus getStatus() {
        return mStatus;
    }

    /**
     * 设置当前下载状态
     */
    public void setStatus(KoalaDownloadStatus status) {
        mStatus = status;
    }

    /**
     * 获取下载时间，即首次进入下载队列的时间，单位：毫秒
     */
    public long getTime() {
        return mTime;
    }

    /**
     * 设置首次进入下载队列的时间，单位：毫秒
     */
    public void setTime(long time) {
        mTime = time;
    }

    /**
     * 获取下载数据详情
     */
    public KoalaDownloadSpecialData[] getSpecialDatas() {
        return mSpecialDatas;
    }

    /**
     * 设置下载数据详情
     */
    public void setSpecialDatas(KoalaDownloadSpecialData[] specialDatas) {
        mSpecialDatas = specialDatas;
    }

    public KoalaDownloadEntity clone(){
        checkValid();

        try {
            KoalaDownloadEntity entity = (KoalaDownloadEntity) super.clone();
            //深复制KoalaDownloadSpecialData
            entity.mSpecialDatas = cloneSpecialDatas();

            return entity;
        } catch (CloneNotSupportedException e) {
            return new KoalaDownloadEntity();
        }
    }

    public KoalaDownloadSpecialData[] cloneSpecialDatas(){
        checkValid();

        KoalaDownloadSpecialData[] specialDatas = new KoalaDownloadSpecialData[this.mSpecialDatas.length];
        for(int i=0; i<this.mSpecialDatas.length; i++){
            specialDatas[i] = this.mSpecialDatas[i].clone();
        }
        return specialDatas;
    }

    public long getTotalBytes(){
        long totalBytes = 0;
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            totalBytes += specialData.getTotalBytes();
        }
        return totalBytes;
    }

    public long getDownloadBytes(){
        long downloadBytes = 0;
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            downloadBytes += specialData.getDownloadBytes();
        }
        return downloadBytes;
    }

    public static KoalaDownloadEntity create(int downloadId, String[] urls,
                                             String downloadLocalRootPath,
                                             IKoalaDownloadLocalPathBuilder localPathBuilder){
        checkUrlIsEmpty(urls);

        KoalaDownloadSpecialData[] specialDatas = new KoalaDownloadSpecialData[urls.length];
        int i=0;
        for(String url: urls) {
            KoalaDownloadSpecialData specialData = new KoalaDownloadSpecialData();
            specialData.setUrl(url);
            specialData.setTotalBytes(-1);
            specialData.setDownloadBytes(0);
            specialData.setLocalPath(localPathBuilder.build(url, downloadLocalRootPath));

            specialDatas[i++] = specialData;
        }

        KoalaDownloadEntity entity = new KoalaDownloadEntity();
        entity.setSpecialDatas(specialDatas);
        entity.setTime(0);
        entity.setStatus(KoalaDownloadStatus.PENDING);
        entity.setId(downloadId);

        entity.checkValid();
        return entity;
    }

    public KoalaDownloadSpecialData findSpecialData(String url){
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            if(specialData.getUrl().equals(url)){
                return specialData;
            }
        }
        return null;
    }

    public boolean isDownloadComplete(){
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            if(!specialData.isDownloadComplete()){
                return false;
            }
        }
        return true;
    }

    public KoalaDownloadSpecialData getDownloadingSpecialData(){
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            if(!specialData.isDownloadComplete()){
                return specialData;
            }
        }
        return null;
    }

    public void checkValid(){
        if(mId <= 0){
            throw new IllegalArgumentException("check data: download id is less than 0 or equal 0!");
        }

        if(mSpecialDatas == null || mSpecialDatas.length == 0){
            throw new IllegalArgumentException("check data: download special data is null!");
        }

        List<String> urls = new ArrayList<>();
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            if(urls.contains(specialData.getUrl())){
                throw new IllegalArgumentException("check data: download url is duplication in a download task!");
            }
            urls.add(specialData.getUrl());
            specialData.checkValid();
        }
    }

    private static void checkUrlIsEmpty(String[] urls) {
        if(urls == null || urls.length == 0)
            throw new IllegalArgumentException("parameter urls is null! ");

        for(String url: urls){
            if(TextUtils.isEmpty(url)){
                throw new IllegalArgumentException("the element in url array is null!");
            }
        }
    }

    public boolean equalUrls(String[] urls){
        if(urls == null || urls.length == 0) return false;

        if(urls.length != mSpecialDatas.length) return false;

        List<String> urlList = new ArrayList<>(Arrays.asList(urls));
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            if(urlList.contains(specialData.getUrl())){
                urlList.remove(specialData.getUrl());
            }
        }
        return urlList.size() == 0;

    }

    public KoalaDownloadProfile snapshot(){
        KoalaDownloadProfile.Detail[] details = new KoalaDownloadProfile.Detail[mSpecialDatas.length];
        int i=0;
        for(KoalaDownloadSpecialData specialData: mSpecialDatas){
            details[i++] = new KoalaDownloadProfile.Detail(specialData.getUrl(),
                    specialData.getLocalPath(),
                    specialData.getTotalBytes(),
                    specialData.getDownloadBytes());
        }
        return new KoalaDownloadProfile(this.getId(), this.getStatus(), this.getTime(), details);
    }
}
