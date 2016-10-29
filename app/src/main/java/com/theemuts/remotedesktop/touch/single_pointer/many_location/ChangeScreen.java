package com.theemuts.remotedesktop.touch.single_pointer.many_location;

import com.theemuts.remotedesktop.touch.TouchType;

/**
 * Created by thomas on 17-10-16.
 */

public class ChangeScreen
        extends AbstractManyLocationResult
{
    public ChangeScreen(Direction direction) {
        super(direction);
    }

    @Override
    public TouchType getType() {
        return TouchType.CHANGE_SCREEN;
    }
}
