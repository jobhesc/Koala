package com.hesc.koala.internal;

import android.util.SparseArray;

import com.hesc.koala.KoalaConfig;
import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.KoalaUtils;
import com.hesc.koala.exception.KoalaDownloadInvalidException;
import com.hesc.koala.exception.KoalaDownloadOutOfSpaceException;
import com.hesc.koala.exception.KoalaDownloadSizeException;
import com.hesc.koala.internal.state.KoalaStateContext;
import com.hesc.koala.intf.IKoalaDownloadListener;
import com.hesc.koala.intf.IKoalaDownloader;
import com.hesc.koala.model.KoalaDownloadEntity;
import com.hesc.koala.model.KoalaDownloadModel;
import com.hesc.koala.model.KoalaDownloadProfile;
import com.hesc.koala.model.KoalaDownloadRoughData;
import com.hesc.koala.model.KoalaDownloadSpecialData;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaDownloadRunnable {
    private KoalaConfig mConfig;
    private KoalaDownloadModel mDownloadModel;

    private KoalaDownloadHolderList mDownloadHolders = new KoalaDownloadHolderList();
    private KoalaStateContext.StateChangedListener mStateChangedListener;

    public KoalaDownloadRunnable(KoalaDownloadModel model, KoalaConfig config,
                                 KoalaStateContext.StateChangedListener stateChangedListener){
        mConfig = config;
        mDownloadModel = model;
        mStateChangedListener = stateChangedListener;
        buildHolders();
    }

    private void buildHolders(){
        mDownloadHolders.clear();
        for(KoalaDownloadEntity entity: mDownloadModel){
            createDownloadHolder(entity, null);
        }
    }

    public int newDownload(String[] urls, IKoalaDownloadListener listener){
        KoalaDownloadEntity entity = mDownloadModel.addNewEntity(urls,
                mConfig.getDownloadLocalRootPath(),
                mConfig.getLocalPathBuilder());
        createDownloadHolder(entity, listener);
        return entity.getId();
    }

    public boolean start(final int downloadId) {
        //确保下载根目录存在
        ensureDownloadLocalRootPath();
        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if(holder == null) return false;
        if(holder.IsPause) return false;

        KoalaDownloadEntity entity = holder.StateContext.getDownloadEntity();
        if(entity.isDownloadComplete()) return false;

        //初始状态就是pending
        holder.StateContext.execute(new KoalaDownloadRoughData()
                .setActionKind(KoalaDownloadRoughData.ActionKind.START)
                .setActionTime(new Date().getTime())
                .setId(downloadId));

        return execDownload(holder);
    }

    private boolean execDownload(KoalaDownloadHolder holder){
        KoalaDownloadEntity entity = holder.StateContext.getDownloadEntity();
        if(entity.getStatus() != KoalaDownloadStatus.PENDING) return false;

        try {
            preStartDownload(holder.Downloader, entity);

            for (KoalaDownloadSpecialData specialData : entity.getSpecialDatas()) {
                if (specialData.isDownloadComplete()) continue;

                holder.Downloader.setDownloadData(entity.getId(),
                        specialData.getUrl(), specialData.getLocalPath());
                holder.Downloader.run(mDownloaderCallback);
                if (holder.Downloader.isCanceled()) break;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            holder.StateContext.execute(new KoalaDownloadRoughData()
                    .setId(entity.getId()).setThrowable(e));
            return false;
        }
    }

    private void preStartDownload(IKoalaDownloader downloader, KoalaDownloadEntity entity)
            throws KoalaDownloadInvalidException,
            KoalaDownloadSizeException,
            KoalaDownloadOutOfSpaceException {

        for(KoalaDownloadSpecialData specialData: entity.getSpecialDatas()){
            //校验本地文件大小是否与已下载大小相同，不同则抛出异常
            checkLocalFileValid(specialData);
            //重新获取下载文件大小
            downloader.setDownloadData(entity.getId(), specialData.getUrl(), specialData.getLocalPath());
            long totalBytes = downloader.getTotalBytes();

            long downloadBytes = specialData.getDownloadBytes();
            if(totalBytes<downloadBytes){
                throw new KoalaDownloadInvalidException(KoalaUtils.formatString(
                        "download bytes[%d] greater than total[%d] in the download file(%s)",
                        downloadBytes, totalBytes, specialData.getUrl()));
            }
            specialData.setTotalBytes(totalBytes);
        }
        //校验剩余空间大小
        checkOverAvailableSpace(entity);
    }

    private KoalaDownloadHolder createDownloadHolder(
        KoalaDownloadEntity entity, IKoalaDownloadListener listener){

        KoalaDownloadHolder holder = mDownloadHolders.get(entity.getId());
        if (holder == null) {
            holder = new KoalaDownloadHolder();
            holder.StateContext = new KoalaStateContext(entity);
            holder.StateContext.setStateChangedListener(mStateChangedListener);
            holder.StateContext.setCustomActions(mConfig.getCustomActionList());
            //下载器
            holder.Downloader = createDownloader();
            mDownloadHolders.put(entity.getId(), holder);
        }

        holder.StateContext.removeDownloadListener(listener);
        holder.StateContext.addDownloadListener(listener);
        return holder;
    }

    private void checkOverAvailableSpace(KoalaDownloadEntity entity)
            throws KoalaDownloadOutOfSpaceException{

    }

    private void checkLocalFileValid(KoalaDownloadSpecialData specialData)
            throws KoalaDownloadInvalidException{

        long localFileSize = KoalaUtils.getFileSize(specialData.getLocalPath());
        long downloadBytes = specialData.getDownloadBytes();

        if(localFileSize != downloadBytes){
            throw new KoalaDownloadInvalidException(KoalaUtils.formatString(
                    "download bytes[%d] is not equal local file(%s)'s length[%d]",
                    downloadBytes, specialData.getLocalPath(), localFileSize));
        }
    }

    private IKoalaDownloader createDownloader(){
        IKoalaDownloader downloader = mConfig.getDownloaderFactory().create();
        if(downloader == null){
            throw new IllegalArgumentException("the downloader is null! please check the result of " +
                    "the create method in IKoalaDownloaderFactory interface");
        }

        IKoalaDownloader.Config downloaderConfig = new IKoalaDownloader.Config();
        downloaderConfig.maxAutoRetryTimes = mConfig.getMaxAutoRetryTimes();
        downloaderConfig.bufferSize = mConfig.getBufferSize();
        downloaderConfig.connectTimeoutMillis = mConfig.getConnectTimeoutMillis();
        downloaderConfig.readTimeoutMillis = mConfig.getReadTimeoutMillis();
        downloader.setConfig(downloaderConfig);
        return downloader;
    }

    private void ensureDownloadLocalRootPath(){
        File file = new File(mConfig.getDownloadLocalRootPath());
        file.mkdirs();
    }

    public boolean stop(final int downloadId) {
        //先暂停
        if(!pause(downloadId)) return false;

        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if(holder == null) return false;

        KoalaDownloadEntity entity = holder.StateContext.getDownloadEntity();
        deleteFile(entity);
        mDownloadHolders.remove(downloadId);
        mDownloadModel.remove(entity);
        return true;
    }

    public void stopAll() {
        forEach(new Action() {
            @Override
            public void execute(int downloadId) {
                stop(downloadId);
            }
        }, true);
    }

    public boolean pause(final int downloadId) {
        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if (holder == null) return false;

        if(holder.Downloader != null && holder.Downloader.isRunning()) {
            holder.Downloader.cancel();
            holder.IsPause = true;
            return true;
        } else {
            //下载器处于排队状态，暂停下载
            KoalaDownloadEntity entity = holder.StateContext.getDownloadEntity();
            KoalaDownloadSpecialData specialData = entity.getDownloadingSpecialData();
            if(specialData == null) return false;

            holder.StateContext.execute(new KoalaDownloadRoughData()
                    .setId(downloadId)
                    .setUrl(specialData.getUrl())
                    .setActionKind(KoalaDownloadRoughData.ActionKind.PAUSE)
            );
            holder.IsPause = true;
            return true;
        }
    }

    public void pauseAll() {
        forEach(new Action() {
            @Override
            public void execute(int downloadId) {
                pause(downloadId);
            }
        }, true);
    }

    public boolean resume(final int downloadId) {
        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if (holder != null && holder.Downloader != null && !holder.Downloader.isRunning()) {
            KoalaDownloadEntity entity = holder.StateContext.getDownloadEntity();
            if(entity.isDownloadComplete()) return false;

            holder.StateContext.execute(new KoalaDownloadRoughData()
                    .setId(entity.getId())
                    .setActionKind(KoalaDownloadRoughData.ActionKind.RESUME));
            holder.IsPause = false;
            return true;
        } else {
            return false;
        }
    }

    public void resumeAll(){
        forEach(new Action() {
            @Override
            public void execute(int downloadId) {
                resume(downloadId);
            }
        }, false);
    }

    public boolean remove(final int downloadId) {
        pause(downloadId);

        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if (holder == null) return false;

        KoalaDownloadEntity entity = holder.StateContext.getDownloadEntity();
        deleteFile(entity);
        mDownloadHolders.remove(downloadId);
        mDownloadModel.remove(entity);
        return true;
    }

    public void removeAll() {
        forEach(new Action() {
            @Override
            public void execute(int downloadId) {
                remove(downloadId);
            }
        }, true);
    }

    public void bindAll(final IKoalaDownloadListener listener) {
        forEach(new Action() {
            @Override
            public void execute(int downloadId) {
                bind(downloadId, listener);
            }
        },false);
    }

    public void bind(int downloadId, IKoalaDownloadListener listener) {
        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if (holder != null) {
            holder.StateContext.addDownloadListener(listener);
        }
    }

    public void unbind(int downloadId, IKoalaDownloadListener listener) {
        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if (holder != null) {
            holder.StateContext.removeDownloadListener(listener);
        }
    }

    public void unbindAll(final IKoalaDownloadListener listener) {
        forEach(new Action() {
            @Override
            public void execute(int downloadId) {
                unbind(downloadId, listener);
            }
        }, false);
    }

    public KoalaDownloadProfile getProfile(int downloadId) {
        KoalaDownloadEntity entity = mDownloadModel.findById(downloadId);
        if(entity == null){
            return KoalaDownloadProfile.empty();
        } else {
            return entity.snapshot();
        }
    }

    public boolean hasRunningTask() {
        Iterator<KoalaDownloadHolder> iterator = mDownloadHolders.iterator(false);
        while (iterator.hasNext()){
            KoalaDownloadHolder holder = iterator.next();
            if(holder.Downloader.isRunning()) return true;
        }
        return false;
    }

    public void forEach(Action action, boolean reverse) {
        Iterator<KoalaDownloadHolder> iterator = mDownloadHolders.iterator(reverse);
        while (iterator.hasNext()){
            KoalaDownloadHolder holder = iterator.next();
            KoalaDownloadEntity entity = holder.StateContext.getDownloadEntity();
            action.execute(entity.getId());
        }
    }

    private void deleteFile(final KoalaDownloadEntity entity){
        for(KoalaDownloadSpecialData specialData: entity.getSpecialDatas()){
            File file = new File(specialData.getLocalPath());
            file.delete();
        }
    }

    public boolean isRunning(int downloadId) {
        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        return holder != null && holder.Downloader.isRunning();
    }

    public void destroy() {
        //暂停所有下载
        pauseAll();
    }

    private KoalaStateContext getStateContext(int downloadId){
        KoalaDownloadHolder holder = mDownloadHolders.get(downloadId);
        if(holder != null) return holder.StateContext;
        return null;
    }


    private IKoalaDownloader.Callback mDownloaderCallback = new IKoalaDownloader.Callback() {
        @Override
        public void onConnect(int downloadId, String url) {
            KoalaStateContext stateContext = getStateContext(downloadId);
            if(stateContext != null) {
                stateContext.execute(new KoalaDownloadRoughData()
                        .setId(downloadId)
                        .setUrl(url));
            }
        }

        @Override
        public void onProgress(int downloadId, String url, long totalBytes, long downloadBytes) {
            KoalaStateContext stateContext = getStateContext(downloadId);
            if(stateContext != null) {
                stateContext.execute(new KoalaDownloadRoughData()
                        .setId(downloadId)
                        .setUrl(url)
                        .setTotalBytes(totalBytes)
                        .setDownloadBytes(downloadBytes)
                );
            }
        }

        @Override
        public void onComplete(int downloadId, String url) {
            KoalaStateContext stateContext = getStateContext(downloadId);
            if(stateContext != null) {
                stateContext.execute(new KoalaDownloadRoughData()
                        .setId(downloadId)
                        .setUrl(url)
                );
            }
        }

        @Override
        public void onError(int downloadId, String url, Throwable e) {
            KoalaStateContext stateContext = getStateContext(downloadId);
            if(stateContext != null) {
                stateContext.execute(new KoalaDownloadRoughData()
                        .setId(downloadId)
                        .setUrl(url)
                        .setThrowable(e)
                );
            }
        }

        @Override
        public void onCancel(int downloadId, String url) {
            KoalaStateContext stateContext = getStateContext(downloadId);
            if(stateContext != null) {
                stateContext.execute(new KoalaDownloadRoughData()
                        .setId(downloadId)
                        .setUrl(url)
                        .setActionKind(KoalaDownloadRoughData.ActionKind.PAUSE)
                );
            }
        }
    };

    private static class KoalaDownloadHolder{
        public IKoalaDownloader Downloader;
        public KoalaStateContext StateContext;
        public boolean IsPause = false;
    }

    public interface Action{
        void execute(int downloadId);
    }

    private static class KoalaDownloadHolderList {

        private SparseArray<KoalaDownloadHolder> mSparseArray = new SparseArray<>();

        public synchronized KoalaDownloadHolder get(int key){
            return mSparseArray.get(key);
        }

        public synchronized void remove(int key){
            mSparseArray.remove(key);
        }

        public synchronized void clear(){
            mSparseArray.clear();
        }

        public synchronized void put(int key, KoalaDownloadHolder holder){
            mSparseArray.put(key, holder);
        }

        public synchronized Iterator<KoalaDownloadHolder> iterator(boolean reverse) {
            return new Itr(reverse);
        }

        public synchronized int size(){
            return mSparseArray.size();
        }

        private class Itr implements Iterator<KoalaDownloadHolder>{
            int cursor;
            int lastRet = -1;
            boolean reverse = false;

            public Itr(boolean reverse){
                this.reverse = reverse;
                cursor = reverse? size()-1: 0;
            }

            @Override
            public boolean hasNext() {
                return (reverse && cursor>=0) || (!reverse && cursor<size());
            }

            @Override
            public KoalaDownloadHolder next() {
                synchronized (KoalaDownloadHolderList.this){
                    int i = cursor;
                    if((reverse && i<0) || (!reverse && i>=size())){
                        throw new NoSuchElementException();
                    }
                    cursor = reverse? i-1 : i+1;
                    int key = mSparseArray.keyAt(lastRet = i);
                    return mSparseArray.get(key);
                }
            }

            @Override
            public void remove() {
                if (lastRet == -1)
                    throw new IllegalStateException();
                synchronized (KoalaDownloadHolderList.this) {
                    int key = mSparseArray.keyAt(lastRet);
                    mSparseArray.remove(key);
                }
                cursor = lastRet;
                lastRet = -1;
            }
        }
    }
}
