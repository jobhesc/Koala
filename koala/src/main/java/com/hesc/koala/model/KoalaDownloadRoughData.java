package com.hesc.koala.model;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaDownloadRoughData {

    private long mActionTime;
    private int mId;
    private String mUrl;
    private ActionKind mActionKind;
    private Throwable mThrowable;

    private long mTotalBytes;
    private long mDownloadBytes;

    public KoalaDownloadRoughData setTotalBytes(long totalBytes) {
        mTotalBytes = totalBytes;
        return this;
    }

    public KoalaDownloadRoughData setDownloadBytes(long downloadBytes) {
        mDownloadBytes = downloadBytes;
        return this;
    }

    public KoalaDownloadRoughData setActionTime(long actionTime) {
        mActionTime = actionTime;
        return this;
    }

    public KoalaDownloadRoughData setId(int id) {
        mId = id;
        return this;
    }

    public KoalaDownloadRoughData setUrl(String url) {
        mUrl = url;
        return this;
    }

    public KoalaDownloadRoughData setActionKind(ActionKind actionKind) {
        mActionKind = actionKind;
        return this;
    }

    public KoalaDownloadRoughData setThrowable(Throwable e){
        mThrowable = e;
        return this;
    }

    public long getActionTime() {
        return mActionTime;
    }

    public int getId() {
        return mId;
    }

    public ActionKind getActionKind() {
        return mActionKind;
    }

    public Throwable getThrowable(){
        return mThrowable;
    }

    public String getUrl(){
        return mUrl;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    public long getDownloadBytes() {
        return mDownloadBytes;
    }

    public enum ActionKind{
        START,
        PAUSE,
        RESUME,
    }
}
