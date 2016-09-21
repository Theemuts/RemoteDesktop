package com.theemuts.remotedesktop.decoder;

import java.util.List;

/**
 * Created by thomas on 12-9-16.
 */
public class DecodedPacket {
    List<int[]> decodedData;
    int packetId;
    int timestamp;

    public DecodedPacket(List<int[]> decodedData, int packetId, int timestamp) {
        this.decodedData = decodedData;
        this.packetId = packetId;
        this.timestamp = timestamp;
    }

    public List<int[]> getDecodedData() { return decodedData; }
    public int getPacketId() { return packetId; }
    public int getTimestamp() { return timestamp; }
}
