package com.droidcrypt;

import android.graphics.Bitmap;

/**
 * Created by Abhishek on 3/15/2015.
 */
public class AccountInfo
{
    private Bitmap bitmap;
    private String name;
    private String password;
    private byte[] HugoArray;
    private int[] HugoBits;

    private static AccountInfo ourInstance = new AccountInfo();

    public static AccountInfo getInstance() { return ourInstance; }

    private AccountInfo() {
    }

    public void setBitmap (Bitmap bmp) {bitmap = bmp;}

    public Bitmap getBitmap () { return bitmap; }

    public void setName (String name) { this.name = name;}

    public String getName () { return name; }

    public void setPassword (String password) { this.password = password; }

    public String getPassword () { return password; }

    public void setHugoArray (byte[] array)
    {
//        HugoArray = array;
//        //Copy Array after embedding
        HugoArray = new byte[array.length];
        System.arraycopy(array, 0, HugoArray, 0, array.length);
    }

    public byte[] getHugoArray () { return HugoArray; }

    public void setHugoBits (int[] bits)
    {
//        HugoBits = bits;
        HugoBits = new int[2];
        System.arraycopy(bits, 0, HugoBits, 0, 2);
    }

    public int[] getHugoBits () { return HugoBits; }

}
