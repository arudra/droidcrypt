package com.droidcrypt.embedder;

import android.util.Log;
import java.util.BitSet;
import com.droidcrypt.common;

/**
 * Created by arudra on 24/02/15.
 */
public class stc_embed_c
{
    private float [] shuffle(float[] v1, float[] v2, int num) {
    int[] a = new int[4];
    float[] out = new float[4];

        for (int i=0; i < 4; i++)
        {
            a[i] = num&0x3;
            num = num >> 2;
        }
        out[3] = v2[a[3]];
        out[2] = v2[a[2]];
        out[1] = v1[a[1]];
        out[0] = v1[a[0]];
        return out;
    }

    double stc_embed( byte[] vector, int vectorlength, BitSet syndrome, int syndromelength, double[] pricevectorv, boolean usefloat,
                      BitSet stego, int matrixheight ) {
        int height, i, k, l, index=0, index2=0, parts, m, sseheight, altm, pathindex=0;
        int column, colmask, state;
        double totalprice = 0.0;

        BitSet ssedone;
        int[] path = null;
        int[][] columns = {{0}, {0}};
        int[] matrices, widths;

        if ( matrixheight > 31 )
            Log.e("EMBED", "Submatrix height must not exceed 31");

        height = 1 << matrixheight;
        colmask = height - 1;
        height = (height + 31) & (~31);

        parts = height >> 5;

        if ( stego != null ) {
            path = new int[vectorlength * parts];
            if ( path == null ) {
                Log.e("EMBED", "Not enough memory, byte array could not be allocated");
                //ss << "Not enough memory (" << (unsigned int) (vectorlength * parts * sizeof(int)) << " byte array could not be allocated).";
            }
            pathindex = 0;
        }
        {
            int shorter, longer, worm;
            double invalpha;

            matrices = new int [syndromelength];
            widths = new int [syndromelength];

            invalpha = (double) vectorlength / syndromelength;
            if ( invalpha < 1 ) {
                Log.d("EMBED"," The message cannot be longer than the cover object");
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
            int[] shift = new int[2];
            shift[0] = 0;
            shift[1] = 4;
            int[] mask = new int [2];
            mask[0] = 0xf0;
            mask[1] = 0x0f;
            float[] prices;
            int [] path8 = path;

            double [] pricevector =  pricevectorv;
            double total = 0;
            float inf = Float.POSITIVE_INFINITY;

            sseheight = height >> 2;
            ssedone = new BitSet(sseheight); // (u8*) malloc( sseheight * sizeof(u8) );
            prices = new float[height]; //aligned_malloc( height * sizeof(float), 16 );
            for (i=0; i < height; i += 4)
            {
                prices[i] = inf;
                prices[i+1] = inf;
                prices[i+2] = inf;
                prices[i+3] = inf;
                ssedone.clear(i >> 2);
            }

            /*
            __m128 fillval = _mm_set1_ps( inf );
            for ( i = 0; i < height; i += 4 ) {
                _mm_store_ps( &prices[i], fillval );
                ssedone.clear(i >> 2);// [i >> 2] = 0;
            } */

            prices[0] = 0.0f;

            for ( index = 0, index2 = 0; index2 < syndromelength; index2++ ) {
                //register __m128 c1, c2;
                float[] c1 = new float[4];
                float[] c2 = new float[4];

                for ( k = 0; k < widths[index2]; k++, index++ ) {
                    column = columns[matrices[index2]][k] & colmask;

                    if ( vector[index] == 0 ) {
                        //c1 = _mm_setzero_ps();
                        //c2 = _mm_set1_ps( (float) pricevector[index] );
                        for(int r = 0; r < 4; r++)
                        {
                            c1[r] = 0;
                            c2[r] = (float)pricevector[index + r];
                        }

                    }
                    else
                    {
                        //c1 = _mm_set1_ps( (float) pricevector[index] );
                        //c2 = _mm_setzero_ps();
                        for(int r = 0; r < 4; r++)
                        {
                            c1[r] = (float)pricevector[index + r];
                            c2[r] = 0;
                        }
                    }

                    total += pricevector[index];

                    for ( m = 0; m < sseheight; m++ ) {
                        if ( !ssedone.get(m) ) {
                            //register __m128 v1, v2, v3, v4;
                            float[] v1 = new float[4], v2 = new float[4];
                            float[] v3 = new float[4], v4 = new float[4];

                            altm = (m ^ (column >> 2));
                            for(int r = 0; r < 4; r++) {
                                //v1 = _mm_load_ps( &prices[m << 2] );
                                //v2 = _mm_load_ps( &prices[altm << 2] );
                                v1[r] = prices[(m << 2) + r];
                                v2[r] = prices[(altm << 2) + r];

                                //v3 = v1;
                                //v4 = v2;
                                v3[r] = v1[r];
                                v4[r] = v2[r];
                            }
                            ssedone.set(m);// [m] = 1;
                            ssedone.set(altm); //[altm] = 1;
                            float[] tmp;
                            switch ( column & 3 ) {
                                case 0:
                                    break;
                                case 1:
                                    //v2 = _mm_shuffle_ps(v2, v2, 0xb1);
                                    //v3 = _mm_shuffle_ps(v3, v3, 0xb1);
                                    tmp = shuffle(v2, v2, 0xb1);
                                    System.arraycopy(tmp, 0, v2, 0, tmp.length);

                                    tmp = shuffle(v3, v3, 0xb1);
                                    System.arraycopy(tmp, 0, v3, 0, tmp.length);
                                    break;
                                case 2:
                                    //v2 = _mm_shuffle_ps(v2, v2, 0x4e);
                                    //v3 = _mm_shuffle_ps(v3, v3, 0x4e);
                                    tmp = shuffle(v2, v2, 0x4e);
                                    System.arraycopy(tmp, 0, v2, 0, tmp.length);

                                    tmp = shuffle(v3, v3, 0x4e);
                                    System.arraycopy(tmp, 0, v3, 0, tmp.length);
                                    break;
                                case 3:
                                    //v2 = _mm_shuffle_ps(v2, v2, 0x1b);
                                    //v3 = _mm_shuffle_ps(v3, v3, 0x1b);
                                    tmp = shuffle(v2, v2, 0x1b);
                                    System.arraycopy(tmp, 0, v2, 0, tmp.length);

                                    tmp = shuffle(v3, v3, 0x1b);
                                    System.arraycopy(tmp, 0, v3, 0, tmp.length);
                                    break;
                            }
                            /*
                            v1 = _mm_add_ps( v1, c1 );
                            v2 = _mm_add_ps( v2, c2 );
                            v3 = _mm_add_ps( v3, c2 );
                            v4 = _mm_add_ps( v4, c1 ); */
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
                                //Put Min value into v1
                                if(Float.compare(v1[r], v2[r]) >= 0) {
                                    v1[r] = v2[r];
                                }

                                //Put Min value into v4
                                if(Float.compare(v3[r], v4[r]) < 0) {
                                    v4[r] = v3[r];
                                }
                            }

                            //_mm_store_ps( &prices[m << 2], v1 );
                            //_mm_store_ps( &prices[altm << 2], v4 );
                            for(int r = 0; r < 4; r++)
                            {
                                prices[(m << 2) + r] = v1[r];
                                prices[(altm << 2) + r] = v4[r];
                            }

                            if ( stego != null ) {
                                //v2 = _mm_cmpeq_ps( v1, v2 );
                                //v3 = _mm_cmpeq_ps( v3, v4 );
                                for (int r = 0; r < 4; r++)
                                {
                                    //check if v1 == v2
                                    if (v1[r] == v2[r]){
                                        v2[r] = 0x1;
                                    }
                                    else {
                                        v2[r] = 0x0;
                                    }

                                    //check if v3 == v4
                                    if (v3[r] == v4[r]) {
                                        v3[r] = 0x1;
                                    }
                                    else {
                                        v3[r] = 0x0;
                                    }
                                }

                                //Setup for MOVEMASK_PS
                                //(_mm_movemask_ps(v2) << shift[m &1]);
                                int tmp2 = 0x0;
                                tmp2 = (int)(v2[3])<<3 | (int)(v2[2])<<2 | ((int)v2[1]<<1) | (int)(v2[0]);
                                if ((pathindex8 + (m >> 1)) > path8.length) {
                                    // ERROR
                                    Log.e("EMBED", "Array out of bound on: (pathindex8 + (m >> 1)) > path8.length ");
                                }
                                else {
                                    path8[pathindex8 + (m >> 1)] = (byte) ((path8[pathindex8 + (m >> 1)] & mask[m & 1]) |
                                            tmp2 << shift[m & 1]);
                                }
                                tmp2 = 0;
                                tmp2 = (int)(v3[3])<<3 | (int)(v3[2])<<2 | ((int)v3[1]<<1) | (int)(v3[0]);
                                if ((pathindex8 + (altm >> 1)) > path8.length) {
                                    // ERROR
                                    Log.e("EMBED", "Array out of bound on: (pathindex8 + (altm >> 1)) > path8.length ");
                                }
                                else {
                                    path8[pathindex8 + (altm >> 1)] = (byte) ((path8[pathindex8 + (altm >> 1)] & mask[altm & 1]) |
                                            tmp2 << shift[altm & 1]);
                                }
                            }
                        }
                    }

                    for ( i = 0; i < sseheight; i++ ) {
                        ssedone.clear(i); //[i] = 0;
                    }

                    pathindex += parts;
                    pathindex8 += parts << 2;
                }

                if ( !syndrome.get(index2) )
                {
                    for ( i = 0, l = 0; i < sseheight; i += 2, l += 4 ) {
                        float [] tmp;
                        float[] v1 = new float[4], v2 = new float[4];
                        System.arraycopy(prices, (i << 2), v1, 0, 4);
                        System.arraycopy(prices, (i + 1)<<2, v2, 0, 4);

                        tmp = shuffle(v1, v2, 0x88);
                        System.arraycopy(tmp, 0, prices, l, 4);
                        //_mm_store_ps( &prices[l], _mm_shuffle_ps(_mm_load_ps(&prices[i << 2]), _mm_load_ps(&prices[(i + 1) << 2]), 0x88) );
                    }
                }
                else
                {
                    for ( i = 0, l = 0; i < sseheight; i += 2, l += 4 )
                    {
                        float [] tmp;
                        float [] v1 = new float[4], v2 = new float[4];
                        System.arraycopy(prices, (i << 2), v1, 0, 4);
                        System.arraycopy(prices, (i + 1) << 2, v2, 0, 4);

                        tmp = shuffle(v1, v2, 0xdd);
                        System.arraycopy(tmp, 0, prices, l, 4);

                        //_mm_store_ps( &prices[l], _mm_shuffle_ps(_mm_load_ps(&prices[i << 2]), _mm_load_ps(&prices[(i + 1) << 2]), 0xdd) );
                    }
                }

                if ( syndromelength - index2 <= matrixheight ) colmask >>= 1;

                {
                    //register __m128 fillval = _mm_set1_ps( inf );
                    for ( l >>= 2; l < sseheight; l++ ) {
                        prices[(l << 2)] = inf;
                        prices[(l << 2)+1] = inf;
                        prices[(l << 2)+2] = inf;
                        prices[(l << 2)+3] = inf;
                        //_mm_store_ps( &prices[l << 2], fillval );
                    }
                }
            }

            totalprice = prices[0];

            if ( totalprice >= total ) {
                Log.e("Error", "No solution exist.");
            }
        }
        else
        {
        /*
         SSE UINT8 VERSION
         */
            /*
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

            totalprice = subprice + prices[0]; */
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
                        state = (state << 1) | (syndrome.get(index2) ? 1 : 0);
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
