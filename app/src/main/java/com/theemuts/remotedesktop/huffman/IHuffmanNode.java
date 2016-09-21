package com.theemuts.remotedesktop.huffman;

import com.theemuts.remotedesktop.exception.InvalidHuffmanCodeException;

/**
 * Created by thomas on 17-8-16.
 */
public interface IHuffmanNode extends Comparable<IHuffmanNode> {
    public boolean isLeaf();
    public int getValue();
    public short getLength();
    public int getIndex() throws InvalidHuffmanCodeException;
    public boolean adjacent(IHuffmanNode node);
    public boolean isLonger(IHuffmanNode node);
    public IHuffmanNode getLeft() throws InvalidHuffmanCodeException;
    public IHuffmanNode getRight() throws InvalidHuffmanCodeException;
    public IHuffmanNode move(boolean direction) throws InvalidHuffmanCodeException;
    public int getSize() throws InvalidHuffmanCodeException;
    public int getZeroRun() throws InvalidHuffmanCodeException;
}
