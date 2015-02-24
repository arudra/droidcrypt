package com.droidcrypt;

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


    public Mat2D (int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;
        vector = new ArrayList<>(rows*cols);
    }

    public Integer Read (int row, int col)
    {
        return vector.get(row*cols+col);
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
