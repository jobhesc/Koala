package com.hesc.koala.internal.state;

import com.hesc.koala.intf.IKoalaDownloadListener;
import com.hesc.koala.intf.IKoalaStateContext;
import com.hesc.koala.model.KoalaDownloadEntity;
import com.hesc.koala.model.KoalaDownloadRoughData;

/**
 * Created by hesc on 16/8/10.
 */
public abstract class KoalaState {
    protected IKoalaStateContext mStateContext;
    protected KoalaDownloadEntity mDownloadEntity;
    protected IKoalaDownloadListener mDownloadListener;

    public KoalaState(IKoalaStateContext stateContext){
        mStateContext = stateContext;
        mDownloadEntity = mStateContext.getDownloadEntity();
        mDownloadListener = mStateContext.getDownloadListener();
    }

    public abstract void execute(KoalaDownloadRoughData roughData);
}
