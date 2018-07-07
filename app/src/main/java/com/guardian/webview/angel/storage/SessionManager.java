package com.guardian.webview.angel.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by DEVOP on 9/28/2017.
 */

public class SessionManager {

    // Shared Preferences
    private SharedPreferences pref;
    // Editor for Shared preferences
    private SharedPreferences.Editor editor;
    // Context
    private Context _context;
    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // SharedPref file name
    private static final String PREF_NAME = "Pref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String IS_LOGOUT = "IsLoggedOut";
    private static final String IS_REG = "IsRegistered";
    private static final String IS_PANIC_MODE = "IsPanicState";
    private static final String IS_EMG_MODE = "IsEmgState";

    // Token Data (make variable public to access from outside)
    private static final String KEY_DEVICE_ID = "device_id_key";
    private static final String KEY_STOP_EMG_ID = "stop_emg_id_key";


    private static final String KEY_EMERGENCY_TYPE = "emg_type_key";

    private static final String KEY_USER_ID = "user_id_key";
    private static final String KEY_EMAIL = "email_key";
    private static final String KEY_PHONE = "phone_key";
    private static final String KEY_NAME = "name_key";
    private static final String KEY_PANIC_ID = "panic_id_key";

    private static final String KEY_LAT = "lat_key";
    private static final String KEY_LONG = "long_key";

    private static final String KEY_UPDATE_INTERVAL = "update_interval_key";
    private static final String KEY_FASTEST_INTERVAL = "fastest_interval_key";
    private static final String KEY_SMALLEST_DISPLACEMENT = "smallest_displacement_key";

    // Constructor
    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context){
        this._context = context;
        pref =_context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void setLoginSession(String device_id_key,String user_id,String name,String email,String phone){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        // Storing name in pref
        editor.putString(KEY_DEVICE_ID, device_id_key);
        editor.putString(KEY_USER_ID, user_id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        // commit changes
        editor.apply();
    }


    public void setLogOutSession(){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, false);
        // commit changes
        editor.apply();
    }

    public void setStopEmgIDSession(String id){
        // Storing login value as TRUE
        editor.putString(KEY_STOP_EMG_ID, id);
        // commit changes
        editor.apply();
    }

    public void setUpdateIntervalSession(){
        // Storing login value as TRUE
        editor.putInt(KEY_UPDATE_INTERVAL, 0);
        // commit changes
        editor.apply();
    }


    public void setFastestIntervalSession(){
        // Storing login value as TRUE
        editor.putInt(KEY_FASTEST_INTERVAL, 0);
        // commit changes
        editor.apply();
    }


    public void setSmallestDisplacementSession(){
        // Storing login value as TRUE
        editor.putInt(KEY_SMALLEST_DISPLACEMENT, 0);
        // commit changes
        editor.apply();
    }



    public void setPanicSession(boolean state){
        // Storing login value as TRUE
        editor.putBoolean(IS_PANIC_MODE, state);
        // commit changes
        editor.apply();
    }



    public void setLatSession(String lat){
        // Storing login value as TRUE
        editor.putString(KEY_LAT, lat);
        // commit changes
        editor.apply();
    }
    public void setLongSession(String lng){
        // Storing login value as TRUE
        editor.putString(KEY_LONG, lng);
        // commit changes
        editor.apply();
    }

    public void setEmgSession(boolean state){
        // Storing login value as TRUE
        editor.putBoolean(IS_EMG_MODE, state);
        // commit changes
        editor.apply();

    }

    public void setPanicID(int id){
        // Storing login value as TRUE
        editor.putInt(KEY_PANIC_ID, id);
        // commit changes
        editor.apply();
    }


    public void setRegSession(){
        // Storing login value as TRUE
        editor.putBoolean(IS_REG, true);
        // commit changes
        editor.apply();
    }

    public void setEmgSession(String type){
        // Storing login value as TRUE
        editor.putString(KEY_EMERGENCY_TYPE, type);
        // commit changes
        editor.apply();
    }

    public String getDeviceID(){
        return pref.getString(KEY_DEVICE_ID, "empty");
    }

    public String getName(){
        return pref.getString(KEY_NAME, "empty");
    }

    public String getLat(){
        return pref.getString(KEY_LAT, "7.2835");
    }

    public String getLong(){
        return pref.getString(KEY_LONG, "3.3664");
    }



    public String getEmail(){
        return pref.getString(KEY_EMAIL, "empty");
    }

    public String getEmgType(){
        return pref.getString(KEY_EMERGENCY_TYPE, "empty");
    }


    public String getPhone(){
        return pref.getString(KEY_PHONE, "empty");
    }
    public String getUser_id(){
        return pref.getString(KEY_USER_ID, "empty");
    }

    public int get_panic_id(){
        return pref.getInt(KEY_PANIC_ID, 0);
    }

    public String getStopEmgID(){
        return pref.getString(KEY_STOP_EMG_ID, String.valueOf(0));
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public boolean isLoggedOut(){
        return pref.getBoolean(IS_LOGOUT, false);
    }


    public boolean isReg(){
        return pref.getBoolean(IS_REG, false);
    }

     public boolean getPanicState(){
            return pref.getBoolean(IS_PANIC_MODE, false);
     }

     public int getUpdateInterval(){
            return pref.getInt(KEY_UPDATE_INTERVAL, 0);
     }

     public int getFastestInterval(){
            return pref.getInt(KEY_FASTEST_INTERVAL, 0);
     }


     public int getSmallestDisplacement(){
            return pref.getInt(KEY_SMALLEST_DISPLACEMENT, 0);
     }



     public boolean getEmgState(){
            return pref.getBoolean(IS_EMG_MODE, false);
     }

}
