package com.theemuts.remotedesktop.touch.single_pointer.double_location;

import com.theemuts.remotedesktop.touch.AbstractTouchResult;

/**
 * Created by thomas on 16-10-16.
 */

public abstract class AbstractDoubleLocationTouchResult extends AbstractTouchResult implements DoubleLocation {
    protected final int x1;
    protected final int y1;
    protected final int screen1;
    protected final int segment1;
    protected final int x2;
    protected final int y2;
    protected final int screen2;
    protected final int segment2;

    AbstractDoubleLocationTouchResult(int x1, int y1, int screen1, int segment1, int x2, int y2, int screen2, int segment2) {
        this.x1 = x1;
        this.y1 = y1;
        this.screen1 = screen1;
        this.segment1 = segment1;
        this.x2 = x2;
        this.y2 = y2;
        this.screen2 = screen2;
        this.segment2 = segment2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getScreen1() {
        return screen1;
    }
    public int getScreen2() {
        return screen2;
    }
    public int getSegment1() {
        return segment1;
    }
    public int getSegment2() {
        return segment2;
    }
}
