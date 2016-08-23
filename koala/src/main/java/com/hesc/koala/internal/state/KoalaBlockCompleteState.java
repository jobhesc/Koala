package com.hesc.koala.internal.state;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.KoalaUtils;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadRoughData;
import com.hesc.koala.model.KoalaDownloadSpecialData;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaBlockCompleteState extends KoalaState {

    public KoalaBlockCompleteState(IKoalaStateContext stateContext) {
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
            if(mDownloadEntity.getStatus() != KoalaDownloadStatus.DOWNLOADING){
                throw new IllegalStateException(KoalaUtils.formatString(
                        "download[%d] state error: block complete state only from downloading state!",roughData.getId()));
            }

            KoalaDownloadSpecialData currentDownloadData = mDownloadEntity.findSpecialData(roughData.getUrl());
            if(currentDownloadData == null){
                throw new IllegalArgumentException(KoalaUtils.formatString(
                        "can't not find the special data with url is %s in download task!", roughData.getUrl()));
            }

            if(currentDownloadData.getDownloadBytes() != currentDownloadData.getTotalBytes()){
                throw new IllegalArgumentException(KoalaUtils.formatString(
                        "download[%d] state error: url[%s] download bytes not equal total bytes!",
                        roughData.getId(), roughData.getUrl()));
            }

            mDownloadEntity.setStatus(KoalaDownloadStatus.BLOCK_COMPLETE);
            mDownloadListener.onBlockComplete(roughData.getId(), roughData.getUrl());

            if(mDownloadEntity.isDownloadComplete()){
                mStateContext.setState(new KoalaCompleteState(mStateContext));
                mStateContext.execute(roughData);
            } else {
                mStateContext.setState(new KoalaPendingState(mStateContext));
            }
        }
    }
}
