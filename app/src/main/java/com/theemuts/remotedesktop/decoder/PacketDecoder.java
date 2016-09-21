package com.theemuts.remotedesktop.decoder;

import com.theemuts.remotedesktop.exception.InvalidDataException;
import com.theemuts.remotedesktop.exception.InvalidHuffmanCodeException;
import com.theemuts.remotedesktop.huffman.HuffmanNode;
import com.theemuts.remotedesktop.huffman.HuffmanTable;
import com.theemuts.remotedesktop.huffman.IHuffmanNode;
import com.theemuts.remotedesktop.huffman.JPEGHuffmanTable;
import com.theemuts.remotedesktop.transform.IDCT;
import com.theemuts.remotedesktop.util.Util;

import java.net.DatagramPacket;
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

    private static final IHuffmanNode LUMA_DC_TREE;
    private static final IHuffmanNode LUMA_AC_TREE;
    private static final IHuffmanNode CHROMA_DC_TREE;
    private static final IHuffmanNode CHROMA_AC_TREE;

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

    private byte[] data;

    private int dataLength;
    private int dataPointer;
    private int currentByte;
    private int dataBuffer;
    private int trailing;
    private HuffmanTable table;
    private int inBlockIndex;
    private int blockIndex;
    private short component;
    private IHuffmanNode pointer;
    private int timestamp;
    private int packetID;

    private short[] yCbCrData;
    private List<int[]> rgbDataList;


    static {
        LUMA_DC_TABLE = JPEGHuffmanTable.StdDCLuminance;
        LUMA_AC_TABLE = JPEGHuffmanTable.StdDCChrominance;
        CHROMA_DC_TABLE = JPEGHuffmanTable.StdACLuminance;
        CHROMA_AC_TABLE = JPEGHuffmanTable.StdACChrominance;

        LUMA_DC_TREE = new HuffmanNode(LUMA_DC_TABLE);
        LUMA_AC_TREE = new HuffmanNode(CHROMA_DC_TABLE);
        CHROMA_DC_TREE = new HuffmanNode(LUMA_AC_TABLE);
        CHROMA_AC_TREE = new HuffmanNode(CHROMA_AC_TABLE);
    }

    public DecodedPacket decode(DatagramPacket packet) throws InvalidDataException {
        rgbDataList = new ArrayList<>(40);

        dataLength = packet.getLength();
        data = packet.getData();

        timestamp = Util.getTimestamp(data);
        packetID = Util.getPacketId(data);

        initBuffer();

        try {
            while(dataPointer < dataLength) {
                decodeMacroblock();
                rgbDataList.add(idctMacroblock());
            }
        } catch (InvalidHuffmanCodeException e) {
            throw new InvalidDataException("The data could not be decoded because an invalid Huffman code was detected");
        }

        return new DecodedPacket(rgbDataList, packetID, timestamp);
    }

    private void initBuffer() {
        int shift;

        dataBuffer = 0;
        trailing = 0;
        inBlockIndex = 0;

        for (dataPointer = 8; dataPointer < 12; dataPointer++) {
            currentByte = data[dataPointer];

            // If v < 0, make sure we change back to 0..255 when converting to int
            if(currentByte < 0)
                currentByte += 256;

            shift = (3 - dataPointer) * 8;
            dataBuffer |= (currentByte << shift);
        }
    }

    private void decodeMacroblock() throws InvalidHuffmanCodeException {
        short index = getBits(10);

        yCbCrData = new short[769];

        for (int i = 0; i < 768; i++) {
            yCbCrData[i] = 0;
        }

        yCbCrData[768] = index;

        for (blockIndex = 0; blockIndex < 4; blockIndex++) {
            decodeBlock();
        }

        alignBuffer();
    }

    private short getBits(int bits) {
        if (bits == 3) {
            short returnValue = (short) (dataBuffer >>> (32 - bits));
            moveBuffer(bits);
            return returnValue;
        } else {
            short returnValue = (short) (dataBuffer >>> (32 - bits));
            moveBuffer(bits);
            return returnValue;
        }
    }

    private void moveBuffer(int bits) {
        dataBuffer <<= bits;
        trailing += bits;

        if ((trailing > 8) && dataPointer < dataLength) {
            currentByte = data[dataPointer];

            if (currentByte < 0)
                currentByte += 256;

            dataBuffer |= (currentByte << (trailing - 8));
            dataPointer++;
            trailing = trailing - 8;
        }
    }

    private void decodeBlock() throws InvalidHuffmanCodeException {
        decodeLuma();
        decodeChroma();
        decodeChroma();
    }

    private void decodeLuma()  throws InvalidHuffmanCodeException {
        inBlockIndex = 0;
        component = 0;

        setPointerToRootOf(HuffmanTable.LUMA_DC);
        decodeDC();
        setPointerToRootOf(HuffmanTable.LUMA_AC);
        decodeAC();
    }

    private void setPointerToRootOf(HuffmanTable table) {
        this.table = table;
        resetPointerToRoot();
    }

    private void resetPointerToRoot() {
        switch(table) {
            case LUMA_AC:
                pointer = LUMA_AC_TREE;
                break;
            case LUMA_DC:
                pointer = LUMA_DC_TREE;
                break;
            case CHROMA_AC:
                pointer = CHROMA_AC_TREE;
                break;
            case CHROMA_DC:
                pointer = CHROMA_DC_TREE;
                break;
        }
    }

    private void decodeDC() throws InvalidHuffmanCodeException {
        // Update in advance.
        inBlockIndex = 1;

        if (checkZero()) {
            return;
        }

        moveUntilLeaf();

        int size = pointer.getIndex();
        short value = getBits(size);
        value = decodeValue(value, size);
        int index = 192 * blockIndex + 64 * component;
        yCbCrData[index] = value;
    }

    private boolean checkZero() {
        if ((table == HuffmanTable.LUMA_AC) & ((dataBuffer >>> 28) == 10)) {
            moveBuffer(4);
            return true;
        } else if ((table != HuffmanTable.LUMA_AC) & ((dataBuffer >>> 30) == 0))  {
            moveBuffer(2);
            return true;
        }

        return false;
    }


    private int moveUntilLeaf() throws InvalidHuffmanCodeException {
        int size = 0;

        while (!pointer.isLeaf()) {
            movePointer();
            size++;
        }

        return size;
    }

    private void movePointer() throws InvalidHuffmanCodeException {
        pointer = pointer.move(dataBuffer < 0);
        moveBuffer(1);
    }

    private short decodeValue(short value, int size) {
        if ((value >>> (size - 1)) == 0) {
            short retVal = (short) -((~value) & (0xFFFF >>> (16 - size)));
            return retVal;
        }

        if (value < 0 && value > -128) {
            return (short) (-value);
        }

        return value;
    }

    private void decodeAC() throws InvalidHuffmanCodeException {
        int size;
        short value;
        short value2;

        while (inBlockIndex <= 63) {
            parseLargeZeroRun();

            if (checkZero()) {
                break;
            }

            moveUntilLeaf();

            inBlockIndex += pointer.getZeroRun();
            int index = 192 * blockIndex + 64 * component + UNZIGZAG[inBlockIndex];
            size = pointer.getSize();

            if (index == 208) {
                value = getBits(size);
                value2 = decodeValue(value, size);
            } else {
                value = getBits(size);
                value2 = decodeValue(value, size);
            }

            yCbCrData[index] = value2;
            inBlockIndex += 1;

            resetPointerToRoot();
        }
    }

    private int parseLargeZeroRun() {
        int zero_run_size = 0;

        if (table == HuffmanTable.LUMA_AC) {
            while ((dataBuffer >>> 21) == 2041) {
                moveBuffer(11);
                inBlockIndex = inBlockIndex + 16;
                zero_run_size +=1;
            }
        } else {
            while ((dataBuffer >>> 22) == 1018) {
                moveBuffer(10);
                inBlockIndex = inBlockIndex + 16;
                zero_run_size +=1;
            }
        }

        return zero_run_size;
    }

    private void decodeChroma()  throws InvalidHuffmanCodeException {
        inBlockIndex = 0;
        component++;
        setPointerToRootOf(HuffmanTable.CHROMA_DC);
        decodeDC();
        setPointerToRootOf(HuffmanTable.CHROMA_AC);
        decodeAC();
    }

    private void alignBuffer() {
        if(trailing != 0) {
            moveBuffer(8 - trailing);
        }
    }

    private int[] idctMacroblock() {
        int[] rgbData = new int[258];

        for (int i = 0; i < 256; i++) {
            rgbData[i] = 0;
        }

        rgbData[256] = yCbCrData[768];
        rgbData[257] = timestamp;

        IDCT.idct(yCbCrData, rgbData);

        return rgbData;
    }
}