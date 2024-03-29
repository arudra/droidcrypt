#include <cstdlib>
#include <cstring>
#include <cmath>
#include <cfloat>
#include <limits>
#include <cstdio>
#include <sstream>
#include <iostream>
//#include <android/log.h>

#include "stc_embed_c.h"


#define  LOG_TAG    "libembedder"
#define  LOGI(...)  //__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  //__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

void *aligned_malloc( unsigned int bytes, int align ) {
    int shift;
    char *temp = (char *) malloc( bytes + align );

    if ( temp == NULL ) return temp;
    shift = align - (int) (((unsigned long long) temp) & (align - 1));
    temp = temp + shift;
    temp[-1] = shift;
    return (void *) temp;
}

void aligned_free( void *vptr ) {
    char *ptr = (char *) vptr;
    free( ptr - ptr[-1] );
    return;
}

/*
inline __m128i maxLessThan255( const __m128i v1, const __m128i v2 ) {
    register __m128i mask = _mm_set1_epi32( 0xffffffff );
    return _mm_max_epu8( _mm_andnot_si128( _mm_cmpeq_epi8( v1, mask ), v1 ), _mm_andnot_si128( _mm_cmpeq_epi8( v2, mask ), v2 ) );
}

inline u8 max16B( __m128i maxp ) {
    u8 mtemp[4];
    maxp = _mm_max_epu8( maxp, _mm_srli_si128(maxp, 8) );
    maxp = _mm_max_epu8( maxp, _mm_srli_si128(maxp, 4) );
    *((int*) mtemp) = _mm_cvtsi128_si32( maxp );
    if ( mtemp[2] > mtemp[0] ) mtemp[0] = mtemp[2];
    if ( mtemp[3] > mtemp[1] ) mtemp[1] = mtemp[3];
    if ( mtemp[1] > mtemp[0] ) return mtemp[1];
    else return mtemp[0];
}

inline u8 min16B( __m128i minp ) {
    u8 mtemp[4];
    minp = _mm_min_epu8( minp, _mm_srli_si128(minp, 8) );
    minp = _mm_min_epu8( minp, _mm_srli_si128(minp, 4) );
    *((int*) mtemp) = _mm_cvtsi128_si32( minp );
    if ( mtemp[2] < mtemp[0] ) mtemp[0] = mtemp[2];
    if ( mtemp[3] < mtemp[1] ) mtemp[1] = mtemp[3];
    if ( mtemp[1] < mtemp[0] ) return mtemp[1];
    else return mtemp[0];
}
 
 */

float * shuffle(float*v1, float*v2, int num) {
    int a[4];
    float* out = new float[4];
    for (int i=0; i < 4; i++) {
        a[i] = num&0x3;
        num = num >> 2;
    }
    out[3] = v2[a[3]];
    out[2] = v2[a[2]];
    out[1] = v1[a[1]];
    out[0] = v1[a[0]];
    return out;
}

inline void swapFloats(float * x, float * y)
{
    float tmp = *x;
    *x = *y;
    *y = tmp;
}

