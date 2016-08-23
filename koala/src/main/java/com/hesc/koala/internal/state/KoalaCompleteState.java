package com.hesc.koala.internal.state;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.KoalaUtils;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadRoughData;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaCompleteState extends KoalaState {

    public KoalaCompleteState(IKoalaStateContext stateContext) {
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
            if(mDownloadEntity.getStatus() != KoalaDownloadStatus.BLOCK_COMPLETE){
                throw new IllegalStateException(KoalaUtils.formatString(
                        "download[%d] state error: complete state only from block complete state!",roughData.getId()));
            }

            mDownloadEntity.setStatus(KoalaDownloadStatus.COMPLETE);
            mDownloadListener.onComplete(roughData.getId());
        }
    }
}
