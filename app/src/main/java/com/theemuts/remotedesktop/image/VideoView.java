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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private SurfaceHolder holder;
    private Bitmap bmp;

    private BitmapHandler handler;
    private Future<?> handlerTask;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean shutdown;
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

    public void add(DecodedPacket p) {
        if (null != handler) handler.add(p);
    }

    public void restartHandler() {
        shutdown = false;

        if(handlerTask != null) {
            if(!(handlerTask.isCancelled() | handlerTask.isDone())) {
                handlerTask.cancel(true);
            }
        }

        if(executor != null) {
            if(!(executor.isShutdown() | executor.isTerminated())) {
                executor.shutdownNow();
            }
        }

        executor = Executors.newSingleThreadExecutor();
        //handlerTask = executor.submit(new BitmapHandler());
    }

    // Init video view
    private void init() {
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
        //handlerTask = executor.submit(new BitmapHandler());
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
            int canvasHeight = canvas.getHeight();
            int delta = (canvasHeight - 2 * HEIGHT)/2;

            Rect src = new Rect(0, 0, WIDTH, HEIGHT);
            Rect dest = new Rect(0, delta, CANVAS_WIDTH, 736 + delta);

            canvas.drawBitmap(bmp, src, dest, null);
        }
    }

    private class BitmapHandler implements Runnable {
        private ConcurrentLinkedDeque<DecodedPacket> newDataQueue;
        private DecodedPacket block;

        private boolean allowRender = false;
        private boolean newData = false;
        private long lastRender = 0;
        private long timeDiff = 0;


        @Override
        public void run() {
            handler = this;
            newDataQueue = new ConcurrentLinkedDeque<>();

                while (!(shutdown|Thread.currentThread().isInterrupted())) {
                    try {
                        timeDiff = System.currentTimeMillis() - lastRender - FRAME_DURATION;

                        if (allowRender & newData & timeDiff >= 0) {
                            redraw();
                            lastRender = System.currentTimeMillis() - timeDiff;
                            timeDiff = -FRAME_DURATION;
                            newData = false;
                        }

                        allowRender = false;
                        block = newDataQueue.poll();

                        if (null != block) {
                            newData = true;
                            updateBitmap(block);
                        } else {
                            allowRender = true;

                            try {
                                if (timeDiff >= -SLEEP_DURATION & timeDiff < 0) {
                                    Thread.sleep(-timeDiff);
                                } else if (timeDiff < -SLEEP_DURATION) {
                                    Thread.sleep(SLEEP_DURATION);
                                }
                            } catch (InterruptedException e) {
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            System.out.println("Exit bmp handler");
        }

        public void add(DecodedPacket p) {
            newDataQueue.add(p);
        }

        private void updateBitmap(DecodedPacket packet) {
            int index, nBlocksX, blockX, blockY, offset, offset0, width, timestamp, currentTimestamp;

            timestamp = packet.getTimestamp();

            for(int[]decodedPixels: packet.getDecodedData()) {
                index = decodedPixels[256];
                currentTimestamp = currentVersion[index];

                if(currentTimestamp < timestamp) {
                    nBlocksX = WIDTH / 16;
                    blockX = index % nBlocksX;
                    blockY = index / nBlocksX;

                    offset0 = 16 * (blockY * WIDTH + blockX);

                    // TODO: refactor data order after IDCT...
                    for (int block = 0; block < 4; block++) {
                        for (int row = 0; row < 8; row++) {
                            switch (block) {
                                case 0:
                                    offset = offset0 +  row * WIDTH;
                                    break;
                                case 1:
                                    offset = offset0 + row * WIDTH + 8;
                                    break;
                                case 2:
                                    offset = offset0 + (row + 8) * WIDTH;
                                    break;
                                default:
                                    offset = offset0 + (row + 8) * WIDTH + 8;
                                    break;
                            }


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
