package com.theemuts.remotedesktop.touch;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.theemuts.remotedesktop.MainActivity;
import com.theemuts.remotedesktop.image.VideoView;
import com.theemuts.remotedesktop.touch.single_pointer.double_location.Drag;
import com.theemuts.remotedesktop.touch.single_pointer.single_location.DoubleClick;
import com.theemuts.remotedesktop.touch.single_pointer.single_location.LeftClick;
import com.theemuts.remotedesktop.touch.single_pointer.single_location.RightClick;
import com.theemuts.remotedesktop.udp.ConnectionManager;
import com.theemuts.remotedesktop.util.ScreenInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.sqrt;

/**
 * Created by thomas on 16-10-16.
 */

public final class TouchEventSequence {
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();

    private boolean isZoomed;
    private boolean isDrag;
    private ClickMode clickMode = ClickMode.LEFT_CLICK;
    private TimedMotionEvent first;
    private TimedMotionEvent last;

    private MainActivity mainActivity;

    private CountDownTimer timer = null;
    private VideoView videoView;
    private int zoomX;
    private int zoomY;

    private MultiPointerEvent events = new MultiPointerEvent();

    private final static List<ScreenInfo> screenInfoList = new ArrayList<>(12);
    private int currentScreen, currentSegment;

    private boolean hasPendingDrag = false;
    private int pendingXDrag, pendingYDrag, pendingScreenDrag, pendingSegmentDrag;

    public TouchEventSequence(VideoView videoView) {
        this.videoView = videoView;
        isZoomed = false;
        isDrag = false;
        zoomX = -1;
        zoomY = -1;
    }

