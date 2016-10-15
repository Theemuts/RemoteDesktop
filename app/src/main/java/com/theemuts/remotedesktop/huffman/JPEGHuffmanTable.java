package com.theemuts.remotedesktop.huffman;

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.theemuts.remotedesktop.util.DecodeUtil;

/**
 * The JPEGHuffmanTable class represents a single JPEG Huffman table. It
 * contains the standard tables from the JPEG specification.
 *
 * @since Android 1.0
 */
public class JPEGHuffmanTable {




    private static final char MASK = 0xFFFF;
    /**
     * The standard DC luminance Huffman table .
     */


    public static final JPEGHuffmanTable StdDCLuminance = new JPEGHuffmanTable(new short[] {
            2, 3, 3, 3, 3, 3, 4, 5, 6, 7, 8, 9,
    }, new int[] {
            0, 2, 3, 4, 5, 6, 14, 30, 62, 126, 254, 510
    });
    /**
     * The standard DC chrominance Huffman table.
     */
    public static final JPEGHuffmanTable StdDCChrominance = new JPEGHuffmanTable(new short[] {
            2, 2, 2, 3, 4,  5,  6,  7,   8,   9,   10,   11
    }, new int[] {
            0, 1, 2, 6, 14, 30, 62, 126, 254, 510, 1022, 2046
    });
    /**
     * The standard AC luminance Huffman table.
     */
    public static final JPEGHuffmanTable StdACLuminance = new JPEGHuffmanTable(new short[] {
            4, 2, 2, 3, 4, 5, 7, 8, 10, 16, 16, 17, 17, 17, 17, 17, 17, 4, 5, 7, 9, 11, 16, 16, 16, 16,
            16, 17, 17, 17, 17, 17, 17, 5, 8, 10, 12, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17,
            17, 6, 9, 12, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 6, 10, 16, 16, 16, 16,
            16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 7, 11, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17,
            17, 17, 17, 17, 7, 12, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 8, 12,
            16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 9, 15, 16, 16, 16, 16, 16, 16,
            16, 16, 17, 17, 17, 17, 17, 17, 9, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17,
            17, 17, 9, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 10, 16, 16, 16,
            16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 10, 16, 16, 16, 16, 16, 16, 16, 16, 16,
            17, 17, 17, 17, 17, 17, 11, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17,
            16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 11, 16, 16, 16, 16, 16, 16,
            16, 16, 16, 16, 17, 17, 17, 17, 17
    }, new int[] {
            10, 0, 1, 4, 11, 26, 120, 248, 1014, 65410, 65411, 0, 0, 0, 0, 0, 0, 12, 27, 121, 502,
            2038, 65412, 65413, 65414, 65415, 65416, 0, 0, 0, 0, 0, 0, 28, 249, 1015, 4084, 65417,
            65418, 65419, 65420, 65421, 65422, 0, 0, 0, 0, 0, 0, 58, 503, 4085, 65423, 65424, 65425,
            65426, 65427, 65428, 65429, 0, 0, 0, 0, 0, 0, 59, 1016, 65430, 65431, 65432, 65433,
            65434, 65435, 65436, 65437, 0, 0, 0, 0, 0, 0, 122, 2039, 65438, 65439, 65440, 65441,
            65442, 65443, 65444, 65445, 0, 0, 0, 0, 0, 0, 123, 4086, 65446, 65447, 65448, 65449,
            65450, 65451, 65452, 65453, 0, 0, 0, 0, 0, 0, 250, 4087, 65454, 65455, 65456, 65457,
            65458, 65459, 65460, 65461, 0, 0, 0, 0, 0, 0, 504, 32704, 65462, 65463, 65464, 65465,
            65466, 65467, 65468, 65469, 0, 0, 0, 0, 0, 0, 505, 65470, 65471, 65472, 65473, 65474,
            65475, 65476, 65477, 65478, 0, 0, 0, 0, 0, 0, 506, 65479, 65480, 65481, 65482, 65483,
            65484, 65485, 65486, 65487, 0, 0, 0, 0, 0, 0, 1017, 65488, 65489, 65490, 65491, 65492,
            65493, 65494, 65495, 65496, 0, 0, 0, 0, 0, 0, 1018, 65497, 65498, 65499, 65500, 65501,
            65502, 65503, 65504, 65505, 0, 0, 0, 0, 0, 0, 2040, 65506, 65507, 65508, 65509, 65510,
            65511, 65512, 65513, 65514, 0, 0, 0, 0, 0, 0, 65515, 65516, 65517, 65518, 65519, 65520,
            65521, 65522, 65523, 65524, 0, 0, 0, 0, 0, 2041, 65525, 65526, 65527, 65528, 65529,
            65530, 65531, 65532, 65533, 65534, 0, 0, 0, 0, 0,
    });
    /**
     * The standard AC chrominance Huffman table.
     */
    public static final JPEGHuffmanTable StdACChrominance = new JPEGHuffmanTable(new short[] {
            2, 2, 3, 4, 5, 5, 6, 7, 9, 10, 12, 17, 17, 17, 17, 17, 17, 4, 6, 8, 9, 11, 12, 16, 16,
            16, 16, 17, 17, 17, 17, 17, 17, 5, 8, 10, 12, 15, 16, 16, 16, 16, 16, 17, 17, 17, 17,
            17, 17, 5, 8, 10, 12, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 6, 9, 16, 16, 16,
            16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 6, 10, 16, 16, 16, 16, 16, 16, 16, 16, 17,
            17, 17, 17, 17, 17, 7, 11, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 7,
            11, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 8, 16, 16, 16, 16, 16, 16,
            16, 16, 16, 17, 17, 17, 17, 17, 17, 9, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17,
            17, 17, 17, 9, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 9, 16, 16,
            16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 9, 16, 16, 16, 16, 16, 16, 16, 16,
            16, 17, 17, 17, 17, 17, 17, 11, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17,
            17, 14, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 10, 15, 16, 16, 16, 16,
            16, 16, 16, 16, 16, 17, 17, 17, 17, 17
    }, new int[] {
            0, 1, 4, 10, 24, 25, 56, 120, 500, 1014, 4084, 0, 0, 0, 0, 0, 0, 11, 57, 246, 501, 2038,
            4085, 65416, 65417, 65418, 65419, 0, 0, 0, 0, 0, 0, 26, 247, 1015, 4086, 32706, 65420,
            65421, 65422, 65423, 65424, 0, 0, 0, 0, 0, 0, 27, 248, 1016, 4087, 65425, 65426, 65427,
            65428, 65429, 65430, 0, 0, 0, 0, 0, 0, 58, 502, 65431, 65432, 65433, 65434, 65435,
            65436, 65437, 65438, 0, 0, 0, 0, 0, 0, 59, 1017, 65439, 65440, 65441, 65442, 65443,
            65444, 65445, 65446, 0, 0, 0, 0, 0, 0, 121, 2039, 65447, 65448, 65449, 65450, 65451,
            65452, 65453, 65454, 0, 0, 0, 0, 0, 0, 122, 2040, 65455, 65456, 65457, 65458, 65459,
            65460, 65461, 65462, 0, 0, 0, 0, 0, 0, 249, 65463, 65464, 65465, 65466, 65467, 65468,
            65469, 65470, 65471, 0, 0, 0, 0, 0, 0, 503, 65472, 65473, 65474, 65475, 65476, 65477,
            65478, 65479, 65480, 0, 0, 0, 0, 0, 0, 504, 65481, 65482, 65483, 65484, 65485, 65486,
            65487, 65488, 65489, 0, 0, 0, 0, 0, 0, 505, 65490, 65491, 65492, 65493, 65494, 65495,
            65496, 65497, 65498, 0, 0, 0, 0, 0, 0, 506, 65499, 65500, 65501, 65502, 65503, 65504,
            65505, 65506, 65507, 0, 0, 0, 0, 0, 0, 2041, 65508, 65509, 65510, 65511, 65512, 65513,
            65514, 65515, 65516, 0, 0, 0, 0, 0, 0, 16352, 65517, 65518, 65519, 65520, 65521, 65522,
            65523, 65524, 65525, 0, 0, 0, 0, 0, 1018, 32707, 65526, 65527, 65528, 65529, 65530,
            65531, 65532, 65533, 65534, 0, 0, 0, 0, 0
    });
    /**
     * The lengths.
     */
    private short lengths[];
    /**
     * The values.
     */
    private int[] values;

