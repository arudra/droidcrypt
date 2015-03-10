#include <stdio.h>
#include <iostream>
#include <fstream>
#include <time.h>
#include <vector>
#include <iomanip>
#include <android/log.h>

#include "image.h"
#include "mi_embedder.h"
#include "cost_model_config.h"
#include "exception.hpp"
#include "cost_model.h"
#include "mat2D.h"
#include "HUGO_like.h"


#define  LOG_TAG    "libembedder"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

mat2D<int> * Mat2dFromImage(unsigned char* img, int width, int height);
void Mat2dToImage(int *src, unsigned char* img, int width, int height);

void gen_random(char *s, const int len) {
    static const char alphanum[] =
    "0123456789"
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    "abcdefghijklmnopqrstuvwxyz";
    
    for (int i = 0; i < len; ++i) {
        s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
    }
    
    s[len] = 0;
}

int HUGO_like(unsigned char * img, int width, int height, char * password)
{
    try {
        float payload = 0.1;
        bool verbose = false;
        unsigned int stc_constr_height = 7;
        float gamma = 2;
        float sigma = 0.5;
        int randSeed = 0;
        
        clock_t begin=clock();
        int len = 10;
        char* msg;
        if (password == NULL) {
            msg = new char[len];
            gen_random(msg, len);
        }
        else {
            len = strlen(password);
            msg = password;
        }
        std::string message(msg);
        //        std::string message = "1234567890";
        LOGI("Here we get all the information: password =%s", msg);
        cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stc_constr_height, randSeed, message);
        
        // Load cover
        mat2D<int> *cover = Mat2dFromImage(img, width, height);
        base_cost_model * model = (base_cost_model *)new cost_model(cover, config);
        
        // Embedding
        float alpha_out, coding_loss_out = 0, distortion = 0;
        unsigned int stc_trials_used = 0;
        mat2D<int> * stego = model->Embed(alpha_out, coding_loss_out, stc_trials_used, distortion);
        
        // get all the necessary information
        uint *num_msg_bits = model->num_bits_used;
        int* stego_px = new int[stego->rows*stego->cols];
        for ( int i = 0; i < stego->rows; i++ )
        {
            for ( int j = 0; j < stego->cols; j++ )
            {
                float tmpValue = (float)stego->Read(i, j)*126;
                stego_px[i * stego->cols + j] = (int)(tmpValue/126.0f);
            }
        }
    
        // Extracting the message from the stego image
        unsigned char *extracted_message = base_cost_model::Extract(stego_px, stego->cols, stego->rows, num_msg_bits, stc_constr_height);
        
        // verify if the embedded message the same as the extracted one!
        bool isSame = base_cost_model::Verify((unsigned char *)(config->message.data()), extracted_message, num_msg_bits);
        if (!isSame) {
            // error message that the message is not the same!
            LOGE("Error: Embedded Message and Extracted message are not the same!");
        }

        Mat2dToImage(stego_px, img, width, height);
    

        delete model;
        
        delete cover;
        delete stego;
        delete config;
        
        clock_t end=clock();
    }
    catch(std::exception& e)
    {
        std::cerr << "error: " << e.what() << "\n";
        LOGE("ERROR exception in HUGO_like");
        return 1;
    }
    catch(...)
    {
        std::cerr << "Exception of unknown type!\n";
        LOGE("Exception of unknown type!");
        return 1;
    }
    
    return 0;
}
    
mat2D<int> * Mat2dFromImage(unsigned char* img, int width, int height)
{
    // move the image into a mat2D class
    mat2D<int> *I = new mat2D<int>(height, width);
    for (int r=0; r<height; r++)
        for (int c=0; c<width; c++)  {
            int pix = rand()%100;
            unsigned char pixByte = img[r*I->rows+c];
            //LOGI("%d", pix);
            //std::cout << pix << " " ;
            I->Write(r, c, (int)pixByte);
        }
    
    return I;
}

void Mat2dToImage(int *src, unsigned char* img, int width, int height)
{
    int totalPixel = width*height;
    for(int r=0; r<totalPixel; r++) {
        img[r] = (unsigned char) src[r];
    }
}

mat2D<int> * Load_Image(std::string imagePath, cost_model_config *config)
{
	// read image using the script in 'image.cpp'
	image *img = new image();
	if (imagePath.substr(imagePath.find_last_of(".")) == ".pgm")
		img->load_from_pgm(imagePath);
    else
        throw exception("File '" + imagePath + "' is in unknown format, we support grayscale 8bit pgm.");
		
	// move the image into a mat2D class
	mat2D<int> *I = new mat2D<int>(img->height, img->width);
	for (int r=0; r<I->rows; r++)
		for (int c=0; c<I->cols; c++)
			I->Write(r, c, (int)img->pixels[c*I->rows+r]);
	delete img;

	return I;
}

void Save_Image(std::string imagePath, mat2D<int>* I)
{
	unsigned char* pixels = new unsigned char[I->rows * I->cols];
	for (int r=0; r<I->rows; r++)
		for (int c=0; c<I->cols; c++)
		  pixels[c*I->rows + r] = (unsigned char)I->Read(r, c);
	image* img = new image(I->cols, I->rows, pixels);
	img->write_to_pgm(imagePath);
}
