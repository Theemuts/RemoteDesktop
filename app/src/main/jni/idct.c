//
// Created by thomas on 20-8-16.
//

#include <android/log.h>

#define APPNAME "MyApp"


/* idct.c, inverse fast discrete cosine transform                           */

/*
 * tmndecode
 * Copyright (C) 1995 Telenor R&D
 *                    Karl Olav Lillevold <kol@nta.no>
 *
 * based on mpeg2decode, (C) 1994, MPEG Software Simulation Group
 * and mpeg2play, (C) 1994 Stefan Eckart
 *                         <stefan@lis.e-technik.tu-muenchen.de>
 *
 */


/**********************************************************/
/* inverse two dimensional DCT, Chen-Wang algorithm       */
/* (cf. IEEE ASSP-32, pp. 803-816, Aug. 1984)             */
/* 32-bit integer arithmetic (8 bit coefficients)         */
/* 11 mults, 29 adds per DCT                              */
/*                                      sE, 18.8.91       */
/**********************************************************/
/* coefficients extended to 12 bit for IEEE1180-1990      */
/* compliance                           sE,  2.1.94       */
/**********************************************************/

/* this code assumes >> to be a two's-complement arithmetic */
/* right shift: (-2)>>1 == -1 , (-3)>>1 == -2               */

#include "idct.h"

#define W1 2841 /* 2048*sqrt(2)*cos(1*pi/16) */
#define W2 2676 /* 2048*sqrt(2)*cos(2*pi/16) */
#define W3 2408 /* 2048*sqrt(2)*cos(3*pi/16) */
#define W5 1609 /* 2048*sqrt(2)*cos(5*pi/16) */
#define W6 1108 /* 2048*sqrt(2)*cos(6*pi/16) */
#define W7 565  /* 2048*sqrt(2)*cos(7*pi/16) */


/* private data */
static short iclip[1024]; /* clipping table */

static short *iclp;

static short qtable_l[64] = { 16, 11, 10, 16,  24,  40,  51,  61,
                              12, 12, 14, 19,  26,  58,  60,  55,
                              14, 13, 16, 24,  40,  57,  69,  56,
                              14, 17, 22, 29,  51,  87,  80,  62,
                              18, 22, 37, 56,  68, 109, 103,  77,
                              24, 35, 55, 64,  81, 104, 113,  92,
                              49, 64, 78, 87, 103, 121, 120, 101,
                              72, 92, 95, 98, 112, 100, 103,  99 };

static short qtable_c[64] = { 17, 18, 24, 47, 99, 99, 99, 99,
                              18, 21, 26, 66, 99, 99, 99, 99,
                              24, 26, 56, 99, 99, 99, 99, 99,
                              47, 66, 99, 99, 99, 99, 99, 99,
                              99, 99, 99, 99, 99, 99, 99, 99,
                              99, 99, 99, 99, 99, 99, 99, 99,
                              99, 99, 99, 99, 99, 99, 99, 99,
                              99, 99, 99, 99, 99, 99, 99, 99};

/* private prototypes */
static void idct_row(jshort* data);
static void idct_col(jshort* data);

/* row (horizontal) IDCT
 *
 *           7                       pi         1
 * dst[k] = sum c[l] * src[l] * cos( -- * ( k + - ) * l )
 *          l=0                      8          2
 *
 * where: c[0]    = 128
 *        c[1..7] = 128*sqrt(2)
 */

static void idct_row(jshort* data) {
    int x0, x1, x2, x3, x4, x5, x6, x7, x8;

    /* shortcut */
    if (!((x1 = data[4]<<11) | (x2 = data[6]) | (x3 = data[2]) |
          (x4 = data[1]) | (x5 = data[7]) | (x6 = data[5]) | (x7 = data[3])))
    {
        data[0]=data[1]=data[2]=data[3]=data[4]=data[5]=data[6]=data[7]=data[0]<<3;
        return;
    }

    x0 = (data[0]<<11) + 128; /* for proper rounding in the fourth stage */

    /* first stage */
    x8 = W7*(x4+x5);
    x4 = x8 + (W1-W7)*x4;
    x5 = x8 - (W1+W7)*x5;
    x8 = W3*(x6+x7);
    x6 = x8 - (W3-W5)*x6;
    x7 = x8 - (W3+W5)*x7;

    /* second stage */
    x8 = x0 + x1;
    x0 -= x1;
    x1 = W6*(x3+x2);
    x2 = x1 - (W2+W6)*x2;
    x3 = x1 + (W2-W6)*x3;
    x1 = x4 + x6;
    x4 -= x6;
    x6 = x5 + x7;
    x5 -= x7;

    /* third stage */
    x7 = x8 + x3;
    x8 -= x3;
    x3 = x0 + x2;
    x0 -= x2;
    x2 = (181*(x4+x5)+128)>>8;
    x4 = (181*(x4-x5)+128)>>8;

    /* fourth stage */
    data[0] = (x7+x1)>>8;
    data[1] = (x3+x2)>>8;
    data[2] = (x0+x4)>>8;
    data[3] = (x8+x6)>>8;
    data[4] = (x8-x6)>>8;
    data[5] = (x0-x4)>>8;
    data[6] = (x3-x2)>>8;
    data[7] = (x7-x1)>>8;
}

