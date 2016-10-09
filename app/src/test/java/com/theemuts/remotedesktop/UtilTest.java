package com.theemuts.remotedesktop;

import com.theemuts.remotedesktop.util.Util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/* This file has been auto-generated. Do not change it. */

public class UtilTest {
    @Test
    public void convertToBitstring_Single_isCorrect() throws Exception {
        byte[] data = { -128 };
        assertEquals(Util.convertToBitstring(data), "10000000");

        data[0] = 1;
        assertEquals(Util.convertToBitstring(data), "00000001");

        data[0] = 65;
        assertEquals(Util.convertToBitstring(data), "01000001");
    }

    @Test
    public void convertToBitstring_Double_isCorrect() throws Exception {
        byte[] data = { -128, 0 };
        assertEquals(Util.convertToBitstring(data), "10000000 00000000");
    }

    @Test
    public void convertToBitstring_Triple_isCorrect() throws Exception {
        byte[] data = { -128, 0, 65 };
        assertEquals(Util.convertToBitstring(data), "10000000 00000000 01000001");
    }

    @Test
    public void convertToBitstring_length_isCorrect() throws Exception {
        byte[] data = { -128, 0, 65 };
        assertEquals(Util.convertToBitstring(data, 2), "10000000 00000000");
    }

    @Test
    public void convertToBitstring_offset_isCorrect() throws Exception {
        byte[] data = { -128, 0, 65 };
        assertEquals(Util.convertToBitstring(data, 2, 1), "00000000 01000001");
    }

    @Test
    public void convertToBitstring_long_isCorrect() throws Exception {
        long data = 1;
        assertEquals(Util.convertToBitstring(data), "00000000 00000000 00000000 00000000 00000000 00000000 00000000 00000001");

        data = 256;
        assertEquals(Util.convertToBitstring(data), "00000000 00000000 00000000 00000000 00000000 00000000 00000001 00000000");

    }

    @Test
    public void convertToBitstring_int_isCorrect() throws Exception {
        int data = 1;
        assertEquals(Util.convertToBitstring(data), "00000000 00000000 00000000 00000001");

        data = 256;
        assertEquals(Util.convertToBitstring(data), "00000000 00000000 00000001 00000000");

    }

    @Test
    public void convertToBitstring_short_isCorrect() throws Exception {
        short data = 1;
        assertEquals(Util.convertToBitstring(data), "00000000 00000001");

        data = 256;
        assertEquals(Util.convertToBitstring(data), "00000001 00000000");
    }

    @Test
    public void convertToBitstring_byte_isCorrect() throws Exception {
        byte data = 1;
        assertEquals(Util.convertToBitstring(data), "00000001");

        data = -128;
        assertEquals(Util.convertToBitstring(data), "10000000");

    }
}