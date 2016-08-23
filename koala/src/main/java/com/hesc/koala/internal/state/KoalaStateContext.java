package com.hesc.koala.internal.state;

import android.os.Handler;
import android.os.Looper;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.intf.IKoalaCustomAction;
import com.hesc.koala.intf.IKoalaDownloadListener;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadEntity;
import com.hesc.koala.model.KoalaDownloadRoughData;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaStateContext implements IKoalaStateContext {
    private KoalaDownloadEntity mDownloadEntity;
    private KoalaState mState;
    private KoalaDownloadListenerChain mDownloadListener;
    private StateChangedListener mStateChangedListener;
    private List<IKoalaCustomAction> mCustomActionList = new ArrayList<>();

    public KoalaStateContext(KoalaDownloadEntity entity){
        mDownloadEntity = entity;
        mDownloadListener = new KoalaDownloadListenerChain();
        init();
    }

    public void setCustomActions(List<IKoalaCustomAction> actions) {
        if(actions == null) return;
        mCustomActionList = new ArrayList<>(actions);
    }

    private void init(){
        switch(mDownloadEntity.getStatus()){
            case PENDING:
                mState = new KoalaPendingState(this);
                break;
            case CONNECTED:
            case DOWNLOADING:
            case BLOCK_COMPLETE:
            case COMPLETE:
            case PAUSE:
                mState = new KoalaPauseState(this);  //其他状态都当暂停处理
                break;
            case ERROR:
                mState = new KoalaErrorState(this);
                break;
            default:
                mState = new KoalaPendingState(this);
                break;
        }
    }

    @Override
    public void setState(KoalaState state) {
        mState = state;

        if(mStateChangedListener != null){
            mStateChangedListener.onStateChanged(mDownloadEntity.getStatus());
        }

        if(mDownloadEntity.getStatus() != KoalaDownloadStatus.ERROR) {
            //执行自定义动作
            try {
                for (IKoalaCustomAction action : mCustomActionList) {
                    mDownloadListener.onBeforeAction(mDownloadEntity.getId());
                    Object result = action.execute(mDownloadEntity.snapshot());
                    mDownloadListener.onAfterAction(mDownloadEntity.getId(), result);
                }
            } catch (Exception e) {
                execute(new KoalaDownloadRoughData()
                        .setId(mDownloadEntity.getId())
                        .setThrowable(e));
            }
        }
    }

    @Override
    public KoalaDownloadEntity getDownloadEntity() {
        return mDownloadEntity;
    }

    @Override
    public IKoalaDownloadListener getDownloadListener() {
        return mDownloadListener;
    }

    @Override
    public void execute(KoalaDownloadRoughData roughData) {
        mState.execute(roughData);
    }

    public void setStateChangedListener(StateChangedListener listener){
        mStateChangedListener = listener;
    }

    public void addDownloadListener(IKoalaDownloadListener listener){
        if(listener == null) return;

        mDownloadListener.addListener(new KoalaMainThreadListener(listener));
        mDownloadListener.onBind(mDownloadEntity.getId());
    }

    public void removeDownloadListener(IKoalaDownloadListener listener){
        if(listener == null) return;
        mDownloadListener.removeListener(listener);
    }

    public void clearDownloadListeners(){
        mDownloadListener.clearListeners();
    }

    public interface StateChangedListener{
        void onStateChanged(KoalaDownloadStatus status);
    }

    private static class KoalaMainThreadListener implements  IKoalaDownloadListener{

        private IKoalaDownloadListener mDownloadListener;
        private Handler mHandler = new Handler(Looper.getMainLooper());

        public KoalaMainThreadListener(IKoalaDownloadListener downloadListener){
            if(downloadListener == null){
                throw new IllegalArgumentException("downloadListener is null! ");
            }
            mDownloadListener = downloadListener;
        }

        private void post(Runnable runnable){
            mHandler.post(runnable);
        }

        @Override
        public void onBind(final int downloadId) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onBind(downloadId);
                }
            });
        }

        @Override
        public void onPending(final int downloadId) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onPending(downloadId);
                }
            });
        }

        @Override
        public void onConnected(final int downloadId, final String url) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onConnected(downloadId, url);
                }
            });
        }

        @Override
        public void onProgress(final int downloadId, final String url, final long totalBytes,
                               final long downloadBytes, final long urlTotalBytes, final long urlDownloadBytes) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onProgress(downloadId, url, totalBytes, downloadBytes, urlTotalBytes, urlDownloadBytes);
                }
            });
        }

        @Override
        public void onBlockComplete(final int downloadId, final String url) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onBlockComplete(downloadId, url);
                }
            });
        }

        @Override
        public void onComplete(final int downloadId) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onComplete(downloadId);
                }
            });
        }

        @Override
        public void onError(final int downloadId, final Throwable e) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onError(downloadId, e);
                }
            });
        }

        @Override
        public void onResume(final int downloadId) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onResume(downloadId);
                }
            });
        }

        @Override
        public void onPause(final int downloadId) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onPause(downloadId);
                }
            });
        }

        @Override
        public void onBeforeAction(final int downloadId) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onBeforeAction(downloadId);
                }
            });
        }

        @Override
        public void onAfterAction(final int downloadId, final Object params) {
            post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.onAfterAction(downloadId, params);
                }
            });
        }

        @Override
        public boolean equals(Object o) {
            if(o == null) return false;
            if(o instanceof KoalaMainThreadListener){
                KoalaMainThreadListener listener = (KoalaMainThreadListener) o;
                return this.mDownloadListener.equals(listener.mDownloadListener);
            } else if(o instanceof IKoalaDownloadListener){
                return this.mDownloadListener.equals(o);
            } else {
                return super.equals(o);
            }
        }

        @Override
        public int hashCode() {
            return mDownloadListener.hashCode();
        }
    }

    private static class KoalaDownloadListenerChain implements IKoalaDownloadListener{
        private List<IKoalaDownloadListener> mListeners = new Vector<>();

        public void addListener(IKoalaDownloadListener listener){
            if(!mListeners.contains(listener))
                mListeners.add(listener);
        }

        public void removeListener(IKoalaDownloadListener listener){
            mListeners.remove(listener);
        }

        public void clearListeners(){
            mListeners.clear();
        }

        @Override
        public void onBind(int downloadId) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onBind(downloadId);
            }
        }

        @Override
        public void onPending(int downloadId) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onPending(downloadId);
            }
        }

        @Override
        public void onConnected(int downloadId, String url) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onConnected(downloadId, url);
            }
        }

        @Override
        public void onProgress(int downloadId, String url, long totalBytes, long downloadBytes,
                               long urlTotalBytes, long urlDownloadBytes) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onProgress(downloadId, url, totalBytes, downloadBytes, urlTotalBytes, urlDownloadBytes);
            }
        }

        @Override
        public void onBlockComplete(int downloadId, String url) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onBlockComplete(downloadId, url);
            }
        }

        @Override
        public void onComplete(int downloadId) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onComplete(downloadId);
            }
        }

        @Override
        public void onError(int downloadId, Throwable e) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onError(downloadId, e);
            }
        }

        @Override
        public void onResume(int downloadId) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onResume(downloadId);
            }
        }

        @Override
        public void onPause(int downloadId) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onPause(downloadId);
            }
        }

        @Override
        public void onBeforeAction(int downloadId) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onBeforeAction(downloadId);
            }
        }

        @Override
        public void onAfterAction(int downloadId, Object params) {
            for(IKoalaDownloadListener listener: mListeners){
                listener.onAfterAction(downloadId, params);
            }
        }
    }
}
