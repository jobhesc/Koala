package com.hesc.koala.intf;

/**
 * Created by hesc on 16/8/10.
 */
public interface IKoalaDownloadListener {
    void onBind(int downloadId);
    void onPending(int downloadId);
    void onConnected(int downloadId, String url);
    void onProgress(int downloadId, String url, long totalBytes, long downloadBytes, long urlTotalBytes, long urlDownloadBytes);
    void onBlockComplete(int downloadId, String url);
    void onComplete(int downloadId);
    void onError(int downloadId, Throwable e);
    void onResume(int downloadId);
    void onPause(int downloadId);
    void onBeforeAction(int downloadId);
    void onAfterAction(int downloadId, Object params);
}
