package com.hesc.koala.internal.state;

import com.hesc.koala.KoalaDownloadStatus;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadRoughData;

/**
 * Created by hesc on 16/8/10.
 */
class KoalaPendingState extends KoalaState {

    public KoalaPendingState(IKoalaStateContext stateContext) {
        super(stateContext);
    }

    @Override
    public void execute(KoalaDownloadRoughData roughData) {
        if (roughData.getThrowable() != null) {
            //如果有异常，则跳转到error状态
            mStateContext.setState(new KoalaErrorState(mStateContext));
            mStateContext.execute(roughData);
        } else if (roughData.getActionKind() == KoalaDownloadRoughData.ActionKind.PAUSE) {
            //暂停
            mStateContext.setState(new KoalaPauseState(mStateContext));
            mStateContext.execute(roughData);
        } else {
            //设置开始下载时间
            if (roughData.getActionKind() == KoalaDownloadRoughData.ActionKind.START &&
                    mDownloadEntity.getTime() <= 0) {
                mDownloadEntity.setTime(roughData.getActionTime());
            }
            //修改状态
            mDownloadEntity.setStatus(KoalaDownloadStatus.PENDING);
            mDownloadListener.onPending(roughData.getId());

            mStateContext.setState(new KoalaConnectedState(mStateContext));
        }
    }
}
