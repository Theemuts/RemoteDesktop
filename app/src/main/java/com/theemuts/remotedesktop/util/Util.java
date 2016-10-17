package com.theemuts.remotedesktop.util;

import com.theemuts.remotedesktop.exception.InvalidPacketException;
import com.theemuts.remotedesktop.udp.PacketType;

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


    static public PacketType getType(DatagramPacket p) throws InvalidPacketException {
        switch (p.getData()[0]) {
            case 0x0:
                return PacketType.HANDSHAKE_REPLY;
            case 0x1:
                return PacketType.SCREEN_INFO;
            case 0x2:
                return PacketType.IMAGE_DATA;
            case 0x3:
                return PacketType.DISCONNECT_ACK;
            default:
                throw new InvalidPacketException();
        }
    }

    static public Integer getPacketId(byte[] data) {
        return getId(data, 5);
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
        if(task != null) {
            if (!(task.isDone() | task.isCancelled())) task.cancel(true);
        }
        executor.shutdown();

        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(1, TimeUnit.SECONDS))
                    System.err.println("+++ Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static String convertToBitstring(long data) {
        StringBuilder builder = new StringBuilder(71);

        for (long i = 63; i >= 0; i--) {
            builder.append((data & ((long) 1 << i)) == 0 ? '0' : '1');

            if(i % 8 == 0 && i != 0) {
                builder.append(' ');
            }
        }

        return builder.toString();
    }

    public static String[] convertToBitstring(long[] datas) {
        String[] result = new String[datas.length];
        long data;
        StringBuilder builder;

        for(int i = 0; i < datas.length; i++) {
            data = datas[i];
            builder = new StringBuilder(71);

            for (long j = 63; j >= 0; j--) {
                builder.append((data & ((long) 1 << j)) == 0 ? '0' : '1');

                if (j % 8 == 0 && j != 0) {
                    builder.append(' ');
                }
            }

            result[i] = builder.toString();
        }

        return result;
    }

    public static String convertToBitstring(int data) {
        StringBuilder builder = new StringBuilder(35);

        for (int i = 31; i >= 0; i--) {
            builder.append((data & (1 << i)) == 0 ? '0' : '1');

            if(i % 8 == 0 && i != 0) {
                builder.append(' ');
            }
        }

        return builder.toString();
    }

    public static String convertToBitstring(short data) {
        StringBuilder builder = new StringBuilder(17);

        for (short i = 15; i >= 0; i--) {
            builder.append((data & (1 << i)) == 0 ? '0' : '1');

            if(i % 8 == 0 && i != 0) {
                builder.append(' ');
            }
        }

        return builder.toString();
    }

    public static String convertToBitstring(byte data) {
        StringBuilder builder = new StringBuilder(8);

        for (short i = 7; i >= 0; i--) {
            builder.append((data & (1 << i)) == 0 ? '0' : '1');
        }

        return builder.toString();
    }

    public static String convertToBitstring(byte[] data) {
        return  convertToBitstring(data, data.length);
    }

    public static String convertToBitstring(byte[] data, int length) {
        return  convertToBitstring(data, length, 0);
    }

    public static String convertToBitstring(byte[] data, int length, int offset) {
        StringBuilder builder = new StringBuilder(9 * length);
        int mask = 1;
        byte element;

        for (int j = offset; j < offset+length; j++) {
            element = data[j];

            for (int i = 7; i >= 0; i--) {
                int intEl = element;
                if (intEl < 0) intEl += 256;

                if ((intEl & (mask << i)) == 0) {
                    builder.append(0);
                } else {
                    builder.append(1);
                }
            }

            builder.append(" ");
        }

        builder.deleteCharAt(9 * length - 1);
        return builder.toString();
    }
}
