package com.theemuts.remotedesktop.udp;

import com.theemuts.remotedesktop.MainActivity;
import com.theemuts.remotedesktop.decoder.DecoderManager;
import com.theemuts.remotedesktop.util.ConnectionInfo;
import com.theemuts.remotedesktop.util.ScreenInfo;
import com.theemuts.remotedesktop.util.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by thomas on 4-9-16.
 */
public class ConnectionManager {
    private static final int BUFFER_SIZE = 1200;
    private static final byte MIN_VERSION = 1;
    private static final byte MAX_VERSION = 1;

    private static ConnectionManager connectionManager = new ConnectionManager();
    private static DecoderManager decoderManager = DecoderManager.getInstance();

    private MainActivity activity;

    private ExecutorService executor;
    private List<Future<?>> tasks;

    private ConcurrentLinkedQueue<Integer> ackQueue;
    private ConcurrentLinkedQueue<UDPMessage> msgQueue;

    private DatagramChannel sendChannel = null;
    private DatagramSocket receiveChannel = null;

    private ConnectionInfo connectionInfo;

    private boolean heartbeat = false;
    private long lastHeartbeat = 0;

    public void setMainActivity(MainActivity activity) {
        this.activity = activity;
    }

    public static ConnectionManager getInstance() {
        return connectionManager;
    }

    public void init(ConnectionInfo connectionInfo) throws IOException {
        this.connectionInfo = connectionInfo;

        ackQueue = new ConcurrentLinkedQueue<>();
        msgQueue = new ConcurrentLinkedQueue<>();
        tasks = new LinkedList<>();

        executor = Executors.newFixedThreadPool(2);

        tasks.add(executor.submit(new ReceiverTask()));
        tasks.add(executor.submit(new SenderTask()));
    }

    public void shutdown(boolean exit) throws InterruptedException {
        if (null != msgQueue)
            msgQueue.clear();

        if (null != sendChannel) {
            if(exit) {
                sendExitMessage();
            } else {
                sendShutdownMessage();

            }
            stopSender();
        }

        if (null != receiveChannel)
            stopReceiver();

        if(null != executor) {
            Util.shutdownExecutor(executor, tasks);
            executor = null;
        }

        ackQueue = null;
        msgQueue = null;
        tasks = null;
    }

    public void stop() {
        if (null != msgQueue)
            msgQueue.clear();

        if (null != sendChannel) {
            stopSender();
        }

        if (null != receiveChannel)
            stopReceiver();

        if(null != executor) {
            Util.shutdownExecutor(executor, tasks);
            executor = null;
        }

        ackQueue = null;
        msgQueue = null;
        tasks = null;
    }

    public void requestScreenInfo() {
        System.out.println("request screen info");
        if(null != msgQueue)
            msgQueue.add(new UDPMessage(UDPMessageType.REQUEST_SCREEN_INFO));
    }

    public void setScreenAndSegment(int screenId, int segmentId) {
        if(null != msgQueue) {
            UDPMessage msg = new UDPMessage(UDPMessageType.REQUEST_VIEW);
            msg.extendMessage((byte) screenId, (byte) segmentId);
            msgQueue.add(msg);
        }
    }

    public void refreshImage() {
        if(null != msgQueue)
            msgQueue.add(new UDPMessage(UDPMessageType.REFRESH));
    }

    public void closeStream() {
        if(null != msgQueue) {
            msgQueue.add(new UDPMessage(UDPMessageType.CLOSE));
        }
    }

    public void heartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void leftClick(int x, int y) {
        if(null != msgQueue) {
            UDPMessage msg = new UDPMessage(UDPMessageType.LEFT_CLICK);
            msg.extendMessage((byte) (x >> 8), (byte) x, (byte) (y >> 8), (byte) y);
            msgQueue.add(msg);
        }
    }

    public void rightClick(int x, int y) {
        if(null != msgQueue) {
            UDPMessage msg = new UDPMessage(UDPMessageType.RIGHT_CLICK);
            msg.extendMessage((byte) (x >> 8), (byte) x, (byte) (y >> 8), (byte) y);
            msgQueue.add(msg);
        }
    }

    public void doubleClick(int x, int y) {
        if(null != msgQueue) {
            UDPMessage msg = new UDPMessage(UDPMessageType.DOUBLE_CLICK);
            msg.extendMessage((byte) (x >> 8), (byte) x, (byte) (y >> 8), (byte) y);
            msgQueue.add(msg);
        }
    }

    public void drag(int x1, int y1, int screen1, int segment1, int x2, int y2, int screen2, int segment2) {
        if(null != msgQueue) {
            UDPMessage msg = new UDPMessage(UDPMessageType.DRAG);
            msg.extendMessage((byte) (x1 >> 8), (byte) x1, (byte) (y1 >> 8), (byte) y1, (byte) screen1, (byte) segment1,
                    (byte) (x2 >> 8), (byte) x2, (byte) (y2 >> 8), (byte) y2, (byte) screen2, (byte) segment2);
            msgQueue.add(msg);
        }
    }

    public void exitServer() {
        if(null != msgQueue)
            msgQueue.add(new UDPMessage(UDPMessageType.EXIT));
    }

