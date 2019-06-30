package com.example.criminalintent.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class PictureUtils
{
    public static Bitmap getScaledBitmap(String path, int destWidt, int desHeight)
    {
        //create a bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        //allow bitmap to be decoded
        options.inJustDecodeBounds = true;
        //decode the file sending in the path and bitmap object
        BitmapFactory.decodeFile(path, options);
        //get the width and height of the bitmap
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        //create the bitmap sample size
        int inSampleSize = 1;
        if (srcHeight > desHeight || srcWidth > destWidt)
        {
            float heightScale = srcHeight / desHeight;
            float widthScale = srcWidth / destWidt;
            //round the scale if the height is greater than the width go with it else go with the width scale
            inSampleSize = Math.round(heightScale > widthScale ? heightScale : widthScale);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        //return a bitmap with the correct sample / scale size
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity)
    {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }
}
