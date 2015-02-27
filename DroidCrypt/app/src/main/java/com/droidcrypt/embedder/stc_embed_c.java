package com.droidcrypt.embedder;

import android.util.Log;
import java.util.BitSet;
import com.droidcrypt.common;

/**
 * Created by arudra on 24/02/15.
 */
public class stc_embed_c
{

    double stc_embed( byte[] vector, int vectorlength, BitSet syndrome, int syndromelength, double[] pricevectorv, boolean usefloat,
                      BitSet stego, int matrixheight ) {
        int height, i, k, l, index, index2, parts, m, sseheight, altm, pathindex;
        int column, colmask, state;
        double totalprice;

        BitSet ssedone;
        int[] path;
        int[][] columns;
        int[] matrices, widths;

        if ( matrixheight > 31 )
            Log.e("Submatrix", "Submatrix height must not exceed 31");

        height = 1 << matrixheight;
        colmask = height - 1;
        height = (height + 31) & (~31);

        parts = height >> 5;

        if ( stego != null ) {
            path = new int[vectorlength * parts];
            if ( path == null ) {
                Log.e("Memory", "Not enough memory, byte array could not be allocated");
                //ss << "Not enough memory (" << (unsigned int) (vectorlength * parts * sizeof(int)) << " byte array could not be allocated).";
            }
            pathindex = 0;
        }

        {
            int shorter, longer, worm;
            double invalpha;

            matrices = new int [syndromelength];
            widths = new int [syndromelength];;

            invalpha = (double) vectorlength / syndromelength;
            if ( invalpha < 1 ) {
                Log.d("Cover"," The message cannot be longer than the cover object");
            }
        /* THIS IS OBSOLETE. Algorithm still works for alpha >1/2. You need to take care of cases with too many Infs in cost vector.
         if(invalpha < 2) {
         printf("The relative payload is greater than 1/2. This may result in poor embedding efficiency.\n");
         }
         */
            shorter = (int) Math.floor(invalpha);
            longer = (int) Math.ceil(invalpha);
            common c = new common();
            if ( (columns[0] = c.getMatrix( shorter, matrixheight )) == null ) {
                return -1;
            }
            if ( (columns[1] = c.getMatrix( longer, matrixheight )) == null ) {
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

        if ( usefloat ) {
        /*
         SSE FLOAT VERSION
         */
            int pathindex8 = 0;
            int[] shift = { 0, 4 };
            byte[] mask = { (byte)0xf0, 0x0f };
            float[] prices;
            byte [] path8 = {0};
            for(int j=0; j < path.length; j++) {
                path8[j] = (byte) path[j];
            }

            double [] pricevector =  pricevectorv;
            double total = 0;
            float inf = Float.POSITIVE_INFINITY;

            sseheight = height >> 2;
            ssedone = new BitSet(sseheight); // (u8*) malloc( sseheight * sizeof(u8) );
            prices = new float[height]; //aligned_malloc( height * sizeof(float), 16 );

            {
                __m128 fillval = _mm_set1_ps( inf );
                for ( i = 0; i < height; i += 4 ) {
                    _mm_store_ps( &prices[i], fillval );
                    ssedone.clear(i >> 2);// [i >> 2] = 0;
                }
            }

            prices[0] = 0.0f;

            for ( index = 0, index2 = 0; index2 < syndromelength; index2++ ) {
                register __m128 c1, c2;

                for ( k = 0; k < widths[index2]; k++, index++ ) {
                    column = columns[matrices[index2]][k] & colmask;

                    if ( vector[index] == 0 ) {
                        c1 = _mm_setzero_ps();
                        c2 = _mm_set1_ps( (float) pricevector[index] );
                    } else {
                        c1 = _mm_set1_ps( (float) pricevector[index] );
                        c2 = _mm_setzero_ps();
                    }

                    total += pricevector[index];

                    for ( m = 0; m < sseheight; m++ ) {
                        if ( !ssedone[m] ) {
                            register __m128 v1, v2, v3, v4;
                            altm = (m ^ (column >> 2));
                            v1 = _mm_load_ps( &prices[m << 2] );
                            v2 = _mm_load_ps( &prices[altm << 2] );
                            v3 = v1;
                            v4 = v2;
                            ssedone.set(m);// [m] = 1;
                            ssedone.set(altm); //[altm] = 1;
                            switch ( column & 3 ) {
                                case 0:
                                    break;
                                case 1:
                                    v2 = _mm_shuffle_ps(v2, v2, 0xb1);
                                    v3 = _mm_shuffle_ps(v3, v3, 0xb1);
                                    break;
                                case 2:
                                    v2 = _mm_shuffle_ps(v2, v2, 0x4e);
                                    v3 = _mm_shuffle_ps(v3, v3, 0x4e);
                                    break;
                                case 3:
                                    v2 = _mm_shuffle_ps(v2, v2, 0x1b);
                                    v3 = _mm_shuffle_ps(v3, v3, 0x1b);
                                    break;
                            }
                            v1 = _mm_add_ps( v1, c1 );
                            v2 = _mm_add_ps( v2, c2 );
                            v3 = _mm_add_ps( v3, c2 );
                            v4 = _mm_add_ps( v4, c1 );

                            v1 = _mm_min_ps( v1, v2 );
                            v4 = _mm_min_ps( v3, v4 );

                            _mm_store_ps( &prices[m << 2], v1 );
                            _mm_store_ps( &prices[altm << 2], v4 );

                            if ( stego != null ) {
                                v2 = _mm_cmpeq_ps( v1, v2 );
                                v3 = _mm_cmpeq_ps( v3, v4 );
                                path8[pathindex8 + (m >> 1)] = (path8[pathindex8 + (m >> 1)] & mask[m & 1]) | (_mm_movemask_ps( v2 ) << shift[m
                                        & 1]);
                                path8[pathindex8 + (altm >> 1)] = (path8[pathindex8 + (altm >> 1)] & mask[altm & 1]) | (_mm_movemask_ps( v3 )
                                        << shift[altm & 1]);
                            }
                        }
                    }

                    for ( i = 0; i < sseheight; i++ ) {
                        ssedone.clear(i); //[i] = 0;
                    }

                    pathindex += parts;
                    pathindex8 += parts << 2;
                }

                if ( !syndrome.get(index2) ) {
                    for ( i = 0, l = 0; i < sseheight; i += 2, l += 4 ) {
                        _mm_store_ps( &prices[l], _mm_shuffle_ps(_mm_load_ps(&prices[i << 2]), _mm_load_ps(&prices[(i + 1) << 2]), 0x88) );
                    }
                } else {
                    for ( i = 0, l = 0; i < sseheight; i += 2, l += 4 ) {
                        _mm_store_ps( &prices[l], _mm_shuffle_ps(_mm_load_ps(&prices[i << 2]), _mm_load_ps(&prices[(i + 1) << 2]), 0xdd) );
                    }
                }

                if ( syndromelength - index2 <= matrixheight ) colmask >>= 1;

                {
                    register __m128 fillval = _mm_set1_ps( inf );
                    for ( l >>= 2; l < sseheight; l++ ) {
                        _mm_store_ps( &prices[l << 2], fillval );
                    }
                }
            }

            totalprice = prices[0];

            if ( totalprice >= total ) {
                Log.e("Error", "No solution exist.");
            }
        } else {
        /*
         SSE UINT8 VERSION
         */
            int pathindex16 = 0, subprice = 0;
            byte maxc = 0, minc = 0;
            byte [] prices;
            byte [] pricevector;

            for(int j = 0; j < pricevectorv.length; j++)
                pricevector[j] = (byte)pricevectorv[j];

            short [] path16;
            for(int j = 0; j < path.length; j++)
                path16[j] = (short) path[j];


            __m128i *prices16B;

            sseheight = height >> 4;
            ssedone = new BitSet(sseheight); //(u8*) malloc(sseheight * sizeof(u8));
            prices = new byte[height]; // (u8*) aligned_malloc(height * sizeof(u8), 16);
            prices16B = (__m128i *) prices;

            {
                __m128i napln = _mm_set1_epi32( 0xffffffff );
                for ( i = 0; i < sseheight; i++ ) {
                    _mm_store_si128( &prices16B[i], napln );
                    ssedone.clear(i);
                }
            }

            prices[0] = 0;

            for ( index = 0, index2 = 0; index2 < syndromelength; index2++ ) {
                register __m128i c1, c2, maxp, minp;

                if ( (int) maxc + pricevector[index] >= 254 ) {
                    Log.e("Limit", "Price vector limit exceeded." );
                }

                for ( k = 0; k < widths[index2]; k++, index++ ) {
                    column = columns[matrices[index2]][k] & colmask;

                    if ( vector[index] == 0 ) {
                        c1 = _mm_setzero_si128();
                        c2 = _mm_set1_epi8( pricevector[index] );
                    } else {
                        c1 = _mm_set1_epi8( pricevector[index] );
                        c2 = _mm_setzero_si128();
                    }

                    minp = _mm_set1_epi8( -1 );
                    maxp = _mm_setzero_si128();

                    for ( m = 0; m < sseheight; m++ ) {
                        if ( !ssedone[m] ) {
                            register __m128i v1, v2, v3, v4;
                            altm = (m ^ (column >> 4));
                            v1 = _mm_load_si128( &prices16B[m] );
                            v2 = _mm_load_si128( &prices16B[altm] );
                            v3 = v1;
                            v4 = v2;
                            ssedone[m] = 1;
                            ssedone[altm] = 1;
                            if ( column & 8 ) {
                                v2 = _mm_shuffle_epi32(v2, 0x4e);
                                v3 = _mm_shuffle_epi32(v3, 0x4e);
                            }
                            if ( column & 4 ) {
                                v2 = _mm_shuffle_epi32(v2, 0xb1);
                                v3 = _mm_shuffle_epi32(v3, 0xb1);
                            }
                            if ( column & 2 ) {
                                v2 = _mm_shufflehi_epi16(v2, 0xb1);
                                v3 = _mm_shufflehi_epi16(v3, 0xb1);
                                v2 = _mm_shufflelo_epi16(v2, 0xb1);
                                v3 = _mm_shufflelo_epi16(v3, 0xb1);
                            }
                            if ( column & 1 ) {
                                v2 = _mm_or_si128( _mm_srli_epi16( v2, 8 ), _mm_slli_epi16( v2, 8 ) );
                                v3 = _mm_or_si128( _mm_srli_epi16( v3, 8 ), _mm_slli_epi16( v3, 8 ) );
                            }
                            v1 = _mm_adds_epu8( v1, c1 );
                            v2 = _mm_adds_epu8( v2, c2 );
                            v3 = _mm_adds_epu8( v3, c2 );
                            v4 = _mm_adds_epu8( v4, c1 );

                            v1 = _mm_min_epu8( v1, v2 );
                            v4 = _mm_min_epu8( v3, v4 );

                            _mm_store_si128( &prices16B[m], v1 );
                            _mm_store_si128( &prices16B[altm], v4 );

                            minp = _mm_min_epu8( minp, _mm_min_epu8( v1, v4 ) );
                            maxp = _mm_max_epu8( maxp, maxLessThan255( v1, v4 ) );

                            if ( stego != null ) {
                                v2 = _mm_cmpeq_epi8( v1, v2 );
                                v3 = _mm_cmpeq_epi8( v3, v4 );
                                path16[pathindex16 + m] = (u16) _mm_movemask_epi8( v2 );
                                path16[pathindex16 + altm] = (u16) _mm_movemask_epi8( v3 );
                            }
                        }
                    }

                    maxc = max16B( maxp );
                    minc = min16B( minp );

                    maxc -= minc;
                    subprice += minc;
                    {
                        register __m128i mask = _mm_set1_epi32( 0xffffffff );
                        register __m128i m = _mm_set1_epi8( minc );
                        for ( i = 0; i < sseheight; i++ ) {
                            register __m128i res;
                            register __m128i pr = prices16B[i];
                            res = _mm_andnot_si128( _mm_cmpeq_epi8( pr, mask ), m );
                            prices16B[i] = _mm_sub_epi8( pr, res );
                            ssedone.clear(i);
                        }
                    }

                    pathindex += parts;
                    pathindex16 += parts << 1;
                }

                {
                    register __m128i mask = _mm_set1_epi32( 0x00ff00ff );

                    if ( minc == 255 ) {
                        Log.e("Syndrome", "The syndrome is not in the syndrome matrix range.");
                    }

                    if ( !syndrome.get(index2) ) {
                        for ( i = 0, l = 0; i < sseheight; i += 2, l++ ) {
                            _mm_store_si128( &prices16B[l], _mm_packus_epi16( _mm_and_si128( _mm_load_si128( &prices16B[i] ), mask ),
                                    _mm_and_si128( _mm_load_si128( &prices16B[i + 1] ), mask ) ) );
                        }
                    } else {
                        for ( i = 0, l = 0; i < sseheight; i += 2, l++ ) {
                            _mm_store_si128( &prices16B[l], _mm_packus_epi16( _mm_and_si128( _mm_srli_si128(_mm_load_si128(&prices16B[i]), 1),
                                    mask ), _mm_and_si128( _mm_srli_si128(_mm_load_si128(&prices16B[i + 1]), 1), mask ) ) );
                        }
                    }

                    if ( syndromelength - index2 <= matrixheight ) colmask >>= 1;

                    register __m128i fillval = _mm_set1_epi32( 0xffffffff );
                    for ( ; l < sseheight; l++ )
                        _mm_store_si128( &prices16B[l], fillval );
                }
            }

            totalprice = subprice + prices[0];
        }

        if ( stego != null ) {
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
                        state = (state << 1) | syndrome.toByteArray()[index2];
                        if ( syndromelength - index2 <= matrixheight ) colmask = (colmask << 1) | 1;
                    }

                    if ( (path[pathindex + (state >> 5)] & (1 << (state & 31))) > 0 ) {
                        stego.set(index); //[index] = 1;
                        state = state ^ (columns[matrices[index2]][k] & colmask);
                    } else {
                        stego.clear(index); //[index] = 0;
                    }
                    pathindex -= parts;
                }
            }
        }

        return totalprice;
    }

}