    /**
     * The size.
     */
    private int size;

    /**
     * The decoder lookup table.
     */
    private int lookupTable[];

    /**
     * Instantiates a new jPEG huffman table.
     *
     * @param lengths
     *            the lengths
     * @param values
     *            the values
     */
    JPEGHuffmanTable(short[] lengths, int[] values) {
        // Construction of standard tables without checks
        // Could be also used for copying of the existing tables
        this.lengths = lengths;
        this.values = values;
        size = values.length;

        if(lengths.length != size) {
            throw new RuntimeException();
        }

        short huffmanCodeLength;
        char value;
        int lookupTableValue;

        lookupTable = new int[65536];

        for (int i = 0; i < size; i++) {
            huffmanCodeLength = lengths[i];
            huffmanCodeLength--;

            if(huffmanCodeLength < 16) {
                value = (char) values[i];
                value <<= (15 - huffmanCodeLength);

                // ((20 bits 0) | (4 bits huff length) | (4 bits zero run) | (4 bits encoded value size))
                lookupTableValue = huffmanCodeLength;
                lookupTableValue <<= 8;
                lookupTableValue |= i;

                lookupTable[value] = lookupTableValue;
            }
        }

        int max = 0xFFFF;
        int mask = max;
        int previous = 0;
        int codeLength = 0;
        int zeroRunLength = 0;
        int encodedLength = 0;
        boolean setValue = false;

        for (int i = 0; i < lookupTable.length; i++) {
            if (lookupTable[i] != 0) {
                previous = lookupTable[i];
                codeLength = previous >>> 8;
                encodedLength = previous & 0xF;
                zeroRunLength = (previous & 0xF0) >> 4;

                if (codeLength + encodedLength < 16) {
                    setValue = true;
                    mask = (int) (DecodeUtil.MASK_START[codeLength + 49] & DecodeUtil.MASK_END[15 - codeLength - encodedLength]);
                } else {
                    setValue = false;
                }
            }

            if(setValue) {
                // ((4 bits 0) | (4 bits encoded length) | (4 bits huff length) | (4 bits zero run) | (16 bits decoded value))
                value = (char) ((i & mask) >>> (15 - codeLength - encodedLength));
                value = DecodeUtil.decodeValue((short) value, encodedLength);

                lookupTable[i] = encodedLength << 25 | (codeLength + encodedLength) << 20 | zeroRunLength << 16 | value;
            } else {
                // Negate so first bit is 1.
                lookupTable[i] = ~previous;
            }
        }
    }

