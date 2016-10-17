package com.theemuts.remotedesktop.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import com.theemuts.remotedesktop.decoder.DecodedPacket;
import com.theemuts.remotedesktop.util.Util;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by thomas on 21-9-16.
 */

public class VideoView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int FRAME_DURATION = 100;
    private static final long SLEEP_DURATION = 5;

    private static final int WIDTH = 640;
    private static final int CANVAS_WIDTH = 1280;
    private static final int HEIGHT = 368;
    private static final int PIXELS = WIDTH*HEIGHT;
    private static final int N_BLOCKS = PIXELS / 256;
    private static final int N_BLOCKS_X = WIDTH / 16;

    private SurfaceHolder holder;
    private Bitmap bmp;

    private Lock zoomLock = new ReentrantLock();
    private volatile boolean zoomedIn = false;
    public volatile int zoomX = 0;
    public volatile int zoomY = 0;


    private BitmapHandler handler;
    private Future<?> handlerTask;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean shutdown;
    private boolean heightSet = false;

    private int[] data = new int[PIXELS];
    private int[] currentVersion = new int[N_BLOCKS];

    //Constructors

    public VideoView(Context context) {
        super(context);
        init();
    }

    public VideoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public VideoView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    public VideoView(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        init();
    }

    public int[] setZoom(int x, int y) {
        zoomLock.lock();

        try {
            if(x >= 0 && y >= 0){
                zoomedIn = true;

                zoomX = x - (WIDTH / 8);
                zoomX = zoomX < 0 ? 0 : zoomX;
                zoomX = zoomX + WIDTH / 4 > WIDTH? 3*WIDTH/4 : zoomX;

                zoomY = y - HEIGHT / 8;
                zoomY = zoomY < 0 ? 0 : zoomY;
                zoomY = zoomY + HEIGHT / 4 > HEIGHT ? 3*HEIGHT/4 : zoomY;
            } else {
                zoomedIn = false;
                zoomX = 0;
                zoomY = 0;
            }
        } finally {
            zoomLock.unlock();
        }

        surfaceChanged(holder, PixelFormat.RGBA_8888, WIDTH, HEIGHT);

        return new int[] { zoomX, zoomY };
    }

    public void add(DecodedPacket p) {
        handler.add(p);
    }

    public void shutdown() {
        System.out.println("+++ Shutdown video handler");
        shutdown = true;
        Util.shutdownExecutor(executor, handlerTask);
        shutdown = false;
    }

    public void restartHandler() {
        System.out.println("+++ Restart video handler");
        shutdown();
        for (int i = 0; i < PIXELS/256; i++) {
            data[i] = 0xFFFFFFFF;
            currentVersion[i] = -1;
        }

        for (int i = PIXELS/256; i < PIXELS; i++) {
            data[i] = i*8;
        }

        bmp.setPixels(data, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        executor = Executors.newSingleThreadExecutor();
        handlerTask = executor.submit(new BitmapHandler());
    }

    // Init video view
    private void init() {
        System.out.println("+++ Init video handler");
        holder = getHolder();
        holder.addCallback(this);

        bmp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        bmp.setHasAlpha(false);

        for (int i = 0; i < PIXELS/256; i++) {
            data[i] = 0xFFFFFFFF;
            currentVersion[i] = -1;
        }

        for (int i = PIXELS/256; i < PIXELS; i++) {
            data[i] = i*8;
        }

        bmp.setPixels(data, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
    }

    public void reset() {
        handler.clear();

        for (int i = 0; i < PIXELS/256; i++) {
            data[i] = 0xFFFFFFFF;
            currentVersion[i] = -1;
        }

        for (int i = PIXELS/256; i < PIXELS; i++) {
            data[i] = i*8;
        }

        handler.redraw();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(!heightSet) {
            holder.setFixedSize(CANVAS_WIDTH, getHeight());
            heightSet = true;
        }

        Canvas canvas = surfaceHolder.lockCanvas();
        drawSomething(canvas);

        try {
            surfaceHolder.unlockCanvasAndPost(canvas);
        } catch(Exception e) {

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Canvas canvas = surfaceHolder.lockCanvas();
        drawSomething(canvas);

        try {
            surfaceHolder.unlockCanvasAndPost(canvas);
        } catch(Exception e) {

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        shutdown = true;
        Util.shutdownExecutor(executor, handlerTask);
    }

    protected void drawSomething(Canvas canvas) {
        if(canvas != null) {
            Rect src;

            int canvasHeight = canvas.getHeight();
            int delta = (canvasHeight - 2 * HEIGHT)/2;

            zoomLock.lock();

            try {
                if (zoomedIn) {
                    src = new Rect(zoomX, zoomY, zoomX + WIDTH / 4, zoomY + HEIGHT / 4);
                } else {
                    src = new Rect(0, 0, WIDTH, HEIGHT);
                }
            } finally {
                zoomLock.unlock();
            }

            Rect dest = new Rect(0, delta, CANVAS_WIDTH, 736 + delta);
            canvas.drawBitmap(bmp, src, dest, null);
        }
    }

    private class BitmapHandler implements Runnable {
        private ConcurrentLinkedQueue<DecodedPacket> newDataQueue;
        private DecodedPacket block;

        private boolean newData = false;
        private long lastRender = 0;
        private long timeDiff = 0;

        private long firstRender = 0;


        @Override
        public void run() {
            System.out.println("+++ Start bmp handler");
            handler = this;
            newDataQueue = new ConcurrentLinkedQueue<>();
            lastRender = System.currentTimeMillis();

             while (!(shutdown || Thread.currentThread().isInterrupted())) {
                 try {
                     if(firstRender == 0 && !newData)
                         lastRender = System.currentTimeMillis();

                     timeDiff = System.currentTimeMillis() - lastRender - FRAME_DURATION;

                     if (newData && timeDiff >= 0) {
                         lastRender = System.currentTimeMillis();
                         if(firstRender == 0) firstRender = lastRender;
                         redraw();
                         timeDiff = -FRAME_DURATION;
                         newData = false;
                     }

                     block = newDataQueue.poll();

                     if (null != block) {
                         newData = true;
                         updateBitmap(block);
                     } else {
                         try {
                             if (timeDiff >= -SLEEP_DURATION & timeDiff < 0) {
                                 Thread.sleep(-timeDiff);
                             } else if (timeDiff < -SLEEP_DURATION) {
                                 Thread.sleep(SLEEP_DURATION);
                             } else if(!newData) {
                                 Thread.sleep(SLEEP_DURATION);
                             }
                         } catch (InterruptedException e) {
                         }
                     }
                 }  catch (Exception e) {
                     e.printStackTrace();
                 }
             }

            System.out.println("+++ Exit bmp handler");
        }

        public void add(DecodedPacket p) {
            newDataQueue.add(p);
        }
        public void clear() {
            newDataQueue.clear();
        }

        private void updateBitmap(DecodedPacket packet) {
            int index, blockX, blockY, offset, offset0, offset1, timestamp, currentTimestamp;
            timestamp = packet.getTimestamp();

            for(int[]decodedPixels: packet.getDecodedData()) {
                index = decodedPixels[256];
                currentTimestamp = currentVersion[index];

                if(currentTimestamp < timestamp || timestamp == 1) {
                    blockX = index % N_BLOCKS_X;
                    blockY = index / N_BLOCKS_X;

                    offset0 = 16 * (blockY * WIDTH + blockX);

                    for (int block = 0; block < 4; block++) {
                        switch (block) {
                            case 0:
                                offset1 = offset0;
                                break;
                            case 1:
                                offset1 = offset0 + 8;
                                break;
                            case 2:
                                offset1 = offset0 + (8 * WIDTH);
                                break;
                            default:
                                offset1 = offset0 + (WIDTH + 1) * 8;
                                break;
                        }

                        for (int row = 0; row < 8; row++) {
                            offset = offset1 +  row * WIDTH;

                            for (int i = 0; i < 8; i++) {
                                data[offset + i] = decodedPixels[i + 8 * row + 64 * block];
                            }
                        }
                    }

                    currentVersion[index] = timestamp;
                }
            }
        }

        private void redraw() {
            bmp.setPixels(data, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
            surfaceChanged(holder, PixelFormat.RGBA_8888, WIDTH, HEIGHT);
        }
    }
}
