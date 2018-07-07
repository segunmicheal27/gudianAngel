package com.guardian.webview.angel;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver {
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.mContext = context;

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("Service Controller", "onReceiver");

        if (!isServiceRunning(LocationService.class)) {
            Log.d("Service Controller", "starting service by AlarmManager");
            context.startService(new Intent(mContext, LocationService.class));
        }else{
            Log.d("Service Controller", "Service already running");
        }
    }

    // this method is very important
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
