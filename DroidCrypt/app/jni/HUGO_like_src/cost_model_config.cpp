#include "cost_model_config.h"
#include "base_cost_model_config.h"

cost_model_config::cost_model_config(float payload, bool verbose, float gamma, float sigma, unsigned int stc_constr_height, int randSeed, std::string message) : base_cost_model_config(payload, verbose, stc_constr_height, randSeed, message)
{
	this->gamma = gamma;
	this->sigma = sigma;
}

cost_model_config::~cost_model_config()
{
}