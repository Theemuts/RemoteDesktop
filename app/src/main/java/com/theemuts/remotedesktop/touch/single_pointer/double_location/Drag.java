package com.theemuts.remotedesktop.touch.single_pointer.double_location;

import com.theemuts.remotedesktop.touch.TouchType;

/**
 * Created by thomas on 16-10-16.
 */

public final class Drag extends AbstractDoubleLocationTouchResult {
    public Drag(int x1, int y1, int screen1, int segment1, int x2, int y2, int screen2, int segment2) {
        super(x1, y1, screen1, segment1, x2, y2, screen2, segment2);
    }

    public TouchType getType() {
        return TouchType.DRAG;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(50);
        builder.append("Drag from (")
                .append(x1)
                .append(", ")
                .append(y1)
                .append(") on screen ")
                .append(screen1)
                .append(".")
                .append(segment1)
                .append(" to (")
                .append(x2)
                .append(", ")
                .append(y2)
                .append(") on screen ")
                .append(screen2)
                .append(".")
                .append(segment2);
        return builder.toString();
    }
}
