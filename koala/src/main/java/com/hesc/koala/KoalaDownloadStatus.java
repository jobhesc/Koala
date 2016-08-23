package com.hesc.koala;

/**
 * Created by hesc on 16/8/10.
 * <p>下载状态</p>
 */
public enum KoalaDownloadStatus {
    /**
     * 进入下载队列，还没开始下载
     */
    PENDING(0),

    /**
     * 网络连接成功，还没开始下载
     */
    CONNECTED(1),

    /**
     * 正在下载中
     */
    DOWNLOADING(2),

    /**
     * 在多个文件的下载队列中，只要一个文件下载完成，则其状态就是blockComplete
     */
    BLOCK_COMPLETE(3),

    /**
     * 所有文件下载完成
     */
    COMPLETE(4),

    /**
     * 暂停状态
     */
    PAUSE(5),

    /**
     * 下载错误状态
     */
    ERROR(6);

    private int value;

    KoalaDownloadStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static KoalaDownloadStatus valueOf(int value){
        switch(value){
            case 0:
                return PENDING;
            case 1:
                return CONNECTED;
            case 2:
                return DOWNLOADING;
            case 3:
                return BLOCK_COMPLETE;
            case 4:
                return COMPLETE;
            case 5:
                return PAUSE;
            case 6:
                return ERROR;
            default:
                throw new IllegalArgumentException("illegal status!");
        }
    }
}
