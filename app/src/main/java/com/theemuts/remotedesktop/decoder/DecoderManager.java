package com.theemuts.remotedesktop.decoder;

import android.provider.ContactsContract;

import com.theemuts.remotedesktop.exception.InvalidDataException;
import com.theemuts.remotedesktop.image.VideoView;
import com.theemuts.remotedesktop.util.Util;

import java.net.DatagramPacket;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 4-9-16.
 */
public class DecoderManager {
    private static final int N_DECODERS = 2;
    private static final DecoderManager manager = new DecoderManager();

    private boolean init = false;

    /*
     * The DecoderManager maintains a PriorityBlockingQueue to be able to
     * handle packets with the largest timestamp and smallest packetID first.
     *
     * This ordering is chosen because packets from a more recent screenshot
     * are more relevant, and the smaller packetIDs of that timestamp have
     * larger block errors.
     */
    private PriorityBlockingQueue<DatagramPacket> pending;

    private List<Future<?>> decoderThreads = new LinkedList<>();

    private VideoView view;
    /*
     * The DecoderManager handles two decoding threads.
     */
    private ExecutorService executor;

    /*
     * We only want one DecoderManager to exist, so we apply the singleton
     * pattern.
     */
    private DecoderManager() {
    }

    /*
     * Get a reference to the DecoderManager.
     */
    public static DecoderManager getInstance() {
        return manager;
    }

    /*
     * Initialize the executor and create the two decoders.
     */

    public void init(VideoView view) {
        System.out.println("Start decoder manager");
        this.view = view;

        if(!init) {
            pending = new PriorityBlockingQueue<>(200, new PacketComparator());
            init = true;
        }

        if (executor == null) {
            executor = Executors.newFixedThreadPool(N_DECODERS);

            for (int i = 0; i < N_DECODERS; i++) {
                decoderThreads.add(executor.submit(new DecoderTask()));
            }
        }
    }

    public void shutdown() {
        System.out.println("Exit decoder manager");

        if (null != pending) {
            pending.clear();
            pending = null;
        }

        if(executor != null) {
            Util.shutdownExecutor(executor, decoderThreads);
            executor = null;
        }
    }

    public void clear() {
        if(pending != null) {
            pending.clear();
        }
    }

    /*
     * Add a new packet to the pending queue. These packets are automatically
     * sorted by timestamp and packetID.
     */
    public void add(DatagramPacket packet) {
        pending.add(packet);
    }

    private class PacketComparator implements Comparator<DatagramPacket> {

        @Override
        public int compare(DatagramPacket p1, DatagramPacket p2) {
            byte[] p1Data = p1.getData();
            byte[] p2Data = p2.getData();

            short v1;
            short v2;

            // TODO:    Technically, overflow *can* be a problem for very,
            // TODO:    very long-running streams.

            // First four bytes: timestamp. Prefer larger ( = newer).
            for (int i = 0; i < 4; i++) {
                v1 = Util.toUnsigned(p1Data[i]);
                v2 = Util.toUnsigned(p2Data[i]);

                if (v1 - v2 != 0) {
                    return v2 - v1;
                }
            }

            // Next four bytes: packetID. Prefer smaller ( = larger error.)
            for (int i = 5; i < 8; i++) {
                v1 = Util.toUnsigned(p1Data[i]);
                v2 = Util.toUnsigned(p2Data[i]);

                if (v1 - v2 != 0) {
                    return v1 - v2;
                }
            }

            return 0;
        }
    }

    private class DecoderTask implements Runnable {
        /*
         * DecoderTasks are created by the DecoderManager. Each instance
         * represents one thread which decodes received packets.
         *
         * While running, it polls the DecoderManager's queue of unhandled
         * packets, decodes it, and sends the results to the bitmap manager to be
         * displayed.
         */
        @Override
        public void run() {
            System.out.println("+++ Start decoder thread");
            PacketDecoder decoder = new PacketDecoder();

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket p = pending.take();
                    DecodedPacket decoded = decoder.decode(p);
                    view.add(decoded);
                } catch (InterruptedException e) {
                    System.out.println("+++ Exit decoder by interrupted exception");
                    return;
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("+++ Exit decoder by null mainPointer exception");
                    return;
                }
            }

            System.out.println("+++ Exit decoder normally");
        }
    }
}
