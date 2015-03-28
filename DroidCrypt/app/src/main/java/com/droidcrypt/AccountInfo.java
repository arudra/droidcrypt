package com.droidcrypt;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;

/**
 * Created by Abhishek on 3/15/2015.
 */
public class AccountInfo
{
    private Bitmap bitmap;
    private String name;
    private String password;
    private int[] HugoBits;
    private String FilePath;
    private String accountType;

    private static AccountInfo ourInstance = new AccountInfo();

    public static AccountInfo getInstance() { return ourInstance; }

    private AccountInfo() {
        bitmap = null;
        name = null;
        password = null;
        HugoBits = null;
        FilePath = null;
        accountType = null;
    }

    public void setBitmap (Bitmap bmp)
    {
        if (bmp != null)
        {
            bitmap = bmp;
            Log.d("GLOBAL", "Setting Bitmap");
        }
        else
            Log.d("GLOBAL", "Bitmap NULL!");
    }

    public Bitmap getBitmap () { Log.d("GLOBAL", "Getting Bitmap"); return bitmap; }

    public void setName (String name) { this.name = name;}

    public String getName () { return name; }

    public void setPassword (String password) { this.password = password; }

    public String getPassword () { return password; }

    public void setHugoBits (int[] bits)
    {
        HugoBits = bits;
        Log.d("GLOBAL", "Setting num bits");
    }

    public int[] getHugoBits () { return HugoBits; }

    public void setFilePath (String filepath) { FilePath = filepath; Log.d("GLOBAL", "Setting file path: " + filepath); }

    public String getFilePath () { Log.d("GLOBAL", "Return file path: " + FilePath); return FilePath; }


    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }


}
