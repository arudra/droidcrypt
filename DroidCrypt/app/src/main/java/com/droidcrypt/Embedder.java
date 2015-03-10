package com.droidcrypt;

//import android.graphics.Bitmap;

/**
 *
 *  This is an NDK interface class to embedded the password in an image
 *
 * Created by dkmiyani on 15-03-04.
 */
public class Embedder {

    static {
        System.loadLibrary("embedder");
    }

    /* NDK interface function, implemented by libembeder.so */
    public static native byte[] embed(byte[] bitmap, int width, int height, String password);

}
