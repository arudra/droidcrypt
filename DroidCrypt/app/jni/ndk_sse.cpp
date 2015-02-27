//
//  ndk_sse.c
//  Hugo
//
//  Created by Dhaval Miyani on 2015-02-24.
//  Copyright (c) 2015 Dhaval Miyani. All rights reserved.
//

#include <stdio.h>
#include "com_droidcrypt_embedder_NativeLib.h"
#include "sse_mathfun.h"

JNIEXPORT jstring JNICALL Java_com_marakana_NativeLib_hello
(JNIEnv * env, jobject obj) {
    return (*env)->NewStringUTF(env, "Hello World!");
}

JNIEXPORT jint JNICALL Java_com_marakana_NativeLib_add
(JNIEnv * env, jobject obj, jint value1, jint value2) {
    return (value1 + value2);
}

JNIEXPORT jfloat JNICALL Java_com_droidcrypt_embedder_NativeLib_calc_1distortion
(JNIEnv * env, jobject obj, jint n, jint k, jfloatArray costs, jfloat lambda) {
    __m128 eps = _mm_set1_ps( std::numeric_limits< float >::epsilon() );
    __m128 v_lambda = _mm_set1_ps( -lambda );
    __m128 z, d, rho, p, dist, mask;
    
    dist = _mm_setzero_ps();
    for ( uint i = 0; i < n / 4; i++ ) { // n must be multiple of 4
        z = _mm_setzero_ps();
        d = _mm_setzero_ps();
        for ( uint j = 0; j < k; j++ ) {
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
    return sum_inplace( dist );

}

JNIEXPORT jfloat JNICALL Java_com_droidcrypt_embedder_NativeLib_calc_1entropy
(JNIEnv * env, jobject obj, jint n, jint k, jfloatArray costs, jfloat lambda) {
    float const LOG2 = log( 2.0f );
    __m128 inf = _mm_set1_ps( F_INF );
    __m128 v_lambda = _mm_set1_ps( -lambda );
    __m128 z, d, rho, p, entr, mask;
    
    entr = _mm_setzero_ps();
    for ( uint i = 0; i < n / 4; i++ ) {
        z = _mm_setzero_ps();
        d = _mm_setzero_ps();
        for ( uint j = 0; j < k; j++ ) {
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
    return sum_inplace( entr ) / LOG2;
}


JNIEXPORT jfloat JNICALL Java_com_droidcrypt_embedder_NativeLib_calc_1m1_1embed
(JNIEnv * env, jobject obj, jint n, jint k, jfloatArray c, jfloat lambda) {
    float* p = align_new< float > ( 4 * n, 16 );
    __m128 v_lambda = _mm_set1_ps( -lambda );
    for ( uint i = 0; i < n / 4; i++ ) {
        __m128 sum = _mm_setzero_ps();
        for ( uint j = 0; j < 4; j++ ) {
            __m128 x = _mm_load_ps( c + j * n + 4 * i );
            x = exp_ps( _mm_mul_ps( v_lambda, x ) );
            _mm_store_ps( p + j * n + 4 * i, x );
            sum = _mm_add_ps( sum, x );
        }
        for ( uint j = 0; j < 4; j++ ) {
            __m128 x = _mm_load_ps( p + j * n + 4 * i );
            x = _mm_div_ps( x, sum );
            _mm_store_ps( p + j * n + 4 * i, x );
        }
    
}
