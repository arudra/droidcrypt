package com.droidcrypt.embedder;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by arudra on 24/02/15.
 */
public class stc_ml_c
{
    private static float F_INF = Float.POSITIVE_INFINITY;
    private static double D_INF = Double.POSITIVE_INFINITY;

    float stc_pm1_pls_embed( Integer cover_length, byte[] cover, float[] costs, int message_length, BitSet message, // input variables
                             int stc_constraint_height, float wet_cost, // other input parameters
                             int[] stego, int[] num_msg_bits, int[] max_trials, float[] coding_loss ) { // output variables

        return stc_pm1_dls_embed( cover_length, cover, costs, message_length, message, F_INF, stc_constraint_height, 0, wet_cost, stego,
                num_msg_bits, max_trials, coding_loss );
    }

    // distortion limited case - returns distortion
    float stc_pm1_dls_embed( Integer cover_length, byte[] cover, float[] costs, int message_length, BitSet message, float target_distortion, // input variables
                             int stc_constraint_height, float expected_coding_loss, float wet_cost, // other input parameters
                             int[] stego, int[] num_msg_bits, int[] max_trials, float[] coding_loss ) { // output variables

        check_costs( cover_length, 3, costs );
        float dist;

        int[] stego_values = new int[4 * cover_length];
        float[] costs_ml2 = new float[4 * cover_length];
        for ( int i = 0; i < cover_length; i++ ) {
            int pixel = cover[i]&0xFF;
            costs_ml2[4 * i + (pixel - 1 + 4) % 4 ] = costs[3 * i + 0]; // set cost of changing by -1
            stego_values[4 * i + (pixel - 1 + 4) % 4] = pixel - 1;
            costs_ml2[4 * i + (pixel + 0 + 4) % 4] = costs[3 * i + 1]; // set cost of changing by 0
            stego_values[4 * i + (pixel + 0 + 4) % 4] = pixel;
            costs_ml2[4 * i + (pixel + 1 + 4) % 4] = costs[3 * i + 2]; // set cost of changing by +1
            stego_values[4 * i + (pixel + 1 + 4) % 4] = pixel + 1;
            costs_ml2[4 * i + (pixel + 2 + 4) % 4] = wet_cost; // set cost of changing by +2
            stego_values[4 * i + (pixel + 2 + 4) % 4] = pixel + 2;
        }

        cover = null;
        // run general 2 layered embedding in distortion limited regime
        dist = stc_ml2_embed( cover_length, costs_ml2, stego_values, message_length, message, target_distortion, stc_constraint_height,
                expected_coding_loss, stego, num_msg_bits, max_trials, coding_loss );

        return dist;
    }


