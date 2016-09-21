package com.theemuts.remotedesktop.util;

import java.net.DatagramPacket;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 4-9-16.
 */
public class Util {
    static public boolean isData(DatagramPacket p) {
        byte[] data = p.getData();

        return !(
                (data[0] == -1) &
                (data[1] == -1) &
                (data[2] == -1) &
                (data[3] == -1) &
                (data[4] == -1) &
                (data[7] == -1)
        );
    }

    static public Integer getTimestamp(byte[] data) {
        return getId(data, 0);
    }

    static public Integer getPacketId(byte[] data) {
        return getId(data, 4);
    }

    static public short toUnsigned(byte val) {
        if (val < 0) {
            short sVal = (short) val;
            sVal += 256;
            return sVal;
        }

        return (short) val;
    }

    static private Integer getId(byte[] data, int shift) {
        int[] els = {0, 0, 0, 0};

        for (int i = 0; i < 4; i++) {
            if (data[i + shift] < 0) {
                els[i] = (int) data[i + shift];
                els[i] += 256;
            } else {
                els[i] = data[i + shift];
            }
        }

        return els[0] << 24 | els[1] << 16 | els[2] << 8 | els[3];
    }

    public static void shutdownExecutor(ExecutorService executor, List<Future<?>> tasks) {
        for(Future task: tasks) {
            if(!(task.isDone()|task.isCancelled())) task.cancel(true);
        }

        executor.shutdown();

        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(1, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static void shutdownExecutor(ExecutorService executor, Future<?> task) {
        if(!(task.isDone()|task.isCancelled())) task.cancel(true);
        executor.shutdown();

        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(1, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
