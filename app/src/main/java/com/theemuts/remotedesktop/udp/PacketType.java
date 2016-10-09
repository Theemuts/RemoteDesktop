package com.theemuts.remotedesktop.udp;

/**
 * Created by thomas on 24-9-16.
 */

public enum PacketType {
    HANDSHAKE_REPLY,
    SCREEN_INFO,
    IMAGE_DATA,
    DISCONNECT_ACK;
}
