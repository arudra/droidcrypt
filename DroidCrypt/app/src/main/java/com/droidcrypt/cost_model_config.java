package com.droidcrypt;

/**
 * Created by dkmiyani on 15-02-24.
 */
public class cost_model_config extends base_cost_model_config {
    public float gamma;
    public float sigma;

    public cost_model_config(float payload, boolean verbose, float gamma, float sigma, Integer stc_constr_height, int randSeed){
        super(payload, verbose, stc_constr_height, randSeed);this.gamma = gamma;

        this.sigma = sigma;
        this.gamma = gamma;
    }
}
