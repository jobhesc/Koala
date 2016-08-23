package com.hesc.koala.model;

import com.hesc.koala.intf.IKoalaDownloadLocalPathBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaDownloadModel extends Vector<KoalaDownloadEntity> {

    private String mKey;

    public KoalaDownloadModel(String key) {
        mKey = key;
    }

    public KoalaDownloadModel(String key, Collection<? extends KoalaDownloadEntity> collection) {
        super(collection);
        mKey = key;
    }

    public String getKey(){
        return mKey;
    }

    public synchronized KoalaDownloadModel deepClone(){
        int size = size();
        List<KoalaDownloadEntity> entities = new Vector<>(size);
        for(int i=0; i<size; i++){
            entities.add(get(i).clone());
        }
        return new KoalaDownloadModel(getKey(), entities);
    }

    public KoalaDownloadEntity findById(int downloadId){
        for(KoalaDownloadEntity entity: this){
            if(entity.getId() == downloadId)
                return entity;
        }
        return null;
    }

    public synchronized KoalaDownloadEntity addNewEntity(String[] urls, String localRootPath,
                                                         IKoalaDownloadLocalPathBuilder localPathBuilder){
        KoalaDownloadEntity entity = KoalaDownloadEntity.create(this.newDownloadId(), urls,
                localRootPath, localPathBuilder);
        this.add(entity);
        return entity;
    }

    public void checkValid(){
        for(KoalaDownloadEntity entity: this){
            entity.checkValid();
        }
    }

    public int newDownloadId(){
        int downloadId = 0;
        for(KoalaDownloadEntity entity: this){
            if(downloadId<entity.getId())
                downloadId = entity.getId();
        }
        return downloadId+1;
    }

}
