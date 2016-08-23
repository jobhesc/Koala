package com.hesc.koala.internal.state;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.KoalaUtils;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadRoughData;
import com.hesc.koala.model.KoalaDownloadSpecialData;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaDownloadingState extends KoalaState {

    public KoalaDownloadingState(IKoalaStateContext stateContext) {
        super(stateContext);
    }

    @Override
    public void execute(KoalaDownloadRoughData roughData) {
        if(roughData.getThrowable() != null){
            //如果有异常，则跳转到error状态
            mStateContext.setState(new KoalaErrorState(mStateContext));
            mStateContext.execute(roughData);
        } else if(roughData.getActionKind() == KoalaDownloadRoughData.ActionKind.PAUSE){
            //暂停
            mStateContext.setState(new KoalaPauseState(mStateContext));
            mStateContext.execute(roughData);
        } else {
            if(mDownloadEntity.getStatus() != KoalaDownloadStatus.CONNECTED &&
                    mDownloadEntity.getStatus() != KoalaDownloadStatus.DOWNLOADING){
                throw new IllegalStateException(KoalaUtils.formatString(
                        "download[%d] state error: downloading state only from " +
                        "connected state or downloading state!",roughData.getId()));
            }

            KoalaDownloadSpecialData currentDownloadData = mDownloadEntity.findSpecialData(roughData.getUrl());
            if(currentDownloadData == null){
                throw new IllegalArgumentException(KoalaUtils.formatString(
                        "can't not find the special data with url is %s in download task!", roughData.getUrl()));
            }

            currentDownloadData.setTotalBytes(roughData.getTotalBytes());
            currentDownloadData.setDownloadBytes(roughData.getDownloadBytes());
            if (!currentDownloadData.isDownloadComplete()) {
                //更新进度
                mDownloadEntity.setStatus(KoalaDownloadStatus.DOWNLOADING);

                mDownloadListener.onProgress(roughData.getId(),
                        roughData.getUrl(),
                        mDownloadEntity.getTotalBytes(),
                        mDownloadEntity.getDownloadBytes(),
                        currentDownloadData.getTotalBytes(),
                        currentDownloadData.getDownloadBytes());
            } else {
                //再次调用进度更新，否则进度就停在99%了
                mDownloadListener.onProgress(roughData.getId(),
                        roughData.getUrl(),
                        mDownloadEntity.getTotalBytes(),
                        mDownloadEntity.getDownloadBytes(),
                        currentDownloadData.getTotalBytes(),
                        currentDownloadData.getDownloadBytes());

                mStateContext.setState(new KoalaBlockCompleteState(mStateContext));
            }
        }
    }
}
