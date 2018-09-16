package com.orbigo.helpers;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CacheImage {
    ImageView imageView;
    String tag;
    Context context;

    public CacheImage(Context context, ImageView imageView, String tag) {
        this.imageView = imageView;
        this.tag = tag;
        this.context = context;
    }


    public Bitmap getCaсhedImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        String path = context.getCacheDir() + "/orbigo/" + tag + ".png";
        File f = new File(path);
        if (!f.exists()) return null;
        return BitmapFactory.decodeFile(path, options);
    }

    public void saveCaсhedImage(byte[] b) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);


        File dir = new File(context.getCacheDir() + "/orbigo");
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, tag + ".png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 20, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeCaсhedImage() {
        String path = context.getCacheDir() + "/orbigo/" + tag + ".png";
        File f = new File(path);
        f.delete();
    }


    public void removeCache() {
        File dir = new File(context.getCacheDir() + "/orbigo");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                new File(dir, aChildren).delete();
            }
        }
    }


    public static void saveCacheFile(Context context, String fileName, String text) {
        File dir = new File(context.getCacheDir() + "");
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);

        try {
            FileWriter fw = new FileWriter(file);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readCacheFile(Context context, String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(context.getCacheDir()+"/"+fileName));
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[10];
            while (reader.read(buffer) != -1) {
                stringBuilder.append(new String(buffer));
                buffer = new char[10];
            }
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }


}
