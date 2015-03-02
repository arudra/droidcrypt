package com.droidcrypt.embedder;

import android.util.Log;

import com.droidcrypt.*;

import java.util.BitSet;
import java.util.Random;

/**
 * Created by dkmiyani on 15-02-24.
 */
public class MI_embedder {

    //mat2D<int>* mi_emb_stc_pls_embedding(base_cost_model *m, float alpha, uint seed,
    // uint stc_constr_height, uint stc_max_trails, float &distortion, float &alpha_out,
    // float &coding_loss_out, uint &stc_trials_used);

    public static Mat2D mi_emb_stc_pls_embedding(base_cost_model m, float alpha, Integer seed,
                                                 Integer stc_constr_height, int stc_max_trails,
                                                 float[] distortion, float[] alpha_out, float[] coding_loss_out,
                                                 int[] stc_trails_used)
                                                 throws Exception {
        distortion[0] = 0;
        Integer n = m.rows * m.cols;
//        unsigned char *stego_pixels = new unsigned char[n];
//        int []cover_px = new int[n];
        int []stego_px = new int[n];
//        for ( int i = 0; i < m.rows; i++ )
//        {
//            for ( int j = 0; j < m.cols; j++ )
//            {
//                cover_px[i * m.cols + j] = m.cover.Read(i, j);
//            }
//        }

//        boost::mt19937 generator( seed );
//        boost::variate_generator< boost::mt19937&, boost::uniform_int< > > rng( generator, boost::uniform_int< >( 0, RAND_MAX ) );
//
        Random rng = new Random();

        //    uint message_length = (uint) floor( alpha * n );
        String msg = m.config.getMessage();
        int message_length = msg.length();

        // convert String to bit array
        byte [] messageData = null;
        messageData = msg.getBytes();
        BitSet message = new BitSet(message_length*8);
        for (int i=0; i<messageData.length; i++) {
            byte b = messageData[i];
            for (int j=0; j<8; j++) {
                int lastBit = b&0x1;
                if (lastBit == 1) {
                     message.set(i*j);
                }
            }
        }

//        unsigned char *message = new unsigned char[message_length+1];
//        memcpy(message, msg.data(), message_length);
//        message[message_length] = 0;
//        uint i=0;
//        for (i = 0; i < message_length; i++ ) // generate random message
//            message[i] = rng() % 2; //reinterpret_cast<unsigned char&>(msg[i]); //rng() % 2;
//        message[i] = 0;

        stc_trails_used[0] = stc_max_trails;
        int [] num_msg_bits = new int[2]; // this array contains number of bits embedded into first and second layer
        stc_ml_c newSTC = new stc_ml_c();

        try
        {
            distortion[0] = newSTC.stc_pm1_pls_embed( n, m.cover.image, m.costs, message_length, message, stc_constr_height, Float.POSITIVE_INFINITY, stego_px, num_msg_bits,
                    stc_trails_used, coding_loss_out);
        }
        catch (Exception e)
        {
            num_msg_bits[0] = 0;
            num_msg_bits[1] = 0;
            distortion[0] = 0;
            coding_loss_out[0] = 0;
        }

        // check that the embedded message can be extracted
        // extract message from 'stego_array' into 'extracted_message' and use STCs with constr. height h
//        unsigned char *extracted_message = new unsigned char[message_length];
        BitSet extracted_message = new BitSet(message_length);
        newSTC.stc_ml_extract( n, stego_px, 2, num_msg_bits, stc_constr_height, (extracted_message) );
//        std::cout << "Checking the extracted message " << msg << " with "<< num_msg_bits[0] << " - " << num_msg_bits[1] << " :  ";
//        for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
//            printf("%x", extracted_message[k]);
//        }
//        std::cout << " vs.  ";
//        for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
//            printf("%x", message[k]);
//        }
//        std::cout << std::endl;

        Log.d("EMBED", "Checking the extracted message " + msg + " with " + num_msg_bits[0] + " - " + num_msg_bits[1] + " :  " +
                extracted_message + "  vs.  " + message);

        // check the extracted message
        boolean msg_ok = true;
        int [] stego_pixels = new int[n];
        for ( int k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
            msg_ok &= (extracted_message.get(k) == message.get(k));
            if ( !msg_ok ) throw new Exception( "ML_STC_ERROR: Extracted message differs in bit " );
        }

        if ( num_msg_bits[0] + num_msg_bits[1] > 0 ) {
            for ( int i = 0; i < n; i++ )
                stego_pixels[i] = stego_px[i];
        } else {
            Log.d("EMBED", "Password as not been stored inside the image");
            for ( int i = 0; i < n; i++ ) {
                int width = m.cols;
                int row = i/width;
                int col = i%width;
                stego_pixels[i] = m.cover.image.getPixel(col, row);
            }
        }

        Mat2D stego = new Mat2D(m.rows, m.cols, null);
        for ( int i = 0; i < m.rows; i++ )
        {
            for ( int j = 0; j < m.cols; j++ )
            {
                stego.Write(i, j, stego_pixels[i*m.cols+j]);
            }
        }
        alpha_out[0] = (float) (num_msg_bits[0] + num_msg_bits[1]) / (float) n;


        return stego;
    }

//    public static float mi_emb_calculate_lambda_from_payload(base_cost_model m, float rel_payload, float lambda_init, float[] alpha_out){
//        return 0;
//    }
//    public static float mi_emb_calc_average_payload(base_cost_model m, float lambda){
//       return 0;
//    }

}
