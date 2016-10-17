package com.theemuts.remotedesktop.touch;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.theemuts.remotedesktop.image.VideoView;
import com.theemuts.remotedesktop.touch.single_location.DoubleClick;
import com.theemuts.remotedesktop.touch.single_location.LeftClick;
import com.theemuts.remotedesktop.touch.single_location.RightClick;
import com.theemuts.remotedesktop.udp.ConnectionManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by thomas on 16-10-16.
 */

public final class TouchEventSequence {
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();

    public static final boolean LEFT_MODE = true;
    public static final boolean RIGHT_MODE = false;

    private boolean isZoomed;
    private boolean clickMode = LEFT_MODE;
    private TimedMotionEvent first;
    private TimedMotionEvent last;
    private List<TimedMotionEvent> eventSequence;

    private CountDownTimer timer = null;
    private VideoView videoView;
    private int zoomX;
    private int zoomY;

    public TouchEventSequence(VideoView videoView) {
        this.videoView = videoView;
        isZoomed = false;
        zoomX = -1;
        zoomY = -1;
        eventSequence = new LinkedList<>();
    }

    public void add(MotionEvent e) {
        if(eventSequence.size() == 0) {
            first = new TimedMotionEvent(e);
            last = new TimedMotionEvent(e);
        } else {
            last = new TimedMotionEvent(e);
        }

        if (last.type == MotionEvent.ACTION_UP) {
            timer = new CountDownTimer(500, 500) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    send();
                }
            };

            timer.start();
        } else if ( timer != null) {
            timer.cancel();

        }

        eventSequence.add(last);
    }

    public long getFirstTime() {
        return first == null ? 0 : first.time;
    }

    public long getLastTime() {
        return last == null ? 0 : last.time;
    }

    private void send() {
        System.out.println("+++ Duration" + (getLastTime() - getFirstTime()));
        TouchInterface seq = parseSequence();

        switch (seq.getType()) {
            case LEFT_CLICK:
                LeftClick u = (LeftClick) seq;
                connectionManager.leftClick(u.getX(), u.getY());
                break;
            case RIGHT_CLICK:
                RightClick v = (RightClick) seq;
                connectionManager.rightClick(v.getX(), v.getY());
                break;
            case DOUBLE_CLICK:
                DoubleClick w = (DoubleClick) seq;
                connectionManager.doubleClick(w.getX(), w.getY());
                break;
            default:
                break;
        }

        eventSequence.clear();

    }

    private void zoomIn() {
        int[] xyZoom = videoView.setZoom(first.x, first.y);
        zoomX = xyZoom[0];
        zoomY = xyZoom[1];
        isZoomed = true;
    }

    private void zoomOut() {
        videoView.setZoom(-1, -1);
        zoomX = -1;
        zoomY = -1;
        isZoomed = false;
    }


    @NonNull
    private TouchInterface parseSequence() {
        if(eventSequence.size() == 2) {
            if ((last.time - first.time) > 250) {
                if (!isZoomed) {
                    zoomIn();
                } else {
                    zoomOut();
                }

                return new NoOp();
            } else {
                if (clickMode == LEFT_MODE) {
                    return new LeftClick(last.x, last.y);
                }

                if (clickMode == RIGHT_MODE) {
                    return new RightClick(last.x, last.y);
                }
            }
        }

        if(eventSequence.size() == 4 && eventSequence.get(1).type == MotionEvent.ACTION_UP) {
            return new DoubleClick((first.x + last.x)/2, (first.y +last.y)/2);
        }

        for(int i = 1; i < eventSequence.size() - 1; i++) {
            if(eventSequence.get(i).type != MotionEvent.ACTION_MOVE) {
                return new NoOp();
            }
        }

        return new NoOp();
        //return new Drag(first.x, first.y, last.x, last.y);
    }

    private class TimedMotionEvent {
        public long time;
        public int type;
        public int x;
        public int y;

        public TimedMotionEvent(MotionEvent e) {
            time = System.currentTimeMillis();
            type = e.getAction();
            x = (zoomX < 0 ? 0 : zoomX) + (int) e.getX() / (zoomX < 0 ? 2 : 8);
            y = (zoomY < 0 ? 0 : zoomY) + (int) (e.getY() - 88) / (zoomY < 0 ? 2 : 8);
        }
    }
}
