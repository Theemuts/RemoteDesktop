package com.theemuts.remotedesktop.touch.double_location;

import com.theemuts.remotedesktop.touch.TouchInterface;

/**
 * Created by thomas on 16-10-16.
 */

public interface DoubleLocation extends TouchInterface {
    int getX1();
    int getY1();
    int getX2();
    int getY2();
}