    private void sendShutdownMessage() {
        try {
            sendChannel.write((new UDPMessage(UDPMessageType.CLOSE)).getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendExitMessage() {
        try {
            sendChannel.write((new UDPMessage(UDPMessageType.EXIT)).getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ReceiverTask implements Runnable {
        private byte[] buffer;

        @Override
        public void run() {
            System.out.println("+++ Start receiver thread");
            startReceiver();

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    doReceive();
                } catch (NullPointerException e) {
                    break;
                }
            }

            System.out.println("+++ Kill receiver thread");
        }

        private void startReceiver() {
            try {
                receiveChannel = new DatagramSocket(connectionInfo.getClientPort());
                receiveChannel.setReceiveBufferSize(250000);
                receiveChannel.setSoTimeout(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void doReceive() {
            Integer packetId;

            buffer = new byte[BUFFER_SIZE];
            DatagramPacket p = new DatagramPacket(buffer, BUFFER_SIZE);

            try {
                receiveChannel.receive(p);

                switch (Util.getType(p)) {
                    case HANDSHAKE_REPLY:
                        handleHandshake(p);
                        activity.setConnectedConnectButtonListeners();
                        return;
                    case SCREEN_INFO:
                        System.out.println("+++ Screen info");
                        List<ScreenInfo> scr = ScreenInfo.decode(p);
                        activity.setScreenInfoList(scr);
                        activity.setConnectedQuitButtonListeners();
                        return;
                    case IMAGE_DATA:
                        packetId = Util.getPacketId(p.getData());
                        ackQueue.add(packetId);
                        decoderManager.add(p);
                        return;
                    case DISCONNECT_ACK:
                        System.out.println("+++ Disconnect.");
                        activity.setDisconnectedConnectButtonListeners();
                        stop();
                        return;
                }
            } catch ( NullPointerException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleHandshake(DatagramPacket p) {
            System.out.println("+++ Handle handshake");
            byte reply = p.getData()[1];

            if (reply == 0) {
                // TODO: what to do if connection rejected?
            } else if (reply >= MIN_VERSION && reply <= MAX_VERSION) {
                requestScreenInfo();
            } else {
                // TODO: Protocol version not supported.
            }
        }
    }

    private class SenderTask implements Runnable {
        @Override
        public void run() {
            System.out.println("+++ Sender thread");
            startSender();

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    doSend();
                } catch (RuntimeException e) {
                    break;
                }
            }

            System.out.println("+++ Kill sender thread");
        }

        private void startSender() {
            try {
                sendChannel = DatagramChannel.open();
                sendChannel = sendChannel.connect(new InetSocketAddress(connectionInfo.getIp(), connectionInfo.getServerPort()));

                UDPMessage handshake = new UDPMessage(UDPMessageType.HANDSHAKE);
                handshake.extendMessage(MIN_VERSION, MAX_VERSION);
                sendChannel.write(handshake.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void doSend() {
            Integer id;
            int nIds = 0;

            if(heartbeat && (System.currentTimeMillis() -lastHeartbeat) > 1000) {
                msgQueue.add(new UDPMessage(UDPMessageType.HEARTBEAT));
                lastHeartbeat = System.currentTimeMillis();
            }

            try {
                UDPMessage msg = msgQueue.poll();

                if(msg != null) {
                    sendChannel.write(msg.getMessage());
                }

                UDPMessage ackMessage = new UDPMessage(UDPMessageType.ACKNOWLEDGE);
                ackMessage.extendMessage((byte) 0);

                while (nIds <= 127) {
                    id = ackQueue.poll();

                    if (null == id) {
                        break;
                    }

                    ackMessage.extendMessage(
                            (byte) (id >> 24),
                            (byte) (id >> 16),
                            (byte) (id >> 8),
                            id.byteValue()
                    );

                    nIds++;
                }

                if (nIds >= 1) {
                    ackMessage.changeValue(1, (byte) nIds);
                    sendChannel.write(ackMessage.getMessage());
                } else if(msg == null) {
                    // Only sleep if no messages handled.
                    Thread.sleep(5);
                }
            } catch (NullPointerException e) {
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //throw new RuntimeException();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopSender() {
        try {
            sendChannel.close();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            sendChannel = null;
        }

    }

    private void stopReceiver() {
        try {
            receiveChannel.close();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            receiveChannel = null;
        }
    }

    private class UDPMessage {
        private ByteBuffer message;

        UDPMessage(UDPMessageType type) {
            message = ByteBuffer.allocate(type.getBufferSize() + 1).put(type.getId());
        }

        public void extendMessage(byte... data) {
            message = message.put(data);
        }

        public void changeValue(int index, byte data) {
            message.put(index, data);
        }

        public ByteBuffer getMessage() {
            return (ByteBuffer) message.flip();
        }
    }

    private enum UDPMessageType {
        HANDSHAKE((byte) 0, 2),

        REQUEST_SCREEN_INFO((byte) 1),
        REQUEST_VIEW((byte) 2, 2),
        REFRESH((byte) 3),

        CLOSE((byte) 4),
        EXIT((byte) 5),

        LEFT_CLICK((byte) 6, 4),
        RIGHT_CLICK((byte) 7, 4),
        DOUBLE_CLICK((byte) 8, 4),
        DRAG((byte) 9, 12),

        KEYBOARD((byte) 10, 1200),

        ACKNOWLEDGE((byte) 11, 1200),
        HEARTBEAT((byte) 12);


        private byte id;
        private int bufferSize;

        UDPMessageType(byte id) {
            this.id = id;
            bufferSize = 0;
        }

        UDPMessageType(byte id, int bufferSize) {
            this.id = id;
            this.bufferSize = bufferSize;
        }

        public byte getId() {
            return id;
        }

        public int getBufferSize() {
            return bufferSize;
        }
    }
}
