package com.mcuhq.ple_v3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ParseException;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class ToolsClass extends AppCompatActivity {


    private final String TAG = MainActivity.class.getSimpleName();



    /*
    Calculate the normalized Value from BL/BLE rssi and Wifi rssi
    https://stackoverflow.com/questions/2873469/issue-with-wifimanager-calculatesignallevelrssi-5/3737569
    https://stackoverflow.com/questions/26280230/equivalent-to-wifimanager-calculatesignallevel-for-bluetooth-rssi
    rssi vs wifi   https://www.researchgate.net/figure/RSSI-versus-distance-for-BLE-Wi-Fi-and-XBee_fig5_317150846
     */

    public ToolsClass() {
    }

    Context mContext;
    public ToolsClass(Context mContext) {
        this.mContext = mContext;

    }

    public int calculateSignalLevel(int rssi, int numLevels, int minRssi, int maxRssi) {
        int MIN_RSSI =  Math.abs((int)(minRssi));  //  nah dran   BL ca 45  für BT  ca 10 für Wifi?  / arbeiten hier mit positiven int werten
        int MAX_RSSI = Math.abs((int)(maxRssi));  //  weit weg / ca 110 für BT  ca 97 für wifi
        if(rssi <= MIN_RSSI) {  // MIN
            return 0;  // annahme
        } else if(rssi >= MAX_RSSI) {   //MAX rssi
            return numLevels - 1;
        } else {
            float inputRange = (MAX_RSSI - MIN_RSSI);
            float outputRange = (numLevels - 1);
            if(inputRange != 0)
                return (int) ((float) (rssi - MIN_RSSI) * outputRange / inputRange);
        }
        return 0;
    }

    /*
     06/17/2020 17:14 kommt HIER so rein ...in der DB und sonst anders definiert 2020 kommt da am anfang!!
     https://www.sqlite.org/lang_datefunc.html
     https://stackoverflow.com/questions/38991605/timeseries-chart-in-mpandroidchart
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public long iso8601StringToUnixTime(String timeStamp) {
        long unixTime = -1;
        TimeZone tzGMT = TimeZone.getTimeZone("GMT");
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        format.setTimeZone(tzGMT);
        try {
            // Reminder: getTime returns the number of milliseconds from the
            // Unix epoch.
            unixTime = format.parse(timeStamp).getTime() / 1000;
        } catch (Exception e) {
            Log.e(TAG, "iso8601StringToUnixTime()/ ",e);
            return 0;
            // Return initialized value of -1;
        }
        return unixTime;
    }

    public Boolean convertIntToBoolean ( int boolValueAsInt){
        try {
           if ( boolValueAsInt == 1)
               return true;
           else
               return false;
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION/convertIntToBoolean()/",e);
        }

        return false;
    }

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public  float roundFloat(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }














}
