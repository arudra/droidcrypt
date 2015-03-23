package com.droidcrypt;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import java.util.Arrays;
import org.opencv.android.*;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class HUGO
{
    //INPUTS: input orig_image path, Password
    private String password;

    // output orig_image path which holds hash of JPEG
    private String outputImage;

    public Bitmap origImage;
    public Bitmap grayImage;
    public byte[] grayArray;
    public int[] num_bits_used;
    public int[] diffColor;

    public HUGO (String pass, Bitmap inputImg, int[] bits)
    {
        password = pass;
        origImage = inputImg;
        grayImage = null;
        grayArray = null;
        diffColor = new int[origImage.getHeight()*origImage.getWidth()];
        num_bits_used =  bits;
    }

    public void testNdkCall()
    {
        num_bits_used[0] = 0;
        num_bits_used[1] = 0;
//        grayArray = toGrayscaleEmbed(origImage);

        /*
        Mat gray1 = rgbToMat(origImage);
        origImage = matToRgb(gray1);
        Mat gray2 = rgbToMat(origImage);

        for (int r=0; r<gray1.rows(); ++r) {
            for (int c=0; c<gray1.cols(); ++c) {
                if (gray1.get(r, c)[0] != gray2.get(r, c)[0]) {
                    Log.d("HUGO", "Changed gray value from " + gray1.get(r, c)[0] + " to " + gray2.get(r, c)[0]);
                }
            }
        }
        */
        Mat gray1 = rgbToMat(origImage);
        Log.d("HUGO", "-----------" + gray1.channels());
        grayArray = matToByteArray(gray1);

        long startTime = System.currentTimeMillis();
        try {
            grayArray = Embedder.embed(grayArray, origImage.getWidth(), origImage.getHeight(),
                    "abcdef0989mvgcg713", num_bits_used);
            long endEmbed = System.currentTimeMillis();
            byteArrayToMat(grayArray, gray1);
            AccountInfo.getInstance().setBitmap(matToRgb(gray1));
            Mat gray2 = rgbToMat(AccountInfo.getInstance().getBitmap());
            byte[] grayArray2 = matToByteArray(gray2);
            if (!Arrays.equals(grayArray2, grayArray)) {
                Log.e("HUGO", "Arrays do not match!!");
            }
            /*
            for (int r=0; r<gray1.rows(); ++r) {
                for (int c=0; c<gray1.cols(); ++c) {
                    if (gray1.get(r, c)[2] != gray2.get(r, c)[2]) {
                        Log.d("HUGO", "MAT: Changed gray value from " + gray1.get(r, c)[2] + " to " + gray2.get(r, c)[2]);
                    }
                }
            }
            */
            int len = gray1.rows()*gray1.cols();
            for (int i=0; i<len; i++) {
                if (grayArray[i] != grayArray2[i]) {
                    Log.d("HUGO", "ByteArray: Changed gray value from " + grayArray[i] + " to " + grayArray2[i]);
                }
            }
            String oPass = Embedder.extract(grayArray2, origImage.getWidth(), origImage.getHeight(), num_bits_used, 7);
            long endExtract = System.currentTimeMillis();
            Log.d("HUGO", "Num_bits_used to embed = " + num_bits_used[0] + " - " + num_bits_used[1]);
            //Log.d("HUGO", "Password extracted  :  " + oPass);
            Log.d("HUGO", "TIME Embed: " + (endEmbed - startTime) / 1000.f + "s  Extract: " + (endExtract - endEmbed) / 1000.f + "s");
        } catch (Exception e) {
            Log.e("HUGO", "Exception in embedder lib : " + e.getMessage());
            e.printStackTrace();
        }
        //grayImage = convertColorHSVColor(origImage);

    }

    public void embed()
    {
        num_bits_used[0] = 2;
        num_bits_used[1] = 2;

//        grayArray = toGrayscaleEmbed(origImage);

        Mat gray1 = rgbToMat(origImage);
        grayArray = matToByteArray(gray1);
        long startTime = System.currentTimeMillis();

        try{
            grayArray = Embedder.embed(grayArray, origImage.getWidth(), origImage.getHeight(), password, num_bits_used);

//            Store embedded array for later extract
            long endTime = System.currentTimeMillis();
//            AccountInfo.getInstance().setBitmap(convertColorHSVColor(origImage));
            byteArrayToMat(grayArray, gray1);
            AccountInfo.getInstance().setBitmap(matToRgb(gray1));

            grayArray = null;

            Log.d("HUGO", "Num_bits_used to embed = " + num_bits_used[0] + " - " + num_bits_used[1]);
            Log.d("HUGO", "TIME Embed: " + (endTime - startTime) / 1000.f + "s");

        } catch (Exception e)
        {
            Log.e("HUGO", "Exception in embedder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String extract()
    {
        String output;
        AccountInfo accountInfo = AccountInfo.getInstance();

        long startTime = System.currentTimeMillis();
        output = Embedder.extract(/*toGrayscaleExtract(accountInfo.getBitmap())*/ matToByteArray(rgbToMat(accountInfo.getBitmap())), accountInfo.getBitmap().getWidth(),
                accountInfo.getBitmap().getHeight(), num_bits_used, 7);
        if (output.equals("")) {
            return "No Password Found!";
        }
        accountInfo.setName(output.split("#")[0]);
        accountInfo.setPassword(output.split("#")[1]);
        long endTime = System.currentTimeMillis();

        Log.d("HUGO", "Info Extracted: " + output);
        Log.d("HUGO", "TIME Extract: " + (endTime - startTime) / 1000.f + "s");

        return output;
    }

    public byte[] matToByteArray(Mat mat) {
        byte[] b = new byte[mat.rows()*mat.cols()];
        for (int r=0; r<mat.rows(); ++r) {
            for (int c=0; c<mat.cols(); ++c) {
                b[r*mat.cols() + c] = (byte)mat.get(r, c)[2];
            }
        }
        return b;
    }

    public void byteArrayToMat(byte[] b, Mat mat) {
        byte[] t = new byte[3];
        for (int r=0; r<mat.rows(); ++r) {
            for (int c = 0; c < mat.cols(); ++c) {
                mat.get(r, c, t);
                t[2] = b[r*mat.cols()+c];
                mat.put(r, c, t);
            }
        }
    }

    public Mat rgbToMat(Bitmap b) {
        Mat tmp = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(b, tmp);
        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2HSV);
        Log.d("HUGO", "Length of Mat: " + tmp.size() + "vs " + b.getWidth()*b.getHeight());
        return tmp;
    }

    public Bitmap matToRgb(Mat mat) {
        Bitmap bmpOut = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_HSV2RGB, 4);
//        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2RGBA);
        Utils.matToBitmap(mat, bmpOut);
        return bmpOut;
    }

    //Convert Bitmap from Color to HSV, then HSV to Color
    public Bitmap convertColorHSVColor(Bitmap src){

        int w = src.getWidth();
        int h = src.getHeight();

        int[] mapSrcColor = new int[w * h];
        int[] mapDestColor= new int[w * h];
        float[] pixelHSV = new float[3];
        float[] pixelHSV1 = new float[3];

        src.getPixels(mapSrcColor, 0, w, 0, 0, w, h);

        int index = 0;
        for(int y = 0; y < h; ++y) {
            for(int x = 0; x < w; ++x) {

                //Convert from Color to HSV
                int srcC = mapSrcColor[index];
                int diffC = diffColor[index];
                Color.colorToHSV(srcC, pixelHSV);
                pixelHSV[2] = (float)(grayArray[index] + diffC)/125.0f;
                //Convert back from HSV to Color
                int destC = Color.HSVToColor(pixelHSV);
                mapDestColor[index] = destC;

                // debug only
                Color.colorToHSV(destC, pixelHSV1);
                int g = (int)(pixelHSV1[2]*125.0f);
                int gorig = (grayArray[index]);
                if (gorig != g) {
                    Log.d("HUGO", "Changed gray value from" + g + " to " + gorig);
                }

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

    private byte[] setupErrorBit(Bitmap bmpOriginal)
    {
        int width, height;

        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        byte[] image = new byte[height*width];
        int[] mapSrcColor = new int[width * height];
        float[] pixelHSV = new float[3];

        bmpOriginal.getPixels(mapSrcColor, 0, width, 0, 0, width, height);

        int count = 0;
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                Color.colorToHSV(mapSrcColor[count], pixelHSV);
                int grayColor = (int)(pixelHSV[2]*125.0f);
                pixelHSV[2] = ((float)grayColor)/125.0f;
                Color.colorToHSV(Color.HSVToColor(pixelHSV), pixelHSV);

                int g = (int)(pixelHSV[2]*125.0f);
                if (g != grayColor) {
                    int diff = grayColor - g;
                    if ((grayColor & 0x1) == 0) {
                        diffColor[count] = 0;
                        image[count++] = (byte)(grayColor+1);

                    } else {
                        diffColor[count] = diff;
                        image[count++] = (byte) grayColor;
                    }
                }
            }
        }

        return image;

    }

    public byte[] toGrayscaleEmbed(Bitmap bmpOriginal)
    {
        int width, height;

        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        byte[] image = new byte[height*width];
        int[] mapSrcColor = new int[width * height];
        float[] pixelHSV = new float[3];

        bmpOriginal.getPixels(mapSrcColor, 0, width, 0, 0, width, height);

        int count = 0;
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                Color.colorToHSV(mapSrcColor[count], pixelHSV);

                //float diffPix = pixelHSV[2];
                //Color.colorToHSV(Color.HSVToColor(pixelHSV), pixelHSV);
                //diffPix = (diffPix - pixelHSV[2]);
                //Log.d("HUGO", "Difference in the conversion " + diffPix);

                int grayColor = (int)(pixelHSV[2]*125.0f);
                pixelHSV[2] = ((float)grayColor)/125.0f;

                Color.colorToHSV(Color.HSVToColor(pixelHSV), pixelHSV);
                int g = (int)(pixelHSV[2]*125.0f);
                if (g != grayColor) {
                    int diff = grayColor - g;
                    if ((grayColor & 0x1) == 0) {
                        diffColor[count] = 0;
                        image[count++] = (byte)(grayColor+1);

                    } else {
                        diffColor[count] = diff;
                        image[count++] = (byte) grayColor;
                    }
                }
                else {
                    diffColor[count] = 0;
                    image[count++] = (byte) grayColor;
                }
            }
        }

        return image;
    }
    public byte[] toGrayscaleExtract(Bitmap bmpOriginal)
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
                Color.colorToHSV(mapSrcColor[count], pixelHSV);
                int grayColor = (int)(pixelHSV[2]*125.0f);
                image[count++] = (byte)grayColor;
            }
        }

        return image;
    }

}
