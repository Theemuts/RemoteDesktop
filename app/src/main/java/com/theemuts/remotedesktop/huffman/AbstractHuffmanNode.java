package com.theemuts.remotedesktop.huffman;

import com.theemuts.remotedesktop.exception.InvalidHuffmanCodeException;

/**
 * Created by thomas on 17-8-16.
 */
public abstract class AbstractHuffmanNode implements IHuffmanNode {
    short length;
    int value;

    public int getValue() {
        return this.value;
    }

    public short getLength() {
        return this.length;
    }

    public IHuffmanNode getLeft() throws InvalidHuffmanCodeException {
        throw new InvalidHuffmanCodeException();
    }

    public IHuffmanNode getRight() throws InvalidHuffmanCodeException {
        throw new InvalidHuffmanCodeException();
    }

    public int getIndex() throws InvalidHuffmanCodeException {
        throw new InvalidHuffmanCodeException();
    }

    public boolean isLeaf() { return false; }

    public boolean adjacent(IHuffmanNode node) {
        if ((this.length == node.getLength()) && ((this.value & 1) == 1)) {
            return this.value - node.getValue() == 1;
        }

        return false;
    }

    public boolean isLonger(IHuffmanNode node) {
        return this.length > node.getLength();
    }

    public int getSize() throws InvalidHuffmanCodeException { throw new InvalidHuffmanCodeException(); }

    public int getZeroRun() throws InvalidHuffmanCodeException { throw new InvalidHuffmanCodeException(); }

    @Override
    // Descending sort
    public int compareTo(IHuffmanNode ob) {
        int lengthDiff = ob.getLength() - this.length;

        if (lengthDiff != 0) {
            return lengthDiff;
        }

        return ob.getValue() - this.value;
    }

    public IHuffmanNode move(boolean direction)
            throws InvalidHuffmanCodeException
    {
        throw new InvalidHuffmanCodeException();
    }
}