package com.hesc.koala.intf;

import com.hesc.koala.model.KoalaDownloadModel;

/**
 * Created by hesc on 16/8/10.
 */
public interface IKoalaPersistence {
    KoalaDownloadModel load(String key);
    void save(KoalaDownloadModel model);
}
