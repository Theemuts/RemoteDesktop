package com.theemuts.remotedesktop.decoder;

import com.theemuts.remotedesktop.exception.InvalidDataException;
import com.theemuts.remotedesktop.exception.InvalidHuffmanCodeException;
import com.theemuts.remotedesktop.huffman.JPEGHuffmanTable;
import com.theemuts.remotedesktop.transform.IDCT;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 5-9-16.
 */
public class PacketDecoder {
    private static final JPEGHuffmanTable LUMA_DC_TABLE;
    private static final JPEGHuffmanTable LUMA_AC_TABLE;
    private static final JPEGHuffmanTable CHROMA_DC_TABLE;
    private static final JPEGHuffmanTable CHROMA_AC_TABLE;

    private static final int[] UNZIGZAG = {
            0,  1,  8, 16,  9,  2,  3, 10,
            17, 24, 32, 25, 18, 11,  4,  5,
            12, 19, 26, 33, 40, 48, 41, 34,
            27, 20, 13,  6,  7, 14, 21, 28,
            35, 42, 49, 56, 57, 50, 43, 36,
            29, 22, 15, 23, 30, 37, 44, 51,
            58, 59, 52, 45, 38, 31, 39, 46,
            53, 60, 61, 54, 47, 55, 62, 63,
    };

    private IntBuffer intBuf;
    private int dataLength;
    private int dataPointer;

    private long buffer;
    private int trailing;

    private JPEGHuffmanTable table;

    private int inBlockIndex;
    private int blockIndex;
    private short component;

    private short[] yCbCrData;
    private List<int[]> rgbDataList;

    static {
        LUMA_DC_TABLE = JPEGHuffmanTable.StdDCLuminance;
        LUMA_AC_TABLE = JPEGHuffmanTable.StdACLuminance;
        CHROMA_DC_TABLE = JPEGHuffmanTable.StdDCChrominance;
        CHROMA_AC_TABLE = JPEGHuffmanTable.StdACChrominance;
    }

    public DecodedPacket decode(DatagramPacket packet) throws InvalidDataException {
        int packetID;
        int timestamp;

        rgbDataList = new ArrayList<>(40);

        try {
            intBuf = ByteBuffer.wrap(packet.getData(), 1, 999).asIntBuffer();
            dataLength = intBuf.remaining();

            timestamp = intBuf.get(0);
            packetID = intBuf.get(1);

            initBuffer();

            try {
                while (buffer != 0) {
                    decodeMacroblock();
                    rgbDataList.add(idctMacroblock(timestamp));
                }
            } catch (InvalidHuffmanCodeException e) {
                e.printStackTrace();
                throw new InvalidDataException("The data could not be decoded because an invalid Huffman code was detected");
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            }

            return new DecodedPacket(rgbDataList, packetID, timestamp);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void initBuffer() {
        long longShift;
        long currentInt;

        buffer = 0;
        trailing = 8;
        inBlockIndex = 0;

        for (dataPointer = 2; dataPointer < 4; dataPointer++) {
            currentInt = ((long) intBuf.get(dataPointer)) &  4294967295L;
            longShift = (3 - dataPointer) * 32;
            buffer |= (currentInt << longShift);
        }

        buffer <<= 8;
    }

    private void decodeMacroblock() throws InvalidHuffmanCodeException {
        short index = (short) getBits(10);

        yCbCrData = new short[769];
        yCbCrData[768] = index;

        for (blockIndex = 0; blockIndex < 4; blockIndex++) {
            decodeBlock();
        }

        alignBuffer();
    }

    private char getBits(int bits) {
        char returnValue = (char) (buffer >>> (64 - bits));
        moveBuffer(bits);
        return returnValue;
    }

    private void moveBuffer(int bits) {
        buffer <<= bits;
        trailing += bits;

        if ((trailing >= 32) && dataLength > dataPointer) {
            long currentInt = ((long) intBuf.get(dataPointer)) &  4294967295L;

            buffer |= (currentInt << (trailing - 32));
            dataPointer++;
            trailing -= 32;
        }

    }

    private void decodeBlock() throws InvalidHuffmanCodeException {
        decodeLuma();
        decodeChroma();
        decodeChroma();
    }

    private void decodeLuma()  throws InvalidHuffmanCodeException {
        inBlockIndex = 1;
        component = 0;

        setTable(LUMA_DC_TABLE);
        decodeDC();
        setTable(LUMA_AC_TABLE);
        decodeAC();
    }

    private void setTable(JPEGHuffmanTable table) {
        this.table = table;
    }

    private void decodeDC() throws InvalidHuffmanCodeException {
        int result = table.lookup(buffer);

        int encodedLength = (result >>> 20);
        int totLength = (encodedLength & 0x1F) + 1;

        encodedLength >>>= 5 & 0xF;

        moveBuffer(totLength);

        if (encodedLength == 0) {
            return;
        }

        yCbCrData[192 * blockIndex + 64 * component] = (short) result;
    }

    private void decodeAC() throws InvalidHuffmanCodeException {
        int encodedLength;
        int totLength;
        int zeroRun;
        int result;

        while (inBlockIndex <= 63) {
            parseLargeZeroRun();

            result = table.lookup(buffer);

            encodedLength = (result >>> 16);
            zeroRun = encodedLength & 0xF;
            encodedLength >>>= 4;
            totLength = (encodedLength & 0x1F) + 1;
            encodedLength >>>= 5 & 0xF;

            moveBuffer(totLength);

            if (encodedLength == 0) {
                return;
            }

            inBlockIndex += zeroRun;

            yCbCrData[192 * blockIndex + 64 * component + UNZIGZAG[inBlockIndex]] = (short) result;
            inBlockIndex++;
        }
    }

    private void parseLargeZeroRun() {
        if (table == LUMA_AC_TABLE) {
            while ((buffer >>> 53) == 2041) {
                moveBuffer(11);
                inBlockIndex += 16;
            }
        } else {
            while ((buffer >>> 54) == 1018) {
                moveBuffer(10);
                inBlockIndex += 16;
            }
        }
    }

    private void decodeChroma()  throws InvalidHuffmanCodeException {
        inBlockIndex = 1;
        component++;

        setTable(CHROMA_DC_TABLE);
        decodeDC();
        setTable(CHROMA_AC_TABLE);
        decodeAC();
    }

    private void alignBuffer() {
        int modTrailing = trailing & 0x7; // Last three bits is trailing % 8

        if(modTrailing != 0) {
            moveBuffer(8 - modTrailing);
        }
    }

    private int[] idctMacroblock(int timestamp) {
        int[] rgbData = new int[258];
        rgbData[256] = yCbCrData[768];
        rgbData[257] = timestamp;

        IDCT.idct(yCbCrData, rgbData);

        return rgbData;
    }
}