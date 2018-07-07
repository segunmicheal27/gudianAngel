package com.guardian.webview.angel;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.chromium.base.ThreadUtils.runOnUiThread;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    final String TAG = "GPS";
    Context mContext;
    SessionManager session;
    JSONObject msg;
    LocationManager locationManager;


    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;

    String[] PERMISSIONS;

    @Override
    public void onCreate() {
        Log.i("App", "MyService Creating");
        session = new SessionManager(getApplicationContext());

//        Toast.makeText(this, "Stored Data: USER_ID: "+session.getUser_id() +" AND DEVICE_ID: "+session.getDeviceID(), Toast.LENGTH_LONG).show();

        System.out.println("Data request: " + "Stored Data: USER_ID: " + session.getUser_id() + " AND DEVICE_ID: " + session.getDeviceID());

        if (session.isLoggedIn() && session.isReg()) {

            locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();


            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }


            if (checkLocation()){
                //check permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(hasPermissions(mContext, PERMISSIONS)){
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
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("App", "In onDestroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id" + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
//        Toast.makeText(this, "MyService Started", Toast.LENGTH_LONG).show();
        return START_STICKY;

    }

    @Override
    public void onLocationChanged(Location location) {
        startLocationUpdates();
        session.setLatSession(String.valueOf(location.getLatitude()));
        session.setLongSession(String.valueOf(location.getLongitude()));

        Log.d(TAG, "onLocationChanged");
        if (session.getEmgState()){
            sendListenChangeCoordinateData(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
        }


    }

    public void showSettingsAlert() {
        startActivity(new Intent (this, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private boolean checkLocation() {
        if (!isLocationEnabled())
            showSettingsAlert();
        return isLocationEnabled();
    }


    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(session.getUpdateInterval())
                .setFastestInterval(session.getFastestInterval())
                .setSmallestDisplacement(0);
        // Request location updates

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }
    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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
