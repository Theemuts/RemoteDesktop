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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 4-9-16.
 */
public class ConnectionManager {
    private static final int BUFFER_SIZE = 1200;

    private static ConnectionManager connectionManager = new ConnectionManager();
    private static DecoderManager decoderManager = DecoderManager.getInstance();

    private boolean shutdown = false;
    private MainActivity activity;

    private ExecutorService executor;
    private List<Future<?>> tasks;

    private ConcurrentLinkedQueue<Integer> ackQueue;

    private DatagramChannel sendChannel = null;
    private DatagramSocket receiveChannel = null;

    private ConnectionInfo connectionInfo;


    private ConnectionManager() {
        ackQueue = new ConcurrentLinkedQueue<>();
        tasks = new LinkedList<>();
    }
    public void setMainActivity(MainActivity activity) {
        this.activity = activity;
    }

    public static ConnectionManager getInstance() {
        return connectionManager;
    }

    public void init(ConnectionInfo connectionInfo) throws IOException {
        shutdown = false;
        this.connectionInfo = connectionInfo;

        executor = Executors.newFixedThreadPool(2);

        tasks.add(executor.submit(new ReceiverTask()));
        tasks.add(executor.submit(new SenderTask()));
    }

    public void shutdown() throws InterruptedException {
        shutdown = true;
        if(null != executor) {
            stopSender();
            stopReceiver();

            Util.shutdownExecutor(executor, tasks);
            executor = null;
        }
    }

    public void setScreen(int screenId) {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.put((byte) 5);
        buf.put((byte) screenId);
        buf.flip();

        try {
            sendChannel.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        buf.clear();
    }

    public void setSegment(int segmentId) {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.put((byte) 2);
        buf.put((byte) segmentId);
        buf.flip();

        try {
            sendChannel.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        buf.clear();
    }

    private class ReceiverTask implements Runnable {
        private byte[] buffer;

        @Override
        public void run() {
            System.out.println("Start receiver thread");
            startReceiver();

            boolean isInterupted = Thread.currentThread().isInterrupted();
            while(!(shutdown | isInterupted)) {
                doReceive();
            }

            System.out.println("Kill receiver thread");
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
            DatagramPacket p;

            buffer = new byte[BUFFER_SIZE];
            p = new DatagramPacket(buffer, BUFFER_SIZE);

            try {
                receiveChannel.receive(p);
                if (Util.isData(p)) {
                    packetId = Util.getPacketId(p.getData());
                    ackQueue.add(packetId);
                    decoderManager.add(p);
                } else {
                    List<ScreenInfo> scr = ScreenInfo.decode(p.getData());
                    activity.setScreenInfoList(scr);
                }
            } catch (IOException e) {
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class SenderTask implements Runnable {
        ByteBuffer idsToAck = ByteBuffer.allocate(140);

        @Override
        public void run() {
            System.out.println("Sender thread");
            startSender();

            while(!(shutdown | Thread.currentThread().isInterrupted())) {
                doSend();
            }

            System.out.println("Kill sender thread");
        }

        private void startSender() {
            try {
                sendChannel = DatagramChannel.open();
                sendChannel = sendChannel.connect(new InetSocketAddress(connectionInfo.getIp(), connectionInfo.getServerPort()));

                ByteBuffer buf = ByteBuffer.allocate(1);
                buf.put((byte) 3);
                buf.flip();
                sendChannel.write(buf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void doSend() {
            Integer id;
            byte[] idB = new byte[4];
            int idV;
            int nIds = 0;

            try {
                idsToAck.clear();
                // opcode for ack: 0
                idsToAck.put((byte) 0);
                // second byte: nIds
                idsToAck.put((byte) 0);

                while (nIds < 32) {
                    id = ackQueue.poll();

                    if (null == id) {
                        break;
                    }

                    idV = id.intValue();
                    idB[0] = (byte) (idV >> 24);
                    idB[1] = (byte) (idV >> 16);
                    idB[2] = (byte) (idV >> 8);
                    idB[3] = (byte) idV;

                    idsToAck.put(idB);
                    nIds++;
                }

                if (nIds >= 1) {
                    try {
                        idsToAck.put(1, (byte) nIds);
                        idsToAck.flip();
                        sendChannel.write(idsToAck);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                       return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopSender() {
        try {
            ByteBuffer buf = ByteBuffer.allocate(1);
            buf.put((byte) 1);
            buf.flip();

            try {
                sendChannel.write(buf);
            } catch (Exception e) {
            }

            try {
                sendChannel.close();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                sendChannel = null;
            }

            buf.clear();
        } catch(Exception e) {
            e.printStackTrace();
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
}
