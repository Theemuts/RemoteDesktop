package com.theemuts.remotedesktop.touch.many_location;

import com.theemuts.remotedesktop.touch.TouchType;

/**
 * Created by thomas on 17-10-16.
 */

public final class ChangeSegment
        extends AbstractManyLocationResult
{
    public ChangeSegment(Direction direction) {
        super(direction);
    }

    @Override
    public TouchType getType() {
        return TouchType.CHANGE_SEGMENT;
    }
}
