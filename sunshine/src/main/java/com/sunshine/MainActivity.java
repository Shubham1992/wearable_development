package com.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener,
        CapabilityApi.CapabilityListener {

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    private static final String DATA_PATH = "/data_path";
    private TextView mTextView;
    private GoogleApiClient mGoogleClientApi;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "TAG";
    private TextView high_textview;
    private double high_temp;
    private TextView time_tv;
    private LinearLayout mRectBackground, mRoundBackground;
    private TextView date_tv;
    private TextView forecast_textview, low_textview;
    private double low_temp;
    private String forecast_text;

    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mRectBackground = (LinearLayout) findViewById(R.id.rect_layout);
                mRoundBackground = (LinearLayout) findViewById(R.id.round_layout);
                setupViews();}
        });




    }

    private void setupViews() {

        high_textview = (TextView) findViewById(R.id.high_textview);
        forecast_textview = (TextView) findViewById(R.id.forecast_textview);
        low_textview = (TextView) findViewById(R.id.low_textview);

        time_tv = (TextView)findViewById(R.id.time_tv);
        date_tv = (TextView) findViewById(R.id.date_tv);

        Date date= new Date();
        time_tv.setText(DateFormat.format("hh:mm", date.getTime()).toString());
        date_tv.setText(DateFormat.format("dd MM yyyy", new Date()).toString());


        Clock c=new Clock(this);
        c.AddClockTickListner(new OnClockTickListner() {

            @Override
            public void OnSecondTick(Time currentTime) {
                Log.d("Tick Test per Second", DateFormat.format("h:mm:ss aa ", currentTime.toMillis(true)).toString());

            }

            @Override
            public void OnMinuteTick(Time currentTime) {
                Log.d("Tick Test per Minute",DateFormat.format("hh:mm", currentTime.toMillis(true)).toString());
                if(time_tv!=null)
                    time_tv.setText(DateFormat.format("hh:mm", currentTime.toMillis(true)).toString());
                date_tv.setText(DateFormat.format("dd-MM-yyyy", new Date()).toString());
            }
        });

    }

    private void setValuesOnViews() {
        high_textview.setText(""+high_temp);
        low_textview.setText(""+low_temp);
        forecast_textview.setText(""+forecast_text);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

        Clock c=new Clock(this);
        c.AddClockTickListner(new OnClockTickListner() {

            @Override
            public void OnSecondTick(Time currentTime) {
                Log.d("Tick Test per Second", DateFormat.format("h:mm:ss aa ", currentTime.toMillis(true)).toString());

            }

            @Override
            public void OnMinuteTick(Time currentTime) {
                Log.d("Tick Test per Minute",DateFormat.format("hh:mm", currentTime.toMillis(true)).toString());
                if(time_tv!=null)
                    time_tv.setText(DateFormat.format("hh:mm", currentTime.toMillis(true)).toString());
            }
        });

    }

    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.CapabilityApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.CapabilityApi.addListener(
                mGoogleApiClient, this, Uri.parse("wear://"), CapabilityApi.FILTER_REACHABLE);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged(): " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (DataLayerListenerService.DATA_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    high_temp = dataMapItem.getDataMap().getDouble("maxTmp");
                    low_temp = dataMapItem.getDataMap().getDouble("minTmp");
                    forecast_text = dataMapItem.getDataMap().getString("forecast");


                    setValuesOnViews();
                } else {
                    LOGD(TAG, "Unrecognized path: " + path);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {

            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived: " + messageEvent);

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals(DATA_PATH)) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }
}
