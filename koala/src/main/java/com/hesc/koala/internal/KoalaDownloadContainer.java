package com.hesc.koala.internal;

import android.support.annotation.NonNull;

import com.hesc.koala.KoalaConfig;
import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.internal.state.KoalaStateContext;
import com.hesc.koala.intf.IKoalaDownloadDashboard;
import com.hesc.koala.intf.IKoalaDownloadListener;
import com.hesc.koala.model.KoalaDownloadEntity;
import com.hesc.koala.model.KoalaDownloadModel;
import com.hesc.koala.model.KoalaDownloadProfile;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaDownloadContainer implements IKoalaDownloadDashboard {
    private Executor mTransmitExecutor;
    private Executor mModelExecutor;
    private Executor mDownloadExecutor;

    private KoalaConfig mConfig;
    private KoalaDownloadModel mDownloadModel;
    private KoalaDownloadRunnable mDownloadRunnable;
    private final Object mModelLoaded = new Object();

    public KoalaDownloadContainer(String key, KoalaConfig config){
        mConfig = config;
        mTransmitExecutor = new ExecutorWrapper(Executors.newSingleThreadExecutor());
        mModelExecutor = Executors.newSingleThreadExecutor();
        mDownloadExecutor = new ExecutorWrapper(mConfig.getDownloadExecutor());

        loadDownloadModel(key);
    }

    private  void waitModelLoaded(){
        synchronized (mModelLoaded){
            if(mDownloadModel == null){
                try {
                    mModelLoaded.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadDownloadModel(final String key){
        mModelExecutor.execute(new Runnable() {
            @Override
            public void run() {
                KoalaDownloadModel model = mConfig.getPersistence().load(key);
                if(model == null){
                    model = new KoalaDownloadModel(key);
                } else {
                    model = model.deepClone();
                }
                model.checkValid();
                synchronized (mModelLoaded) {
                    mDownloadModel = model;
                    mDownloadRunnable = new KoalaDownloadRunnable(mDownloadModel, mConfig, mStateChangedListener);

                    mModelLoaded.notifyAll();
                }
            }
        });
    }

    private void saveDownloadModel(){
        mModelExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mConfig.getPersistence().save(mDownloadModel.deepClone());
            }
        });
    }

    @Override
    public void start(final String url, final IKoalaDownloadListener listener) {
        start(new String[]{ url }, listener);
    }

    @Override
    public void start(final String[] urls, final IKoalaDownloadListener listener) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //先添加到队列中
                int downloadId = mDownloadRunnable.newDownload(urls, listener);
                startDownload(downloadId);
            }
        });

    }

    private void startDownload(final int downloadId){
        mDownloadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.start(downloadId);
            }
        });
    }

    @Override
    public void stop(final int downloadId) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.stop(downloadId);
            }
        });
    }

    @Override
    public void stopAll() {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.stopAll();
            }
        });
    }

    @Override
    public void pause(final int downloadId) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.pause(downloadId);
            }
        });
    }

    @Override
    public void pauseAll() {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.pauseAll();
            }
        });
    }

    @Override
    public void resume(final int downloadId) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.resume(downloadId);
                startDownload(downloadId);
            }
        });
    }

    @Override
    public void resumeAll() {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.resumeAll();

                mDownloadRunnable.forEach(new KoalaDownloadRunnable.Action() {
                    @Override
                    public void execute(final int downloadId) {
                        startDownload(downloadId);
                    }
                }, false);
            }
        });
    }

    @Override
    public void remove(final int downloadId) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.remove(downloadId);
            }
        });
    }

    @Override
    public void removeAll() {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.removeAll();
            }
        });
    }

    @Override
    public void bindAll(final IKoalaDownloadListener listener) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.bindAll(listener);
            }
        });
    }

    @Override
    public void bind(final int downloadId, final IKoalaDownloadListener listener) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.bind(downloadId, listener);
            }
        });
    }

    @Override
    public void unbind(final int downloadId, final IKoalaDownloadListener listener) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.unbind(downloadId, listener);
            }
        });
    }

    @Override
    public void unbindAll(final IKoalaDownloadListener listener) {
        mTransmitExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadRunnable.unbindAll(listener);
            }
        });
    }

    @Override
    public KoalaDownloadProfile getProfile(int downloadId) {
        if(mDownloadRunnable == null) return KoalaDownloadProfile.empty();
        return mDownloadRunnable.getProfile(downloadId);
    }

    @Override
    public boolean hasRunningTask() {
        return mDownloadRunnable != null && mDownloadRunnable.hasRunningTask();
    }

    @Override
    public boolean isRunning(int downloadId) {
        return mDownloadRunnable != null && mDownloadRunnable.isRunning(downloadId);
    }

    @Override
    public void destroy() {
        if(mDownloadRunnable != null){
            mDownloadRunnable.destroy();
        }
        //持久化数据
        saveDownloadModel();
    }

    private KoalaStateContext.StateChangedListener mStateChangedListener = new KoalaStateContext.StateChangedListener() {
        @Override
        public void onStateChanged(KoalaDownloadStatus status) {
            //持久化状态数据
            saveDownloadModel();
        }
    };

    private class ExecutorWrapper implements Executor {
        private Executor mExecutor;

        public ExecutorWrapper(Executor executor){
            mExecutor = executor;
        }

        @Override
        public void execute(@NonNull final Runnable command) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    waitModelLoaded();
                    command.run();
                }
            });
        }
    }
}
