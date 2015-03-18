#include <stdio.h>
#include <iostream>
#include <fstream>
#include <time.h>
#include <vector>
#include <iomanip>
#include <bitset>
#include <cstring>
#include <android/log.h>

#include "image.h"
#include "mi_embedder.h"
#include "cost_model_config.h"
//#include "exception.hpp"
#include "cost_model.h"
#include "mat2D.h"
#include "HUGO_like.h"

#define bitset std::bitset

#define  LOG_TAG    "libembedder"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

mat2D<int> * Mat2dFromImage(unsigned char* img, int width, int height);
void Mat2dToImage(int *src, unsigned char* img, int width, int height);
unsigned char * string_to_bit_array(char * input);
char * bit_array_to_string(unsigned char *input, int len);


unsigned char * string_to_bit_array(char * input) {
  int len = strlen(input);
  unsigned char * tmp = new unsigned char[len*8];
  int i;
  for (i=0; i<len; i++) {
      bitset<8> byteArray(input[i]);
      for (int j=0; j<8; j++) {
          if (byteArray[j]) 
                tmp[i*8 + j] = 1;
            else tmp[i*8 + j] = 0;
      }
          //cout << byteArray << "  " << len << endl;
  }
  return tmp;
}

char * bit_array_to_string(unsigned char *input, int len) {
    int i;
    char * output = new char[len/8+1];
    bitset<8> oByteArray;
  for (i=0; i<len; i++) {
    for (int j=0; j<8; j++) {
      if (input[i*8 + j] == 1)
        oByteArray.set(j, true);
    else oByteArray.set(j, false);
    }
    //cout << oByteArray << endl;
    unsigned long c = oByteArray.to_ulong();
    output[i] = static_cast<char>( c ); 
  }
  output[len>>3] = '\0';
  return output;
}

void gen_random(char *s, const int len) {
    static const char alphanum[] =
    "0123456789"
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    "abcdefghijklmnopqrstuvwxyz";
    
    for (int i = 0; i < len; ++i) {
        s[i] = alphanum[rand() % (sizeof(alphanum) - 1)];
    }
    
    s[len-1] = '\0';
}

int HUGO_like(unsigned char * img, int width, int height, char * password, int* num_bits_used)
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
        unsigned char* msg;
        if (password == NULL) {
            password = new char[len];
            gen_random(password, len);
        }
        msg = string_to_bit_array(password);  //password;
        len = strlen(password)*8;
        std::string message(password);
        //        std::string message = "1234567890";
        char *tmp = bit_array_to_string(msg, len);
        LOGI("Here we get all the information: password =%s (%d)", tmp, len);
        //delete[] tmp;
        payload = width*height/len;
        cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stc_constr_height, randSeed, message);
        config->embedMsg = msg;
        config->length = (uint) len;

        // Load cover
        LOGI("Loading the cover image");
        mat2D<int> *cover = Mat2dFromImage(img, width, height);
        base_cost_model * model = (base_cost_model *)new cost_model(cover, config);
        
        // Embedding
        LOGI("Starting to embed...");
        float alpha_out, coding_loss_out = 0, distortion = 0;
        unsigned int stc_trials_used = 0;
        
        mat2D<int> * stego = model->Embed(alpha_out, coding_loss_out, stc_trials_used, distortion);
        
        LOGI("Copying mat2d back to stego_px");
        // get all the necessary information
        uint *num_msg_bits = model->num_bits_used;
        int* stego_px = new int[stego->rows*stego->cols];
        for ( int i = 0; i < stego->rows; i++ )
        {
            for ( int j = 0; j < stego->cols; j++ )
            {
                float tmpValue = (float)stego->Read(i, j);
                stego_px[i * stego->cols + j] = (int)(tmpValue);
            }
        }

        LOGI("Extracting - %d, %d", num_msg_bits[0], num_msg_bits[1]);
        // Extracting the message from the stego image
        unsigned char *extracted_message = base_cost_model::Extract(stego_px, stego->cols, stego->rows, num_msg_bits, stc_constr_height);
        
        // verify if the embedded message the same as the extracted one!
        bool isSame = base_cost_model::Verify((unsigned char *)(config->message.data()), extracted_message, num_msg_bits);
        
        if (!isSame) {
            // error message that the message is not the same!
            LOGE("Error: Embedded Message and Extracted message are not the same!");
            num_bits_used[0] = 0;
            num_bits_used[1] = 0;
        }
        else {
            char *tmp = bit_array_to_string(extracted_message, num_msg_bits[0] + num_msg_bits[1]);      
            LOGI("Successfully extracted_message is %s", tmp);
            //Mat2dToImage(stego_px, img, width, height);
            num_bits_used[0] = num_msg_bits[0];
            num_bits_used[1] = num_msg_bits[1];
            //delete[] tmp;
        }
        
        LOGI("config");
        delete config;
        LOGI("model");
        delete model;
        LOGI("cover");
        delete cover;
        LOGI("stego");
        delete stego;
        LOGI("msg");
        delete[] msg;
        LOGI("stego_px");
        delete[] stego_px;
        
        stego = NULL;
        model= NULL;
        msg= NULL;
        stego_px= NULL;
        cover= NULL;
        config= NULL;

        //delete[] extracted_message;
        
        //clock_t end=clock();
    }
    catch(std::exception& e)
    {
        LOGE("ERROR exception in HUGO_like");
        return 1;
    }
    catch(...)
    {
        LOGE("Exception of unknown type!");
        return 1;
    }
    
    LOGI("EXITING HUGO");
    return 0;
}

char * HUGO_like_extract(unsigned char *img, int width, int height, int stc_constr_height, int* num_msg_bits)
{
    LOGI("Enter HUGO_like_extract");
    // convert unsigned char* to int *
    int len = width*height;
    int *stego_px = new int[len];
    for (int i=0; i<len; i++) {
        stego_px[i] = (int)img[i];
    }

    // Extracting the message from the stego image
    unsigned char *extracted_message = base_cost_model::Extract(stego_px, width, height, (uint*)num_msg_bits, stc_constr_height);
    char *output = bit_array_to_string(extracted_message, num_msg_bits[0] + num_msg_bits[1]);
    LOGI("extracted_message is %s", output);
    // convert the bit array into string
    //delete[] extracted_message;
    delete[] stego_px;
    stego_px = NULL;

    return output;
}
    
mat2D<int> * Mat2dFromImage(unsigned char* img, int width, int height)
{
    // move the image into a mat2D class
    mat2D<int> *I = new mat2D<int>(height, width);
    for (int r=0; r<height; r++)
        for (int c=0; c<width; c++)  {
            //int pix = rand()%100;
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
