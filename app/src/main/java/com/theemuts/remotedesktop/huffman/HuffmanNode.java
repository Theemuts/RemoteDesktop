package com.theemuts.remotedesktop.huffman;

import com.theemuts.remotedesktop.exception.InvalidHuffmanCodeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by thomas on 17-8-16.
 */
public class HuffmanNode extends AbstractHuffmanNode {
    IHuffmanNode left;
    IHuffmanNode right;

    public HuffmanNode(JPEGHuffmanTable table) {
        // Huffman nodes for iterating over pairs of nodes.
        IHuffmanNode previous;
        IHuffmanNode current;

        // Number of merged nodes this iteration,
        int merged;

        // Get table lengths and values
        short[] lengths = table.getLengths();
        int[] values = table.getValues();
        int nLeafs = lengths.length;

        // List of nodes to keep track of parsing process
        List<IHuffmanNode> nodes = new ArrayList<>(nLeafs);
        List<IHuffmanNode> processed;

        // Turn tables into leafs
        for(int i = 0; i < nLeafs; i++) {
            if (lengths[i] <= 16) {
                nodes.add(new HuffmanLeaf(lengths[i], values[i], i));
            }
        }

        // Sort in descending order
        Collections.sort(nodes);

        // While we have more than just a root node
        while (nodes.size() > 1) {
            // Set merged to 0, lastMerged to -2 to avoid errors
            merged = 0;
            int size = nodes.size();
            processed = new ArrayList<>(size);

            // Get first node
            previous = nodes.get(0);

            for (int i = 1; i < nodes.size(); i++) {
                // Get next node
                current = nodes.get(i);

                // Check if they are adjacent
                if(previous.adjacent(current)) {
                    previous = mergeAdjacent(previous, current);

                    // increase merged counter and update last merged
                    merged++;
                } else {
                    processed.add(previous);
                    previous = current;
                }
            }

            processed.add(previous);

            if (merged == 0) {
                // Merged first with null because no data merged.
                mergeFirstWithNull(processed);
            }

            Collections.sort(processed);
            nodes.clear();

            for (IHuffmanNode node: processed) {
                nodes.add(node);
            }
        }

        IHuffmanNode node = nodes.get(0);
        try {
            left = node.getLeft();
            right = node.getRight();
        } catch (InvalidHuffmanCodeException e) {
            left = null;
            right = null;
        }

        value = 0;
        length = 0;
    }

    private HuffmanNode(IHuffmanNode left, IHuffmanNode right,
                       short length, int value) {
        this.left = left;
        this.right = right;
        this.value = value;
        this.length = length;
    }

    @Override
    public IHuffmanNode move(boolean direction) throws InvalidHuffmanCodeException {
        if (direction) {
            if(null == this.left) {
                throw new InvalidHuffmanCodeException();
            }

            return this.left;
        }

        if(null == this.right) {
            throw new InvalidHuffmanCodeException();
        }

        return this.right;
    }

    @Override
    public IHuffmanNode getLeft() throws InvalidHuffmanCodeException {
        return this.left;
    }

    @Override
    public IHuffmanNode getRight() throws InvalidHuffmanCodeException {
        return this.right;
    }

    private static void mergeFirstWithNull(List<IHuffmanNode> processed) {
        IHuffmanNode newNode;
        IHuffmanNode previous = processed.get(0);

        short len = previous.getLength();
        int val = previous.getValue();

        len--; // decrement length

        if ((val & 1) == 1) {
            val >>= 1; // shift value
            newNode = new HuffmanNode(previous, null, len, val);
        } else {
            val >>= 1; // shift value
            newNode = new HuffmanNode(null, previous, len, val);
        }

        processed.set(0, newNode);
    }

    private static IHuffmanNode mergeAdjacent(IHuffmanNode previous,
                                              IHuffmanNode current)
    {
        // getting previous ór current is irrelevant:
        // lens and shifted vals are equal.
        short len = previous.getLength();
        int val = previous.getValue();

        len--; // decrement length
        val >>= 1; // shift value

        // Perform merge
        IHuffmanNode newNode = new HuffmanNode(previous, current, len, val);

        return newNode;
    }
}