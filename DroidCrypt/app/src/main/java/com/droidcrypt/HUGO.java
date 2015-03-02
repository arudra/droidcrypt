package com.droidcrypt;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;


public class HUGO
{
  
    //INPUTS: input image path, Password
    private String imagePath, password;

    // output image path which holds hash of JPEG
    private String outputImage;

    private Drawable image;
    private Context context;

    public HUGO (String input, String pass, Drawable inputImage, Context context)
    {
        imagePath = input;
        password = pass;
        image = inputImage;
        this.context = context;

        MD5hash md5hash = new MD5hash();
        outputImage = md5hash.generateHash(imagePath);
    }


    public void execute ()
    {
        //convert to PGM
        //String image = convertToPGM(imagePath);


        //  Load Cover
        // create config
        float payload, gamma, sigma;
        int randSeed;
        boolean verbose = false;
        int stcHeight = 0;


        //cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stcHeight, randSeed, message);


        //  -> call function Load_Image from Mat2D and put inside Mat2D variable "cover"
        Mat2D cover = loadImage(imagePath);

        //base_cost_model *model = (base_cost_model *)new cost_model(cover,config);

        //  Embed Image
        final float alpha, code = 0, distortion = 0;
        final int stcTrials = 0;
        Mat2D stego = null; //= model->Embed(alpha, code, stcTrials, distortion);

        //Save stego
        saveImage(outputImage, stego);

    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Mat2D loadGrayImage (Drawable input)
    {
        Bitmap grayBitmap = toGrayscale(((BitmapDrawable) input).getBitmap());

        int width = grayBitmap.getWidth();
        int height = grayBitmap.getHeight();

        return new Mat2D(height, width, grayBitmap);
    }

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
    }

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

    public String convertToPGM (String input)
    {
        File inputFile = new File(input);
        String output = null;





        return output;
    }






}
