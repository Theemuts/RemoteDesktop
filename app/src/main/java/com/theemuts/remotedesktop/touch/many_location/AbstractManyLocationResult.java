package com.theemuts.remotedesktop.touch.many_location;

/**
 * Created by thomas on 17-10-16.
 */

public abstract class AbstractManyLocationResult
        implements ManyLocation
{
    Direction direction;

    AbstractManyLocationResult(Direction direction) {
        this.direction = direction;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }
}
