#include "base_cost_model.h"
#include "mat2D.h"
#include "base_cost_model_config.h"
#include "mi_embedder.h"

#define  LOG_TAG    "libembedder"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
//#include <boost/random/uniform_int.hpp>
//#include <boost/random/variate_generator.hpp>
//#include <boost/random/mersenne_twister.hpp>

base_cost_model::base_cost_model(mat2D<int>* cover, base_cost_model_config *config)
{
    this->cover = cover;
    this->rows = cover->rows;
    this->cols = cover->cols;
    this->config = config;
    
    costs = new float[3 * cover->rows * cover->cols];
    this->num_bits_used = new uint[2];
    num_bits_used[0] = 0;
    num_bits_used[1] = 0;
}

base_cost_model::~base_cost_model()
{
    //delete this->num_bits_used;
    delete this->costs;
}

mat2D<int> * base_cost_model::Embed(float &alpha_out, float &coding_loss_out, unsigned int &stc_trials_used, float &distortion)
{
    // Tomas Filler's segment
    float pls_lambda = -1; // this is initial value
    //boost::mt19937 generator(this->config->randSeed);
    //boost::variate_generator< boost::mt19937&, boost::uniform_int< > > rng( generator, boost::uniform_int< >( 0, RAND_MAX ) );
    
    mat2D<int> *stego;
    if (config->stc_constr_height==0)
    {
        // payload-limited sender with given payload; lambda is initialized in the first run and then reused
        //stego = mi_emb_simulate_pls_embedding(this, config->payload, rng(), pls_lambda, distortion, alpha_out);
        //Should never come here!
        //LOGE("Error: base_cost_model::Embed - stc_constr_height MUST NEVER be 0");
    }
    else
    {
        // use STCs
        //LOGI("Starting to Embed data");
        unsigned int stc_max_trials = 10; // maximum number of trials for ML STCs
        stego = mi_emb_stc_pls_embedding(this, config->payload, 3, config->stc_constr_height, stc_max_trials, distortion, alpha_out, coding_loss_out, stc_trials_used );
    }
    
    return stego;
}

unsigned char * base_cost_model::Extract(int *stego_px, int rows, int cols, uint *num_msg_bits, int stc_constr_height)
{
    return mi_extract_message(stego_px, rows, cols, 2, num_msg_bits, stc_constr_height);
}
/*
    This is an internal tool to verify the correctness of STC and HUGO
 */
bool base_cost_model::Verify(unsigned char* message, unsigned char *extracted_message, uint *num_msg_bits) {
    
    std::cout << "Verifying the extracted message " << num_msg_bits[0] << " - " << num_msg_bits[1] << " :  " << std::endl;
    //LOGI("Checking the extracted message %s (%d) %d-%d:", msg.c_str(), message_length, num_msg_bits[0], num_msg_bits[1] );
    for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
        printf("%x", extracted_message[k]);
        //LOGI("%x", extracted_message[k]);
    }
    std::cout << " vs.  ";
    //LOGI("  vs. ");
    for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
        printf("%x", message[k]);
        //LOGI("%x", message[k]);
    }
    std::cout << std::endl;
    
    //std::cout << "\nOriginal message " << msg << std::endl;
    //std::cout << "Extracted message " << msg << std::endl;
    
    // check the extracted message
    bool msg_ok = true;
    for ( uint k = 0; k < num_msg_bits[0] + num_msg_bits[1]; k++ ) {
        msg_ok &= (extracted_message[k] == message[k]);
        if ( !msg_ok ) {
            //LOGE("ML_STC_ERROR: Extracted message differs in bit " );
            std::cout << "ML_STC_ERROR: Extracted message differs in bit " << std::endl;
            return false;
        }
    }
    
    //LOGI("____Password MATCHED !! ____");
    return true;
}
