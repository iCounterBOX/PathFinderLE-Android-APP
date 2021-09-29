/*

target(new LatLng(48.797129, 9.001671))  // Maybachstraße i leonberg
 String geo = getGeoData4Map(dataBaseHelper, "-0.6");
  Log.e(TAG, "That's not an url... ");
 */


package com.mcuhq.ple_v3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;

public class HeatMapActivity1 extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = MainActivity.class.getSimpleName();

    // TextView
    private TextView mTV_HeatMapInfo1;

    //MapBox
    private MapView mapView;
    private MapboxMap mapboxMap;
    private static final String HEATMAP_SOURCE_ID = "HEATMAP_SOURCE_ID";
    private static final String HEATMAP_MY_LOC_SOURCE_ID = "HEATMAP_MY_LOC_SOURCE_ID";
    private static final String HEATMAP_LAYER_ID = "HEATMAP_LAYER_ID";
    private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private Expression[] listOfHeatmapColors;
    private Expression[] listOfHeatmapRadiusStops;
    private Float[] listOfHeatmapIntensityStops;
    private int index;
    private double _zoom = 17.3;
    Style _style = null;
    private Boolean _MapBoxReady4Loop = false;

    //DataBase sqLite  & TOOLS
    DataBaseHelper dataBaseHelper;
    ToolsClass mToolsClass;

    // SERVICE
    // https://developer.android.com/reference/android/app/Service#LocalServiceSample
    // https://stackoverflow.com/questions/26148235/getting-satellites-in-view-and-satellites-in-use-counts-in-android
    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
    private boolean mShouldUnbind;
    // To invoke the bound service, first make sure that this value
    // is not null.
    private BroadcastReceiver _geoLocationBroadcastReceiver;

    // GEO GEO GEO
    static Double _LAT = 0.0;
    static Double _LON = 0.0;
    static String _PROVIDER = "";
    static float _ACCURANCY = 0;
    static String _SatellitesInView = "";
    static String _SatellitesInUse = "";
    static String _distanceInMeters = "";
    static String _GeoStateTxt = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //DataBase sqLite
        dataBaseHelper = new DataBaseHelper(this);

        mToolsClass = new ToolsClass();

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        index = 2; // mein lieblings heat!
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        // this layout
        setContentView(R.layout.activity_heatmap1);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Textviews
        mTV_HeatMapInfo1 = (TextView) findViewById(R.id.tv_HeatMapInfo1);

        //boxStyle(mapboxMap);  // NICHT aufrufen!!! Das wird via onMapReady gestartet!!

        StartActivityTimer();  // wir setzen erst einmal die MAP und dann den loop timer

        //String s = toJson(null);
        //String s1 = getGeoData4Map(dataBaseHelper, "-5");  // der letzten 5 Minuten

        // SERVICE - che ck credentials
