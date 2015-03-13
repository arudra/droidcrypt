#ifndef HUGO_LIKE_H_
#define HUGO_LIKE_H_

int HUGO_like(unsigned char * img, int width, int height, char * password, int * num_bits_used);
char * HUGO_like_extract(unsigned char *img, int width, int height, int stc_constr_height, int* num_msg_bits);


#endif