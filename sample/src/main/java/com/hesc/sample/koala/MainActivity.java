package com.hesc.sample.koala;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hesc.koala.Koala;
import com.hesc.koala.KoalaConfig;
import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.impl.KoalaDownloader;
import com.hesc.koala.intf.IKoalaCustomAction;
import com.hesc.koala.intf.IKoalaDownloadListener;
import com.hesc.koala.intf.IKoalaDownloader;
import com.hesc.koala.model.KoalaDownloadProfile;
import com.hesc.koala.model.KoalaDownloadSpecialData;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Koala mKoala;
    private ProgressBar mProgressBar;
    private TextView mTextViewProgress;
    private ProgressBar mProgressBar1;
    private TextView mTextViewProgress1;

    private int mDownloadId1, mDownloadId2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mTextViewProgress = (TextView) findViewById(R.id.txtProgress);

        mProgressBar1 = (ProgressBar) findViewById(R.id.progress1);
        mTextViewProgress1 = (TextView) findViewById(R.id.txtProgress1);

        mProgressBar.setProgress(0);
        mProgressBar1.setProgress(0);
        mKoala = Koala.create(new KoalaConfig.Builder(this)
                .setDownloadExecutor(Executors.newSingleThreadExecutor())
                .addCustomAction(new IKoalaCustomAction() {
                    @Override
                    public Object execute(KoalaDownloadProfile profile) throws Exception {
                        if(profile.getStatus() == KoalaDownloadStatus.BLOCK_COMPLETE &&
                                profile.isDownloadComplete()){
                            return "解压中";
                        } else {
                            return "";
                        }
                    }
                }).build());

        mKoala.bindAll(mDownloadListener);
    }

    @Override
    protected void onDestroy() {
        if(mKoala != null)
            mKoala.destroy();
        super.onDestroy();
    }

    public void resumeOrPause(View view){
        if(mKoala.hasRunningTask())
            mKoala.pauseAll();
        else {
            mKoala.resumeAll();
        }
    }

    public void start(View view) throws IOException {
        mProgressBar.setProgress(0);
        mTextViewProgress.setText("0%");
        mKoala.start(new String[]{"http://124.163.204.78/apk.r1.market.hiapk.com/data/upload/apkres/2016/8_10/14/com.autonavi.minimap_022703.apk?wsiphost=local",
                "http://211.94.114.44/apk.r1.market.hiapk.com/data/upload/apkres/2016/8_5/13/com.baidu.BaiduMap_015402.apk?wsiphost=local"}, mDownloadListener);

        mKoala.start(new String[]{"http://124.163.204.78/apk.r1.market.hiapk.com/data/upload/apkres/2016/8_10/14/com.autonavi.minimap_022703.apk?wsiphost=local",
                "http://211.94.114.44/apk.r1.market.hiapk.com/data/upload/apkres/2016/8_5/13/com.baidu.BaiduMap_015402.apk?wsiphost=local"}, mDownloadListener);

    }

    public void remove(View view){
        mKoala.removeAll();
        mTextViewProgress.setText("0%");
        mProgressBar.setMax(0);
        mProgressBar.setProgress(0);

        mTextViewProgress1.setText("0%");
        mProgressBar1.setMax(0);
        mProgressBar1.setProgress(0);
    }

    private IKoalaDownloadListener mDownloadListener = new IKoalaDownloadListener() {
        @Override
        public void onBind(int downloadId) {
            if(mDownloadId1<=0){
                mDownloadId1 = downloadId;
            }

            Log.i("kkk", "onBind");
            KoalaDownloadProfile profile = mKoala.getProfile(downloadId);

            long totalBytes = profile.getTotalBytes();
            long downloadBytes = profile.getDownloadBytes();

            KoalaDownloadProfile.Detail detail = profile.getDownloadingDetail();
            if(detail == null){
                KoalaDownloadProfile.Detail[] details = profile.getDetails();
                if(details != null && details.length>0){
                    detail = details[0];
                }
            }

            File file = new File(URI.create(detail.getUrl()).getPath());

            if(mDownloadId1 == downloadId) {

                mTextViewProgress.setText((int) (downloadBytes * 1.0 / totalBytes * 100) + "%  " + file.getName());
                mProgressBar.setMax((int) totalBytes);
                mProgressBar.setProgress((int) downloadBytes);
            } else {
                mTextViewProgress1.setText((int) (downloadBytes * 1.0 / totalBytes * 100) + "%  " + file.getName());
                mProgressBar1.setMax((int) totalBytes);
                mProgressBar1.setProgress((int) downloadBytes);
            }
        }

        @Override
        public void onPending(int downloadId) {
            Log.i("kkk", "onPending");
        }

        @Override
        public void onConnected(int downloadId, String url) {
            Log.i("kkk", "onConnected");
        }

        @Override
        public void onProgress(int downloadId, String url, long totalBytes, long downloadBytes, long urlTotalBytes, long urlDownloadBytes) {
//            Log.i("kkk", "onProgress");
            File file = new File(URI.create(url).getPath());
            if(mDownloadId1 == downloadId) {
                mTextViewProgress.setText((int) (downloadBytes * 1.0 / totalBytes * 100) + "%  " + file.getName());
                mProgressBar.setMax((int) totalBytes);
                mProgressBar.setProgress((int) downloadBytes);
            } else {
                mTextViewProgress1.setText((int) (downloadBytes * 1.0 / totalBytes * 100) + "%  " + file.getName());
                mProgressBar1.setMax((int) totalBytes);
                mProgressBar1.setProgress((int) downloadBytes);
            }
        }

        @Override
        public void onBlockComplete(int downloadId, String url) {
            Log.i("kkk", "onBlockComplete");
        }

        @Override
        public void onComplete(int downloadId) {
            Log.i("kkk", "onComplete");
        }

        @Override
        public void onError(int downloadId, Throwable e) {
            Log.i("kkk", "onError");
        }

        @Override
        public void onResume(int downloadId) {
            Log.i("kkk", "onResume");
        }

        @Override
        public void onPause(int downloadId) {
            Log.i("kkk", "onPause");
        }

        @Override
        public void onBeforeAction(int downloadId) {
            Log.i("kkk", "onBeforeAction");
        }

        @Override
        public void onAfterAction(int downloadId, Object params) {
            Log.i("kkk", "onAfterAction: " + params.toString());
        }
    };
}
