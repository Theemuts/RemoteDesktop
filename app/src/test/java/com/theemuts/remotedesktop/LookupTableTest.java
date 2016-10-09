package com.theemuts.remotedesktop;

import com.theemuts.remotedesktop.huffman.JPEGHuffmanTable;
import com.theemuts.remotedesktop.util.Util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/* This file has been auto-generated. Do not change it. */

/**
 * Created by thomas on 9-10-16.
 */

public class LookupTableTest {
    @Test
    public void lookup_length_one_isCorrect() throws Exception {
        JPEGHuffmanTable table = JPEGHuffmanTable.StdDCLuminance;

        long buffer;
        short value;
        int totLength, result, encodedLength, zeroRun;

        buffer =  ((long) 2 << 61);
        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 3);
        assertEquals(encodedLength, 1);
        assertEquals(value, -1);

        buffer =  ((long) 2 << 61);
        buffer |= ((long) 1 << 60);
        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 3);
        assertEquals(encodedLength, 1);
        assertEquals(value, 1);
    }

    @Test
    public void lookup_length_two_isCorrect() throws Exception {
        JPEGHuffmanTable table = JPEGHuffmanTable.StdDCLuminance;

        long buffer;
        short value;
        int totLength, result, encodedLength, zeroRun;

        buffer =  ((long) 3 << 61);
        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 4);
        assertEquals(encodedLength, 2);
        assertEquals(value, -3);

        buffer =  ((long) 3 << 61);
        buffer |= ((long) 1 << 59);

        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 4);
        assertEquals(encodedLength, 2);
        assertEquals(value, -2);

        buffer =  ((long) 3 << 61);
        buffer |= ((long) 2 << 59);

        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 4);
        assertEquals(encodedLength, 2);
        assertEquals(value, 2);

        buffer =  ((long) 3 << 61);
        buffer |= ((long) 3 << 59);

        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 4);
        assertEquals(encodedLength, 2);
        assertEquals(value, 3);
    }

    @Test
    public void lookup_length_eleven_isCorrect() throws Exception {
        JPEGHuffmanTable table = JPEGHuffmanTable.StdDCLuminance;

        long buffer;
        short value;
        int totLength, result, encodedLength, zeroRun;

        buffer = (255L << 56);
        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 19);
        assertEquals(encodedLength, 11);
        assertEquals(value, -2047);

        buffer = (255L << 56);
        buffer |= (1L << 44);
        result = table.lookup(buffer);

        encodedLength = (result >>> 16);
        zeroRun = encodedLength & 0xF;
        encodedLength >>>= 4;
        totLength = (encodedLength & 0x1F);
        encodedLength >>>= 5 & 0xF;
        value = (short) result;

        assertEquals(zeroRun, 0);
        assertEquals(totLength, 19);
        assertEquals(encodedLength, 11);
        assertEquals(value, -2046);
    }
}
