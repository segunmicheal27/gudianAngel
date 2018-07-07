package com.guardian.webview.angel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.guardian.webview.angel.storage.SessionManager;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import static org.chromium.base.ContextUtils.getApplicationContext;
import static org.chromium.base.ThreadUtils.runOnUiThread;

/**
 * Created by DEVOP on 9/28/2017.
 */

public class JavaScriptInterface{
    private Context mContext;
    private SessionManager session;
    private XWalkView xWalkWebView;

    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS;

    LocationManager locationManager;
    /** Instantiate the interface and set the context */
    JavaScriptInterface(Context c, XWalkView xWalkWebView) {
        this.mContext = c;
        this.xWalkWebView = xWalkWebView;
        // Session Manager
        this.session = new SessionManager(c);
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void sendBack()
    {
        if (!session.getPanicState()){
            session.setPanicSession(true);
             sendHPanicRequest();
             sendHelpRequest(1,"Panic Request");
        }else{
            session.setPanicSession(false);
             sendHPanicCancelRequest();
             sendHelpRequest(0,"Panic Request Cancel");
        }
//        mContext.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @JavascriptInterface
    public void reqEmg(String type, String id){

        if (checkLocation()) {

            //check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

                if (!hasPermissions(mContext, PERMISSIONS)) {
                    ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, PERMISSION_ALL);
                } else {

                    if (type.equalsIgnoreCase(session.getEmgType())){

//                        Toast.makeText(mContext, "request ready to cancel", Toast.LENGTH_LONG).show();

                        sendEmgCancelData(session.getStopEmgID());

                    }else if (type.equalsIgnoreCase(session.getEmgType())){
                        Toast.makeText(mContext, "request ready to cancel", Toast.LENGTH_LONG).show();
                    }else if (session.getEmgType().equalsIgnoreCase("Medical")) {
                        Toast.makeText(mContext, "Cancel active request,before you request for another: ", Toast.LENGTH_LONG).show();
                    } else if (session.getEmgType().equalsIgnoreCase("Security")) {
                        Toast.makeText(mContext, "Cancel active request,before you request for another", Toast.LENGTH_LONG).show();
                    } else if (session.getEmgType().equalsIgnoreCase("Others")) {
                        Toast.makeText(mContext, "Cancel active request,before you request for another", Toast.LENGTH_LONG).show();
                    } else {
                        session.setStopEmgIDSession(id);
                        session.setEmgSession(type);
                        Toast.makeText(mContext, "request received, we will get back to you shortly", Toast.LENGTH_LONG).show();
                    }

                }

            } else {

//                Toast.makeText(mContext, "type: "+type, Toast.LENGTH_LONG).show();

                if (type.equalsIgnoreCase(session.getEmgType())){
//                    Toast.makeText(mContext, "request ready to cancel", Toast.LENGTH_LONG).show();
                    sendEmgCancelData(session.getStopEmgID());
                }else if (session.getEmgType().equalsIgnoreCase("Medical")) {
                    Toast.makeText(mContext, "Cancel active request,before you request for another: "+session.getEmgType(), Toast.LENGTH_LONG).show();
                } else if (session.getEmgType().equalsIgnoreCase("Security")) {
                    Toast.makeText(mContext, "Cancel active request,before you request for another: "+session.getEmgType(), Toast.LENGTH_LONG).show();
                } else if (session.getEmgType().equalsIgnoreCase("Others")) {
                    Toast.makeText(mContext, "Cancel active request,before you request for another", Toast.LENGTH_LONG).show();
                }else{
                    session.setStopEmgIDSession(id);
                    session.setEmgSession(type);
                    Toast.makeText(mContext, "request received, we will get back to you shortly", Toast.LENGTH_LONG).show();
                }



            }

        }else{
            checkLocation();
        }
    }

