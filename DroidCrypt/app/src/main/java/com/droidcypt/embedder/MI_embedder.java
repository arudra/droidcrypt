package com.droidcypt.embedder;

import com.droidcrypt.*;

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
                                                 int[] stc_trails_used){
        distortion[0] = 0;
        Integer n = m.rows * m.cols;
//        unsigned char *stego_pixels = new unsigned char[n];
        Byte[] stego_pixels = new Byte[n];

        int []cover_px = new int[n];
        int []stego_px = new int[n];

        for ( int i = 0; i < m.rows; i++ )
        {
            for ( int j = 0; j < m.cols; j++ )
            {
                cover_px[i * m.cols + j] = m.cover.Read(i, j);
            }
        }

//        boost::mt19937 generator( seed );
//        boost::variate_generator< boost::mt19937&, boost::uniform_int< > > rng( generator, boost::uniform_int< >( 0, RAND_MAX ) );
//
        Random rng = new Random();

        //    uint message_length = (uint) floor( alpha * n );
        String msg = m.config.getMessage();
        int message_length = msg.length();

        // convert String to bit array
        byte [] messageData = new byte[message_length]; // each byte represents a bit


//        unsigned char *message = new unsigned char[message_length+1];
//        memcpy(message, msg.data(), message_length);
//        message[message_length] = 0;
//        uint i=0;
//        for (i = 0; i < message_length; i++ ) // generate random message
//            message[i] = rng() % 2; //reinterpret_cast<unsigned char&>(msg[i]); //rng() % 2;
//        message[i] = 0;

        stc_trials_used = stc_max_trails;
        uint *num_msg_bits = new uint[2]; // this array contains number of bits embedded into first and second layer

        try
        {
            distortion[0] = stc_pm1_pls_embed( n, cover_px, m.costs, message_length, message, stc_constr_height, F_INF, stego_px, num_msg_bits,
                    stc_trials_used, &coding_loss );
        }
        catch (...)
        {
            num_msg_bits[0] = 0;
            num_msg_bits[1] = 0;
            distortion[0] = 0;
            coding_loss = 0;
        }

        // check that the embedded message can be extracted
        // extract message from 'stego_array' into 'extracted_message' and use STCs with constr. height h
        unsigned char *extracted_message = new unsigned char[message_length];
        stc_ml_extract( n, stego_px, 2, num_msg_bits, stc_constr_height, (extracted_message) );
        std::cout << "Checking the extracted message " << msg << " with "<< num_msg_bits[0] << " - " << num_msg_bits[1] << " :  ";
        for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
            printf("%x", extracted_message[k]);
        }
        std::cout << " vs.  ";
        for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
            printf("%x", message[k]);
        }
        std::cout << std::endl;
        // check the extracted message
        bool msg_ok = true;
        for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
            msg_ok &= (extracted_message[k] == message[k]);
            if ( !msg_ok ) throw exception( "ML_STC_ERROR: Extracted message differs in bit " );
        }

        if ( num_msg_bits[0] + num_msg_bits[1] > 0 ) {
            for ( uint i = 0; i < n; i++ )
                stego_pixels[i] = stego_px[i];
        } else {
            for ( uint i = 0; i < n; i++ )
                stego_pixels[i] = cover_px[i];
        }

        mat2D<int>* stego = new mat2D<int>(m.rows, m.cols);
        for ( int i = 0; i < m.rows; i++ )
        {
            for ( int j = 0; j < m.cols; j++ )
            {
                stego.Write(i, j, stego_pixels[i*m.cols+j]);
            }
        }
        alpha_out = (float) (num_msg_bits[0] + num_msg_bits[1]) / (float) n;

        delete[] stego_pixels;
        delete[] cover_px;
        delete[] stego_px;
        delete[] message;
        delete[] extracted_message;
        delete[] num_msg_bits;

        return stego;
        return null;
    }

    public static float mi_emb_calculate_lambda_from_payload(base_cost_model m, float rel_payload, float lambda_init, float[] alpha_out){
        return 0;
    }
    public static float mi_emb_calc_average_payload(base_cost_model m, float lambda){
       return 0;
    }

}