/* Doppelt gemoppelt?? delMe
        if (PermissionUtils.hasPermission(this,   android.Manifest.permission.ACCESS_COARSE_LOCATION)  &&
                PermissionUtils.hasPermission(this,   android.Manifest.permission.ACCESS_FINE_LOCATION) ) {
          //NICE
        } else
        {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            PermissionUtils.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ID);
        }
        /*
 */

    } // onCreate-END


    //neu:  SUPER: https://developer.android.com/guide/components/bound-services.html#java




    // MENU  With Options
    // @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
    // MENU ..on options listener  - https://www.youtube.com/watch?v=zwabHRv2taA
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.settings){
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "onOptionsItemSelected()/SETTINGS! " , Toast.LENGTH_SHORT).show();
        } else  if(item.getItemId() == R.id.help){
            Toast.makeText(getApplicationContext(), "onOptionsItemSelected()/HELP! " , Toast.LENGTH_SHORT).show();
        } else{
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
    // END Options-Menu

    // PathFinder Data visualization  - PathFinder Data visualization

    /*
    der gibt uns die daten:
    with nx as
(
SELECT strftime('%Y/%m/%d %H%M', s.TS) ts, s.wifiMacAddr as mac , MAX(s.wifiRSSI) as rssiAligned , s.lat as lat, s.lon as lon
FROM tbl_wifiScan s WHERE s.TS >= Datetime('now', '-500 minutes', 'localtime') GROUP BY strftime('%Y/%m/%d %H%M', s.TS), s.wifiMacAddr
) select nx.ts, nx.mac, nx.rssiAligned,  nx.lat, nx.lon
from nx group by nx.ts, nx.mac;

2020/06/29 0948	D6A550EA1598	-62.0	48.796974	9.001905
2020/06/29 0949	2E6F80B0A5BD	-87.0	48.796974	9.001905
2020/06/29 0949	479B7E0EBD5	-86.0	48.796974	9.001905
2020/06/29 0949	7AE81FDC2589	-85.0	48.796974	9.001905
2020/06/29 0949	7CB27D93D523	-90.0	48.796974	9.001905
2020/06/29 0949	BA1FB96ABCDA	-82.0	48.796974	9.001905
2020/06/29 0949	D678131FF8BA	-69.0	48.796974	9.001905
2020/06/29 0949	F2F259D393A	-89.0	48.796974	9.001905

     */


    /*

MapBox / HeatMap / Query:
with nx as
(
SELECT ROW_NUMBER()OVER() AS NoId, strftime('%Y/%m/%d %H%M', s.TS) ts, s.wifiMacAddr as mac , MAX(s.wifiRSSI) as rssiAligned , s.lat as lat, s.lon as lon
FROM tbl_wifiScan s WHERE s.TS BETWEEN datetime('now', 'localtime','-1 days') AND datetime('now', 'localtime') and s.lat > 0 GROUP BY strftime('%Y/%m/%d %H%M', s.TS), s.wifiMacAddr
) select nx.NoId, nx.ts, nx.mac, nx.rssiAligned,  nx.lat, nx.lon
from nx group by nx.ts, nx.mac order by nx.NoID;

 ts	            mac	        rssiAligned	lat	lon
2020/06/28 2109	1CBFCE11FD8	-24.0	    48.797139	9.001763
2020/06/28 2109	30E37A46C76	-85.0	    48.797139	9.001763
2020/06/28 2109	B827EB9A27D6	-92.0	48.797139	9.001763

ACHTUNG: ROW_NUMBER()OVER() AS NoId funktioniert im sqLite Studion NICHT!! nur viewer!! / ROW_NUMBER()OVER() AS NoId,

Filter nach den letzten Tagen: auch -1 als string   / String lastXdays
 s.TS BETWEEN datetime('now', 'localtime','" + lastXdays + " days') AND datetime('now', 'localtime')
 Achtung ..Performance-Falle!!  Viele Daten über tage!!    deshalb zzz minuten
     */

    public String getGeoData4Map(DataBaseHelper dataBaseHelper, String lastXMinusMinutes) {
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5
        JSONObject featureCollection = new JSONObject();

        try {

            String qs ="with nx as\n" +
                    "(\n" +
                    "SELECT  strftime('%Y/%m/%d %H%M', s.TS) ts, s.wifiMacAddr as mac , MAX(s.wifiRSSI) as rssiAligned , s.lat as lat, s.lon as lon\n" +
                    "FROM tbl_wifiScan s WHERE s.TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') AND datetime('now', 'localtime') and abs(s.lat) > 0 GROUP BY strftime('%Y/%m/%d %H%M', s.TS), s.wifiMacAddr \n" +
                    ") select  nx.ts, nx.mac, nx.rssiAligned,  nx.lat, nx.lon\n" +
                    "from nx group by nx.ts, nx.mac ;";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cursorData = db.rawQuery(qs,null);

            if (cursorData.getCount() != 0){
                try{
                    featureCollection.put("type", "FeatureCollection");
                    JSONObject properties = new JSONObject();
                    properties.put("name", "ESPG:4326");
                    JSONObject crs = new JSONObject();
                    crs.put("type", "name");
                    crs.put("properties", properties);
                    featureCollection.put("crs", crs);

                    // loop results  create new cursor objects
                    JSONArray features = new JSONArray();
                    int i = 0;

                    while (cursorData.moveToNext()) {
                        int NoId = i;  // id selbst generiert
                        String ts = cursorData.getString(0); // datum tsx  2020/06/18 1615
                        String mac = cursorData.getString(1);
                        float rssi = Math.abs( cursorData.getFloat(2));  // -95
                        double lat = cursorData.getDouble(3);
                        double lon = cursorData.getDouble(4);

                        JSONObject feature = new JSONObject();  // wichtig nur wenn hier die objecte NEW sind werden die auch dazu ge add et
                        feature.put("type", "Feature");
                        JSONObject featureProperties = new JSONObject();
                        JSONObject geometry = new JSONObject();
                        JSONArray JSONArrayCoord = new JSONArray();

                        featureProperties.put("id", NoId);
                        featureProperties.put("mag", rssi/10);
                        JSONArrayCoord.put(0, lon);  // lon
                        JSONArrayCoord.put(1, lat); // lat
                        JSONArrayCoord.put(2, 0);  // strength ?
                        geometry.put("type", "Point");
                        geometry.put("coordinates", JSONArrayCoord);
                        feature.put("properties", featureProperties);
                        feature.put("geometry", geometry);
                        features.put(feature);
                        i++;
                    }
                    // create a dataset and give it a type
                    featureCollection.put("features", features);
                    //Statistik Info anzeigen
                    _GeoStateTxt =  MainActivity._LAT + "|" + MainActivity._LON + " / "
                            + MainActivity._PROVIDER + " SIU: " + MainActivity._SatellitesInUse + " SIV: " +  MainActivity._SatellitesInView
                            + " ACC: " + MainActivity._ACCURANCY + " Dist(m): " +  MainActivity._distanceInMeters;
                    mTV_HeatMapInfo1.setText("<" + i + "> Items in the last " + lastXMinusMinutes + " Minutes found" + "\n" + _GeoStateTxt);

                }catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(this, "getGeoData4Map()/No Data", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "getGeoData4Map() - No Data Found! ");
            }
        } catch (Exception e) {
            Toast.makeText(this, "getGeoData4Map()/ERR", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "EXCEPTION/getGeoData4Map()/",e);
        }

        return featureCollection.toString();
    }


    /* MapBox - MapBox - MapBox - MapBox - MapBox - MapBox - MapBox - MapBox -
    EXAMPLE / Styling HeatMap / https://docs.mapbox.com/android/maps/examples/add-multiple-heatmap-styles/
     */

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        HeatMapActivity1.this.mapboxMap = mapboxMap;
        boxStyle(mapboxMap);  // extra fkt ..da wir den style reloaden und somit die daten source
    }

    private void boxStyle(@NonNull final MapboxMap mapboxMap) {
        try{
            mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull final Style style) {

                    _LAT = MainActivity._LAT;  // ToDo:  werde wohl diese activity NUR aufrufen, wenn auch eine Lat lon meiner pos vernünftig anliegt!!
                    _LON = MainActivity._LON;


                    // Test  service geo auslesen
                    // https://developer.android.com/guide/components/bound-services.html#java



                    if (Math.abs(_LAT) == 0 ) return; // EXIT

                    CameraPosition cameraPositionForFragmentMap = new CameraPosition.Builder()
                            .target(new LatLng(_LAT, _LON))
                            .zoom(_zoom) // 11.047   recht NAH
                            .build();
                    mapboxMap.animateCamera( CameraUpdateFactory.newCameraPosition(cameraPositionForFragmentMap), 2600);
                    try {
                        String geo = getGeoData4Map(dataBaseHelper, "-5");
                        style.addSource(new GeoJsonSource(HEATMAP_SOURCE_ID, geo));

                        if ( Math.abs( _LAT ) > 0  && Math.abs(_LON) > 0 ) {
                            style.addSource(new GeoJsonSource(HEATMAP_MY_LOC_SOURCE_ID, Feature.fromGeometry(Point.fromLngLat(_LON, _LAT))));
                        }
                    } catch ( Exception e) {
                        Log.e(TAG, "That's not an url... ");
                    }

                    // MARKER meiner position...- https://docs.mapbox.com/android/maps/overview/annotations/
                    //Deprecated?? muss das irgendwie noch an den style hängen

                    initHeatmapColors();
                    initHeatmapRadiusStops();
                    initHeatmapIntensityStops();
                    addHeatmapLayer(style);
                    setUpImage(style);
                    setUpMarkerLayer(style);

                    findViewById(R.id.switch_heatmap_style_fab).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try{
                                index++;
                                Log.i(TAG, "HeatMap-Index: " + index);
                                if (index == listOfHeatmapColors.length - 1) {
                                    index = 0;
                                }
                                Layer heatmapLayer = style.getLayer(HEATMAP_LAYER_ID);
                                if (heatmapLayer != null) {
                                    heatmapLayer.setProperties(
                                            heatmapColor(listOfHeatmapColors[index]),
                                            heatmapRadius(listOfHeatmapRadiusStops[index]),
                                            heatmapIntensity(listOfHeatmapIntensityStops[index])
                                    );
                                }
                            }catch (Exception e) {
                                String err = "boxStyle()/OnClick/EXPt: " + e.getMessage();
                                Log.e(TAG, err);
                            }
                        }
                    });
                    /*
                    VIP !!!  Das ist der virtuelle BUTTON der die Map NEU lädt !!! VIP
                     */
                    findViewById(R.id.switch_heatmap_style_ReloadData).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try{
                                Log.i(TAG, "HeatMap-Reload Data..");
                                _zoom = mapboxMap.getCameraPosition().zoom;  // https://github.com/mapbox/mapbox-gl-native/issues/4216
                                Log.i(TAG, "HeatMap-zoom: " + _zoom);
                                style.removeLayer(HEATMAP_MY_LOC_SOURCE_ID); // meine aktuelle locale pos von dieser PathFinder APP
                                style.removeLayer(MARKER_LAYER_ID);
                                style.removeLayer(HEATMAP_LAYER_ID);
                                style.removeSource(HEATMAP_SOURCE_ID);
                                boxStyle(mapboxMap);
                            }catch (Exception e) {
                                String err = "boxStyle()/OnClick/EXPt: " + e.getMessage();
                                Log.e(TAG, err);
                            }

                        }
                    });
                    _MapBoxReady4Loop = true;  // wenn alles soweit durch ist ERSZ den Timer-Loop mit reload der Map zulassen!!
                }
            });
        } catch (Exception e) {
            String err = "boxStyle()/EXPt: " + e.getMessage();
            Log.e(TAG, err);
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        }
    }

    
    private void addHeatmapLayer(@NonNull Style loadedMapStyle) {
        // Create the heatmap layer
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, HEATMAP_SOURCE_ID);

        // Heatmap layer disappears at whatever zoom level is set as the maximum
        layer.setMaxZoom(18);

        layer.setProperties(
                // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
                // Begin color ramp at 0-stop with a 0-transparency color to create a blur-like effect.
                heatmapColor(listOfHeatmapColors[index]),

                // Increase the heatmap color weight weight by zoom level
                // heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(listOfHeatmapIntensityStops[index]),

                // Adjust the heatmap radius by zoom level
                heatmapRadius(listOfHeatmapRadiusStops[index]
                ),

                heatmapOpacity(1f)
        );

        // Add the heatmap layer to the map and above the "water-label" layer
        loadedMapStyle.addLayerAbove(layer, "waterway-label");
    }


    /**
     * Adds the marker image to the map for use as a SymbolLayer icon
     */
    private void setUpImage(@NonNull Style loadedStyle) {
        loadedStyle.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                this.getResources(), R.drawable.phone32));
    }
    /**
     * Setup a layer with maki icons, eg. west coast city.
     */
    private void setUpMarkerLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, HEATMAP_MY_LOC_SOURCE_ID)  // HEATMAP_SOURCE_ID: dann werden die Marker auf die MAC´s bzw. deren GEO  gesetzt!!
                .withProperties(
                        iconImage(MARKER_IMAGE_ID),
                        iconAllowOverlap(true),
                        iconOffset(new Float[] {0f, -8f})
                ));
    }

    @Override
    protected void onStart() {
        super.onStart();
      //  Intent i =new Intent(getApplicationContext(),GeoLocationListenerService.class);
      //  startService(i);
        mapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        /*  - im moment NICHT notwendig hier den broadcast zu lesen..wir lesen einfach aus MainActivity aus
        if(_geoLocationBroadcastReceiver == null){
            _geoLocationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ArrayList<String> coords;
                    coords = new ArrayList<>();
                    coords = intent.getStringArrayListExtra("coordinates");
                    _LAT = Double.parseDouble(coords.get(0) );
                    _LON = Double.parseDouble(coords.get(1) );
                    _PROVIDER = coords.get(2) ;
                    _ACCURANCY= Float.parseFloat(coords.get(3) );
                    _SatellitesInView = coords.get(4) ;
                    _SatellitesInUse= coords.get(5) ;
                    _distanceInMeters = coords.get(6) ;
                    _GeoStateTxt = coords.get(7);
                }
            };
        }
        registerReceiver(_geoLocationBroadcastReceiver,new IntentFilter("location_update"));

         */
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        // Unbind from the service
        /*
        Intent i =new Intent(getApplicationContext(),GeoLocationListenerService.class);
        stopService(i);
        if(_geoLocationBroadcastReceiver != null) {
            unregisterReceiver(_geoLocationBroadcastReceiver);
        }

         */
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        /*
        Intent i =new Intent(getApplicationContext(),GeoLocationListenerService.class);
        stopService(i);
        if (_geoLocationBroadcastReceiver != null) {
            unregisterReceiver(_geoLocationBroadcastReceiver);
        }

         */
    }

    private void initHeatmapColors() {
        listOfHeatmapColors = new Expression[] {
// 0
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.25), rgba(224, 176, 63, 0.5),
                        literal(0.5), rgb(247, 252, 84),
                        literal(0.75), rgb(186, 59, 30),
                        literal(0.9), rgb(255, 0, 0)
                ),
