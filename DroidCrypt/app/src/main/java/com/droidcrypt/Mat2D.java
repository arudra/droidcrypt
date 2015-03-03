package com.droidcrypt;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Abhishek on 2/23/2015.
 *
 * This is generic class (std::vector<T>) inside original implementation
 * but I made it specific to integers since that's how it's used inside HUGO.java
 */
public class Mat2D
{
    public int rows;
    public int cols;
    private ArrayList<Integer> vector;
    public Bitmap orig_image;
    public byte[] image;

    public Mat2D (int rows, int cols, byte[] input)
    {
        this.rows = rows;
        this.cols = cols;
        vector = new ArrayList<Integer>(rows*cols);
        orig_image = null;
        image = input;
    }

    public Byte Read (int row, int col)
    {
        return image[row*col+col];
    }

    public void Write (int row, int col, Integer val)
    {
        orig_image.setPixel(row, col, val);
    }

//    public void PermuteElements()
//    {
//        Collections.shuffle(vector);
//    }

}
