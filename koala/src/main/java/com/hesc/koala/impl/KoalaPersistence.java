package com.hesc.koala.impl;

import android.content.Context;

import com.hesc.koala.KoalaUtils;
import com.hesc.koala.intf.IKoalaPersistence;
import com.hesc.koala.model.KoalaDownloadModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by hesc on 16/8/10.
 */
public class KoalaPersistence implements IKoalaPersistence{
    private String mFilePath;

    public KoalaPersistence(Context context){
        String cacheFilePath = KoalaUtils.getCacheFilePath(context);
        mFilePath = cacheFilePath + File.separator + "Koala" + File.separator + "persistence" + File.separator;
    }

    @Override
    public KoalaDownloadModel load(String key) {
        File file = new File(mFilePath);
        if(!file.exists()){
            file.mkdirs();
        }
        file = new File(file, key + ".dat");
        if(!file.exists()) return null;

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(file));
            return (KoalaDownloadModel) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void save(KoalaDownloadModel model) {
        File file = new File(mFilePath);
        if(!file.exists()){
            file.mkdirs();
        }
        file = new File(file, model.getKey() + ".dat");

        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(model);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
