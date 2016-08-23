package com.hesc.koala.exception;

/**
 * Created by hesc on 16/8/10.
 * <p>下载任务正在下载过程中，对下载参数进行设置异常</p>
 */
public class KoalaDownloadAccessException extends RuntimeException {

    public KoalaDownloadAccessException(String message){
        super(message);
    }

}
