package com.hesc.koala.internal.state;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.KoalaUtils;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadRoughData;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaErrorState extends KoalaState {

    public KoalaErrorState(IKoalaStateContext stateContext) {
        super(stateContext);
    }

    @Override
    public void execute(KoalaDownloadRoughData roughData) {
        if(roughData.getActionKind() == KoalaDownloadRoughData.ActionKind.RESUME){
            if(mDownloadEntity.isDownloadComplete()){
                throw new IllegalStateException(KoalaUtils.formatString(
                        "download[%d] is complete, so can't be resumed!", roughData.getId()));
            }
            mDownloadListener.onResume(roughData.getId());
            mStateContext.setState(new KoalaPendingState(mStateContext));
        } else {
            mDownloadEntity.setStatus(KoalaDownloadStatus.ERROR);
            mDownloadListener.onError(roughData.getId(), roughData.getThrowable());
        }
    }
}
