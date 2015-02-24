package com.droidcrypt;

/**
 * Created by dkmiyani on 15-02-24.
 */
public abstract class base_cost_model_config {
    private float payload;
    private boolean verbose;
    private Integer stc_constr_height;
    private int randSeed;

    base_cost_model_config() {

    }

    public base_cost_model_config(float payload, boolean verbose, Integer stc_constr_height, int randSeed) {
        this.payload = payload;
        this.verbose = verbose;
        this.stc_constr_height = stc_constr_height;
        this.randSeed = randSeed;
    }

    // getters and setters
    public int getRandSeed() {
        return randSeed;
    }

    public void setRandSeed(int randSeed) {
        this.randSeed = randSeed;
    }

    public float getPayload() {
        return payload;
    }

    public void setPayload(float payload) {
        this.payload = payload;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Integer getStc_constr_height() {
        return stc_constr_height;
    }

    public void setStc_constr_height(Integer stc_constr_height) {
        this.stc_constr_height = stc_constr_height;
    }

}
