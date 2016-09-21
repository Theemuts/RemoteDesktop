package com.theemuts.remotedesktop.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by thomas on 17-9-16.
 */
public class ScreenInfo {
    String name;
    int nSegmentsX;
    int nSegmentsY;

    private ScreenInfo(String name, int nSegmentsX, int nSegmentsY) {
        this.name = name;
        this.nSegmentsX = nSegmentsX;
        this.nSegmentsY = nSegmentsY;
    }


    public static List<ScreenInfo> decode(byte[] data) {
        int nScreens = data[8];
        List<ScreenInfo> infoList = new ArrayList<>(nScreens);

        int index = 9;
        int nameLength;
        ByteBuffer buf;

        for (int i = 0; i < nScreens; i++) {
            nameLength = data[index];
            buf = ByteBuffer.allocate(nameLength);
            index++;
            buf.put(data, index, nameLength);
            String v = new String( buf.array(), Charset.forName("UTF-8") );
            index += nameLength;
            infoList.add(new ScreenInfo(v, data[index], data[index + 1]));
            index += 2;
        }

        return infoList;
    }

    public String getName() {
        return name;
    }

    public int getnSegmentsX() {
        return nSegmentsX;
    }

    public int getnSegmentsY() {
        return nSegmentsY;
    }

    @Override
    public String toString() {
        return name + " (" + nSegmentsX + ", " + nSegmentsY + ")";
    }
}
