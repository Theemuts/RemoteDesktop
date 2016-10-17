package com.theemuts.remotedesktop.touch.many_location;

import com.theemuts.remotedesktop.touch.TouchInterface;
import com.theemuts.remotedesktop.touch.single_location.SingleLocation;

/**
 * Created by thomas on 17-10-16.
 */

public interface ManyLocation
        extends TouchInterface
{
    Direction getDirection();
}
