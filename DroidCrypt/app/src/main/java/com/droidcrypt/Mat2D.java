package com.droidcrypt;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;

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
    private Bitmap image;


    public Mat2D (int rows, int cols, Bitmap input)
    {
        this.rows = rows;
        this.cols = cols;
        vector = new ArrayList<Integer>(rows*cols);
        image = input;
    }

    public Integer Read (int row, int col)
    {
        return image.getPixel(row, col);
    }

    public void Write (int row, int col, Integer val)
    {
        vector.set(row*cols+col, val);
    }

    public void PermuteElements()
    {
        Collections.shuffle(vector);
    }

}
