package com.droidcrypt;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.security.SecureRandom;

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
    private String masterPassword;

    private static AccountInfo ourInstance = new AccountInfo();

    public static AccountInfo getInstance() { return ourInstance; }
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static SecureRandom rnd = new SecureRandom();

    private AccountInfo() {
        bitmap = null;
        name = null;
        password = null;
        HugoBits = null;
        FilePath = null;
        accountType = null;
        masterPassword = null;
    }

    public void setMasterPassword (String password) { masterPassword = password; }

    public String getMasterPassword () { return masterPassword; }

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

    public String randomPassword( int len )
    {
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
