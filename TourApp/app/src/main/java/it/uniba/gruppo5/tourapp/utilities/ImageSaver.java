package it.uniba.gruppo5.tourapp.utilities;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public final class ImageSaver {

    private ImageSaver(){}

    public static File saveBitmapImage(Context context, Bitmap imageBitmap){
        String path  = context.getCacheDir() + "/" + UUID.randomUUID().toString();

        return saveToPath(imageBitmap, path);
    }

    public static File saveToPath(Bitmap imageBitmap, String path){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);
        byte[] bitmapdata = bos.toByteArray();

        File fileImage = new File(path);

        //compressione e salvataggio su file
        try (FileOutputStream out = new FileOutputStream(fileImage)) {

            out.write(bitmapdata);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileImage;
    }
}
