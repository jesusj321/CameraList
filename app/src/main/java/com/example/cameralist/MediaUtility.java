package com.example.cameralist;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JesusManuel on 07/03/16.
 */
public class MediaUtility {

    public static final int REQUEST_CAPTURE_IMAGE = 100;

    public static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static boolean isExternalStorageAvailable() {
        return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
    }

    public static File getMediaStorageDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }

    public static File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        return File.createTempFile(
                imageFileName,
                ".jpg",
                getMediaStorageDir()
        );
    }

    public static void decodeBitmapAndCompress(String currentPhotoPath, DecodeBitmapAndCompressAsyncTask decodeBitmapAndCompressAsyncTask) {
        decodeBitmapAndCompressAsyncTask.execute(currentPhotoPath);
    }

    public static abstract class DecodeBitmapAndCompressAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private int quality;
        private int reqWidth;
        private int reqHeight;
        private String errorMessage = "";

        public DecodeBitmapAndCompressAsyncTask(int quality, int reqWidth, int reqHeight) {
            this.quality = quality;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return decodeBitmapAndCompress(params[0], quality, reqWidth, reqHeight);
            } catch (IOException e) {
                //e.printStackTrace();
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap == null) {
                onBitmapError(errorMessage);
            } else {
                onBitmapDecoded(bitmap);
            }
        }

        public abstract void onBitmapDecoded(Bitmap result);

        public abstract void onBitmapError(String errorMessage);
    }

    public static Bitmap decodeBitmapAndCompress(String currentPhotoPath, int quality, int reqWidth, int reqHeight) throws IOException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = addWaterMark(new Date().toString(), BitmapFactory.decodeFile(currentPhotoPath, options));
        FileOutputStream fileOutputStream = new FileOutputStream(currentPhotoPath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        return bitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap addWaterMark(String time, Bitmap dest) {

        Bitmap result = Bitmap.createBitmap(dest.getWidth(), dest.getHeight(), dest.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(dest, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(18); //size

        canvas.drawText(time, 18, 18, paint);

        return result;
    }
}
