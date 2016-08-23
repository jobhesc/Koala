package com.hesc.koala.internal.state;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.KoalaUtils;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadRoughData;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaPauseState extends KoalaState {

    public KoalaPauseState(IKoalaStateContext stateContext) {
        super(stateContext);
    }

    @Override
    public void execute(KoalaDownloadRoughData roughData) {
        if(roughData.getThrowable() != null){
            //如果有异常，则跳转到error状态
            mStateContext.setState(new KoalaErrorState(mStateContext));
            mStateContext.execute(roughData);
        } else if(roughData.getActionKind() == KoalaDownloadRoughData.ActionKind.RESUME){
            if(mDownloadEntity.isDownloadComplete()){
                throw new IllegalStateException(KoalaUtils.formatString(
                        "download[%d] is complete, so can't be resumed!", roughData.getId()));
            }

            //恢复下载
            mDownloadListener.onResume(roughData.getId());
            mStateContext.setState(new KoalaPendingState(mStateContext));
        } else if(mDownloadEntity.getStatus() != KoalaDownloadStatus.PAUSE) {
            mDownloadEntity.setStatus(KoalaDownloadStatus.PAUSE);
            mDownloadListener.onPause(roughData.getId());
        }
    }
}
