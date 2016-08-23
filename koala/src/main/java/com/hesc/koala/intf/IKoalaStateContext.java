package com.hesc.koala.intf;

import com.hesc.koala.internal.state.KoalaState;
import com.hesc.koala.model.KoalaDownloadEntity;
import com.hesc.koala.model.KoalaDownloadRoughData;

/**
 * Created by hesc on 16/8/10.
 */
public interface IKoalaStateContext {
    void setState(KoalaState state);
    KoalaDownloadEntity getDownloadEntity();
    IKoalaDownloadListener getDownloadListener();
    void execute(KoalaDownloadRoughData roughData);
}
