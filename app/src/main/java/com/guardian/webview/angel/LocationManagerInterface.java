package com.guardian.webview.angel;

import android.location.Location;

/**
 * Created by DEVOP on 9/28/2017.
 */

public interface LocationManagerInterface {
    String TAG = LocationManagerInterface.class.getSimpleName();
    void locationFetched(Location mLocation, Location oldLocation, String time, String locationProvider);

}
