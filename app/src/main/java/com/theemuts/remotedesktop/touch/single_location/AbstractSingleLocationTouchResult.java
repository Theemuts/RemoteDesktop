package com.theemuts.remotedesktop.touch.single_location;

import com.theemuts.remotedesktop.touch.AbstractTouchResult;

/**
 * Created by thomas on 16-10-16.
 */

public abstract class AbstractSingleLocationTouchResult
        extends AbstractTouchResult
        implements SingleLocation
{
    private final int x;
    private final int y;

    AbstractSingleLocationTouchResult(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
