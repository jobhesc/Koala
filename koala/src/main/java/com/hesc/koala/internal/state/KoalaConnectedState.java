package com.hesc.koala.internal.state;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.KoalaUtils;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadRoughData;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaConnectedState extends KoalaState {

    public KoalaConnectedState(IKoalaStateContext stateContext) {
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
            if(mDownloadEntity.getStatus() != KoalaDownloadStatus.PENDING){
                throw new IllegalStateException(KoalaUtils.formatString(
                        "download[%d] state error: connected state only from pending state!",roughData.getId()));
            }

            //已连接
            mDownloadEntity.setStatus(KoalaDownloadStatus.CONNECTED);
            mDownloadListener.onConnected(roughData.getId(), roughData.getUrl());

            mStateContext.setState(new KoalaDownloadingState(mStateContext));
        }
    }
}
