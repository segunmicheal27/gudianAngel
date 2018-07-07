package com.guardian.webview.angel;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PermissionActivity extends AppCompatActivity {
    private final static int ALL_PERMISSIONS_RESULT = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_activity);
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                ALL_PERMISSIONS_RESULT);
    }
}