    public void setScreenInfoList(List<ScreenInfo> screenInfoList) {
        this.screenInfoList.clear();

        for(ScreenInfo screenInfo: screenInfoList) {
            this.screenInfoList.add(screenInfo);
        }
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setCurrentView(int currentScreen, int currentSegment) {
        this.currentScreen = currentScreen;
        this.currentSegment = currentSegment;
    }

    public void add(MotionEvent e) {
        if(events.nEvents == 0) {
            first = new TimedMotionEvent(e);
            last = first;
        } else {
            last = new TimedMotionEvent(e);
        }

        events.addEvent(last);

        if ( timer != null) {
            timer.cancel();
            timer = null;
        }

        if (last.action == MotionEvent.ACTION_UP && events.nEvents < 4 && last.time - first.time < 250) {
            timer = new CountDownTimer(300, 300) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    send();
                }
            };

            timer.start();
        } else if(last.action == MotionEvent.ACTION_UP) {
            send();
        }
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
            case DRAG:
                System.out.println("+++ " + seq);
                Drag x = (Drag) seq;
                connectionManager.drag(x.getX1(), x.getY1(), x.getScreen1(), x.getSegment1(), x.getX2(), x.getY2(), x.getScreen2(), x.getSegment2());
                break;
            default:
                break;
        }

        events = new MultiPointerEvent();
    }

    private void zoomIn() {
        int[] xyZoom = videoView.setZoom(first.x[first.mainPointer], first.y[first.mainPointer]);
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
        events.calculateDeviationAndDistance();
        return events.getAction();
    }

    private class TimedMotionEvent {
        public long time;
        public int action;
        public int[] x;
        public int mainPointer;
        public int nPointers;
        public int[] y;
        public int pointerIds[];

        public TimedMotionEvent(MotionEvent e) {
            time = System.currentTimeMillis();
            action = e.getActionMasked();
            mainPointer = e.getPointerId(e.getActionIndex());
            nPointers = e.getPointerCount();

            x = new int[nPointers];
            y = new int[nPointers];
            pointerIds = new int[nPointers];

            int x0 = zoomX < 0 ? 0 : zoomX;
            int y0 = zoomY < 0 ? 0 : zoomY;

            int div = (zoomX < 0 ? 2 : 8);

            for (int i = 0; i < nPointers; i++) {
                pointerIds[i] = e.getPointerId(i);
                x[i] = x0 + (int) e.getX(i) / div;
                y[i] = y0 + (int) (e.getY(i) - 88) / div;
            }
        }
    }

    private class MultiPointerEvent {
        private List<List<List<Integer>>> xSequences;
        private List<List<List<Integer>>> ySequences;

        private List<List<Float>> maxDeviation;
        private List<List<Float>> distance;
        private List<List<Long>> duration;

        private int nPointers;

        public int nEvents = 0;

        MultiPointerEvent() {
            xSequences = new ArrayList<>(5);
            ySequences = new ArrayList<>(5);
            duration = new ArrayList<>(5);
        }

        void addEvent(TimedMotionEvent e) {
            nEvents++;
            int[] pointers = e.pointerIds;
            int[] x = e.x;
            int[] y = e.y;
            int pointer;

            if (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_POINTER_UP) {
                duration.get(e.mainPointer).set(0, e.time - duration.get(e.mainPointer).get(0));
            }

            if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_POINTER_DOWN) {
                if (e.mainPointer == duration.size()) {
                    List<Long> newPointerDuration = new LinkedList<>();
                    newPointerDuration.add(e.time);
                    duration.add(newPointerDuration);
                } else {
                    ((LinkedList) xSequences.get(e.mainPointer)).addFirst(new ArrayList<Integer>(100));
                    ((LinkedList) ySequences.get(e.mainPointer)).addFirst(new ArrayList<Integer>(100));
                    ((LinkedList) duration.get(e.mainPointer)).addFirst(e.time);
                }
            }

            for (int i = 0; i < pointers.length; i++) {
                pointer = pointers[i];

                if (pointer == xSequences.size()) {
                    xSequences.add(new LinkedList<List<Integer>>());
                    ySequences.add(new LinkedList<List<Integer>>());

                    xSequences.get(pointer).add(new ArrayList<Integer>(100));
                    ySequences.get(pointer).add(new ArrayList<Integer>(100));
                }

                xSequences.get(pointer).get(0).add(x[i]);
                ySequences.get(pointer).get(0).add(y[i]);
            }
        }

        void calculateDeviationAndDistance() {
            float x0, xN, y0, yN, a, b, deviation, previousX, previousY, currentX, currentY;
            List<List<Integer>> xSuperSeq, ySuperSeq;
            List<Integer> xSeq, ySeq;

            nPointers = xSequences.size();

            if (xSequences.size() != ySequences.size()) {
                return;
            }

            maxDeviation = new ArrayList<>(xSequences.size());
            distance = new ArrayList<>(xSequences.size());

            for(int i = 0; i < xSequences.size(); i++) {
                xSuperSeq = xSequences.get(i);
                ySuperSeq = ySequences.get(i);

                if (xSuperSeq.size() != ySuperSeq.size()) {
                    return;
                }

                maxDeviation.add(new ArrayList<Float>(xSuperSeq.size()));
                distance.add(new ArrayList<Float>(xSuperSeq.size()));

                for (int j = 0; j < xSuperSeq.size(); j++) {
                    distance.get(i).add(0.0f);
                    maxDeviation.get(i).add(0.0f);

                    xSeq = xSuperSeq.get(j);
                    ySeq = ySuperSeq.get(j);

                    x0 = xSeq.get(0);
                    y0 = ySeq.get(0);

                    xN = xSeq.get(xSeq.size() - 1);
                    yN = ySeq.get(ySeq.size() - 1);

                    a = (yN - y0) / (xN - x0);
                    b = yN - a * xN;

                    previousX = x0;
                    previousY = y0;

                    for (int k = 1; k < xSeq.size() - 1; k++) {
                        currentX = xSeq.get(k);
                        currentY = ySeq.get(k);

                        deviation = a * currentX - currentY + b;
                        deviation /= sqrt(a * a + 1);
                        deviation = abs(deviation);

                        if (deviation >= maxDeviation.get(i).get(j)) {
                            maxDeviation.get(i).set(j, deviation);
                        }

                        distance.get(i).set(j,distance.get(i).get(j) + (float) sqrt((currentX - previousX) * (currentX - previousX) + (currentY - previousY) * (currentY - previousY)));
                        previousX = currentX;
                        previousY = currentY;
                    }
                }
            }
        }

        TouchInterface getAction() {
            int dx;
            int dy;

            if (nPointers == 1) {
                if (duration.get(0).size() == 1) {
                    if(distance.get(0).get(0) < 5) {
                        if (duration.get(0).get(0) < 250) {
                            switch (clickMode) {
                                case LEFT_CLICK:
                                    return new LeftClick((first.x[last.mainPointer] + last.x[last.mainPointer])/2, (first.y[first.mainPointer] +last.y[last.mainPointer])/2);
                                case RIGHT_CLICK:
                                    return new RightClick((first.x[last.mainPointer] + last.x[last.mainPointer])/2, (first.y[first.mainPointer] +last.y[last.mainPointer])/2);
                                case LEFT_CLICK_DRAG:
                                    System.out.println("+++ Left Click (drag mode)");
                                    if(hasPendingDrag) {
                                        hasPendingDrag = false;
                                        return new Drag(pendingXDrag,
                                                pendingYDrag,
                                                pendingScreenDrag,
                                                pendingSegmentDrag,
                                                (first.x[last.mainPointer] + last.x[last.mainPointer])/2,
                                                (first.y[first.mainPointer] +last.y[last.mainPointer])/2,
                                                currentScreen,
                                                currentSegment);
                                    }

                                    pendingXDrag = (first.x[last.mainPointer] + last.x[last.mainPointer])/2;
                                    pendingYDrag = (first.y[first.mainPointer] +last.y[last.mainPointer])/2;
                                    pendingScreenDrag = currentScreen;
                                    pendingSegmentDrag = currentSegment;

                                    hasPendingDrag = true;
                                    return new NoOp();
                                case RIGHT_CLICK_DRAG:
                                    System.out.println("+++ Right Click (drag mode)");
                                    break;
                            }
                        } else {
                            if(!isZoomed) {
                                zoomIn();
                                System.out.println("+++ Zoom Mode on");
                            } else {
                                zoomOut();
                                System.out.println("+++ Zoom Mode off");
                            }
                        }
                    } else {
                        if (distance.get(0).get(0) > 100  && maxDeviation.get(0).get(0) < 15) {
                            dx = xSequences.get(0).get(0).get(0);
                            dx -= xSequences.get(0).get(0).get(xSequences.get(0).get(0).size() - 1);

                            dy = ySequences.get(0).get(0).get(0);
                            dy -= ySequences.get(0).get(0).get(ySequences.get(0).get(0).size() - 1);

                            ScreenInfo screenInfo = screenInfoList.get(currentScreen);
                            int nSegments = screenInfo.getnSegmentsX() * screenInfo.getnSegmentsY();

                            if (dx == 0) {
                                if (dy > 0) {
                                    System.out.println("+++ Change segment down " + currentSegment);
                                    currentSegment += screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                    System.out.println("+++ Changed segment down " + currentSegment);
                                } else {
                                    System.out.println("+++ Change segment up " + currentSegment);
                                    currentSegment += nSegments - screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                    System.out.println("+++ Changed segment up " + currentSegment);
                                }

                                return new NoOp();
                            }

                            double a = (double) dy / (double) dx;
                            a = (1 - 2*atan(a)/PI)/2;

                            if (a > 0.9 || a < 0.1) {
                                if (dy > 0) {
                                    System.out.println("+++ Change segment down");
                                    currentSegment += screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                } else {
                                    System.out.println("+++ Change segment up");
                                    currentSegment += nSegments - screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                }

                                return new NoOp();
                            }

                            if (a > 0.4 && a < 0.6) {
                                if (dx > 0) {
                                    System.out.println("+++ Change segment right");
                                    if (currentSegment % screenInfo.getnSegmentsX() == screenInfo.getnSegmentsX() - 1) {
                                        currentSegment -= screenInfo.getnSegmentsX();
                                    }

                                    currentSegment++;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                } else {
                                    System.out.println("+++ Change segment left");
                                    if (currentSegment % screenInfo.getnSegmentsX() == 0) {
                                        currentSegment += screenInfo.getnSegmentsX();
                                    }

                                    currentSegment--;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                }

                                return new NoOp();
                            }

                            if (a > 0.65 && a < 0.85) {
                                if (dx > 0) {
                                    System.out.println("+++ Change segment up-right");
                                    currentSegment += nSegments - screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;

                                    if (currentSegment % screenInfo.getnSegmentsX() == screenInfo.getnSegmentsX() - 1) {
                                        currentSegment -= screenInfo.getnSegmentsX();
                                    }

                                    currentSegment++;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                } else {
                                    System.out.println("+++ Change segment down-left");
                                    currentSegment += screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;

                                    if (currentSegment % screenInfo.getnSegmentsX() == 0) {
                                        currentSegment += screenInfo.getnSegmentsX();
                                    }

                                    currentSegment--;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                }

                                return new NoOp();
                            }

                            if (a > 0.15 && a < 0.35) {
                                if (dx > 0) {
                                    System.out.println("+++ Change segment down-right");
                                    currentSegment += screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;

                                    if (currentSegment % screenInfo.getnSegmentsX() == screenInfo.getnSegmentsX() - 1) {
                                        currentSegment -= screenInfo.getnSegmentsX();
                                    }

                                    currentSegment++;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                } else {
                                    System.out.println("+++ Change segment up-left");
                                    currentSegment += nSegments - screenInfo.getnSegmentsX();
                                    currentSegment %= nSegments;

                                    if (currentSegment % screenInfo.getnSegmentsX() == 0) {
                                        currentSegment += screenInfo.getnSegmentsX();
                                    }

                                    currentSegment--;
                                    mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                                }

                                return new NoOp();
                            }

                        }
                    }

                    return new NoOp();
                }

                if (duration.get(0).size() == 2 &&
                        distance.get(0).get(0) < 5 &&
                        distance.get(0).get(1) < 20) {
                    dx = xSequences.get(0).get(0).get(0);
                    dx -= xSequences.get(0).get(1).get(0);

                    dy = ySequences.get(0).get(0).get(0);
                    dy -= ySequences.get(0).get(1).get(0);

                    if (abs(dx) < 20 && abs(dy) < 20) {
                        return new DoubleClick((first.x[last.mainPointer] + last.x[last.mainPointer])/2, (first.y[first.mainPointer] +last.y[last.mainPointer])/2);
                    }

                    return new NoOp();
                }
            }

            if (nPointers == 2) {
                if(duration.get(0).size() == 1 &&
                        duration.get(0).get(0) > 250 &&
                        duration.get(1).size() == 1 &&
                        duration.get(1).get(0) < 250 &&
                        distance.get(0).get(0) < 5 &&
                        distance.get(1).get(0) < 5)
                {
                    if(isDrag) {
                        if (clickMode == ClickMode.LEFT_CLICK_DRAG) {
                            clickMode = ClickMode.LEFT_CLICK;
                        } else {
                            clickMode = ClickMode.RIGHT_CLICK;
                        }
                        System.out.println("+++ Drag mode off");
                    } else {
                        if (clickMode == ClickMode.LEFT_CLICK) {
                            clickMode = ClickMode.LEFT_CLICK_DRAG;
                        } else {
                            clickMode = ClickMode.RIGHT_CLICK_DRAG;
                        }
                        System.out.println("+++ Drag mode on");
                    }
                    isDrag = !isDrag;
                    return new NoOp();
                }

                if(duration.get(0).size() == 1 &&
                        duration.get(0).get(0) > 250 &&
                        distance.get(0).get(0) < 5 &&
                        duration.get(1).size() == 1 &&
                        duration.get(1).get(0) > 250 &&
                        distance.get(1).get(0) < 5)
                {
                    switch (clickMode) {
                        case RIGHT_CLICK:
                            System.out.println("+++ Left click mode");
                            clickMode = ClickMode.LEFT_CLICK;
                            break;
                        case LEFT_CLICK:
                            System.out.println("+++ Right click mode");
                            clickMode = ClickMode.RIGHT_CLICK;
                            break;
                        case RIGHT_CLICK_DRAG:
                            System.out.println("+++ Left click drag mode");
                            clickMode = ClickMode.LEFT_CLICK_DRAG;
                            break;
                        case LEFT_CLICK_DRAG:
                            System.out.println("+++ Right click drag mode");
                            clickMode = ClickMode.RIGHT_CLICK_DRAG;
                            break;
                    }

                    return new NoOp();
                }

                dx = xSequences.get(0).get(0).get(0);
                dx -= xSequences.get(0).get(0).get(xSequences.get(0).get(0).size() - 1);
                int dx2 = xSequences.get(1).get(0).get(0);
                dx2 -= xSequences.get(1).get(0).get(xSequences.get(1).get(0).size() - 1);

                if
                        (duration.get(0).size() == 1 &&
                        distance.get(0).get(0) > 100  &&
                        maxDeviation.get(0).get(0) < 15 &&
                        abs(dx) < 50 &&
                        duration.get(1).size() == 1 &&
                        distance.get(1).get(0) > 100  &&
                        maxDeviation.get(1).get(0) < 15 &&
                        abs(dx2) < 50)
                {
                    dy = ySequences.get(0).get(0).get(0);
                    dy -= ySequences.get(0).get(0).get(ySequences.get(0).get(0).size() - 1);

                    int dy2 = ySequences.get(1).get(0).get(0);
                    dy2 -= ySequences.get(1).get(0).get(ySequences.get(1).get(0).size() - 1);

                    if(dy > 0 && dy2 > 0 ) {
                        System.out.println("+++ Change Screen Up");
                        currentScreen += screenInfoList.size() - 1;
                        currentScreen %= screenInfoList.size();
                        currentSegment = 0;
                        mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                    } else {
                        System.out.println("+++ Change Screen Down");
                        currentScreen++;
                        currentScreen %= screenInfoList.size();
                        currentSegment = 0;
                        mainActivity.setScreenAndSegment(currentScreen, currentSegment);
                    }

                    return new NoOp();
                }
            }

            return new NoOp();
        }
    }
}
