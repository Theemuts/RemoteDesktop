package com.theemuts.remotedesktop.huffman;

/**
 * Created by thomas on 17-8-16.
 */
public class HuffmanLeaf extends AbstractHuffmanNode {
    int index;
    int size;
    int zeroRun;

    public HuffmanLeaf(short length, int value, int index) {
        this.index  = index;
        this.length = length;
        this.value  = value;
        this.zeroRun = (index & 0xF0) >> 4;
        this.size = (index & 0x0F);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int getIndex() { return index; }

    @Override
    public int getSize() { return size; }

    @Override
    public int getZeroRun() { return zeroRun; }
}