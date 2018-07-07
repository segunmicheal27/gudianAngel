package com.guardian.webview.angel;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.guardian.webview.angel.storage.SessionManager;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;

import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    XWalkView wv;
    Context mContext;
    // Session Manager Class
    SessionManager session;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS;
    LocationManager locationManager;

    JSONObject msg;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

//    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
//    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Session Manager
        session = new SessionManager(this);

        sendBroadcast(new Intent(getApplicationContext(), LocationReceiver.class));
        this.mContext = MainActivity.this;




        locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        if (mLocation != null) {
            startLocationUpdates();
        }


        // turn on debugging





        if(session.isLoggedIn() && session.isReg()){


//            Toast.makeText(this, "Users Registered", Toast.LENGTH_LONG).show();


            PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            //check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if(!hasPermissions(this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                }
            }

        }



        XWalkPreferences.setValue("enable-javascript", true);
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        wv = findViewById(R.id.wv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            wv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        wv.addJavascriptInterface(new JavaScriptInterface(getApplicationContext(), wv),"reg_result");
        wv.addJavascriptInterface(new JavaScriptInterface(this, wv),"login_result");
        wv.addJavascriptInterface(new JavaScriptInterface(this, wv),"dev");
        wv.addJavascriptInterface(new JavaScriptInterface(this, wv),"req");
        wv.addJavascriptInterface(new JavaScriptInterface(this, wv),"reqType");
        wv.addJavascriptInterface(new JavaScriptInterface(this, wv),"getEmgSessType");


        if (session.isReg() || session.isLoggedIn()){

            if (session.isLoggedIn()) {
                wv.loadUrl("file:///android_asset/html/dashboard.html",null);
            }else if (session.isLoggedOut()) {
                wv.loadUrl("file:///android_asset/html/login.html",null);
            }else {
                wv.loadUrl("file:///android_asset/html/login.html",null);
            }

        }else{
            wv.loadUrl("file:///android_asset/html/index.html",null);   // now it will not fail here
        }

//        wv.clearCache(true);
        wv.setResourceClient(new ResourceClient(wv));

    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {
            session.setLatSession(String.valueOf(mLocation.getLatitude()));
            session.setLongSession(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected from main activity", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("App", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("App", "Connection failed. Error: " + connectionResult.getErrorCode());
    }


    @Override
    public void onLocationChanged(Location location) {
        startLocationUpdates();
        session.setLatSession(String.valueOf(location.getLatitude()));
        session.setLongSession(String.valueOf(location.getLongitude()));
        Log.d("App", "onLocationChanged");
            if (session.getEmgState()){
                sendListenChangeCoordinateData(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
            }

        if (checkLocation()){
            //check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(hasPermissions(this, PERMISSIONS)){
                    //report locate data Pusher listener
                    sendPusherListnerData("_request_location_channel","_request_location_event");
                    //Stop emergency Pusher listener
                    sendPusherListnerData("_stop_emg_request_channel","_stop_emg_request_event");
                }
            }else{
                //report locate data Pusher listener
                sendPusherListnerData("_request_location_channel","_request_location_event");
                //Stop emergency Pusher listener
                sendPusherListnerData("_stop_emg_request_channel","_stop_emg_request_event");
            }
        }


    }

    private class ResourceClient extends XWalkResourceClient {

        ResourceClient(XWalkView xwalkView) {
            super(xwalkView);
        }

        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);
            Log.d("App", "loadUrl Started:" + url);
        }

        public void onLoadFinished(XWalkView view, String url) {
            super.onLoadFinished(view, url);
            Log.d("App", "loadUrl Finished:" + url);

            switch (url){

                case "file:///android_asset/html/dashboard.html":


                    PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


                    if (checkLocation()){
                        //check permissions
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(!hasPermissions(getApplicationContext(), PERMISSIONS)){
                                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
                            }
                        }

                    }else{
                        checkLocation();
                    }


//                    if (session.getPanicState()) {
//                        wv.loadUrl("javascript:changeElementContent('panic', 'Stop')",null);
//                    }else {
//                        wv.loadUrl("javascript:changeElementContent('panic', 'Locate Me!')",null);
//                    }

                break;

                case "file:///android_asset/html/emergency.html":


                        wv.loadUrl("javascript:changeInputElementContent('user_id', '"+ session.getUser_id() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('device_id', '"+ session.getDeviceID() +"')",null);

                        wv.loadUrl("javascript:changeInputElementContent('name', '"+ session.getName() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('email', '"+ session.getEmail() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('phone', '"+ session.getPhone() +"')",null);

                    if (session.getEmgType().equalsIgnoreCase("Medical")){

                        wv.loadUrl("javascript:changeElementContent('sp_amb', 'Stop medical request')",null);

//                        wv.loadUrl("javascript:hideElement('amb_parent', 'none','hidden')",null);
//                        wv.loadUrl("javascript:hideElement('amb_parent_stop', 'block','show')",null);

                    }else if (session.getEmgType().equalsIgnoreCase("Security")){

                        wv.loadUrl("javascript:changeElementContent('sp_hosp', 'Stop security request')",null);

//                        wv.loadUrl("javascript:hideElement('host_parent_stop', 'block','show')",null);
//                        wv.loadUrl("javascript:hideElement('host_parent', 'none','hidden')",null);


                    }else if (session.getEmgType().equalsIgnoreCase("Others")){
                        wv.loadUrl("javascript:changeElementContent('sp_oth', 'Stop Other request')",null);
//                        wv.loadUrl("javascript:hideElement('host_parent_stop', 'block','show')",null);
//                        wv.loadUrl("javascript:hideElement('host_parent', 'none', 'hidden')",null);
                    }

                    System.out.println("Emg Type: "+session.getEmgType());
                    System.out.println("Emg State: "+session.getEmgState());

                break;
                case "file:///android_asset/html/vettedartisant.html":
                        wv.loadUrl("javascript:changeInputElementContent('name', '"+ session.getName() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('email', '"+ session.getEmail() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('phone', '"+ session.getPhone() +"')",null);
                break;

                case "file:///android_asset/html/bodyguard.html":
                        wv.loadUrl("javascript:changeInputElementContent('name', '"+ session.getName() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('email', '"+ session.getEmail() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('phone', '"+ session.getPhone() +"')",null);
                break;

                case "file:///android_asset/html/eventsecurity.html":
                        wv.loadUrl("javascript:changeInputElementContent('name', '"+ session.getName() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('email', '"+ session.getEmail() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('phone', '"+ session.getPhone() +"')",null);
                break;
                case "file:///android_asset/html/normalsecurityguards.html":
                        wv.loadUrl("javascript:changeInputElementContent('name', '"+ session.getName() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('email', '"+ session.getEmail() +"')",null);
                        wv.loadUrl("javascript:changeInputElementContent('phone', '"+ session.getPhone() +"')",null);
                break;

            }
        }

        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
            Log.d("App", "Loading Progress:" + progressInPercent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean checkLocation() {
        if (!isLocationEnabled())
            showSettingsAlert();
        return isLocationEnabled();
    }


    private boolean isLocationEnabled() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return Objects.requireNonNull(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private void showSettingsAlert() {
        mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

    }

    @SuppressWarnings("MissingPermission")
    protected void startLocationUpdates() {
        // Create the location request
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(session.getUpdateInterval())
                .setFastestInterval(session.getFastestInterval())
                .setSmallestDisplacement(session.getFastestInterval());
        // Request location updates

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    private void sendPusherListnerData(String ch, String event){
        PusherOptions options = new PusherOptions();
        options.setCluster("eu");
        Pusher pusher = new Pusher("9ab0cdd9423d87131ea6", options);
        Channel channel = pusher.subscribe(session.getUser_id() + "_" + session.getDeviceID() + ch);
        channel.bind(session.getUser_id() + "_" + session.getDeviceID() + event, (channelName, eventName, data) -> {

            System.out.println("Data request: " + data);
            try {
                msg = new JSONObject(data);
                if (ch.equalsIgnoreCase("_request_location_channel")){

                    if (msg.getString("message").equalsIgnoreCase("request position")) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Reporting present location", Toast.LENGTH_LONG).show());
                        sendListenChangeCoordinateData(session.getLat(),session.getLong());
                    }

                }else {
                    if (msg.getJSONObject("message").getString("msg").equalsIgnoreCase("stop emergency request")) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Stopping emergency request", Toast.LENGTH_LONG).show());
                        sendEmgCancelData(String.valueOf(msg.getJSONObject("message").getString("emg_id")));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        pusher.connect();
    }
    private void sendListenChangeCoordinateData(String lat, String lng){

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("update_location", "update_location")
                .addFormDataPart("user_id", session.getUser_id())
                .addFormDataPart("device_id", session.getDeviceID())
                .addFormDataPart("latitude", lat)
                .addFormDataPart("longitude",  lng)
                .build();

        String url = "http://guardianangels.com.ng/application/public/api/crud";
        Request request = new Request.Builder()
                .url(url)
                .method("POST", RequestBody.create(null, new byte[0]))
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                String results = response.body().string();
                if (!response.isSuccessful()) {
                    System.out.println("Error: " + results);
                } else {
                      runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Location reported", Toast.LENGTH_LONG).show());
                    System.out.println("Success Message: " + results);
                }
            }
        });


    }
    private void sendEmgCancelData(String id){

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("update_emergency", String.valueOf(1))
                .addFormDataPart("emg_id", id)
                .build();

        String url = "http://guardianangels.com.ng/application/public/api/crud";
        Request request = new Request.Builder()
                .url(url)
                .method("POST", RequestBody.create(null, new byte[0]))
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                String results = response.body().string();
                if (!response.isSuccessful()) {
                    System.out.println("Error: " + results);

                } else {
                    session.setEmgSession("empty");
                    session.setEmgSession(false);
                    System.out.println("Success Message: " + results);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Emergency request stopped", Toast.LENGTH_LONG).show());
                }
            }
        });

    }
}
