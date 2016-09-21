package com.theemuts.remotedesktop.transform;

/**
 * Created by thomas on 20-8-16.
 */
public class IDCT {
    public static native void idct(short[] input, int[] output);
}
