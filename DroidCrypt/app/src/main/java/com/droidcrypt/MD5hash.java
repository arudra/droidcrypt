package com.droidcrypt;

import android.graphics.Bitmap;

import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Created by Abhishek on 2/23/2015.
 */
public class MD5hash {

    public String generateHash (String image)
    {
        StringBuffer hexString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(image);

            byte[] dataBytes = new byte[1024];

            int read = 0;
            while ((read = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, read);
            }

            byte[] hashBytes = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for(byte i : hashBytes) {
            //for (int i = 0; i < hashBytes.length; i++) {
                sb.append(Integer.toString((i & 0xff) + 0x100, 16).substring(1));
            }

            //convert the byte to hex format method 2
            hexString = new StringBuffer();
            for (byte j : hashBytes) {
            //for (int i = 0; i < hashBytes.length; i++) {
                String hex = Integer.toHexString(0xff & j);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
        } catch (Exception e) { e.printStackTrace(); }

        return hexString.toString();
    }
}
