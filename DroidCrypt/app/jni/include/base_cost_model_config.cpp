#include "base_cost_model_config.h"

base_cost_model_config::base_cost_model_config(float payload, bool verbose, unsigned int stc_constr_height, int randSeed, std::string message)
{
	this->payload = payload;
	this->verbose = verbose;
	this->stc_constr_height = stc_constr_height;
	this->randSeed = randSeed;
    this->message = message;
    this->embedMsg = NULL;
    this->length = 0;
}

base_cost_model_config::~base_cost_model_config()
{
}