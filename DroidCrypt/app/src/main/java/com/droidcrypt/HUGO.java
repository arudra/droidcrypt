package com.droidcrypt;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;


public class HUGO
{
  
    //INPUTS: input orig_image path, Password
    private String imagePath, password;

    // output orig_image path which holds hash of JPEG
    private String outputImage;

    private Bitmap image;
    private Context context;
    private Embedder e;

    public HUGO (String input, String pass, Bitmap inputImg, Context context)
    {
        imagePath = input;
        password = pass;
        image = inputImg;
        this.context = context;
//        e = new Embedder();
    }

    public void testNdkCall()
    {

        byte[] grayPix = toGrayscale(image);

        e.embed(grayPix, image.getWidth(), image.getHeight(), "Testing Password");

    }

    public void execute ()
    {
        //convert to PGM
        //String orig_image = convertToPGM(imagePath);


        //  Load Cover
        // create config
        float payload=0.4f, gamma=2, sigma=0.5f;
        int randSeed = 12345;
        boolean verbose = false;
        int stcHeight = 10;
        String message = password;

        //cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stcHeight, randSeed, message);
        cost_model_config config = new cost_model_config(payload, verbose, gamma, sigma, stcHeight, randSeed, message);

        //  -> call function Load_Image from Mat2D and put inside Mat2D variable "cover"
        Mat2D cover = loadGrayImage(image);

        //base_cost_model *model = (base_cost_model *)new cost_model(cover,config);

        cost_model model = new cost_model(cover, config);

        //  Embed Image
        float []alpha_out, code_out, distortion_out;
        int []stcTrials_out;
        alpha_out = new float[1];
        code_out = new float[1];
        distortion_out = new float[1];
        stcTrials_out = new int[1];
        Mat2D stego = model.Embed(alpha_out, code_out, stcTrials_out, distortion_out);

        //Save stego
//        saveImage(outputImage, stego);

    }

    public byte[] toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        byte[] image = new byte[height*width];

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        //return bmpGrayscale;

        int count = 0;
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                int pixel = bmpOriginal.getPixel(i, j);
                int redValue = Color.red(pixel);
                int blueValue = Color.blue(pixel);
                int greenValue = Color.green(pixel);
                int grayColor = (int)(0.2126f*redValue + 0.7152f*greenValue + 0.07722f*blueValue);
                if (grayColor < 0) grayColor = 0;
                else if (grayColor > 255) grayColor = 255;
                image[count++] = (byte)grayColor;
            }
        }

        return image;
    }

    public Mat2D loadGrayImage (Bitmap input)
    {
        byte[] grayBitmap = toGrayscale(input);

        int width = input.getWidth();
        int height = input.getHeight();
        input.recycle();
        input = null;

        return new Mat2D(height, width, grayBitmap);
    }

    /*
    public Mat2D loadImage (String imagePath)
    {
        FileInputStream fileInputStream = null;
        Mat2D img = null;

        try {
            fileInputStream = new FileInputStream(convertToPGM(imagePath));


            Scanner scan = new Scanner(fileInputStream);
            scan.nextLine(); //discard magic number
            scan.nextLine(); //discard comment
            int width = scan.nextInt();
            int height = scan.nextInt();
            int max = scan.nextInt();

            img = new Mat2D(height, width, null);

            fileInputStream.close();

            fileInputStream = new FileInputStream(imagePath);

            DataInputStream dis = new DataInputStream(fileInputStream);

            //Discard header
            for (int i = 0; i < 4; i++) {
                char c;
                do {
                    c = (char) (dis.readUnsignedByte());
                } while (c != '\n');
            }

            //Read Image into Mat2D instance
            for (int row = 0; row < height; row++)
            {
                for(int col = 0; col < width; col++)
                {
                    img.Write(row, col, dis.readUnsignedByte());
                }
            }

        } catch (Exception e) { e.printStackTrace(); }

        return img;
    } */

    public void saveImage (String imagePath, Mat2D instance)
    {
        byte[] pixels = new byte[instance.rows * instance.cols];

        for(int r = 0; r < instance.rows; r++)
        {
            for(int c = 0; c < instance.cols; c++)
            {
                pixels[c*instance.rows + r] = instance.Read(r, c).byteValue();
            }
        }

        //Write to PGM file
        File newFile = new File(imagePath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            for(int i = 0; i < instance.rows; i++)
            {
                for(int j = 0; j < instance.cols; j++)
                {   //write one byte
                    fileOutputStream.write(pixels[j*instance.rows+i]);
                }
            }
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) { e.printStackTrace(); }

    }

    /*
    public String convertToPGM (String input)
    {
        File inputFile = new File(input);
        String output = null;
        return output;
    }
    */






}
