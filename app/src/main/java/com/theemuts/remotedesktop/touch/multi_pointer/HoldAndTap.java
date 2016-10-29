package com.theemuts.remotedesktop.touch.multi_pointer;

import com.theemuts.remotedesktop.touch.TouchType;

/**
 * Created by thomas on 18-10-16.
 */

public class HoldAndTap implements MultiPointer {
    @Override
    public TouchType getType() {
        return TouchType.CHANGE_DRAG_MODE;
    }
}
