package com.theemuts.remotedesktop;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    //private static final BitmapManager bitmapManager = BitmapManager.getInstance();
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private static final DecoderManager decoderManager = DecoderManager.getInstance();

    //ImageView img;

    int currentScreen = 0;
    VideoView videoView;
    Button screenButton;
    Button segmentButton;
    Button refreshButton;
    Button keyboardButton;


    Button connectButton;
    Button quitButton;

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

    private void initImage() {
        /*img = (ImageView) findViewById(R.id.imageView);

        bitmapManager.setView(img);
        bitmapManager.setBitmap();
        img.setOnTouchListener(new View.OnTouchListener() {
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
        });*/
    }

    public void setScreenInfoList(List<ScreenInfo> newInfo) {
        screenInfoList.clear();

        for (ScreenInfo sc: newInfo) {
            screenInfoList.add(sc);
        }
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
        //img = (ImageView) findViewById(R.id.imageView);
        //img.setImageResource(0);

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
        videoView.restartHandler();

        screenButton = (Button) findViewById(R.id.setScreenButton);
        segmentButton  = (Button) findViewById(R.id.setSegmentButton);
        refreshButton = (Button) findViewById(R.id.refreshImageButton);
        keyboardButton = (Button) findViewById(R.id.openKeyboardButton);
        connectButton = (Button) findViewById(R.id.connectButton);
        quitButton = (Button) findViewById(R.id.quitButton);

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            String mText = "";

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Connect to IP");

                final EditText input = new EditText(MainActivity.this);
                input.setText("192.168.1.133:9998:36492");
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mText = input.getText().toString();
                        try {
                            ConnectionInfo con = new ConnectionInfo(mText);
                            (new InitUDP()).execute(con).get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        initImage();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        screenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int nScreens = screenInfoList.size();
                CharSequence[] screens = new CharSequence[nScreens];

                for (int i = 0; i < nScreens; i++) {
                    screens[i] = screenInfoList.get(i).getName();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(R.string.select_screen)
                        .setItems(screens, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (currentScreen != which) {
                                    currentScreen = which;
                                    (new SetScreen()).execute(which);
                                }
                            }
                        });

                builder.create().show();
            }
        });

        segmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        .setItems(segments, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                (new SetSegment()).execute(which);
                            }
                        });

                builder.create().show();
            }
        });

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

        try {
            (new QuitApp()).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private class InitUDP extends AsyncTask<ConnectionInfo, Void, Integer> {
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

    private class QuitApp extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            try {
                connectionManager.shutdown();
                decoderManager.shutdown();
                //bitmapManager.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class SetScreen extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... params) {
            connectionManager.setScreen(params[0]);

            return null;
        }
    }

    private class SetSegment extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... params) {
            connectionManager.setSegment(params[0]);

            return null;
        }
    }

    static {
        System.loadLibrary("remote-desktop");
    }
}
