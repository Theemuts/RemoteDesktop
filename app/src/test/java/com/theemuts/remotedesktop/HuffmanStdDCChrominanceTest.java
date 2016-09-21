package com.theemuts.remotedesktop;

import com.theemuts.remotedesktop.huffman.HuffmanNode;
import com.theemuts.remotedesktop.huffman.IHuffmanNode;
import com.theemuts.remotedesktop.huffman.JPEGHuffmanTable;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/* This file has been auto-generated. Do not change it. */

public class HuffmanStdDCChrominanceTest {
    @Test
    public void tree_isCorrect() throws Exception {
        IHuffmanNode tree = new HuffmanNode(JPEGHuffmanTable.StdDCChrominance);
        IHuffmanNode pointer;

        pointer = tree.move(false);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 0);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(false);
        pointer = pointer.move(true);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 1);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 2);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 3);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 4);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 5);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 6);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 7);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 8);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 9);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 10);
        assertEquals(pointer.getZeroRun(), 0);

        pointer = tree.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(true);
        pointer = pointer.move(false);
        assertTrue(pointer.isLeaf());
        assertEquals(pointer.getSize(), 11);
        assertEquals(pointer.getZeroRun(), 0);
    }
}
