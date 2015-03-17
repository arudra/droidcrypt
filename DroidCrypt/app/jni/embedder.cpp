/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <time.h>
//#include <android/bitmap.h>
#include <android/log.h>
#define  LOG_TAG    "libembedder"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include "com_droidcrypt_Embedder.h" 
#include "HUGO_like.h"

/* Set to 1 to enable debug log traces. */
#define DEBUG 0

/* Set to 1 to optimize memory stores when generating plasma. */
#define OPTIMIZE_WRITES  1

jbyteArray as_byte_array(JNIEnv * env, unsigned char* buf, int len) {
    jbyteArray array = env->NewByteArray (len);
    env->SetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return array;
}

jintArray as_int_array(JNIEnv * env, int* buf, int len) {
    jintArray array = env->NewIntArray (len);
    env->SetIntArrayRegion (array, 0, len, reinterpret_cast<jint*>(buf));
    return array;
}

unsigned char* as_unsigned_char_array(JNIEnv * env, jbyteArray array) {
    int len = env->GetArrayLength (array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}

int* as_int_array(JNIEnv * env, jintArray array) {
    int len = env->GetArrayLength (array);
    int* buf = new int[len];
    env->GetIntArrayRegion (array, 0, len, reinterpret_cast<jint*>(buf));
    return buf;
}

JNIEXPORT jbyteArray JNICALL Java_com_droidcrypt_Embedder_embed
(JNIEnv * env, jclass  obj, jbyteArray bitmap,  jint jwidth, jint jheight, jstring  msg, jintArray num_bits_embeded)
{
    //AndroidBitmapInfo  info;
    int                ret;
    int                width;
    int                height;
    static int         init;
    /*
    unsigned char* pixels = as_unsigned_char_array(env, bitmap);
    (env)->DeleteLocalRef(bitmap);
    */
    int* num_bits_used = new int[2];

/*
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
*/
/*
    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("Bitmap format is not RGB_565 !");
        int bitmapFormat = info.format;
        LOGE("Bitmap Format is %d", bitmapFormat);
    }
*/
/*
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }
*/
    LOGI("Successfully got the pixels");
    width = (int) jwidth;
    height = (int) jheight;

    LOGI("Image Width and height are: %d, %d", width, height);
    const char *password = NULL;//(env)->GetStringUTFChars(msg, JNI_FALSE);

    int returnFromHugo = HUGO_like(NULL, width, height, (char *)password, num_bits_used);
/*
    (env)->ReleaseStringUTFChars(msg, password);

    jbyteArray outbitmap = as_byte_array(env, pixels, width*height);

   // num_bits_embeded = as_int_array(env, num_bits_used, 2);
    env->SetIntArrayRegion (num_bits_embeded, 0, 2, reinterpret_cast<jint*>(num_bits_used));   
    // jint* tmpIntArray = (env)->GetIntArrayElements(num_bits_embeded, NULL);
   // //num_bits_embeded = as_int_array(env, num_bits_used, 2);
   // tmpIntArray[0] = num_bits_used[0];
   // tmpIntArray[1] = num_bits_used[1];
   // (env)->ReleaseIntArrayElements(num_bits_embeded, tmpIntArray, 0);

   delete[] pixels;
*/   delete[] num_bits_used;

   return NULL;

}

JNIEXPORT jstring JNICALL Java_com_droidcrypt_Embedder_extract
(JNIEnv * env, jclass  obj, jbyteArray bitmap,  jint jwidth, jint jheight, jintArray num_bits_embeded, jint stc_constr_height)
{

    //AndroidBitmapInfo  info;
    LOGI("Extracting the text from an image");

    int                ret;
    int                width;
    int                height;
    static int         init;
    unsigned char* pixels = as_unsigned_char_array(env, bitmap);
    int* num_bits_used = new int[2];

   jint* tmpIntArray = (env)->GetIntArrayElements(num_bits_embeded, NULL);
   //num_bits_embeded = as_int_array(env, num_bits_used, 2);
   num_bits_used[0] = tmpIntArray[0];
   num_bits_used[1] = tmpIntArray[1];
   //(env)->ReleaseIntArrayElements(num_bits_embeded, tmpIntArray, 0);
/*
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
*/
/*
    if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
        LOGE("Bitmap format is not RGB_565 !");
        int bitmapFormat = info.format;
        LOGE("Bitmap Format is %d", bitmapFormat);
    }
*/
/*
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }
*/
    width = (int) jwidth;
    height = (int) jheight;

    LOGI("Image Width and height are: %d, %d", width, height);
    LOGI("num_bits_used [%d, %d]", num_bits_used[0], num_bits_used[1]);

    char *password = HUGO_like_extract(pixels, width, height, stc_constr_height, num_bits_used);
    jstring jstrBuf = (env)->NewStringUTF(password);
    //(env)->DeleteLocalRef(jstrBuf);

   delete[] pixels;
   delete[] num_bits_used;
   delete[] password;

    LOGI("EXIT Extracting");
   return jstrBuf;

}

