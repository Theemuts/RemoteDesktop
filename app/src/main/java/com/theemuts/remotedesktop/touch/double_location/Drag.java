package com.theemuts.remotedesktop.touch.double_location;

import com.theemuts.remotedesktop.touch.AbstractTouchResult;
import com.theemuts.remotedesktop.touch.TouchType;

/**
 * Created by thomas on 16-10-16.
 */

public final class Drag extends AbstractDoubleLocationTouchResult {
    public Drag(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }

    public TouchType getType() {
        return TouchType.DRAG;
    }
}
