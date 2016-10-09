package com.theemuts.remotedesktop;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.theemuts.remotedesktop.decoder.DecoderManager;
import com.theemuts.remotedesktop.image.VideoView;
import com.theemuts.remotedesktop.udp.ConnectionManager;
import com.theemuts.remotedesktop.util.ConnectionInfo;
import com.theemuts.remotedesktop.util.ScreenInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {

    //private static final BitmapManager bitmapManager = BitmapManager.getInstance();
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private static final DecoderManager decoderManager = DecoderManager.getInstance();

    EditText input;

    int currentScreen = 0;
    VideoView videoView;

    SetScreenButton setScreenButton;
    SetSegmentButton setSegmentButton;
    RefreshButton refreshButton;
    KeyboardButton keyboardButton;

    QuitButton quitButton = new QuitButton();
    ConnectButton connectButton = new ConnectButton();

    private final ReentrantLock screenInfoReplyLock = new ReentrantLock();

    private final Condition screenInfoExists = screenInfoReplyLock.newCondition();

    final static List<ScreenInfo> screenInfoList = new ArrayList<>(12);

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout rel = (RelativeLayout) findViewById(R.id.relativeLayout);
        videoView = new VideoView(this);
        rel.addView(videoView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Set the initial image and init udp sockets
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void setScreenInfoList(List<ScreenInfo> newInfo) {
        screenInfoReplyLock.lock();

        try {
            screenInfoList.clear();

            for (ScreenInfo sc : newInfo) {
                screenInfoList.add(sc);
            }

            screenInfoExists.signal();
        } finally {
            screenInfoReplyLock.unlock();
        }
    }

    public void setConnectedQuitButtonListeners() {
        quitButton.setAfterInitListener();
    }

    public void setConnectedConnectButtonListeners() {
        connectButton.setAfterConnectListener();
    }

    public void setDisconnectedConnectButtonListeners() {

        input = new EditText(this);
        connectButton.setBeforeConnectListener();
    }

    private String getAction(int act) {
        String action;

        switch(act) {
            case MotionEvent.ACTION_DOWN:
                action = "DOWN";
                break;
            case MotionEvent.ACTION_UP:
                action = "UP";
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "POINTER DOWN";
                break;
            case MotionEvent.ACTION_POINTER_UP:
                action = "POINTER UP";
                break;
            case MotionEvent.ACTION_MOVE:
                action = "MOVE";
                break;
            default:
                action = "?";
                break;
        }

        return action;
    }

    @Override
    public void onStart() {
        super.onStart();

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                StringBuilder b = new StringBuilder();
                b.append(getAction(event.getActionMasked()));
                b.append(": (");
                b.append(event.getX());
                b.append(", ");
                b.append(event.getY());
                b.append(")");
                System.out.println(b.toString());

                return true;
            }
        });

        connectionManager.setMainActivity(this);

        setScreenButton = new SetScreenButton();
        setScreenButton.create();
        setScreenButton.setVisibility(View.INVISIBLE);

        setSegmentButton = new SetSegmentButton();
        setSegmentButton.create();
        setSegmentButton.setVisibility(View.INVISIBLE);

        refreshButton = new RefreshButton();
        refreshButton.create();
        refreshButton.setVisibility(View.INVISIBLE);

        keyboardButton = new KeyboardButton();
        keyboardButton.create();
        keyboardButton.setVisibility(View.INVISIBLE);

        input = new EditText(this);
        connectButton.create();
        connectButton.setBeforeConnectListener();

        quitButton.create();
        quitButton.setBeforeInitListener();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.theemuts.remotedesktop/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.theemuts.remotedesktop/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }



    private class InitUDPAndDecoders extends AsyncTask<ConnectionInfo, Void, Integer> {
        protected Integer doInBackground(ConnectionInfo... params) {
            try {
                connectionManager.init(params[0]);
                decoderManager.init(videoView);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }

    }
    private class Refresh extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            connectionManager.refreshImage();
            return null;
        }
    }

    private int screenNumber;


    private class SetScreenAndSegment extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... params) {
            connectionManager.setScreenAndSegment(params[0], params[1]);

            return null;
        }
    }

    private class ConnectButton {
        Button connectButton;


        private String mText = "";

        private View.OnClickListener connectListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                connectionAlert();
            }
        };

        private View.OnClickListener disconnectListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // disconnect
                try {
                    (new StopStream()).execute().get();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    decoderManager.clear();
                    videoView.restartHandler();
                    videoView.reset();
                }

            }
        };

        private DialogInterface.OnClickListener connectOkListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mText = input.getText().toString();
                screenInfoList.clear();

                try {
                    ConnectionInfo con = new ConnectionInfo(mText);
                    (new InitUDPAndDecoders()).execute(con).get();

                    screenInfoReplyLock.lock();

                    try {
                        while (screenInfoList.size() == 0)
                            screenInfoExists.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        screenInfoReplyLock.unlock();
                    }

                    dialog.dismiss();
                    screenAlert();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        };

        private DialogInterface.OnClickListener screenListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                screenNumber = which;

                dialog.dismiss();
                segmentAlert();
            }
        };

        private DialogInterface.OnClickListener segmentListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int segmentNumber) {
                (new SetScreenAndSegment()).execute(screenNumber, segmentNumber);
                dialog.dismiss();
            }
        };

        public void create() {
            connectButton = (Button) findViewById(R.id.connectButton);
        }

        public void setBeforeConnectListener() {
            connectButton.post(new Runnable() {
                @Override
                public void run() {
                    setScreenButton.setVisibility(View.INVISIBLE);
                    setSegmentButton.setVisibility(View.INVISIBLE);
                    refreshButton.setVisibility(View.INVISIBLE);
                    keyboardButton.setVisibility(View.INVISIBLE);

                    connectButton.setText("Connect");
                    connectButton.setOnClickListener(connectListener);
                }
            });
        }

        public void setAfterConnectListener() {
            connectButton.post(new Runnable() {
                @Override
                public void run() {
                    setScreenButton.setVisibility(View.VISIBLE);
                    setSegmentButton.setVisibility(View.VISIBLE);
                    refreshButton.setVisibility(View.VISIBLE);
                    //keyboardButton.setVisibility(View.VISIBLE); // Not visible yet, not implemented.

                    connectButton.setText("Disconnect");
                    connectButton.setOnClickListener(disconnectListener);
                }
            });
        }

        private void connectionAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Connect to IP");

            input.setText("192.168.1.133:9998:36492");
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            builder.setView(input);

            builder.setPositiveButton("OK", connectOkListener);
            builder.setNegativeButton("Cancel", cancelListener);

            builder.show();
        }

        private void screenAlert() {
            int nScreens = screenInfoList.size();
            CharSequence[] screens = new CharSequence[nScreens];

            for (int i = 0; i < nScreens; i++) {
                screens[i] = screenInfoList.get(i).getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(R.string.select_screen)
                   .setItems(screens, screenListener)
                   .create()
                   .show();
        }

        private void segmentAlert() {
            ScreenInfo current = screenInfoList.get(currentScreen);
            int nSegments = current.getnSegmentsX() * current.getnSegmentsY();
            CharSequence[] segments = new CharSequence[nSegments];

            for (int j = 0; j < current.getnSegmentsY(); j++) {
                for (int i = 0; i < current.getnSegmentsX(); i++) {
                    segments[j * current.getnSegmentsX() + i] = "(" + i + ", " + j + ")";
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.select_segment)
                   .setItems(segments, segmentListener)
                   .create()
                   .show();
        }

        private class StopStream extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
                connectionManager.closeStream();
                return null;
            }
        }
    }

    private class SetScreenButton {
        Button setScreenButton;

        private View.OnClickListener screenListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                screenAlert();
            }
        };

        private DialogInterface.OnClickListener screenClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                screenNumber = which;

                dialog.dismiss();
                segmentAlert();
            }
        };

        private DialogInterface.OnClickListener segmentListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int segmentNumber) {
                (new SetScreenAndSegment()).execute(screenNumber, segmentNumber);
                dialog.dismiss();
            }
        };

        private void create() {
            setScreenButton = (Button) findViewById(R.id.setScreenButton);
            setScreenButton.setOnClickListener(screenListener);
        }

        private void setVisibility(int visibility) {
            setScreenButton.setVisibility(visibility);
        }

        private void screenAlert() {
            int nScreens = screenInfoList.size();
            CharSequence[] screens = new CharSequence[nScreens];

            for (int i = 0; i < nScreens; i++) {
                screens[i] = screenInfoList.get(i).getName();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(R.string.select_screen)
                    .setItems(screens, screenClickListener)
                    .create()
                    .show();
        }

        private void segmentAlert() {
            ScreenInfo current = screenInfoList.get(currentScreen);
            int nSegments = current.getnSegmentsX() * current.getnSegmentsY();
            CharSequence[] segments = new CharSequence[nSegments];

            for (int j = 0; j < current.getnSegmentsY(); j++) {
                for (int i = 0; i < current.getnSegmentsX(); i++) {
                    segments[j * current.getnSegmentsX() + i] = "(" + i + ", " + j + ")";
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.select_segment)
                    .setItems(segments, segmentListener)
                    .create()
                    .show();
        }
    }

    private class SetSegmentButton {
        Button setSegmentButton;

        private View.OnClickListener segmentListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                segmentAlert();
            }
        };

        private DialogInterface.OnClickListener segmentClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int segmentNumber) {
                (new SetScreenAndSegment()).execute(screenNumber, segmentNumber);
                dialog.dismiss();
            }
        };

        private void create() {
            setSegmentButton = (Button) findViewById(R.id.setSegmentButton);
        }

        private void setVisibility(int visibility) {
            setSegmentButton.setVisibility(visibility);
            setSegmentButton.setOnClickListener(segmentListener);
        }

        private void segmentAlert() {
            ScreenInfo current = screenInfoList.get(currentScreen);
            int nSegments = current.getnSegmentsX() * current.getnSegmentsY();
            CharSequence[] segments = new CharSequence[nSegments];

            for (int j = 0; j < current.getnSegmentsY(); j++) {
                for (int i = 0; i < current.getnSegmentsX(); i++) {
                    segments[j * current.getnSegmentsX() + i] = "(" + i + ", " + j + ")";
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.select_segment)
                    .setItems(segments, segmentClickListener)
                    .create()
                    .show();
        }
    }

    private class RefreshButton {
        Button refreshButton;

        private View.OnClickListener refreshListener = new View.OnClickListener() {
            public void onClick(View view) {
                (new Refresh()).execute();
            }
        };

        private void create() {
            refreshButton = (Button) findViewById(R.id.refreshImageButton);
            refreshButton.setOnClickListener(refreshListener);
        }

        private void setVisibility(int visibility) {
            refreshButton.setVisibility(visibility);
        }
    }

    private class KeyboardButton {
        Button keyboardButton;

        private void create() {
            keyboardButton = (Button) findViewById(R.id.openKeyboardButton);
        }

        private void setVisibility(int visibility) {
            keyboardButton.setVisibility(visibility);
        }
    }

    private class QuitButton {
        Button quitButton;

        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            try {
                (new QuitApp()).execute();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                finish();
                dialog.dismiss();
            }
            }
        };

        DialogInterface.OnClickListener cancelistener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        };

        DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    (new ExitApp()).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finish();
                    dialog.dismiss();
                }
            }
        };

        View.OnClickListener beforeInitListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = basicExitDialiog();
                builder.show();

            }
        };

        View.OnClickListener afterInitListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = basicExitDialiog();
                builder.setNegativeButton("Stop server", exitListener);
                builder.show();

            }
        };

        public void create() {
            quitButton = (Button) findViewById(R.id.quitButton);
        }

        public void setBeforeInitListener() {
            quitButton.setOnClickListener(beforeInitListener);
        }

        public void setAfterInitListener() {
            quitButton.setOnClickListener(afterInitListener);
        }

        private AlertDialog.Builder basicExitDialiog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Quit or exit app?");

            builder.setPositiveButton("Exit client", quitListener);
            builder.setNeutralButton("Cancel", cancelistener);

            return builder;
        }

        private class QuitApp extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
                try {
                    connectionManager.shutdown(false);
                    decoderManager.shutdown();
                    //bitmapManager.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }

        private class ExitApp extends AsyncTask<Void, Void, Void> {
            protected Void doInBackground(Void... params) {
                try {
                    connectionManager.shutdown(true);
                    decoderManager.shutdown();
                    //bitmapManager.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }
    }



    static {
        System.loadLibrary("remote-desktop");
    }
}