    /**
     * Gets an array of lengths in the Huffman table.
     *
     * @return the array of short values representing the length values in the
     *         Huffman table.
     */
    public short[] getLengths() {
        short newLengths[] = new short[lengths.length];
        System.arraycopy(lengths, 0, newLengths, 0, lengths.length);
        return newLengths;
    }
    /**
     * Gets an array of values represented by increasing length of their codes.
     *
     * @return the array of values.
     */
    public int[] getValues() {
        int newValues[] = new int[values.length];
        System.arraycopy(values, 0, newValues, 0, values.length);
        return newValues;
    }

    /**
     * Returns the string representation of this JPEGHuffmanTable object.
     *
     * @return the string representation of this JPEGHuffmanTable object.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("JPEGHuffmanTable:\nlengths:");
        for (short length : lengths) {
            sb.append(' ').append(length);
        }
        sb.append("\nvalues:");
        for (int value : values) {
            sb.append(' ').append(value);
        }
        return sb.toString();
    }

    public int lookup(long buffer) {
        int maybeResult = lookupTable[(int) (buffer >>> 48)];

        if(maybeResult > 0)
            return  maybeResult;

        maybeResult = ~maybeResult;

        // ((20 bits 0) | (4 bits huff length) | (4 bits zero run) | (4 bits encoded value size))
        int huffmanCodeLength = maybeResult >> 8;
        int zeroRun = (maybeResult >> 4) & 0xF;
        int encodedValueLength = maybeResult & 0xF;

        char value = (char) (buffer >> (63 - huffmanCodeLength - encodedValueLength));
        value &= DecodeUtil.MASK_START[64 - encodedValueLength];
        value = DecodeUtil.decodeValue((short) value, encodedValueLength);

        // ((4 bits 0) | (4 bits encoded length) | (4 bits huff length) | (4 bits zero run) | (16 bits decoded value))
        return (encodedValueLength << 25) |(encodedValueLength + huffmanCodeLength) << 20 | zeroRun << 16 | value;
    }
}