static void idct_col(jshort* data) {
    int x0, x1, x2, x3, x4, x5, x6, x7, x8;

    /* shortcut */
    if (!((x1 = (data[8 * 4]<<8)) | (x2 = data[8 * 6]) | (x3 = data[8 * 2]) |
          (x4 = data[8*1]) | (x5 = data[8 * 7]) | (x6 = data[8 * 5]) | (x7 = data[8 * 3]))){
        data[8*0]=data[8*1]=data[8 * 2]=data[8 * 3]=data[8 * 4]=data[8 * 5]=data[8 * 6]=data[8 * 7]=
                (data[8*0]+32)>>6;
        return;
    }

    x0 = (data[8*0]<<8) + 8192;

    /* first stage */
    x8 = W7*(x4+x5) + 4;
    x4 = (x8+(W1-W7)*x4)>>3;
    x5 = (x8-(W1+W7)*x5)>>3;
    x8 = W3*(x6+x7) + 4;
    x6 = (x8-(W3-W5)*x6)>>3;
    x7 = (x8-(W3+W5)*x7)>>3;

    /* second stage */
    x8 = x0 + x1;
    x0 -= x1;
    x1 = W6*(x3+x2) + 4;
    x2 = (x1-(W2+W6)*x2)>>3;
    x3 = (x1+(W2-W6)*x3)>>3;
    x1 = x4 + x6;
    x4 -= x6;
    x6 = x5 + x7;
    x5 -= x7;

    /* third stage */
    x7 = x8 + x3;
    x8 -= x3;
    x3 = x0 + x2;
    x0 -= x2;
    x2 = (181 * (x4 + x5) + 128) >> 8;
    x4 = (181 * (x4 - x5) + 128) >> 8;

    /* fourth stage */
    data[8 * 0] = (x7 + x1) >> 14;
    data[8 * 1] = (x3 + x2) >> 14;
    data[8 * 2] = (x0 + x4) >> 14;
    data[8 * 3] = (x8 + x6) >> 14;
    data[8 * 4] = (x8 - x6) >> 14;
    data[8 * 5] = (x0 - x4) >> 14;
    data[8 * 6] = (x3 - x2) >> 14;
    data[8 * 7] = (x7 - x1) >> 14;
}

/* two dimensional inverse discrete cosine transform */
void idct(jshort* data) {
    int i;

    // LUMA
    for (i = 0; i < 64; i++) {
        data[i] *= qtable_l[i];
    }

    for (i = 0; i < 8; i++) {
        idct_row(data + 8*i);
    }

    for (i = 0; i < 8; i++) {
        idct_col(data + i);
    }

    // chroma_b
    for (i = 0; i < 64; i++) {
        data[64 + i] *= qtable_c[i];
    }

    for (i = 0; i < 8; i++) {
        idct_row(data + 64 + 8*i);
    }

    for (i = 0; i < 8; i++) {
        idct_col(data + 64 + i);
    }

    // chroma_r
    for (i = 0; i < 64; i++) {
        data[128 + i] *= qtable_c[i];
    }

    for (i = 0; i < 8; i++) {
        idct_row(data + 128 + 8*i);
    }

    for (i = 0; i < 8; i++) {
        idct_col(data + 128 + i);
    }

    for(i = 0; i < 64; i++) {
        data[i] += 128;
    }
}

void init_idct()
{
    int i;

    iclp = iclip+512;
    for (i= -512; i<512; i++)
        iclp[i] = (i<-256) ? -256 : ((i>255) ? 255 : i);
}