    float stc_ml2_embed( Integer cover_length, float[] costs, int[] stego_values, int message_length, BitSet message, float target_distortion, // input variables
                         int stc_constraint_height, float expected_coding_loss, // other input parameters
                         int[] stego, int[] num_msg_bits, int[] max_trials, float[] coding_loss ) { // output and optional variables

        float distortion, dist_coding_loss, lambda=0, m_max=0;
        int m_actual=0;
        int n = cover_length + 4 - (cover_length % 4); // cover length rounded to multiple of 4

        check_costs( cover_length, 4, costs );
        // if only binary embedding is sufficient, then use only 1st LSB layer
        boolean lsb1_only = true;
        for ( int i = 0; i < cover_length; i++ ) {
            int n_finite_costs = 0; // number of finite cost values
            int lsb_xor = 0;
            for ( int k = 0; k < 4; k++ )
                if ( costs[4 * i + k] != F_INF ) {
                    n_finite_costs++;
                    lsb_xor ^= (k % 2);
                }
            lsb1_only &= ((n_finite_costs <= 2) & (lsb_xor == 1));
        }
        if ( lsb1_only ) { // use stc_ml1_embed method
            distortion = 0;
            int[]cover = new int[cover_length];
            short[] direction = new short[cover_length];
            float[]costs_ml1 = new float[cover_length];
            for ( int i = 0; i < cover_length; i++ ) { // normalize such that minimal element is 0 - this helps numerical stability
                int min_id = 0;
                float f_min = F_INF;
                for ( int j = 0; j < 4; j++ )
                    if ( f_min > costs[4 * i + j] ) {
                        f_min = costs[4 * i + j]; // minimum value
                        min_id = j; // index of the minimal entry
                    }
                costs_ml1[i] = F_INF;
                cover[i] = stego_values[4 * i + min_id];
                for ( int j = 0; j < 4; j++ )
                    if ( (costs[4 * i + j] != F_INF) && (min_id != j) ) {
                        distortion += f_min;
                        costs_ml1[i] = costs[4 * i + j] - f_min;
                        direction[i] = (short)(stego_values[4 * i + j] - cover[i]);
                    }
            }

            distortion += stc_ml1_embed( cover_length, cover, direction, costs_ml1, message_length, message, target_distortion,
                    stc_constraint_height, expected_coding_loss, stego, num_msg_bits, max_trials, coding_loss );

            return distortion;
        }

        // copy and transpose data for faster reading via SSE instructions
        float[] c =  new float[ 4 * n];
        for(int i = 0 ; i < 4*n; i++)
        {
            c[i] = F_INF;
        }
        for(int j = 0; j < n; j++)
        {
            c[j] = 0.0f;
        }

        //std::fill_n( c, 4 * n, F_INF );
        //std::fill_n( c, n, 0.0f );

        for ( int i = 0; i < 4 * cover_length; i++ )
            c[n * (i % 4) + i / 4] = costs[i];
        // write_matrix_to_file<float>(n, 4, c, "cost_ml2.txt");
        for ( int i = 0; i < n; i++ ) { // normalize such that minimal element is 0 - this helps numerical stability
            float f_min = F_INF;
            for ( int j = 0; j < 4; j++ )
                f_min = Math.min(f_min, c[j * n + i]);
            for ( int j = 0; j < 4; j++ )
                c[j * n + i] -= f_min;
        }

        if ( target_distortion != F_INF ) {
            lambda = get_lambda_distortion( n, 4, c, target_distortion, 2 , 0, 0);
            m_max = (1 - expected_coding_loss) * calc_entropy( n, 4, c, lambda );
            m_actual = Math.min( message_length, (int) Math.floor( m_max ) );
        }
        if ( (target_distortion == F_INF) || (m_actual < Math.floor( m_max )) ) {
            m_actual = Math.min( 2 * cover_length, message_length );
            lambda = get_lambda_entropy( n, 4, c, (float)m_actual);
        }
    /*
     p = exp(-lambda*costs);
     p = p./(ones(4,1)*sum(p));
     */
//        float[] p = new float[4*n]; //align_new< float > ( 4 * n, 16 );
        /*
        __m128 v_lambda = _mm_set1_ps( -lambda );
        for ( int i = 0; i < n / 4; i++ ) {
            __m128 sum = _mm_setzero_ps();
            for ( int j = 0; j < 4; j++ ) {
                __m128 x = _mm_load_ps( c + j * n + 4 * i );
                x = exp_ps( _mm_mul_ps( v_lambda, x ) );
                _mm_store_ps( p + j * n + 4 * i, x );
                sum = _mm_add_ps( sum, x );
            }
            for ( int j = 0; j < 4; j++ ) {
                __m128 x = _mm_load_ps( p + j * n + 4 * i );
                x = _mm_div_ps( x, sum );
                _mm_store_ps( p + j * n + 4 * i, x );
            }
        }
        */
        // this is for debugging purposes
        // float payload_dbg = entropy_array(4*n, p);


        int bs = 4; // block size
        float[] p = new float [bs * n];
        float[] v_lambda = new float[bs];
        //initializeZero(v_lambda, 4);

        for ( int i = 0; i < n / bs; i++ ) {
            //__m128 sum = _mm_setzero_ps();
            float [] sum = new float[4];
            float [] x = new float[4];
            for(int k=0; k<bs; k++) {
                sum[k] = 0;
            }
            for ( int j = 0; j < bs; j++ ) {

                //__m128 x = _mm_load_ps( c + j * n + 4 * i );

                for (int k=0; k<bs; k++) {
                    x[k] = c[j * n + bs * i + k];
                }
                //x = exp_ps( _mm_mul_ps( v_lambda, x ) );
                //sum = _mm_add_ps( sum, x );
                for (int k=0; k<bs; k++) {
                    x[k] = (float)Math.pow(2, -lambda*x[k]);
                    sum[k] += x[k];
                }
                //_mm_store_ps( p + j * n + 4 * i, x );
                for (int k=0; k<bs; k++) {
                    p[j * n + bs * i + k] = x[k];
                }
            }
            for ( int j = 0; j < bs; j++ ) {
                //__m128 x = _mm_load_ps( p + j * n + 4 * i );
                for (int k=0; k<bs; k++) {
                    x[k] = p[j * n + bs * i + k];
                }
                //x = _mm_div_ps( x, sum );
                for (int k=0; k<bs; k++) {
                    x[k] = x[k]/sum[k];
                }

                //_mm_store_ps( p + j * n + 4 * i, x );
                for (int k=0; k<bs; k++) {
                    p[j * n + 4 * i + k] = x[k];
                }

            }
        }


        int trial = 0;
        float[] p10 = new float[cover_length];
        float[] p20 = new float[cover_length];
        BitSet stego1 = new BitSet(cover_length);
        BitSet stego2 = new BitSet(cover_length);
        int[]perm1 = new int[cover_length];
        int[]perm2 = new int[cover_length];

    /* LAYER OF 2ND LSBs */
        for ( int i = 0; i < cover_length; i++ )
            p20[i] = p[i] + p[i + n]; // p20 = p(1,:)+p(2,:);         % probability of 2nd LSB of stego equal 0
        num_msg_bits[1] = (int) Math.floor( binary_entropy_array( cover_length, p20 ) ); // msg_bits(2) = floor(sum(binary_entropy(p20)));    % number of msg bits embedded into 2nd LSBs
        try {
            stc_embed_trial( cover_length, p20, message, stc_constraint_height, num_msg_bits[1], perm2, stego2, trial, max_trials[0], "cost2.txt" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    /* LAYER OF 1ST LSBs */
        for ( int i = 0; i < cover_length; i++ ) //
            if ( !stego2.get(perm2[i]) ) // % conditional probability of 1st LSB of stego equal 0 given LSB2=0
                p10[i] = p[i] / (p[i] + p[i + n]); // p10(i) = p(1,i)/(p(1,i)+p(2,i));
            else // % conditional probability of 1st LSB of stego equal 0 given LSB2=1
                p10[i] = p[i + 2 * n] / (p[i + 2 * n] + p[i + 3 * n]); // p10(i) = p(3,i)/(p(3,i)+p(4,i));
        num_msg_bits[0] = m_actual - num_msg_bits[1]; // msg_bits(1) = m_actual-msg_bits(2); % number of msg bits embedded into 1st LSBs
        try {
            stc_embed_trial( cover_length, p10, message.get(num_msg_bits[1], message.length() - 1), stc_constraint_height, num_msg_bits[0], perm1, stego1, trial,
                    max_trials[0], "cost1.txt" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }


    /* FINAL CALCULATIONS */
        distortion = 0;
        for ( int i = 0; i < cover_length; i++ ) {
            stego[i] = stego_values[4 * i + 2 * stego2.toByteArray()[perm2[i]] + stego1.toByteArray()[perm1[i]]];
            distortion += costs[4 * i + 2 * stego2.toByteArray()[perm2[i]] + stego1.toByteArray()[perm1[i]]];
        }
        if ( coding_loss[0] != 0 ) {
            dist_coding_loss = 0;
            for ( int i = 0; i < cover_length; i++ )
                dist_coding_loss += c[i + n * (2 * stego2.toByteArray()[perm2[i]] + stego1.toByteArray()[perm1[i]])];
            float lambda_dist = get_lambda_distortion( n, 4, c, dist_coding_loss, lambda, 0, 20 ); // use 20 iterations to make lambda_dist precise
            float max_payload = calc_entropy( n, 4, c, lambda_dist );
            (coding_loss[0]) = (max_payload - m_actual) / max_payload; // fraction of max_payload lost due to practical coding scheme
        }
        max_trials[0] = trial;

        return distortion;
    }

    // SANITY CHECKS for cost arrays
    void check_costs( Integer n, int k, float[] costs ) {

        boolean test_nan, test_non_inf, test_minus_inf;
        for ( int i = 0; i < n; i++ ) {
            test_nan = false; // Is any element NaN? Should be FALSE
            test_non_inf = false; // Is any element finite? Should be TRUE
            test_minus_inf = false; // Is any element minus Inf? should be FALSE
            for ( int j = 0; j < k; j++ ) {
                test_nan |= (costs[k * i + j] != costs[k * i + j]);
                test_non_inf |= ((costs[k * i + j] != -F_INF) & (costs[k * i + j] != F_INF));
                test_minus_inf |= (costs[k * i + j] == -F_INF);
            }

            if ( test_nan ) {
                String s = "Incorrect cost array." + i + "-th element contains NaN value. This is not a valid cost.";
                Log.d("EMBED",s);
//                throw new Exception(s);
            }
            if ( !test_non_inf ) {
                String s =  "Incorrect cost array." + i + "-th element does not contain any finite cost value. This is not a valid cost.";
                Log.d("EMBED",s);
//                throw stc_exception( ss.str(), 6 );
            }
            if ( test_minus_inf ) {
                String s =  "Incorrect cost array." + i + "-th element contains -Inf value. This is not a valid cost.";
                Log.d("EMBED",s);
//                throw stc_exception( ss.str(), 6 );
            }
        }
    }

    float get_lambda_distortion( int n, int k, float[] costs, float distortion, float initial_lambda, float precision, int iter_limit) {

        float dist1, dist2, dist3, lambda1, lambda2, lambda3;
        int j = 0;
        int iterations = 0;
        initial_lambda = 10;
        if(precision==0) precision = (float)1e-3;
        if(iter_limit==0) iter_limit = 30;

        lambda1 = 0;
        dist1 = calc_distortion( n, k, costs, lambda1 );
        lambda3 = initial_lambda;
        dist2 = F_INF; // this is just an initial value
        lambda2 = initial_lambda;
        dist3 = distortion + 1;
        while ( dist3 > distortion ) {
            lambda3 *= 2;
            dist3 = calc_distortion( n, k, costs, lambda3 );
            j++;
            iterations++;
            // beta is probably unbounded => it seems that we cannot find beta such that
            // relative payload will be smaller than requested. Binary search cannot converge.
            if ( j > 10 ) {
                return lambda3;
            }
        }
        while ( (Math.abs( dist2 - distortion ) / n > precision) && (iterations < iter_limit) ) { // binary search for parameter lambda
            lambda2 = lambda1 + (lambda3 - lambda1) / 2;
            dist2 = calc_distortion( n, k, costs, lambda2 );
            if ( dist2 < distortion ) {
                lambda3 = lambda2;
                dist3 = dist2;
            } else {
                lambda1 = lambda2;
                dist1 = dist2;
            }
            iterations++; // this is for monitoring the number of iterations
        }
        return lambda1 + (lambda3 - lambda1) / 2;
    }

    float calc_distortion( int n, int k, float[] costs, float lambda )
    {
        int b=4;
        float [] eps, v_lambda, z, d, rho, p, dist;
        float sum=0;
        eps = new float[4];
        v_lambda = new float[4];
        z = new float[4];
        d = new float[4];
        rho = new float[4];
        p = new float[4];
        dist = new float[4];
        for (int l=0; l<b; l++) {
            eps[l] = Math.ulp(1.0f);
            v_lambda[l] = -lambda;
            dist[l] = 0;
        }
        for (int i=0; i<n/b; i++) {
            for (int l=0; l<b; l++) {
                z[l] = 0;
                d[l] = 0;
            }
            for (int j=0; j<k; j++) {
                for (int l=0; l<b; l++) {
                    rho[l] = costs[j * n + 4 * i + k];
                    p[l] = (float)Math.pow(2, v_lambda[l]*rho[l]);
                    z[l] += p[l];
                    p[l] = p[l]*rho[l];
                    if (p[l] >= eps[l]) {
                        d[l] += p[l];
                    }
                }
            }
            for (int l=0; l<b; l++) {
                dist[l] += (d[l]*z[l]);
            }
        }
        for (int l=0; l<b;l++) sum += dist[l];
        return sum;

        /*
        __m128 eps = _mm_set1_ps( Math.ulp(1.0) ); //std::numeric_limits< float >::epsilon() );
        __m128 v_lambda = _mm_set1_ps( -lambda );
        __m128 z, d, rho, p, dist, mask;

        dist = _mm_setzero_ps();
        for ( int i = 0; i < n / 4; i++ ) { // n must be multiple of 4
            z = _mm_setzero_ps();
            d = _mm_setzero_ps();
            for ( int j = 0; j < k; j++ ) {
                rho = _mm_load_ps( costs + j * n + 4 * i ); // costs array must be aligned in memory
                p = exp_ps( _mm_mul_ps( v_lambda, rho ) );
                z = _mm_add_ps( z, p );
                mask = _mm_cmplt_ps( p, eps ); // if p<eps, then do not accumulate it to d since x*exp(-x) tends to zero
                p = _mm_mul_ps( rho, p );
                p = _mm_andnot_ps( mask, p );
                d = _mm_add_ps( d, p );
            }
            dist = _mm_add_ps( dist, _mm_div_ps( d, z ) );
        }
        return sum_inplace( dist ); */
    }

    float calc_entropy( int n, int k, float[] costs, float lambda ) {


        int b=4;
        final float LOG2 = (float) Math.log( 2.0f );
        float inf = Float.POSITIVE_INFINITY;
        float [] z, d, rho, p, entr;
        float sum=0;
        z = new float[4];
        d = new float[4];
        rho = new float[4];
        p = new float[4];
        entr = new float[4];

        for (int l=0; l<b; l++) {
            entr[l] = 0;
        }
        for (int i=0; i<n/b; i++) {
            for (int l=0; l<b; l++) {
                z[l] = 0;
                d[l] = 0;
            }
            for (int j=0; j<k; j++) {
                for (int l=0; l<b; l++) {
                    rho[l] = costs[j * n + 4 * i + l];
                    p[l] = (float) Math.pow(2, -lambda*rho[l]);
                    z[l] += p[l];
                    p[l] = p[l]*rho[l];
                    if (rho[l] != inf) {
                        d[l] += p[l];
                    }
                }
            }
            for (int l=0; l<b; l++) {
                entr[l] = entr[l] - (-lambda*d[l]/z[l]);
                entr[l] += (float) (Math.log(z[l])/Math.log(2));
            }
        }
        for (int l=0; l<b;l++) {
            sum += entr[l];
        }
        return sum/LOG2;

//        entr = _mm_setzero_ps();
//        for ( int i = 0; i < n / 4; i++ ) {
//            z = _mm_setzero_ps();
//            d = _mm_setzero_ps();
//            for ( int j = 0; j < k; j++ ) {
//                rho = _mm_load_ps( costs + j * n + 4 * i ); // costs array must be aligned in memory
//                p = exp_ps( _mm_mul_ps( v_lambda, rho ) );
//                z = _mm_add_ps( z, p );
//
//                mask = _mm_cmpeq_ps( rho, inf ); // if p<eps, then do not accumulate it to d since x*exp(-x) tends to zero
//                p = _mm_mul_ps( rho, p );
//                p = _mm_andnot_ps( mask, p ); // apply mask
//                d = _mm_add_ps( d, p );
//            }
//            entr = _mm_sub_ps( entr, _mm_div_ps( _mm_mul_ps( v_lambda, d ), z ) );
//            entr = _mm_add_ps( entr, log_ps( z ) );
//        }
//        return sum_inplace( entr ) / LOG2;
    }

    // binary entropy function in single precision
    public static float bin_entropyf(float x) {

        float LOG2 = (float) Math.log(2.0f);
        float EPS = (float)Math.ulp(1.0);
        float z;

        if ((x<EPS) || ((1-x)<EPS)) {
        return 0;
        } else {
            z = (float)(-x*Math.log(x)-(1-x)*Math.log(1-x))/LOG2;
            return z;
        }
    }

    float get_lambda_entropy( int n, int k, float[] costs, float payload) {

        float p1, p2, p3, lambda1, lambda2, lambda3;
        int j = 0;
        int iterations = 0;
        float initial_lambda = 10;

        lambda1 = 0;
        p1 = (float)(n * Math.log( (float)k ) / Math.log( 2.0f ));
        lambda3 = initial_lambda;
        p3 = payload + 1; // this is just an initial value
        lambda2 = initial_lambda;
        while ( p3 > payload ) {
            lambda3 *= 2;
            p3 = calc_entropy( n, k, costs, lambda3 );
            j++;
            iterations++;
            // beta is probably unbounded => it seems that we cannot find beta such that
            // relative payload will be smaller than requested. Binary search does not make sence here.
            if ( j > 10 ) {
                return lambda3;
            }
        }
        while ( (p1 - p3) / n > payload / n * 1e-2 ) { // binary search for parameter lambda
            lambda2 = lambda1 + (lambda3 - lambda1) / 2;
            p2 = calc_entropy( n, k, costs, lambda2 );
            if ( p2 < payload ) {
                lambda3 = lambda2;
                p3 = p2;
            } else {
                lambda1 = lambda2;
                p1 = p2;
            }
            iterations++; // this is for monitoring the number of iterations
        }
        return lambda1 + (lambda3 - lambda1) / 2;
    }

    float binary_entropy_array( int n, float[] prob ) {

        float h = 0;
        float LOG2 = (float)Math.log( 2.0f );
        float EPS = (float)Math.ulp(1.0); //std::numeric_limits< float >::epsilon();

        for (int i = 0; i < n; i++ )
            if ( (prob[i] > EPS) && (1 - prob[i] > EPS) ) h -= prob[i] * Math.log(prob[i]) + (1 - prob[i]) * Math.log(1 - prob[i]);

        return h / LOG2;
    }

    void stc_embed_trial( int n, float[] cover_bit_prob0, BitSet message, int stc_constraint_height, int num_msg_bits, int[] perm, BitSet stego,
                          int trial, int max_trials, String debugging_file ) {

        if(debugging_file=="") debugging_file= "cost.txt";
        boolean success = false;
        byte[] cover = new byte[n];
        double[] cost = new double[n];
        while ( !success ) {
            randperm( n, num_msg_bits, perm );
            for ( int i = 0; i < n; i++ ) {
                cover[perm[i]] = (byte)((cover_bit_prob0[i] < 0.5) ? 1 : 0);
                cost[perm[i]] = -Math.log((1 / Math.max(cover_bit_prob0[i], 1 - cover_bit_prob0[i])) - 1);
                if ( cost[perm[i]] != cost[perm[i]] ) // if p20[i]>1 due to numerical error (this is possible due to float data type)
                    cost[perm[i]] = D_INF; // then cost2[i] is NaN, it should be Inf
            }
            //stego = Arrays.copyOf(cover, n);// initialize stego array by cover array

            for(int i = 0; i < n; i++)
            {
                if(cover[i] == 0)
                    stego.clear(i);
                else
                    stego.set(i);
            }

            // debugging
            // write_vector_to_file<double>(n, cost, debugging_file);
            stc_embed_c stcEmbedC = new stc_embed_c();
            try {
                if ( num_msg_bits != 0 ) stcEmbedC.stc_embed(cover, n, message, num_msg_bits, cost, true, stego, stc_constraint_height);
                success = true;
            } catch ( Exception e ) {
                e.printStackTrace();
                num_msg_bits--; // by decreasing the number of  bits, we change the permutation used to shuffle the bits
                trial++;
                if ( trial > max_trials ) {
                    Log.d("Trials","Maximum number of trials in layered construction exceeded.");
                }
            }
        }
    }


    /* Generates random permutation of length n based on the MT random number generator with seed 'seed'. */
    void randperm( int n, int seed, int[] perm )
    {

        /*boost::mt19937 *generator = new boost::mt19937( seed );
        boost::variate_generator< boost::mt19937, boost::uniform_int< > > *randi = new boost::variate_generator< boost::mt19937,
                boost::uniform_int< > >( *generator, boost::uniform_int< >( 0, INT_MAX ) ); */
        Random rn = new Random();
        rn.setSeed(seed);

        // generate random permutation - this is used to shuffle cover pixels to randomize the effect of different neighboring pixels
        for ( int i = 0; i < n; i++ )
            perm[i] = i;
        for ( int i = 0; i < n; i++ ) {
            int j = rn.nextInt(32768) % (n-i); //(*randi)() % (n - i);
            int tmp = perm[i];
            perm[i] = perm[i + j];
            perm[i + j] = tmp;
        }
    }

    // algorithm for embedding into 1 layer, both payload- and distortion-limited case
    float stc_ml1_embed( int cover_length, int[] cover, short[] direction, float[] costs, int message_length, BitSet message,
                         float target_distortion,// input variables
                         int stc_constraint_height, float expected_coding_loss, // other input parameters
                         int[] stego, int[] num_msg_bits, int[] max_trials, float[] coding_loss ) { // output variables

        float distortion=0, lambda=0, m_max=0;
        boolean success = false;
        int m_actual=0;
        int n = cover_length + 4 - (cover_length % 4); // cover length rounded to multiple of 4
        int[] perm1 = new int[n];

        float[] c = new float[2*n]; //align_new< float > ( 2 * n, 16 );
        //std::fill_n( c, 2 * n, F_INF );
        //std::fill_n( c, n, 0.0f );

        for(int i = 0 ; i < 2*n; i++)
        {
            c[i] = F_INF;
        }
        for(int j = 0; j < n; j++)
        {
            c[j] = 0.0f;
        }


        for ( int i = 0; i < cover_length; i++ ) { // copy and transpose data for better reading via SSE instructions
            c[ (cover[i] % 2) * n + i] = 0; // cost of not changing the element
            c[( (cover[i] + 1) % 2 ) * n + i] = costs[i]; // cost of changing the element
        }

        if ( target_distortion != F_INF ) { // distortion-limited sender
            lambda = get_lambda_distortion( n, 2, c, target_distortion, 2 , 0, 0); //
            m_max = (1 - expected_coding_loss) * calc_entropy( n, 2, c, lambda ); //
            m_actual = Math.min(message_length, (int) Math.floor(m_max)); //
        }
        if ( (target_distortion == F_INF) || (m_actual < Math.floor(m_max)) ) { // payload-limited sender
            m_actual = Math.min(cover_length, message_length); // or distortion-limited sender with
        }

    /* SINGLE LAYER OF 1ST LSBs */
        num_msg_bits[0] = m_actual;
        int trial = 0;
        byte[] cover1 = new byte[cover_length];
        double[] cost1 = new double[cover_length];
        BitSet stego1 = new BitSet(cover_length);
        while ( !success ) {
            randperm( cover_length, num_msg_bits[0], perm1 );
            for ( int i = 0; i < cover_length; i++ ) {
                cover1[perm1[i]] =  (byte)(cover[i] % 2);
                cost1[perm1[i]] = costs[i];
                if ( cost1[perm1[i]] != cost1[perm1[i]] ) cost1[perm1[i]] = D_INF;
            }
            //stego1 = Arrays.copyOf(cover1, cover_length); // initialize stego array by cover array
            for(int i = 0; i < cover_length; i++)
            {
                if(cover1[i] == 0)
                    stego1.clear(i);
                else
                    stego1.set(i);
            }

            // debugging
            // write_vector_to_file<double>(n, cost, debugging_file);
            stc_embed_c stcEmbedC = new stc_embed_c();
            try {
                if ( num_msg_bits[0] != 0 ) stcEmbedC.stc_embed(cover1, cover_length, message, num_msg_bits[0], cost1, true, stego1,
                        stc_constraint_height);
                success = true;
            } catch ( Exception e ) {
                e.printStackTrace();
                num_msg_bits[0]--; // by decreasing the number of  bits, we change the permutation used to shuffle the bits
                trial++;
                if ( trial > max_trials[0] ) {
                    Log.d( "Trial", "Maximum number of trials in layered construction exceeded.");
                }
            }
        }

    /* FINAL CALCULATIONS */
        distortion = 0;
        for ( int i = 0; i < cover_length; i++ ) {
            stego[i] = ((stego1.get(perm1[i]) ? 1 : 0) == cover1[perm1[i]]) ? cover[i] : cover[i] + direction[i];
            distortion += ((stego1.get(perm1[i]) ? 1 : 0) == cover1[perm1[i]]) ? 0 : costs[i];
        }
        if ( coding_loss[0] != 0 )
        {
            float lambda_dist = get_lambda_distortion( n, 2, c, distortion, lambda, 0, 20 ); // use 20 iterations to make lambda_dist precise
            float max_payload = calc_entropy( n, 2, c, lambda_dist );
            coding_loss[0] = (max_payload - m_actual) / max_payload; // fraction of max_payload lost due to practical coding scheme
        }
        max_trials[0] = trial;

        return distortion;
    }

    // EXTRACTION ALGORITHMS **********************************************************************************************************
    void stc_ml_extract( int stego_length, int[] stego, int num_of_layers, int[] num_msg_bits, // input variables
                         int stc_constraint_height, // other input parameters
                         BitSet message ) { // output variables

        BitSet stego_bits = new BitSet(stego_length);
        BitSet msg_ptr = message;
        int [] perm = new int[stego_length];

        stc_extract_c stcExtractC = new stc_extract_c();
        for ( int l = num_of_layers; l > 0; l-- ) { // extract message from every layer starting from most significant ones
            // extract bits from l-th LSB plane
            if ( num_msg_bits[l - 1] > 0 ) {
                randperm( stego_length, num_msg_bits[l - 1], perm );
                for ( int i = 0; i < stego_length; i++ ) {
                    stego_bits.toByteArray()[perm[i]] = (byte)((stego[i] % (1 << l)) >> (l - 1));
                }
                stcExtractC.stc_extract(stego_bits, stego_length, msg_ptr, num_msg_bits[l - 1], stc_constraint_height);
                //msg_ptr += num_msg_bits[l - 1];
                msg_ptr = msg_ptr.get(num_msg_bits[l-1], msg_ptr.length());
            }
        }

    }

}
