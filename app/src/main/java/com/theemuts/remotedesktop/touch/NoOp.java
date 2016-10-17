package com.theemuts.remotedesktop.touch;

/**
 * Created by thomas on 16-10-16.
 */

public final class NoOp implements TouchInterface {

    public TouchType getType() {
        return TouchType.NO_OP;
    }
}
