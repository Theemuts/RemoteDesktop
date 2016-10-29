//
// Created by thomas on 20-8-16.
//

#include <string.h>
#include <jni.h>
#include "remote-desktop.h"
#include "idct.h"
#include <android/log.h>

/*
 * Copyright (C) 2009 The Android Open Source Project
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
 *
 */

void convert_to_rgb(jshort* block_buff, jint* rgb_buff);

 JNIEXPORT void JNICALL Java_com_theemuts_remotedesktop_transform_IDCT_idct
            (JNIEnv* env, jclass thiz, jshortArray dct_macroblocks, jintArray pixels)
    {
        init_idct();
        int i;

        jshort block_buff[192];
        jint rgb_buff[64];
        for(i = 0; i < 4; i++) {
            (*env)->GetShortArrayRegion(env, dct_macroblocks, i*192, 192, block_buff);

            idct(block_buff);
            convert_to_rgb(block_buff, rgb_buff);

            (*env)->SetIntArrayRegion(env, pixels, i*64, 64, rgb_buff);
        }

    }

void convert_to_rgb(jshort* block_buff, jint* rgb_buff) {
    int i;
    int r;
    int g;
    int b;
    int val;

    for (i = 0; i < 64; i++) {
        r = block_buff[i] +
                (short) (1.402 * (double) block_buff[i + 128]);

        g = block_buff[i] +
                - (short) (-0.344136 * (double) block_buff[i+64]) +
                (short) (-0.714136 * (double) block_buff[i + 128]);

        b = block_buff[i] +
                (short) (1.772 * (double) (block_buff[i + 64]));

        if (r > 255) r = 255;
        else if (r < 0) r = 0;

        if (g > 255) g = 255;
        else if (g < 0) g = 0;

        if (b > 255) b = 255;
        else if (b < 0) b = 0;

        val = 0x00000000 | (r << 16) | (g << 8) | b;
        rgb_buff[i] = val;
        /*rgb_buff[2*i] = val;
        rgb_buff[2*i+1] = val;*/
    }
}