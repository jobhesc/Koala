package com.hesc.koala;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.net.URI;
import java.util.Locale;

/**
 * Created by hesc on 16/8/16.
 */
public class KoalaUtils {

    public static long getFileSize(String filePath){
        File file = new File(filePath);
        return file.exists()?file.length():0;
    }

    public static long getFreeSpaceBytes(final String path) {
        long freeSpaceBytes;
        final StatFs statFs = new StatFs(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            freeSpaceBytes = statFs.getAvailableBytes();
        } else {
            //noinspection deprecation
            freeSpaceBytes = statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        }

        return freeSpaceBytes;
    }

    public static String formatString(String format, Object... args){
        return String.format(Locale.ENGLISH, format, args);
    }

    public static String getFileExt(String url){
        try {
            URI uri = URI.create(url);
            File file = new File(uri.getPath());
            String fileName = file.getName();
            String[] strings = fileName.split("\\.");
            return strings.length > 0 ? "." + strings[strings.length - 1] : "";
        } catch (Throwable e){
            e.printStackTrace();
            return "";
        }
    }

    public static String getCacheFilePath(Context context){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File cacheFile = context.getExternalFilesDir(null);
            if(cacheFile != null) {  //getExternalFilesDir(null)居然会返回null,这应该是由于外存储设备被锁了缘故
                return cacheFile.getAbsolutePath();
            } else {
                return context.getFilesDir().getAbsolutePath();
            }
        } else {
            return context.getFilesDir().getAbsolutePath();
        }
    }
}
