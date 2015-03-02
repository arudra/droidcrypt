package com.droidcrypt;


import android.graphics.drawable.Drawable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;


public class HUGO
{
  
    //INPUTS: input image path, Password
    private String inputImage, password;

    // output image path which holds hash of JPEG
    private String outputImage;

    public HUGO (String input, String pass)
    {
        inputImage = input;
        password = pass;

        MD5hash md5hash = new MD5hash();
        outputImage = md5hash.generateHash(inputImage);
    }


    public void execute ()
    {
        //convert to PGM
        //String image = convertToPGM(inputImage);


        //  Load Cover
        // create config
        float payload=0.4f, gamma=2, sigma=0.5f;
        int randSeed = 12345;
        boolean verbose = false;
        int stcHeight = 10;
        String message = "Hello World!";

        //cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stcHeight, randSeed, message);
        cost_model_config config = new cost_model_config(payload, verbose, gamma, sigma, stcHeight, randSeed, message);

        //  -> call function Load_Image from Mat2D and put inside Mat2D variable "cover"
        Mat2D cover = loadImage(inputImage);

//        base_cost_model *model = (base_cost_model *)new cost_model(cover,config);

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
        saveImage(outputImage, stego);

    }

    public Mat2D loadImage (String imagePath)
    {
        FileInputStream fileInputStream = null;
        Mat2D img = null;
        try {
            fileInputStream = new FileInputStream(imagePath);


            Scanner scan = new Scanner(fileInputStream);
            scan.nextLine(); //discard magic number
            scan.nextLine(); //discard comment
            int width = scan.nextInt();
            int height = scan.nextInt();
            int max = scan.nextInt();

            img = new Mat2D(height, width);

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