// 1
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(255, 255, 255, 0.4),
                        literal(0.25), rgba(4, 179, 183, 1.0),
                        literal(0.5), rgba(204, 211, 61, 1.0),
                        literal(0.75), rgba(252, 167, 55, 1.0),
                        literal(1), rgba(255, 78, 70, 1.0)
                ),
// 2
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(12, 182, 253, 0.0),
                        literal(0.25), rgba(87, 17, 229, 0.5),
                        literal(0.5), rgba(255, 0, 0, 1.0),
                        literal(0.75), rgba(229, 134, 15, 0.5),
                        literal(1), rgba(230, 255, 55, 0.6)
                ),
// 3
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(135, 255, 135, 0.2),
                        literal(0.5), rgba(255, 99, 0, 0.5),
                        literal(1), rgba(47, 21, 197, 0.2)
                ),
// 4
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(4, 0, 0, 0.2),
                        literal(0.25), rgba(229, 12, 1, 1.0),
                        literal(0.30), rgba(244, 114, 1, 1.0),
                        literal(0.40), rgba(255, 205, 12, 1.0),
                        literal(0.50), rgba(255, 229, 121, 1.0),
                        literal(1), rgba(255, 253, 244, 1.0)
                ),
// 5
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.05), rgba(0, 0, 0, 0.05),
                        literal(0.4), rgba(254, 142, 2, 0.7),
                        literal(0.5), rgba(255, 165, 5, 0.8),
                        literal(0.8), rgba(255, 187, 4, 0.9),
                        literal(0.95), rgba(255, 228, 173, 0.8),
                        literal(1), rgba(255, 253, 244, .8)
                ),
