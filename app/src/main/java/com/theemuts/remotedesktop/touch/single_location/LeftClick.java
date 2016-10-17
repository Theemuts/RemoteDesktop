package com.theemuts.remotedesktop.touch.single_location;

import com.theemuts.remotedesktop.touch.TouchType;

/**
 * Created by thomas on 16-10-16.
 */

public final class LeftClick
        extends AbstractSingleLocationTouchResult
{
    public LeftClick(int x, int y) {
        super(x, y);
    }

    public TouchType getType() {
        return TouchType.LEFT_CLICK;
    }
}
