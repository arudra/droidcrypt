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
    public byte[] image;

    public Mat2D (int rows, int cols, byte[] input)
    {
        this.rows = rows;
        this.cols = cols;
        //vector = new ArrayList<Integer>(rows*cols);
        if (input == null) {
            input = new byte[rows*cols];
        }
        else
            image = input;
    }

    public Byte Read (int row, int col)
    {
        return image[row*cols+col];
    }

    public void Write (int row, int col, byte val)
    {
        image[row*cols+col]= val;
    }

//    public void PermuteElements()
//    {
//        Collections.shuffle(vector);
//    }

    static Mat2D Padding_Mirror(Mat2D mat, int padSizeRows, int padSizeCols)
    {
        Mat2D result = new Mat2D(mat.rows + (2*padSizeRows), mat.cols + (2*padSizeCols), null);
        for (int row=0; row < result.rows; row++)
        {
            int rowOrig;
            if (row < padSizeRows)
                rowOrig = padSizeRows - row - 1;
            else if (row > mat.rows + padSizeRows - 1)
                rowOrig = 2*(mat.rows) + padSizeRows - 1 - row;
            else rowOrig = row - padSizeRows;

            for (int col=0; col < result.cols; col++)
            {
                int colOrig;
                if (col < padSizeCols)
                    colOrig = padSizeCols - col - 1;
                else if (col > mat.cols + padSizeCols - 1)
                    colOrig = 2*(mat.cols) + padSizeCols - 1 - col;
                else colOrig = col - padSizeCols;

                result.Write(row, col, mat.Read(rowOrig, colOrig));
            }
        }
        return result;
    }

}
