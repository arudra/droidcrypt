package com.droidcrypt;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;
import java.util.Random;


public class HUGO
{
  
    //INPUTS: input orig_image path, Password
    private String imagePath, password;

    // output orig_image path which holds hash of JPEG
    private String outputImage;

    public Bitmap origImage;
    public Bitmap grayImage;
    public byte[] grayArray;
    private Context context;
    private Embedder e;

    public HUGO (String input, String pass, Bitmap inputImg, Context context)
    {
        imagePath = input;
        password = pass;
        origImage = inputImg;
        grayImage = null;
        grayArray = null;
        this.context = context;
    }

    public void testNdkCall()
    {
        grayArray = toGrayscale(origImage);
        //grayArray = bitmapToByteArray(origImage);
        e.embed(grayArray, origImage.getWidth(), origImage.getHeight(), "123456789012345");
        grayImage = convertColorHSVColor(origImage);
    }

    private byte[] bitmapToByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap byteArrayToBitmap(byte[] bitmapdata) {
        return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

    }

    /*
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
        Mat2D cover = loadGrayImage(origImage);

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
    */

    //Convert Bitmap from Color to HSV, then HSV to Color
    private Bitmap convertColorHSVColor(Bitmap src){

        int w = src.getWidth();
        int h = src.getHeight();

        int[] mapSrcColor = new int[w * h];
        int[] mapDestColor= new int[w * h];
        float[] pixelHSV = new float[3];

        src.getPixels(mapSrcColor, 0, w, 0, 0, w, h);

        int index = 0;
        for(int y = 0; y < h; ++y) {
            for(int x = 0; x < w; ++x) {

                //Convert from Color to HSV
                Color.colorToHSV(mapSrcColor[index], pixelHSV);
                //int value = (int)(pixelHSV[2]*255);
                //int pixelValue = (0xFF << 24) | (value << 16) | (value << 8) | value;
                pixelHSV[2] = ((float)grayArray[index])/100.0f;
                //Convert back from HSV to Color
                mapDestColor[index] = Color.HSVToColor(pixelHSV);

                index++;
            }
        }

        Bitmap.Config destConfig = src.getConfig();
        /*
         * If the bitmap's internal config is in one of the public formats, return that config,
         * otherwise return null.
         */

        if (destConfig == null){
            destConfig = Bitmap.Config.ARGB_8888;
        }

        Bitmap newBitmap = Bitmap.createBitmap(mapDestColor, w, h, destConfig);

        return newBitmap;
    }


    public byte[] toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        byte[] image = new byte[height*width];

        int[] mapSrcColor = new int[width * height];
        float[] pixelHSV = new float[3];

        bmpOriginal.getPixels(mapSrcColor, 0, width, 0, 0, width, height);

        /*grayImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(grayImage);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(grayImage, 0, 0, paint);
        //return bmpGrayscale;
*/
        int count = 0;
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
//                int pixel = bmpOriginal.getPixel(i, j);
//                int redValue = Color.red(pixel);
//                int blueValue = Color.blue(pixel);
//                int greenValue = Color.green(pixel);
//                int grayColor = (int)(0.2126f*redValue + 0.7152f*greenValue + 0.07722f*blueValue);
//                if (grayColor < 0) grayColor = 0;
//                else if (grayColor > 126) grayColor = 126;
//                grayColor = rn.nextInt(100);
                Color.colorToHSV(mapSrcColor[count], pixelHSV);
                int grayColor = (int)(pixelHSV[2]*100);

                image[count++] = (byte)grayColor;
            }
        }

        return image;
    }
    /*
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
    */

    /*
    public String convertToPGM (String input)
    {
        File inputFile = new File(input);
        String output = null;
        return output;
    }
    */






}
