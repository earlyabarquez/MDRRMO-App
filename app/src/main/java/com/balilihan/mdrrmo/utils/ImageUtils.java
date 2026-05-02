// utils/ImageUtils.java

package com.balilihan.mdrrmo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    private static final int    MAX_SIZE_BYTES   = 1024 * 1024; // 1MB
    private static final int    MAX_DIMENSION    = 1280;        // max width/height
    private static final int    JPEG_QUALITY     = 85;          // 85% quality

    // Compresses the image at the given path to max 1MB
    // Returns the path of the compressed file
    public static String compressImage(Context context, String imagePath)
            throws IOException {

        // Decode image dimensions without loading full bitmap into memory
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate sample size to reduce memory usage
        options.inSampleSize    = calculateInSampleSize(options);
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        // Fix rotation — Android cameras often save images rotated
        bitmap = fixRotation(bitmap, imagePath);

        // Scale down if still too large
        bitmap = scaleBitmap(bitmap);

        // Save compressed image to app's cache directory
        File outputDir  = context.getCacheDir();
        File outputFile = File.createTempFile("report_", ".jpg", outputDir);

        // Compress and write — reduce quality until under 1MB
        int quality = JPEG_QUALITY;
        FileOutputStream out = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        out.flush();
        out.close();

        // If still over 1MB, reduce quality further
        while (outputFile.length() > MAX_SIZE_BYTES && quality > 50) {
            quality -= 10;
            out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
        }

        bitmap.recycle();
        return outputFile.getAbsolutePath();
    }

    private static int calculateInSampleSize(BitmapFactory.Options options) {
        int height    = options.outHeight;
        int width     = options.outWidth;
        int inSample  = 1;

        if (height > MAX_DIMENSION || width > MAX_DIMENSION) {
            int halfH = height / 2;
            int halfW = width  / 2;
            while ((halfH / inSample) >= MAX_DIMENSION
                    && (halfW / inSample) >= MAX_DIMENSION) {
                inSample *= 2;
            }
        }
        return inSample;
    }

    private static Bitmap scaleBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        if (w <= MAX_DIMENSION && h <= MAX_DIMENSION) return bitmap;

        float ratio = Math.min(
                (float) MAX_DIMENSION / w,
                (float) MAX_DIMENSION / h
        );
        return Bitmap.createScaledBitmap(
                bitmap,
                Math.round(w * ratio),
                Math.round(h * ratio),
                true
        );
    }

    private static Bitmap fixRotation(Bitmap bitmap, String path)
            throws IOException {
        // Read EXIF rotation data from the image file
        ExifInterface exif    = new ExifInterface(path);
        int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        );

        int rotation = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:  rotation = 90;  break;
            case ExifInterface.ORIENTATION_ROTATE_180: rotation = 180; break;
            case ExifInterface.ORIENTATION_ROTATE_270: rotation = 270; break;
        }

        if (rotation == 0) return bitmap;

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix, true
        );
    }
}