#include <jni.h>

#ifndef REMOTE_DESKTOP_IDCT_H
#define REMOTE_DESKTOP_IDCT_H
void init_idct(void);
void idct(jshort* data);
#endif
