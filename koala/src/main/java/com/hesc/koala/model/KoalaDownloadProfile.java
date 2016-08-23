package com.hesc.koala.model;

import com.hesc.koala.KoalaDownloadStatus;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaDownloadProfile {

    private int mId;
    private KoalaDownloadStatus mStatus;
    private long mDownloadTime;
    private Detail[] mDetails;

    KoalaDownloadProfile(int id, KoalaDownloadStatus status, long time, Detail[] details){
        this.mId = id;
        this.mStatus = status;
        this.mDownloadTime = time;

        if(details == null){
            this.mDetails = new Detail[0];
        } else {
            this.mDetails = details;
        }
    }

    public static KoalaDownloadProfile empty(){
        return new KoalaDownloadProfile(0, KoalaDownloadStatus.PENDING, 0, new Detail[0]);
    }

    public boolean isEmpty(){
        return mId<=0;
    }

    public int getId() {
        return mId;
    }

    public KoalaDownloadStatus getStatus() {
        return mStatus;
    }

    public long getDownloadTime() {
        return mDownloadTime;
    }

    public Detail[] getDetails() {
        return mDetails;
    }

    public Detail getDetail(String url){
        for(Detail detail: mDetails){
            if(detail.getUrl().equals(url)){
                return detail;
            }
        }
        return null;
    }

    public Detail getDownloadingDetail(){
        for(Detail detail: mDetails){
            if(!detail.isDownloadComplete()){
                return detail;
            }
        }
        return null;
    }

    public long getTotalBytes(){
        long totalBytes = 0;
        for(Detail detail: mDetails){
            totalBytes += detail.getTotalBytes();
        }
        return totalBytes;
    }

    public long getDownloadBytes(){
        long downloadBytes = 0;
        for(Detail detail: mDetails){
            downloadBytes += detail.getDownloadBytes();
        }
        return downloadBytes;
    }

    public boolean isDownloadComplete(){
        for(Detail detail: mDetails){
            if(!detail.isDownloadComplete()){
                return false;
            }
        }
        return true;
    }

    public static class Detail {
        private long mTotalBytes;
        private long mDownloadBytes;
        private String mLocalPath;
        private String mUrl;

        Detail(String url, String localPath, long totalBytes, long downloadBytes){
            this.mUrl = url;
            this.mLocalPath = localPath;
            this.mTotalBytes = totalBytes;
            this.mDownloadBytes = downloadBytes;
        }

        public long getTotalBytes() {
            return mTotalBytes;
        }

        public long getDownloadBytes() {
            return mDownloadBytes;
        }

        public String getLocalPath() {
            return mLocalPath;
        }

        public String getUrl() {
            return mUrl;
        }

        public boolean isDownloadComplete(){
            return mTotalBytes <= mDownloadBytes;
        }
    }
}
