package com.example.android.popularmovies.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Aaron Helton on 2/15/2016
 */
public final class FileUtils
{
    private static final String LOG_TAG = FileUtils.class.getSimpleName();

    /**
     * Stores an image in the internal cache directory.
     * @param context the context for storing the file
     * @param image the image to be stored
     * @param fileName the name of the file to be stored
     * @return the filepath for the stored image. Null if the save was unsuccessful.
     */
    public static String storeImage(Context context, Bitmap image, String fileName)
    {
        //Sanity checks
        if(image == null || fileName == null || fileName.isEmpty() || context == null)
            return null;

        File pictureFile;
        String extension = getExtension(fileName);
        if(extension == null) {
            extension = "png";
            pictureFile = new File(context.getFilesDir(), fileName + "." + extension);
        }
        else {
            pictureFile = new File(context.getFilesDir(), fileName);
        }

        Bitmap.CompressFormat format;
        switch(extension.toLowerCase()) {
            case "png":
                format = Bitmap.CompressFormat.PNG; break;
            case "jpg":case "jpeg":
                format = Bitmap.CompressFormat.JPEG; break;
            case "webp":
                format = Bitmap.CompressFormat.WEBP; break;
            default:
                throw new UnsupportedOperationException("Unsupported Format: "
                                                    + extension.toUpperCase());
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(format, 0, fos);
            fos.close();
            return pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
            return null;
        }
    }

    public static Bitmap getImage(String file)
    {
        return BitmapFactory.decodeFile(file);
    }

    public static String getExtension(String fileName)
    {
        int dotIndex = fileName.lastIndexOf(".");
        if(dotIndex == -1) {
            return null;
        } else {
            return fileName.substring(dotIndex+1);
        }
    }
}
