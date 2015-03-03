//
//  ndk_sse.c
//  Hugo
//
//  Created by Dhaval Miyani on 2015-02-24.
//  Copyright (c) 2015 Dhaval Miyani. All rights reserved.
//
#include <cstdlib>
#include <cmath>
#include <cfloat>
#include <stdio.h>
#include <limits>
#include "com_droidcrypt_embedder_NativeLib.h"

typedef unsigned char u8;
typedef unsigned int u32;
typedef unsigned short u16;

JNIEXPORT jstring JNICALL Java_com_marakana_NativeLib_hello
(JNIEnv * env, jobject obj) {
    return (*env)->NewStringUTF(env, "Hello World!");
}

JNIEXPORT jint JNICALL Java_com_marakana_NativeLib_add
(JNIEnv * env, jobject obj, jint value1, jint value2) {
    return (value1 + value2);
}

jfloat  * shuffle(float*v1, float*v2, jint num) {
    jint a[4];
    float* out = new float[4];
    for (jint i=0; i < 4; i++) {
        a[i] = num&0x3;
        num = num >> 2;
    }
    out[3] = v2[a[3]];
    out[2] = v2[a[2]];
    out[1] = v1[a[1]];
    out[0] = v1[a[0]];
    return out;
}

JNIEXPORT jfloat   JNICALL Java_com_droidcrypt_embedder_NativeLib_stc_1embed
(JNIEnv *, jobject, jbyteArray vector, jint vectorlength, jobject syndrome, jint syndromelength, jdoubleArray pricevectorv, jboolean usefloat, jobject stego, jint matrixheight) {
    jint height, i, k, l, index, index2, parts, m, sseheight, altm, pathindex=0;
    u32 column, colmask, state;
    jdouble totalprice;
    
    u8 *ssedone;
    u32 *path, *columns[2];
    jint *matrices, *widths;
    
    if ( matrixheight > 31 ) //throw stc_exception( "Submatrix height must not exceed 31.", 1 );
    
    height = 1 << matrixheight;
    colmask = height - 1;
    height = (height + 31) & (~31);
    
    parts = height >> 5;
    
    if ( stego != NULL ) {
        path = (u32*) malloc( vectorlength * parts * sizeof(u32) );
        if ( path == NULL ) {
//            std::stringstream ss;
//            ss << "Not enough memory (" << (unsigned int) (vectorlength * parts * sizeof(u32)) << " byte array could not be allocated).";
            //throw stc_exception( ss.str(), 2 );
        }
        pathindex = 0;
    }
    
    {
        jint shorter, longer, worm;
        double invalpha;
        
        matrices = (jint *) malloc( syndromelength * sizeof(jint) );
        widths = (jint *) malloc( syndromelength * sizeof(jint) );
        
        invalpha = (jdouble) vectorlength / syndromelength;
        if ( invalpha < 1 ) {
            free( matrices );
            free( widths );
            if ( stego != NULL ) free( path );
            //throw stc_exception( "The message cannot be longer than the cover object.", 3 );
        }
        /* THIS IS OBSOLETE. Algorithm still works for alpha >1/2. You need to take care of cases with too many Infs in cost vector.
         if(invalpha < 2) {
         printf("The relative payload is greater than 1/2. This may result in poor embedding efficiency.\n");
         }
         */
        shorter = (jint) floor( invalpha );
        longer = (jint) ceil( invalpha );
        if ( (columns[0] = getMatrix( shorter, matrixheight )) == NULL ) {
            free( matrices );
            free( widths );
            if ( stego != NULL ) free( path );
            return -1;
        }
        if ( (columns[1] = getMatrix( longer, matrixheight )) == NULL ) {
            free( columns[0] );
            free( matrices );
            free( widths );
            if ( stego != NULL ) free( path );
            return -1;
        }
        worm = 0;
        for ( i = 0; i < syndromelength; i++ ) {
            if ( worm + longer <= (i + 1) * invalpha + 0.5 ) {
                matrices[i] = 1;
                widths[i] = longer;
                worm += longer;
            } else {
                matrices[i] = 0;
                widths[i] = shorter;
                worm += shorter;
            }
        }
    }
    
    if ( true ) {
        /*
         SSE jfloat  VERSION
         */
        
        jint pathindex8 = 0;
        jint shift[2] = { 0, 4 };
        u8 mask[2] = { 0xf0, 0x0f };
        jfloat  *prices;
        u8 *path8 = (u8*) path;
        jdouble *pricevector = (jdouble*) pricevectorv;
        jdouble total = 0;
        jfloat  inf = std::numeric_limits< jfloat  >::infinity();
        
        sseheight = height >> 2;
        ssedone = (u8*) malloc( sseheight * sizeof(u8) );
        //prices = (float*) aligned_malloc( height * sizeof(float), 16 );
        prices = (jfloat*) malloc( height * sizeof(jfloat));
        
        {
            //__m128 fillval = _mm_set1_ps( inf );
            for ( i = 0; i < height; i += 4 ) {
                prices[i] = inf;
                prices[i+1] = inf;
                prices[i+2] = inf;
                prices[i+3] = inf;
                //_mm_store_ps( &prices[i], fillval );
                ssedone[i >> 2] = 0;
            }
        }
        
        prices[0] = 0.0f;
        
        for ( index = 0, index2 = 0; index2 < syndromelength; index2++ ) {
            //register __m128 c1, c2;
            jfloat  c1[4], c2[4];
            
            for ( k = 0; k < widths[index2]; k++, index++ ) {
                column = columns[matrices[index2]][k] & colmask;
                
                if ( vector[index] == 0 ) {
                    for(jint r = 0; r < 4; r++)
                    {
                        c1[r] = 0;
                        c2[r] = (jfloat)pricevector[index + r];
                    }
                    
                    //c1 = _mm_setzero_ps();
                    //c2 = _mm_set1_ps( (float) pricevector[index] );
                } else {
                    for(jint r = 0; r < 4; r++)
                    {
                        c1[r] = (jfloat)pricevector[index + r];
                        c2[r] = 0;
                    }
                    //c1 = _mm_set1_ps( (float) pricevector[index] );
                    //c2 = _mm_setzero_ps();
                }
                
                total += pricevector[index];
                
                for ( m = 0; m < sseheight; m++ ) {
                    if ( !ssedone[m] ) {
                        //register __m128 v1, v2, v3, v4;
                        jfloat  v1[4], v2[4], v3[4], v4[4];
                        
                        altm = (m ^ (column >> 2));
                        for(jint r = 0; r < 4; r++)
                        {
                            //v1 = _mm_load_ps( &prices[m << 2] );
                            //v2 = _mm_load_ps( &prices[altm << 2] );
                            v1[r] = prices[(m << 2) + r];
                            v2[r] = prices[(altm << 2) + r];
                            
                            //v3 = v1;
                            //v4 = v2;
                            v3[r] = v1[r];
                            v4[r] = v2[r];
                        }
                        ssedone[m] = 1;
                        ssedone[altm] = 1;
                        jfloat* tmp;
                        switch ( column & 3 ) {
                            case 0:
                                break;
                            case 1:
                                //v2 = _mm_shuffle_ps(v2, v2, 0xb1); // 10 11 00 01 = 2 3 0 1
                                //v3 = _mm_shuffle_ps(v3, v3, 0xb1);
                                //Swap indexes 0 & 1 + indexes 2 & 3
                                tmp = shuffle(v2, v2, 0xb1);
                                for (jint ir = 0; ir <4; ir++) {
                                    v2[ir] = tmp[ir];
                                }
                                free(tmp);
                                tmp = shuffle(v3, v3, 0xb1);
                                for (jint ir = 0; ir <4; ir++) {
                                    v3[ir] = tmp[ir];
                                }
                                free(tmp);
                                //                                swapFloats(&v2[0],&v2[1]);
                                //                                swapFloats(&v2[2],&v2[3]);
                                //                                swapFloats(&v3[0],&v3[1]);
                                //                                swapFloats(&v3[2],&v3[3]);
                                break;
                            case 2:
                                //v2 = _mm_shuffle_ps(v2, v2, 0x4e);
                                //v3 = _mm_shuffle_ps(v3, v3, 0x4e);
                                //Swap indexes 0 & 2 + indexes 1 & 3
                                //                                swapFloats(&v2[0],&v2[2]);
                                //                                swapFloats(&v2[1],&v2[3]);
                                //                                swapFloats(&v3[0],&v3[2]);
                                //                                swapFloats(&v3[1],&v3[3]);
                                tmp = shuffle(v2, v2, 0x4e);
                                for (jint ir = 0; ir <4; ir++) {
                                    v2[ir] = tmp[ir];
                                }
                                free(tmp);
                                tmp = shuffle(v3, v3, 0x4e);
                                for (jint ir = 0; ir <4; ir++) {
                                    v3[ir] = tmp[ir];
                                }
                                free(tmp);
                                
                                break;
                            case 3:
                                //v2 = _mm_shuffle_ps(v2, v2, 0x1b);
                                //v3 = _mm_shuffle_ps(v3, v3, 0x1b);
                                //Swap indexes 0 & 3 + indexes 1 & 2
                                //                                swapFloats(&v2[0],&v2[3]);
                                //                                swapFloats(&v2[1],&v2[2]);
                                //                                swapFloats(&v3[0],&v3[3]);
                                //                                swapFloats(&v3[1],&v3[2]);
                                tmp = shuffle(v2, v2, 0x1b);
                                for (jint ir = 0; ir <4; ir++) {
                                    v2[ir] = tmp[ir];
                                }
                                free(tmp);
                                tmp = shuffle(v3, v3, 0x1b);
                                for (jint ir = 0; ir <4; ir++) {
                                    v3[ir] = tmp[ir];
                                }
                                free(tmp);
                                
                                break;
                        }
                        
                        //v1 = _mm_add_ps( v1, c1 );
                        //v2 = _mm_add_ps( v2, c2 );
                        //v3 = _mm_add_ps( v3, c2 );
                        //v4 = _mm_add_ps( v4, c1 );
                        for(jint r = 0; r < 4; r++)
                        {
                            v1[r] += c1[r];
                            v2[r] += c2[r];
                            v3[r] += c2[r];
                            v4[r] += c1[r];
                        }
                        
                        //v1 = _mm_min_ps( v1, v2 );
                        //v4 = _mm_min_ps( v3, v4 );
                        for(jint r = 0; r < 4; r++)
                        {
                            v1[r] = fminf(v1[r],v2[r]);
                            v4[r] = fminf(v3[r],v4[r]);
                        }
                        
                        //_mm_store_ps( &prices[m << 2], v1 );
                        //_mm_store_ps( &prices[altm << 2], v4 );
                        for(jint r = 0; r < 4; r++)
                        {
                            prices[(m << 2) + r] = v1[r];
                            prices[(altm << 2) + r] = v4[r];
                        }
                        
                        if ( stego != NULL ) {
                            
                            //v2 = _mm_cmpeq_ps( v1, v2 );
                            //v3 = _mm_cmpeq_ps( v3, v4 );
                            for(jint r = 0; r < 4; r++)
                            {
                                //check if v1 == v2
                                if (v1[r] == v2[r]) {
                                    v2[r] = 0x1;
                                }
                                else {
                                    v2[r] = 0x0;
                                }
                                
                                // check if v3 == v4
                                if (v3[r] == v4[r]) {
                                    v3[r] = 0x1;
                                }
                                else {
                                    v3[r] = 0x0;
                                }
                            }
                            
                            //Setup for MOVEMASK_PS
                            //(_mm_movemask_ps( v2 ) << shift[m& 1]);
                            jint tmp = 0x0;
                            tmp = (jint)(v2[3])<<3 | (jint)(v2[2])<<2 | ((jint)v2[1]<<1) | (jint)(v2[0]);
                            
                            path8[pathindex8 + (m >> 1)] = (path8[pathindex8 + (m >> 1)] & mask[m & 1]) |
                            tmp << shift[m & 1];
                            
                            tmp = 0;
                            tmp = (jint)(v3[3])<<3 | (jint)(v3[2])<<2 | ((jint)v3[1]<<1) | (jint)(v3[0]);
                            
                            path8[pathindex8 + (altm >> 1)] = (path8[pathindex8 + (altm >> 1)] & mask[altm & 1]) |
                            (tmp << shift[altm & 1]);
                        }
                    }
                }
                
                for ( i = 0; i < sseheight; i++ ) {
                    ssedone[i] = 0;
                }
                
                pathindex += parts;
                pathindex8 += parts << 2;
            }
            
            if ( syndrome[index2] == 0 ) {
                for ( i = 0, l = 0; i < sseheight; i += 2, l += 4 ) {
                    jfloat  *tmp;
                    jfloat v1[4], v2[4];
                    for (jint ir = 0; ir <4; ir++) {
                        v1[ir] = prices[(i<<2)+ir];
                        v2[ir] = prices[((i + 1) << 2)+ir];
                    }
                    tmp = shuffle(v1, v2, 0x88);
                    for (jint ir = 0; ir <4; ir++) {
                        prices[l+ir] = tmp[ir];
                    }
                    free(tmp);
                    //                    prices[l] = prices[(i+1) << 2];
                    //                    prices[l+1] = prices[((i+1) << 2) + 2];
                    //                    prices[l+2] = prices[i << 2];
                    //                    prices[l+3] = prices[(i << 2) + 2];
                    //_mm_store_ps( &prices[l], _mm_shuffle_ps(_mm_load_ps(&prices[i << 2]), _mm_load_ps(&prices[(i + 1) << 2]), 0x88) );
                }
            } else {
                for ( i = 0, l = 0; i < sseheight; i += 2, l += 4 ) {
                    jfloat *tmp;
                    jfloat v1[4], v2[4];
                    for (jint ir = 0; ir <4; ir++) {
                        v1[ir] = prices[(i<<2)+ir];
                        v2[ir] = prices[((i + 1) << 2)+ir];
                    }
                    tmp = shuffle(v1, v2, 0xdd);
                    for (jint ir = 0; ir <4; ir++) {
                        prices[l+ir] = tmp[ir];
                    }
                    free(tmp);
                    //                    prices[l] = prices[((i+1) << 2) + 1];
                    //                    prices[l+1] = prices[((i+1) << 2) + 3];
                    //                    prices[l+2] = prices[(i << 2) + 1];
                    //                    prices[l+3] = prices[(i << 2) + 3];
                    //_mm_store_ps( &prices[l], _mm_shuffle_ps(_mm_load_ps(&prices[i << 2]), _mm_load_ps(&prices[(i + 1) << 2]), 0xdd) );
                }
            }
            
            if ( syndromelength - index2 <= matrixheight ) colmask >>= 1;
            
            {
                //                register __m128 fillval = _mm_set1_ps( inf );
                for ( l >>= 2; l < sseheight; l++ ) {
                    prices[(l << 2)]  = inf;
                    prices[(l << 2)+1]  = inf;
                    prices[(l << 2)+2]  = inf;
                    prices[(l << 2)+3]  = inf;
                    //                    _mm_store_ps( &prices[l << 2], fillval );
                }
            }
        }
        
        totalprice = prices[0];
        
        free( prices );
        free( ssedone );
        
        if ( totalprice >= total ) {
            free( matrices );
            free( widths );
            free( columns[0] );
            free( columns[1] );
            if ( stego != NULL ) free( path );
            //throw stc_exception( "No solution exist.", 4 );
        }
        
    }
    
    if ( stego != NULL ) {
        pathindex -= parts;
        index--;
        index2--;
        state = 0;
        
        // unused
        // jint h = syndromelength;
        state = 0;
        colmask = 0;
        for ( ; index2 >= 0; index2-- ) {
            for ( k = widths[index2] - 1; k >= 0; k--, index-- ) {
                if ( k == widths[index2] - 1 ) {
                    state = (state << 1) | syndrome[index2];
                    if ( syndromelength - index2 <= matrixheight ) colmask = (colmask << 1) | 1;
                }
                
                if ( path[pathindex + (state >> 5)] & (1 << (state & 31)) ) {
                    stego[index] = 1;
                    state = state ^ (columns[matrices[index2]][k] & colmask);
                } else {
                    stego[index] = 0;
                }
                
                pathindex -= parts;
            }
        }
        free( path );
    }
    
    free( matrices );
    free( widths );
    free( columns[0] );
    free( columns[1] );
    
    return totalprice;
    
}
