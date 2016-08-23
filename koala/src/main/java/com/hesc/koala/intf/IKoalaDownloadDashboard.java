package com.hesc.koala.intf;

import com.hesc.koala.model.KoalaDownloadProfile;

/**
 * Created by hesc on 16/8/10.
 * <p>下载操作仪表盘，定义所有能够执行的下载操作</p>
 */
public interface IKoalaDownloadDashboard {

    /**
     * 开始进行文件下载，如果url等于下载任务(包含已经下载完成的)的url，则按照原下载任务进行下载，否则执行新的下载任务
     *
     * @param url 文件下载的url路径
     * @param listener 文件下载过程侦听类
     */
    void start(String url, IKoalaDownloadListener listener);

    /**
     * 开始进行多个文件下载，如果url数组完全等于下载任务的url数组，则按照原下载任务进行下载，否则执行新的下载任务
     *
     * @param urls 文件下载的url路径数组
     * @param listener 文件下载过程侦听类
     */
    void start(String[] urls, IKoalaDownloadListener listener);

    /**
     * 停止某一个正在下载的任务，并把已下载部分文件删除。
     * 如果该id对应的下载任务不是downloading状态，则不作任何操作
     *
     * @param downloadId 需要停止下载的id
     */
    void stop(int downloadId);

    /**
     * 停止所有的正在下载的任务，并把已下载的部分文件删除。
     * 如果没有正在下载的任务(状态是downloading)，则不作任何操作；
     * 如果有部分正在下载任务，则把这部分下载任务停止，并删除这部分下载数据，对于已经下载状态不是downloading的任务不作任何操作；
     */
    void stopAll();

    /**
     * 暂停某一个正在下载的任务。
     * 如果该id对应的下载任务不是downloading状态，则不作任何操作
     *
     * @param downloadId 需要暂停下载的id
     */
    void pause(int downloadId);

    /**
     * 暂停所有下载任务。
     * 如果没有正在下载的任务(状态是downloading)，则不作任何操作；
     * 如果有部分正在下载任务，则把这部分下载任务暂停，对于已经下载状态不是downloading的任务不作任何操作；
     */
    void pauseAll();

    /**
     * 恢复某一个暂停下载的任务。
     * 如果该id对应的下载任务状态不是pause，则不作任何操作
     *
     * @param downloadId 需要恢复下载的id
     */
    void resume(int downloadId);

    /**
     * 恢复所有已经暂停下载的任务
     * 如果没有暂停下载的任务(状态是pause)，则不作任何操作；
     * 如果有部分暂停下载任务，则把这部分下载任务恢复，对于下载状态不是pause的任务不作任何操作；
     */
    void resumeAll();

    /**
     * 移除指定下载id的下载任务，不管这个下载任务是什么状态，都会从所有相关的地方移除掉
     *
     * @param downloadId 需要移除的下载id
     */
    void remove(int downloadId);

    /**
     * 移除所有下载任务，不管这个下载任务是什么状态，都会从所有相关的地方移除掉
     */
    void removeAll();

    /**
     * 绑定所有下载任务的侦听器
     * @param listener 文件下载过程侦听类
     */
    void bindAll(IKoalaDownloadListener listener);

    /**
     * 绑定指定下载id对应的下载任务的侦听器
     *
     * @param downloadId 下载id
     * @param listener 文件下载过程侦听类
     */
    void bind(int downloadId, IKoalaDownloadListener listener);

    /**
     * 解除指定下载id对应的下载任务的侦听绑定
     *
     * @param downloadId 下载id
     * @param listener 文件下载过程侦听类
     */
    void unbind(int downloadId, IKoalaDownloadListener listener);

    /**
     * 解除所有下载任务的侦听绑定
     *
     * @param listener 文件下载过程侦听类
     */
    void unbindAll(IKoalaDownloadListener listener);

    /**
     * 获取指定下载id的下载状态数据
     *
     * @param downloadId 下载id
     */
    KoalaDownloadProfile getProfile(int downloadId);

    /**
     * 是否存在正在运行的下载任务
     */
    boolean hasRunningTask();

    /**
     * 判断某个下载任务是否还在运行
     *
     * @param downloadId 下载id
     */
    boolean isRunning(int downloadId);

    /**
     * 处理销毁对象操作
     */
    void destroy();
}