//6
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.3), rgba(82, 72, 151, 0.4),
                        literal(0.4), rgba(138, 202, 160, 1.0),
                        literal(0.5), rgba(246, 139, 76, 0.9),
                        literal(0.9), rgba(252, 246, 182, 0.8),
                        literal(1), rgba(255, 255, 255, 0.8)
                ),

//7
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
// 8
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
// 9
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
// 10
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
// 11
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.25),
                        literal(0.25), rgba(229, 12, 1, .7),
                        literal(0.30), rgba(244, 114, 1, .7),
                        literal(0.40), rgba(255, 205, 12, .7),
                        literal(0.50), rgba(255, 229, 121, .8),
                        literal(1), rgba(255, 253, 244, .8)
                )
        };
    }

    private void initHeatmapRadiusStops() {
        listOfHeatmapRadiusStops = new Expression[] {
// 0
                interpolate(
                        linear(), zoom(),
                        literal(6), literal(50),
                        literal(20), literal(100)
                ),
// 1
                interpolate(
                        linear(), zoom(),
                        literal(12), literal(70),
                        literal(20), literal(100)
                ),
// 2
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(5), literal(50)
                ),
// 3
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(5), literal(50)
                ),
// 4
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(5), literal(50)
                ),
// 5
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(15), literal(200)
                ),
