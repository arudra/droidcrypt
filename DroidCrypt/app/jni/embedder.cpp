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

unsigned char* as_unsigned_char_array(JNIEnv * env, jbyteArray array) {
    int len = env->GetArrayLength (array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}

JNIEXPORT void JNICALL Java_com_droidcrypt_Embedder_embed
(JNIEnv * env, jclass  obj, jbyteArray bitmap,  jint jwidth, jint jheight, jstring  msg)
{
    //AndroidBitmapInfo  info;
    int                ret;
    int                width;
    int                height;
    static int         init;

    unsigned char* pixels = as_unsigned_char_array(env, bitmap);

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
    const char *password = (env)->GetStringUTFChars(msg, JNI_FALSE);

   // use your string
    LOGI(password);

    int returnFromHugo = HUGO_like(pixels, width, height, (char *)password);

   (env)->ReleaseStringUTFChars(msg, password);

    //AndroidBitmap_unlockPixels(env, bitmap);


}