double stc_embed( const u8 *vector, int vectorlength, const u8 *syndrome, int syndromelength, const void *pricevectorv, bool usefloat,
        u8 *stego, int matrixheight ) {

    LOGI("ENTER stc_embed");
    int height, i, k, l, index, index2, parts, m, sseheight, altm, pathindex=0;
    u32 column, colmask, state;
    double totalprice;

    u8 *ssedone;
    u32 *path, *columns[2];
    int *matrices, *widths;

    if ( matrixheight > 31 ) {
        LOGE("Submatrix height must not exceed 31.");
        // throw stc_exception( "Submatrix height must not exceed 31.", 1 );
    }
    height = 1 << matrixheight;
    colmask = height - 1;
    height = (height + 31) & (~31);

    parts = height >> 5;

    if ( stego != NULL ) {
        path = (u32*) malloc( vectorlength * parts * sizeof(u32) );
        if ( path == NULL ) {
            std::stringstream ss;
            ss << "Not enough memory (" << (unsigned int) (vectorlength * parts * sizeof(u32)) << " byte array could not be allocated).";
            LOGE(ss.str().c_str());
            // throw stc_exception( ss.str(), 2 );
        }
        pathindex = 0;
    }

    {
        int shorter, longer, worm;
        double invalpha;

        matrices = (int *) malloc( syndromelength * sizeof(int) );
        widths = (int *) malloc( syndromelength * sizeof(int) );

        invalpha = (double) vectorlength / syndromelength;
        if ( invalpha < 1 ) {
            free( matrices );
            free( widths );
            if ( stego != NULL ) free( path );
            LOGE("The message cannot be longer than the cover object.");
            // throw stc_exception( "The message cannot be longer than the cover object.", 3 );
        }
        /* THIS IS OBSOLETE. Algorithm still works for alpha >1/2. You need to take care of cases with too many Infs in cost vector.
         if(invalpha < 2) {
         printf("The relative payload is greater than 1/2. This may result in poor embedding efficiency.\n");
         }
         */
        shorter = (int) floor( invalpha );
        longer = (int) ceil( invalpha );
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
         SSE FLOAT VERSION
         */

         LOGI("stc_embed_c inside Float block");
        
        int pathindex8 = 0;
        int shift[2] = { 0, 4 };
        u8 mask[2] = { 0xf0, 0x0f };
        float *prices;
        u8 *path8 = (u8*) path;
        double *pricevector = (double*) pricevectorv;
        double total = 0;
        float inf = std::numeric_limits< float >::infinity();

        sseheight = height >> 2;
        ssedone = (u8*) malloc( sseheight * sizeof(u8) );
        prices = (float*) aligned_malloc( height * sizeof(float), 16 );
        //prices = (float*) malloc( height * sizeof(float));

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
            float c1[4], c2[4];

            for ( k = 0; k < widths[index2]; k++, index++ ) {
                column = columns[matrices[index2]][k] & colmask;

                if ( vector[index] == 0 ) {
                    for(int r = 0; r < 4; r++)
                    {
                        c1[r] = 0;
                        c2[r] = (float)pricevector[index + r];
                    }

                    //c1 = _mm_setzero_ps();
                    //c2 = _mm_set1_ps( (float) pricevector[index] );
                } else {
                    for(int r = 0; r < 4; r++)
                    {
                        c1[r] = (float)pricevector[index + r];
                        c2[r] = 0;
                    }
                    //c1 = _mm_set1_ps( (float) pricevector[index] );
                    //c2 = _mm_setzero_ps();
                }

                total += pricevector[index];

                for ( m = 0; m < sseheight; m++ ) {
                    if ( !ssedone[m] ) {
                        //register __m128 v1, v2, v3, v4;
                        float v1[4], v2[4], v3[4], v4[4];

                        altm = (m ^ (column >> 2));
                        for(int r = 0; r < 4; r++)
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
                        float* tmp;
                        switch ( column & 3 ) {
                            case 0:
                                break;
                            case 1:
                                //v2 = _mm_shuffle_ps(v2, v2, 0xb1); // 10 11 00 01 = 2 3 0 1
                                //v3 = _mm_shuffle_ps(v3, v3, 0xb1);
                                //Swap indexes 0 & 1 + indexes 2 & 3
                                tmp = shuffle(v2, v2, 0xb1);
                                for (int ir = 0; ir <4; ir++) {
                                    v2[ir] = tmp[ir];
                                }
                                delete[] tmp;
                                tmp = shuffle(v3, v3, 0xb1);
                                for (int ir = 0; ir <4; ir++) {
                                    v3[ir] = tmp[ir];
                                }
                                delete[] tmp;
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
                                for (int ir = 0; ir <4; ir++) {
                                    v2[ir] = tmp[ir];
                                }
                                delete[] tmp;
                                tmp = shuffle(v3, v3, 0x4e);
                                for (int ir = 0; ir <4; ir++) {
                                    v3[ir] = tmp[ir];
                                }
                                delete[] tmp;
                                
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
                                for (int ir = 0; ir <4; ir++) {
                                    v2[ir] = tmp[ir];
                                }
                                delete[] tmp;
                                tmp = shuffle(v3, v3, 0x1b);
                                for (int ir = 0; ir <4; ir++) {
                                    v3[ir] = tmp[ir];
                                }
                                delete[] tmp;
                                
                                break;
                        }

                        //v1 = _mm_add_ps( v1, c1 );
                        //v2 = _mm_add_ps( v2, c2 );
                        //v3 = _mm_add_ps( v3, c2 );
                        //v4 = _mm_add_ps( v4, c1 );
                        for(int r = 0; r < 4; r++)
                        {
                            v1[r] += c1[r];
                            v2[r] += c2[r];
                            v3[r] += c2[r];
                            v4[r] += c1[r];
                        }

                        //v1 = _mm_min_ps( v1, v2 );
                        //v4 = _mm_min_ps( v3, v4 );
                        for(int r = 0; r < 4; r++)
                        {
                            v1[r] = fminf(v1[r],v2[r]);
                            v4[r] = fminf(v3[r],v4[r]);
                        }

                        //_mm_store_ps( &prices[m << 2], v1 );
                        //_mm_store_ps( &prices[altm << 2], v4 );
                        for(int r = 0; r < 4; r++)
                        {
                            prices[(m << 2) + r] = v1[r];
                            prices[(altm << 2) + r] = v4[r];
                        }

                        if ( stego != NULL ) {

                            //v2 = _mm_cmpeq_ps( v1, v2 );
                            //v3 = _mm_cmpeq_ps( v3, v4 );
                            for(int r = 0; r < 4; r++)
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
                            int tmp = 0x0;
                            tmp = (int)(v2[3])<<3 | (int)(v2[2])<<2 | ((int)v2[1]<<1) | (int)(v2[0]);
                            
                            path8[pathindex8 + (m >> 1)] = (path8[pathindex8 + (m >> 1)] & mask[m & 1]) |
                                                tmp << shift[m & 1];
                            
                            tmp = 0;
                            tmp = (int)(v3[3])<<3 | (int)(v3[2])<<2 | ((int)v3[1]<<1) | (int)(v3[0]);
                            
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
                    float *tmp;
                    float v1[4], v2[4];
                    for (int ir = 0; ir <4; ir++) {
                        v1[ir] = prices[(i<<2)+ir];
                        v2[ir] = prices[((i + 1) << 2)+ir];
                    }
                    tmp = shuffle(v1, v2, 0x88);
                    for (int ir = 0; ir <4; ir++) {
                        prices[l+ir] = tmp[ir];
                    }
                    delete[] tmp;
//                    prices[l] = prices[(i+1) << 2];
//                    prices[l+1] = prices[((i+1) << 2) + 2];
//                    prices[l+2] = prices[i << 2];
//                    prices[l+3] = prices[(i << 2) + 2];
                    //_mm_store_ps( &prices[l], _mm_shuffle_ps(_mm_load_ps(&prices[i << 2]), _mm_load_ps(&prices[(i + 1) << 2]), 0x88) );
                }
            } else {
                for ( i = 0, l = 0; i < sseheight; i += 2, l += 4 ) {
                    float *tmp;
                    float v1[4], v2[4];
                    for (int ir = 0; ir <4; ir++) {
                        v1[ir] = prices[(i<<2)+ir];
                        v2[ir] = prices[((i + 1) << 2)+ir];
                    }
                    tmp = shuffle(v1, v2, 0xdd);
                    for (int ir = 0; ir <4; ir++) {
                        prices[l+ir] = tmp[ir];
                    }
                    delete[] tmp;
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

        aligned_free( prices );
        free( ssedone );

        if ( totalprice >= total ) {
            free( matrices );
            free( widths );
            free( columns[0] );
            free( columns[1] );
            if ( stego != NULL ) free( path );
            LOGE("No solution exist");
            throw stc_exception( "No solution exist.", 4 );
        }
        
    } 
    if ( stego != NULL ) {
        pathindex -= parts;
        index--;
        index2--;
        state = 0;

        // unused
        // int h = syndromelength;
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
    LOGI("EXIT stc_embed");
    return totalprice;
}