    @JavascriptInterface
    public void setEmgReq()
    {



        if (checkLocation()) {

            //check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

                if (hasPermissions(mContext, PERMISSIONS)) {

                    if (!session.getEmgState()) {
                        sendEmgRequest();
                        session.setEmgSession(true);
                    } else {
                        session.setEmgSession(false);
                    }
                }

            } else {

                if (!session.getEmgState()) {
                    sendEmgRequest();
                    session.setEmgSession(true);
                } else {
                    session.setEmgSession(false);
                }
            }
        }


    }

    @JavascriptInterface
    public void getJSONTData(String jsonData) {
        try {
            JSONObject data = new JSONObject(jsonData); //Convert from string to object, can also use JSONArray

      if (data.getString("message").equalsIgnoreCase("Logined in!")){
                session.setRegSession();
                session.setLoginSession(data.getJSONObject("device").getString("device_id"),
                        data.getJSONObject("user").getString("id"),
                        data.getJSONObject("user").getString("name"),
                        data.getJSONObject("user").getString("email"),
                        data.getJSONObject("user").getString("phone")
                );
      }
            System.out.println("Data Result: "+data.toString());
        } catch (Exception e) {
            System.out.println("error login: "+e.getMessage());
            e.getStackTrace();
        }

        System.out.println("Login Result: "+jsonData);
    }

    @JavascriptInterface
    public void getRegData(final String jsonData) {

        try {

            JSONObject reg_data = new JSONObject(jsonData); //Convert from string to object, can also use JSONArray

            if (reg_data.getString("message").equalsIgnoreCase("success!")){

                session.setRegSession();
                session.setLoginSession(reg_data.getJSONObject("device").getString("device_id"),
                        reg_data.getJSONObject("user").getString("id"),
                        reg_data.getJSONObject("user").getString("name"),
                        reg_data.getJSONObject("user").getString("email"),
                        reg_data.getJSONObject("user").getString("phone")
                );
            }

            System.out.println("reg_data: "+reg_data);
        } catch (Exception e) {
            System.out.println("error Reg: "+e.getMessage());
            e.getStackTrace();
        }

        System.out.println("Data Reg Result: "+jsonData);
    }

    private String checkDetailsEmpty() {
        if (session.getDeviceID().equalsIgnoreCase("empty") && session.getUser_id().equalsIgnoreCase("empty")) {
            return "new_devices";
        }
        return "old";
    }


    @JavascriptInterface
    public String getLoginType() {
        return checkDetailsEmpty();
    }

    @JavascriptInterface
    public String getDeviceType() {
        return Build.MODEL;
    }

    @JavascriptInterface
    public String getEmgType() {

        String type = "";

        if (checkLocation()) {

            //check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

                if (hasPermissions(mContext, PERMISSIONS)) {

                    type = session.getEmgType();
                }

            } else {
                type =  session.getEmgType();
            }
        }



        return type;

    }

    @JavascriptInterface
    public String getDeviceVersion() {
        return "v"+Build.VERSION.RELEASE;
    }

    @JavascriptInterface
    public String getDeviceMANUFACTURER() {
        return Build.MANUFACTURER;
    }

    @JavascriptInterface
    public String getDeviceSerialNumber() {
        @SuppressLint("HardwareIds") String serialNumber = Build.SERIAL != Build.UNKNOWN ? Build.SERIAL : Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return serialNumber;
    }

    private void sendHelpRequest(int status, String feedBack){

        Long tsLong = System.currentTimeMillis()/1000;
        String app_id = "394235";
        String key = "9ab0cdd9423d87131ea6";
        String secret = "5a9b0cc7294a1daedc92";
        String auth_version = "1.0";
        String auth_timestamp = tsLong.toString();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {

            JSONObject message = new JSONObject();

            JSONObject messageBody = new JSONObject();
            messageBody.put("msg","Panic");
            messageBody.put("status",status);
            messageBody.put("device_id",session.getDeviceID());
            messageBody.put("user_id",session.getUser_id());

            message.put("message", messageBody);
            JSONObject parameter = new JSONObject();
            parameter.put("data",message.toString());
            parameter.put("name", session.getUser_id()+"_"+session.getDeviceID()+"_rescue_event");
            parameter.put("channel", session.getUser_id()+"_"+session.getDeviceID()+"_rescue_channel");

            System.out.println("Parameter: "+ parameter);
            String body_md5 = md5(parameter.toString());
            System.out.println("Body MD5: "+ body_md5);

            String  string_to_sign =
                    "POST\n/apps/" + app_id +
                            "/events\nauth_key=" + key +
                            "&auth_timestamp=" + auth_timestamp +
                            "&auth_version=" + auth_version +
                            "&body_md5=" + body_md5;

            String auth_signature = null;

            try {
                auth_signature = encode(secret, string_to_sign);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("auth_signature Key: "+ auth_signature);
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = RequestBody.create(JSON, parameter.toString());
            String url = "https://api-eu.pusher.com/apps/"+app_id+"/events?body_md5="+body_md5+"&auth_version="+auth_version+ "" +
                    "&auth_key="+key+"&auth_timestamp="+auth_timestamp+"&auth_signature="+auth_signature+"";
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", RequestBody.create(null, new byte[0]))
                    .post(formBody)
                    .addHeader("content-type", "application/json; charset=utf-8")
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
                        runOnUiThread(() -> {
                            Toast.makeText(mContext, "Request failed, try again", Toast.LENGTH_LONG).show();
                            System.out.println("Error: " + results);
                        });

                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(mContext, feedBack, Toast.LENGTH_LONG).show();
                            System.out.println("Message: " + results);
                        });

                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendHPanicRequest(){

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("panic", "panic")
                .addFormDataPart("user_id", session.getUser_id())
                .addFormDataPart("device_id", session.getDeviceID())
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

                    try {
                        JSONObject dataJson = new JSONObject(results);
                        session.setPanicID(Integer.parseInt(dataJson.getJSONObject("panic").getString("id")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Success Message: " + results);

                }
            }
        });

    }

    private void sendEmgRequest(){

        Long tsLong = System.currentTimeMillis()/1000;
        String app_id = "394235";
        String key = "9ab0cdd9423d87131ea6";
        String secret = "5a9b0cc7294a1daedc92";
        String auth_version = "1.0";
        String auth_timestamp = tsLong.toString();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {

            JSONObject message = new JSONObject();

            message.put("name", session.getName());
            message.put("message", "hospital emergency");

            JSONObject parameter = new JSONObject();
            parameter.put("data",message.toString());
            parameter.put("name", "got-request-event");
            parameter.put("channel", "got-request-channel");

            System.out.println("Parameter: "+parameter);
            String body_md5 = md5(parameter.toString());
            System.out.println("Body MD5: "+ body_md5);

            String  string_to_sign =
                    "POST\n/apps/" + app_id +
                            "/events\nauth_key=" + key +
                            "&auth_timestamp=" + auth_timestamp +
                            "&auth_version=" + auth_version +
                            "&body_md5=" + body_md5;

            String auth_signature = null;

            try {
                auth_signature = encode(secret, string_to_sign);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("auth_signature Key: "+auth_signature);
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = RequestBody.create(JSON, parameter.toString());
            String url = "https://api-eu.pusher.com/apps/"+app_id+"/events?body_md5="+body_md5+"&auth_version="+auth_version+ "" +
                    "&auth_key="+key+"&auth_timestamp="+auth_timestamp+"&auth_signature="+auth_signature+"";
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", RequestBody.create(null, new byte[0]))
                    .post(formBody)
                    .addHeader("content-type", "application/json; charset=utf-8")
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
                        runOnUiThread(() -> {
                            Toast.makeText(mContext, "Request failed, try again", Toast.LENGTH_LONG).show();
                            System.out.println("Error: " + results);
                        });

                    } else {
                        runOnUiThread(() -> {
//                            Toast.makeText(mContext, feedBack, Toast.LENGTH_LONG).show();
                            System.out.println("Message: " + results);
                        });

                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void sendHPanicCancelRequest(){

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("cancel_panic", "cancel_panic")
                .addFormDataPart("id", String.valueOf(session.get_panic_id()))
                .addFormDataPart("user_id", session.getUser_id())
                .addFormDataPart("device_id", session.getDeviceID())
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
                    System.out.println("Success Message: " + results);
                }
            }
        });

    }

    private static String encode(String key, String data) {
        try {

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return new String(Hex.encodeHex(sha256_HMAC.doFinal(data.getBytes("UTF-8"))));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showSettingsAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    private void showSettingsAlert() {
        mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

    }
    private void sendEmgCancelData(String id){

        Toast.makeText(mContext, "Stopping emergency request", Toast.LENGTH_LONG).show();

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
                    runOnUiThread(() -> {
                        xWalkWebView.loadUrl("file:///android_asset/html/emergency.html",null);
                        Toast.makeText(getApplicationContext(), "Emergency request stopped", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });

    }

}