package com.droidcrypt;

import com.droidcypt.embedder.MI_embedder;

import java.util.Random;

/**
 * Created by dkmiyani on 15-02-24.
 */
public abstract class base_cost_model {
    public float [] costs;
    public base_cost_model_config config;
    public Mat2D cover;
    public int rows, cols;

    public base_cost_model() {}

    public base_cost_model(Mat2D cover, base_cost_model_config config) {
        this.cover = cover;
        this.rows = cover.rows;
        this.cols = cover.cols;
        this.config = config;
        costs = new float[3*rows*cols];
    }

    // values are pass by reference...
    // mat2D<int> * base_cost_model::Embed(float &alpha_out, float &coding_loss_out, unsigned int &stc_trials_used, float &distortion)

    Mat2D Embed(float[] alpha_out, float[] coding_loss_out, int[] stc_trials_used, float[] distortion){
        // Tomas Filler's segment
        float pls_lambda = -1; // this is initial value
//        boost::mt19937 generator(this->config->randSeed);
//        boost::variate_generator< boost::mt19937&, boost::uniform_int< > > rng( generator, boost::uniform_int< >( 0, RAND_MAX ) );
        Random rand = new Random();
        Mat2D stego = null;
        if (config.getStc_constr_height()==0)
        {
            // error.. this should never happen
        }
        else
        {
            // use STCs
            int stc_max_trials = 10; // maximum number of trials for ML STCs
            stego = MI_embedder.mi_emb_stc_pls_embedding(this, config.getPayload(), rand.nextInt(), config.getStc_constr_height(), stc_max_trials, distortion, alpha_out, coding_loss_out, stc_trials_used );
        }

        return stego;
    }
}