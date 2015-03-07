#include <stdio.h>
#include <iostream>
#include <fstream>
#include <time.h>
#include <vector>
#include <iomanip>

#include "image.h"
#include "mi_embedder.h"
#include "cost_model_config.h"
#include "exception.hpp"
#include "cost_model.h"
#include "mat2D.h"

typedef unsigned int uint;

void Save_Image(std::string imagePath, mat2D<int>* I);
mat2D<int> * Load_Image(std::string imagePath, cost_model_config *config);
mat2D<int> * Embed(mat2D<int> *cover, cost_model_config * config, float &alpha_out, float &coding_loss_out, unsigned int &stc_trials_used, float &distortion);
mat2D<int> * Mat2dFromImage(int* img, int width, int height);

void printInfo(){
	std::cout << "This program embeds a payload using while minimizing 'HUGO_like' steganographic distortion [1][2] to all greyscale 'PGM' images in the directory input-dir and saves the stego images into the output-dir." << std::endl << std::endl;
	std::cout << "[1] Gibbs Construction in Steganography, Tomas Filler and Jessica Fridrich, IEEE Transactions on Information Forensics and Security, December 2010." << std::endl << std::endl;
	std::cout << "Author: Vojtech Holub, e-mail: vojtech_holub@yahoo.com" << std::endl << std::endl;
	std::cout << "usage: HUGO_like -I input-dir -O output-dir -a payload [-v] [-g gamma] [-s sigma] [-h STC-height] \n\n";
}

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

int HUGO_like(int * img, int width, int height, char * password)
{
    try {
        float payload = 0.1;
        bool verbose = true;
        unsigned int stc_constr_height = 7;
        float gamma = 1;
        float sigma = 1;
        int randSeed = 0;
        
        clock_t begin=clock();
        int len = 100;
        char* msg;
        if (true || password == NULL) {
            msg = new char[len];
            gen_random(msg, len-1);
        }
        else {
            len = strlen(password);
            msg = password;
        }
        std::string message(msg);
        //        std::string message = "1234567890";
        
        cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stc_constr_height, randSeed, message);
        
        // Load cover
        mat2D<int> *cover = Mat2dFromImage(img, width, height);
        base_cost_model * model = (base_cost_model *)new cost_model(cover, config);
        
        // Embedding
        float alpha_out, coding_loss_out = 0, distortion = 0;
        unsigned int stc_trials_used = 0;
        mat2D<int> * stego = model->Embed(alpha_out, coding_loss_out, stc_trials_used, distortion);
        
        delete model;
        
        delete cover;
        delete stego;
        delete config;
        
        clock_t end=clock();
    }
    catch(std::exception& e)
    {
        std::cerr << "error: " << e.what() << "\n";
        return 1;
    }
    catch(...)
    {
        std::cerr << "Exception of unknown type!\n";
        return 1;
    }
    
    return 0;
}
    
mat2D<int> * Mat2dFromImage(int* img, int width, int height)
{
    // move the image into a mat2D class
    mat2D<int> *I = new mat2D<int>(height, width);
    for (int r=0; r<height; r++)
        for (int c=0; c<width; c++) {
            int pix = rand()%126;
            //std::cout << pix << " " ;
            I->Write(r, c, pix/*(int)img->pixels[c*I->rows+r]*/);
        }
           // I->Write(r, c, img[c*I->rows+r]);
    
    return I;
}
    
int main(int argc, char** argv)
{
    //HUGO_like(NULL, 200, 200, NULL);
    //return 0;
	try { 
		std::string iDir, oDir;
        float payload = 0.04;
        bool verbose = true;
        unsigned int stc_constr_height = 7;
        float gamma = 2;
        float sigma = 0.5;
        int randSeed = 0;
        
        int width = 512;
        int height = 512;

        if (verbose) {
            std::cout << "# HUGO_like DISTORTION EMBEDDING SIMULATOR" << std::endl;
//			if (vm.count("input-dir")) std::cout << "# input directory = " << iDir << std::endl;
            std::cout << "# output directory = " << oDir << std::endl;
            std::cout << "# running payload-limited sender with alpha = " << payload << std::endl;
            if (stc_constr_height==0)
                std::cout << "# simulating embedding as if the best coding scheme is available" << std::endl;
            else
                std::cout << "# using STCs with constraint height h=" << stc_constr_height << std::endl;
            std::cout << ")" << std::endl;
            std::cout << std::endl;
            std::cout << "#file name     seed            size          rel. payload     rel. distortion  ";
			if (stc_constr_height>0) std::cout << "coding loss      # stc emb trials";
			std::cout << std::endl;
        }

		clock_t begin=clock();
        int len = 100;
        char* msg = new char[len];
        gen_random(msg, len);
        std::string message(msg);
//        std::string message = "1234567890";
        
		cost_model_config *config = new cost_model_config(payload, verbose, gamma, sigma, stc_constr_height, randSeed, message);

        // Load cover
//			mat2D<int> *cover = Load_Image(images[imageIndex], config);
            mat2D<int> *cover = Mat2dFromImage(NULL, width, height);

            if ( verbose ) std::cout << std::right << std::setw( 4 ) << cover->cols << "x" << std::left << std::setw( 10 )
                    << cover->rows << std::flush;

			base_cost_model * model = (base_cost_model *)new cost_model(cover, config);

			// Embedding
			float alpha_out, coding_loss_out = 0, distortion = 0;
			unsigned int stc_trials_used = 0;
			mat2D<int> * stego = model->Embed(alpha_out, coding_loss_out, stc_trials_used, distortion);

			delete model;

			// Save stego
			//Save_Image(stegoPath.string(), stego);

			if (verbose)
			{
				std::cout	<< std::left << std::setw( 17 ) << alpha_out
							<< std::left << std::setw( 17 ) << distortion / (cover->cols * cover->rows);
				if (stc_constr_height>0)
					std::cout	<< std::left << std::setw( 17 ) << coding_loss_out
								<< std::left << std::setw( 17 ) << stc_trials_used
								<< std::endl << std::flush;
				std::cout << std::endl;
			}

			delete cover;
			delete stego;
		
		delete config;

		clock_t end=clock();
		if(config->verbose) std::cout << std::endl << "Time elapsed: " << double(((double)end-begin)/CLOCKS_PER_SEC) << " s"<< std::endl;
	}
	catch(std::exception& e) 
	{ 
		std::cerr << "error: " << e.what() << "\n"; return 1; 
	} 
	catch(...) 
	{ 
		std::cerr << "Exception of unknown type!\n"; 
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
	mat2D<int> *I = new mat2D<int>(img->height-312, img->width-312);
    for (int r=0; r<I->rows; r++) {
        for (int c=0; c<I->cols; c++) {
            int pix = rand()%100;
            //std::cout << pix << " " ;
			I->Write(r, c, pix/*(int)img->pixels[c*I->rows+r]*/);
        }
    }
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
