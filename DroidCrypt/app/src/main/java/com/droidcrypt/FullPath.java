package com.droidcrypt;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by Abhishek on 3/17/2015.
 */
public class FullPath
{

    public static String getPath_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);
        Log.d("FullPath","wholeID: " + wholeID);

        // Split at colon, use second item in the array
        String id;
        if(wholeID != null && wholeID.indexOf(':') > 0)
            id = wholeID.split(":")[1];
        else
            return wholeID;


        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getPath_API11(Context context, Uri contentUri) {
        String[] media = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, media, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }


}
