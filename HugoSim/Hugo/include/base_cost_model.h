#ifndef BASE_COST_MODEL_H_
#define BASE_COST_MODEL_H_

#include "mat2D.h"
#include "base_cost_model_config.h"

class base_cost_model
{
public:
	float *costs;
	base_cost_model_config *config;
	mat2D<int> *cover;
	int rows, cols;
    uint *num_bits_used;

	base_cost_model(mat2D<int> *cover, base_cost_model_config *config);
	~base_cost_model();
	mat2D<int> * Embed(float &alpha_out, float &coding_loss_out, unsigned int &stc_trials_used, float &distortion);
    static unsigned char * Extract(int *stego_px, int rows, int cols, uint *num_msg_bits, int stc_constr_height);
    static bool Verify(unsigned char* message, unsigned char *extracted_message, uint *num_msg_bits);
};
#endif