// 6
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(70)
                ),
// 7
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
// 8
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
// 9
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
// 10
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
// 11
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
        };
    }

    private void initHeatmapIntensityStops() {
        listOfHeatmapIntensityStops = new Float[] {
// 0
                0.6f,
// 1
                0.3f,
// 2
                1f,
// 3
                1f,
// 4
                1f,
// 5
                1f,
// 6
                1.5f,
// 7
                0.8f,
// 8
                0.25f,
// 9
                0.8f,
// 10
                0.25f,
// 11
                0.5f
        };
    }




    // local timer - TIMER - TIMER - TIMER - TIMER -

    private Handler handler;
    private Runnable handlerTask;

    void StartActivityTimer(){
        final int[] count = {0};
        handler = new Handler();
        handlerTask = new Runnable()
        {
            @Override
            public void run() {
                //do something

                // test to reload the data
                try {
                    if (_MapBoxReady4Loop){
                        findViewById(R.id.switch_heatmap_style_ReloadData).performClick(); // fake button click..dient dem reload der daten
                    }
                } catch (Exception e) {
                    Log.e(TAG, "HeatMapActivity/ EXCEPTION / TimerActivity()/",e);
                }
                handler.postDelayed(handlerTask, 15000);
            }
        };
        handlerTask.run();
    }

}
