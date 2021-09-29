package com.mcuhq.ple_v3;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class GeoLocationListenerService extends Service  {

    private final String TAG = GeoLocationListenerService.class.getSimpleName();

    boolean isGPSEnabled = false; // flag for GPS satellite status
    boolean isNetworkEnabled = false; // flag for cellular network status
    boolean canGetLocation = false; // flag for either cellular or satellite status

    private GpsStatus mGpsStatus;

    private LocationListener listener;
    protected LocationManager locationManager;
    protected GpsListener gpsListener = new GpsListener();

    Location mLocationNW; // location
    Location mLocationGPS;
    Location _oldLocation =  null;
    Location _newLocation =  null;

    public void set_oldLocation(Location _oldLocation) {
        this._oldLocation = _oldLocation;
    }

    // Sat attributes
    double dLatitude, dAltitude, dLongitude, dAccuracy, dSpeed, dSats;
    String szSignalSource, szAltitude, szAccuracy, szSpeed;
    public static String szSatTime;
    public String _szSatellitesInUse, _szSatellitesInView;
    float _distanceInMeters;
    String _GeoStateTxt = "";


    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    private static final long MIN_TIME_BW_UPDATES = 1000; //1 second


    //  das braucht es weil wir   extends Service machen !!
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // im moment hier kein Broadcast!! nur onGpsStatusChanged broadcast / ggf. nach outdoor-test aktivieren??
                Intent i = new Intent("location_update");
                //i.putExtra("coordinates","locL: " + roundDouble (location.getLongitude(),7) + " "+  roundDouble (location.getLatitude(),7) + " / " + location.getProvider() );
                //i.putExtra("coordinates", location);
                //sendBroadcast(i);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };


        // die kombi aus network und dem Listener - schneller und höhere Trefferquote
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);// getting GPS satellite status
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);// getting cellular network status

        if (isNetworkEnabled) {//GPS is enabled, getting lat/long via cellular towers
            locationManager.addGpsStatusListener(gpsListener);//inserted new
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        }
        if (isGPSEnabled) {//GPS is enabled, gettoing lat/long via satellite
            locationManager.addGpsStatusListener(gpsListener);//inserted new
            locationManager.getGpsStatus(null);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        }
    }



    class GpsListener implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int event) {
            Log.i("GPS event", "onGpsStatusChanged()/ has fired an event");
             _newLocation = getServiceLocation();  // get the current location from the listener service
            //determinNewVsOld_Location(); // determine if new or old location is better..result in any case _newLocation

            if (_newLocation.getAccuracy() > 20 ) {
                Toast.makeText(getApplicationContext(), "No good GPS-Accurancy <" +  String.valueOf(_newLocation.getAccuracy()) + ">" , Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent("location_update");
            //i.putExtra("coordinates","gpsL: " + roundDouble (l.getLongitude(),7) + " "+  roundDouble (l.getLatitude(),7) + " / " + l.getProvider() );
            ArrayList<String> coords = new ArrayList<>();
            coords.add( String.valueOf(_newLocation.getLatitude()) ); // 0   - gehe zu MainActivity Zeile 671
            coords.add( String.valueOf(_newLocation.getLongitude()) ); // 1
            coords.add( _newLocation.getProvider() );                  // 2
            coords.add( String.valueOf(_newLocation.getAccuracy()) );  // 3
            coords.add( _szSatellitesInView );  // 4
            coords.add( _szSatellitesInUse );  // 5
            coords.add( String.valueOf(_distanceInMeters) );  // 6
            coords.add( _GeoStateTxt);  // 7


            i.putStringArrayListExtra("coordinates", coords);
            sendBroadcast(i);
        }
    }


    @SuppressLint("MissingPermission")
    public Location getServiceLocation() {
        szAltitude = " NA(using cell towers)";
        _szSatellitesInView = "-";
        _szSatellitesInUse = "-";
        Location FinalResultLocation = null;

        try {
            if (isNetworkEnabled) {
                this.canGetLocation = true;
                Log.i("GPS event", "Cell tower enabled");
                if (locationManager != null) {
                    FinalResultLocation= locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (FinalResultLocation != null) {
                        Log.i("GPS event", "Cell tower-location (lat/lon): " + FinalResultLocation.getLatitude() + " " + FinalResultLocation.getLongitude() + "");
                        return FinalResultLocation;
                    }
                }
            }
            // GPS
            if (isGPSEnabled) {
                Log.i("GPS event", "GPS Enabled");
                if (locationManager != null) {
                                FinalResultLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (FinalResultLocation != null) {

                                    /**************************************************************
                                     * Provides a count of satellites in view, and satellites in use
                                     **************************************************************/
                                    mGpsStatus = locationManager.getGpsStatus(mGpsStatus);
                                    Iterable<GpsSatellite> satellites = mGpsStatus.getSatellites();
                                    int iTempCountInView = 0;
                                    int iTempCountInUse = 0;
                                    if (satellites != null) {
                                        for (GpsSatellite gpsSatellite : satellites) {
                                            iTempCountInView++;
                                            if (gpsSatellite.usedInFix()) {
                                                iTempCountInUse++;
                                            }
                                        }
                                    }
                                    _szSatellitesInView = String.valueOf(iTempCountInView);
                                    _szSatellitesInUse = String.valueOf(iTempCountInUse);
                                    Log.i("GPS event", "Satelite (GPS) - (lat/lon): " + FinalResultLocation.getLatitude()+ " |" +
                                            " " + FinalResultLocation.getLongitude()+" " +  "TempCountInView: " +   _szSatellitesInView +  " SatellitesInUse: " +  _szSatellitesInUse );
                                    return FinalResultLocation;

                                } else {
                                    Log.i("GPS event", "getLOcation/ Satelite (GPS) / NO valid GPS Data! " );
                               }
                }
            }
        }
        catch (Exception e) {
             Log.e(TAG, "EXCEPTION/getLocation()/",e);
        }

       //Falls GPS nicht kam, dann ggf Network
        return FinalResultLocation;
    }


   // Check if NEW or OLD location is better

    public void determinNewVsOld_Location() {
        try{
                if ( _newLocation != null ){
                    if (_oldLocation != null){
                        if(isBetterLocation(_oldLocation, _newLocation)) {     // test auf der strasse war nicht gut wenig updates der position
                            // If location is better, do some user preview.
                            Toast.makeText(getApplicationContext(), "Better location found! " , Toast.LENGTH_SHORT).show();
                            _distanceInMeters =  _oldLocation.distanceTo(_newLocation);
                            _GeoStateTxt =  _newLocation.getLatitude() + "|" +_newLocation.getLongitude() + "(NEW) \nSAT: " + _szSatellitesInView +  " / " +  _szSatellitesInUse  +
                                    "  Dist(m): " +   roundFloat(_distanceInMeters,2);
                        }else{
                            _GeoStateTxt = _oldLocation.getLatitude() + " / " + _oldLocation.getLongitude() + "(OLD)";
                            _newLocation = _oldLocation;  // Alte war besser!!
                        }
                    }
                    set_oldLocation(_newLocation); // ...auf ein neues :-)
                }else{
                    _GeoStateTxt  = "No GEO Coordinates Currently - Wait or Check GPS!";
                    Toast.makeText(getApplicationContext(), _GeoStateTxt , Toast.LENGTH_SHORT).show();
                }

        } catch ( Exception e){
            String err = "DeterminNewVsOld_Location()/Exception: " + e.getMessage();
            Log.e(TAG, err);
            Toast.makeText(getApplicationContext(), "NO GEO LOC / Check Satelite!", Toast.LENGTH_SHORT).show();
        }
    }


    /*
      GEO sub function
        ISSUE:  schein das ich von dem Service diese location objekte Nicht ain der externen Methode  benutzen kann

   */


    //Satelite infos: - MapBox activity mit heatMap - OK
    //    mAPbOX. https://www.youtube.com/watch?v=sTlOaFm6ttU
    //    MARKER-ICON..free: https://freeicons.io/regular-life-icons/device-mobile-phone-icon-17801#

    /**
     * Time difference threshold set for one minute.
     */
    static final int TIME_DIFFERENCE_THRESHOLD =    1 * 10 * 1000;   //  1 * 60 * 1000;

    /**
     * https://blog.codecentric.de/en/2014/05/android-gps-positioning-location-strategies/
     *
     * Decide if new location is better than older by following some basic criteria.
     * This algorithm can be as simple or complicated as your needs dictate it.
     * Try experimenting and get your best location strategy algorithm.
     *
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            return true;
        }
        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();
        if(isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else if(isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if(timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
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