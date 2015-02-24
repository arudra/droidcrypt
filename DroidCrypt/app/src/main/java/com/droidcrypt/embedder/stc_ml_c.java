package com.droidcrypt.embedder;

import java.util.BitSet;

/**
 * Created by arudra on 24/02/15.
 */
public class stc_ml_c
{
    private static float F_INF = Float.POSITIVE_INFINITY;

    float stc_pm1_pls_embed( Integer cover_length, int[] cover, float[] costs, int message_length, BitSet message, // input variables
                             int stc_constraint_height, float wet_cost, // other input parameters
                             int[] stego, int[] num_msg_bits, int[] max_trials, float[] coding_loss ) { // output variables

        return stc_pm1_dls_embed( cover_length, cover, costs, message_length, message, F_INF, stc_constraint_height, 0, wet_cost, stego,
                num_msg_bits, max_trials, coding_loss );
    }

    // distortion limited case - returns distortion
    float stc_pm1_dls_embed( Integer cover_length, int[] cover, float[] costs, int message_length, BitSet message, float target_distortion, // input variables
                             int stc_constraint_height, float expected_coding_loss, float wet_cost, // other input parameters
                             int[] stego, int[] num_msg_bits, int[] max_trials, float[] coding_loss ) { // output variables

        check_costs( cover_length, 3, costs );
        float dist = 0;

        int[] stego_values = new int[4 * cover_length];
        float[] costs_ml2 = new float[4 * cover_length];
        for ( Integer i = 0; i < cover_length; i++ ) {
            costs_ml2[4 * i + (cover[i] - 1 + 4) % 4 ] = costs[3 * i + 0]; // set cost of changing by -1
            stego_values[4 * i + (cover[i] - 1 + 4) % 4] = cover[i] - 1;
            costs_ml2[4 * i + (cover[i] + 0 + 4) % 4] = costs[3 * i + 1]; // set cost of changing by 0
            stego_values[4 * i + (cover[i] + 0 + 4) % 4] = cover[i];
            costs_ml2[4 * i + (cover[i] + 1 + 4) % 4] = costs[3 * i + 2]; // set cost of changing by +1
            stego_values[4 * i + (cover[i] + 1 + 4) % 4] = cover[i] + 1;
            costs_ml2[4 * i + (cover[i] + 2 + 4) % 4] = wet_cost; // set cost of changing by +2
            stego_values[4 * i + (cover[i] + 2 + 4) % 4] = cover[i] + 2;
        }

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
        //TODO: FIX THIS
        float[] p = new float[4*n]; //align_new< float > ( 4 * n, 16 );
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
        } */
        // this is for debugging purposes
        // float payload_dbg = entropy_array(4*n, p);

        int trial = 0;
        float[] p10 = new float[cover_length];
        float[] p20 = new float[cover_length];
        byte[] stego1 = new byte[cover_length];
        byte[] stego2 = new byte[cover_length];
        int[]perm1 = new int[cover_length];
        int[]perm2 = new int[cover_length];

    /* LAYER OF 2ND LSBs */
        for ( int i = 0; i < cover_length; i++ )
            p20[i] = p[i] + p[i + n]; // p20 = p(1,:)+p(2,:);         % probability of 2nd LSB of stego equal 0
        num_msg_bits[1] = (int) Math.floor( binary_entropy_array( cover_length, p20 ) ); // msg_bits(2) = floor(sum(binary_entropy(p20)));    % number of msg bits embedded into 2nd LSBs
        try {
            stc_embed_trial( cover_length, p20, message, stc_constraint_height, num_msg_bits[1], perm2, stego2, trial, max_trials, "cost2.txt" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    /* LAYER OF 1ST LSBs */
        for ( int i = 0; i < cover_length; i++ ) //
            if ( stego2[perm2[i]] == 0 ) // % conditional probability of 1st LSB of stego equal 0 given LSB2=0
                p10[i] = p[i] / (p[i] + p[i + n]); // p10(i) = p(1,i)/(p(1,i)+p(2,i));
            else // % conditional probability of 1st LSB of stego equal 0 given LSB2=1
                p10[i] = p[i + 2 * n] / (p[i + 2 * n] + p[i + 3 * n]); // p10(i) = p(3,i)/(p(3,i)+p(4,i));
        num_msg_bits[0] = m_actual - num_msg_bits[1]; // msg_bits(1) = m_actual-msg_bits(2); % number of msg bits embedded into 1st LSBs
        try {
            stc_embed_trial( cover_length, p10, message + num_msg_bits[1], stc_constraint_height, num_msg_bits[0], perm1, stego1, trial,
                    max_trials, "cost1.txt" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }


    /* FINAL CALCULATIONS */
        distortion = 0;
        for ( int i = 0; i < cover_length; i++ ) {
            stego[i] = stego_values[4 * i + 2 * stego2[perm2[i]] + stego1[perm1[i]]];
            distortion += costs[4 * i + 2 * stego2[perm2[i]] + stego1[perm1[i]]];
        }
        if ( coding_loss[0] != 0 ) {
            dist_coding_loss = 0;
            for ( int i = 0; i < cover_length; i++ )
                dist_coding_loss += c[i + n * (2 * stego2[perm2[i]] + stego1[perm1[i]])];
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
            /*
            if ( test_nan ) {
                std::stringstream ss;
                ss << "Incorrect cost array." << i << "-th element contains NaN value. This is not a valid cost.";
                throw stc_exception( ss.str(), 6 );
            }
            if ( !test_non_inf ) {
                std::stringstream ss;
                ss << "Incorrect cost array." << i << "-th element does not contain any finite cost value. This is not a valid cost.";
                throw stc_exception( ss.str(), 6 );
            }
            if ( test_minus_inf ) {
                std::stringstream ss;
                ss << "Incorrect cost array." << i << "-th element contains -Inf value. This is not a valid cost.";
                throw stc_exception( ss.str(), 6 );
            } */
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

    //TODO: FIX THIS
    float calc_distortion( int n, int k, float[] costs, float lambda )
    {
        /*
        __m128 eps = _mm_set1_ps( std::numeric_limits< float >::epsilon() );
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
        return 0;
    }

    //TODO: FIX THIS
    float calc_entropy( int n, int k, float[] costs, float lambda ) {

        float LOG2 = (float)Math.log( 2.0f );/*
        __m128 inf = _mm_set1_ps( F_INF );
        __m128 v_lambda = _mm_set1_ps( -lambda );
        __m128 z, d, rho, p, entr, mask;

        entr = _mm_setzero_ps();
        for ( int i = 0; i < n / 4; i++ ) {
            z = _mm_setzero_ps();
            d = _mm_setzero_ps();
            for ( int j = 0; j < k; j++ ) {
                rho = _mm_load_ps( costs + j * n + 4 * i ); // costs array must be aligned in memory
                p = exp_ps( _mm_mul_ps( v_lambda, rho ) );
                z = _mm_add_ps( z, p );

                mask = _mm_cmpeq_ps( rho, inf ); // if p<eps, then do not accumulate it to d since x*exp(-x) tends to zero
                p = _mm_mul_ps( rho, p );
                p = _mm_andnot_ps( mask, p ); // apply mask
                d = _mm_add_ps( d, p );
            }
            entr = _mm_sub_ps( entr, _mm_div_ps( _mm_mul_ps( v_lambda, d ), z ) );
            entr = _mm_add_ps( entr, log_ps( z ) );
        }
        return sum_inplace( entr ) / LOG2; */
        return 0;
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
}
