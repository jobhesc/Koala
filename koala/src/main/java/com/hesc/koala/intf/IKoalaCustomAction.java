package com.hesc.koala.intf;

import com.hesc.koala.model.KoalaDownloadProfile;

/**
 * Created by hesc on 16/8/10.
 */
public interface IKoalaCustomAction {
    Object execute(KoalaDownloadProfile profile) throws Exception;
}
