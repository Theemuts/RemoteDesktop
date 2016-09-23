package com.theemuts.remotedesktop.huffman;

import com.theemuts.remotedesktop.exception.InvalidHuffmanCodeException;

/**
 * Created by thomas on 17-8-16.
 */
public interface IHuffmanNode extends Comparable<IHuffmanNode> {
    boolean isLeaf();
    int getValue();
    short getLength();
    int getIndex() throws InvalidHuffmanCodeException;
    boolean adjacent(IHuffmanNode node);
    boolean isLonger(IHuffmanNode node);
    IHuffmanNode getLeft() throws InvalidHuffmanCodeException;
    IHuffmanNode getRight() throws InvalidHuffmanCodeException;
    IHuffmanNode move(boolean direction) throws InvalidHuffmanCodeException;
    int getSize() throws InvalidHuffmanCodeException;
    int getZeroRun() throws InvalidHuffmanCodeException;
}
