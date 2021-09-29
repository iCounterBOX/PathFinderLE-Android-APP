/*
FatFinderLE & M5StickC   -  ESP32 --> BLclassic ---> APP

Prerequisite:
Min SDK version: Marshmallow 6.0 / API 23
HUAWEI M2-801W ( API 22 NOT working )
OUKITEL K9 ( Android PIE (9) / API 28 ) - OK

S T A T E:
NEW Mobile OUKITEL K9 - ok.
Build of FREEZE function.. in build

21.11.20:
rangeSlider für Historychart  / https://www.material.io/develop/android/components/sliders
https://www.youtube.com/watch?v=IibybM4oM1w


10.11.20:
optimierungen bei Geo-Location ( kompakter ..nur noch im listenerService...BroadCast )
MockLocation..
https://stackoverflow.com/questions/2531317/how-to-mock-location-on-device
https://github.com/mcastillof/FakeTraveler

siehe auch  Alarm Manager Example / https://stackoverflow.com/questions/4459058/alarm-manager-example

02.11.20:
New FREEZE function
we prpare the M5 ESP sniffer version to share it on GitHub - some doc text  is therefore deleted in INO:
known IP´s  e.g MAC:
WLANs:
ACER NB WLAN 1 -  50-E0-85-86-35-27
ACER NB WLAN 2 -  52-E0-85-86-35-26
NetGear HotSpot - DA-A1-19-95-C9-F4
K9 OuKiTel      - 00-27-15-7C-E8-F8
CAT S41 Gregor  - 98-29-A6-AD-B8-8B

Bluetooth:
ACER            - 50-E0-85-86-35-2A
K9              - 20-20-16-26-39-14
CAT S41 Gregor  - 98-29-A6-AC-F5-3B

14.06.20:
we lift up the Main-Page og PathFinder..klare struktur und vorweg die Buttons mit allen möglichen Funktionen die zt später dann rein kommen
table structure layout: https://stackoverflow.com/questions/16213801/how-to-get-a-tablelayout-with-2-columns-of-equal-width/26581334
https://www.tutorialspoint.com/android/android_table_layout.htm
>>>>>>>  BUTTON BACKGROUND:   https://angrytools.com/android/button/
Viel gemacht auf der main-page mit NestedScrollView und scrollable Listview ( https://stackoverflow.com/questions/9833834/android-listview-only-show-the-first-result )
Parameter  vom setting  für die esp MAC und Name ..ausgerollt, damit neue geräte leicht
angeschlossen werden können

12.06.20:
siehe mail an zoltan..wir können jetzt Bin files erzeugen und versenden
https://randomnerdtutorials.com/bin-binary-files-sketch-arduino-ide/
planning of an OPTION activity..

esp-Modul:
----------
-Name ( Name of the ESP scanner Modul )
-MAC-Addr
Timer:
------
- ESP32_trigger_Timer ( ca. alle 10 Sec. Uhrzeit als Trigger zum ESP )
- Wifi_BarChart_ActivityTimer ( ca. 4 sec refresh period )
- BT_BarChart_ActivityTimer ( ca. 3 sec refresh period )
- wifi_BT_DoubleChart_ActivityTimer ( ca. 3 sec refresh period )
SocialDistancing-ALERT LEVEL:
-----------------------------
- dBm ( e.g. -40 dBm ..approx 5 Meter..)

11.05.20 / SnifferDaten gehen in die sqLite DB und werden nur aufgelistet
erste Test mit Chart ( BarChart ) in einer eigenen activity


12.06.20:
siehe mail an zoltan..wir können jetzt Bin files erzeugen und versenden
https://randomnerdtutorials.com/bin-binary-files-sketch-arduino-ide/


11.05.20 / SnifferDaten gehen in die sqLite DB und werden nur aufgelistet
erste Test mit Chart ( BarChart ) in einer eigenen activity

ToDo: ( prio in list )
- mHandler in BluetoothConnectionService / jetzt auch message  nach freeze..ABER sowas sollte in einen broadcast receiver übertragen werden...verwende jetzt TRY um nicht auf fehler zu gehen denn die activity ist ja anfangs nicht geladen!!
- automatisches pairen - ok
- beim drehen connection halten  - OK nur noch portrait möglich
- radarChart alles umbenennen zu BarChart - OK
- BarChart teilen ( infoListe und chart ) - ok
- wenn keine daten kommen..listen leeren und Toast - ok
- coole Buttons designen -ok
  e.g.: https://angrytools.com/android/button/
  https://www.youtube.com/watch?v=iPrXdgj74YU
- Uhrzeit an ESP statt trigger senden - ok
- activity(slide) für parameter und optionen - ok
- HistoryGaph soll exposure line zus. bekommen
  als simple activity über menu-bar abrufbar / nach diesem schema (table)
  https://www.techotopia.com/index.php?title=An_Android_6_TableLayout_and_TableRow_Tutorial&mobileaction=toggle_view_mobile
  und : https://www.youtube.com/watch?v=veOZTvAdzJ8
  https://www.youtube.com/watch?v=kknBxoCOYXI&pbjreload=101
  https://www.youtube.com/watch?v=zwabHRv2taA
  Options werden als json lokal auf der APP gespeichert (R/W)  -  https://stackoverflow.com/questions/14219253/writing-json-file-and-read-that-file-in-android
  jsonArray..read: https://www.youtube.com/watch?v=h71Ia9iFWfI
  with GSON: https://www.youtube.com/watch?v=f-kcvxYZrB4&list=PLrnPJCHvNZuBdsuDMl3I-EOEOnCh6JNF3&index=2&pbjreload=101
  PROBLEM: der assetFolder ist NUR Read ONLY...wir müssten auf external Storage gehen!!
  ich werde einfach die DB nehmen
- MapBox activity mit heatMap - OK
    mAPbOX. https://www.youtube.com/watch?v=sTlOaFm6ttU
    MARKER-ICON..free: https://freeicons.io/regular-life-icons/device-mobile-phone-icon-17801#


- Manufactorer/DNS Checker (MAC): https://dnschecker.org/mac-lookup.php?query=18%3A1D%3AEA%3A85%3A91%3AC0
- alarm wenn schwellwert überschritten ( target auf 5 meter zB ) - ok ( lief schon mal im test..zz Deaktiviert )
- DB für global data
- geo-location zum scan - ok
    ok: D:\ALL_PROJECT\a_UQBATE\ANDROID\studioExamples\GEO_CurrentLocation  gibt die geo
    https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android

- BL + BLE scan
    BL scan: https://www.youtube.com/watch?v=hv_-tX1VwXE
    Beacon Scan: https://altbeacon.github.io/android-beacon-library/javadoc/reference/org/altbeacon/beacon/Beacon.html
                 https://www.programcreek.com/java-api-examples/?code=bjaanes%2FBeaconMqtt%2FBeaconMqtt-master%2Fapp%2Fsrc%2Fmain%2Fjava%2Fcom%2Fgjermundbjaanes%2Fbeaconmqtt%2FBeaconApplication.java#
                 scanFilter: https://stackoverflow.com/questions/60096255/android-ble-how-to-filter-scan-by-raw-advertising-data
                 https://code.tutsplus.com/tutorials/create-a-bluetooth-scanner-with-androids-bluetooth-api--cms-24084

- SONAR RADAR simulation
  halbkreis: https://sites.google.com/site/arduinosonar/a



- Satelites infos: https://stackoverflow.com/questions/26148235/getting-satellites-in-view-and-satellites-in-use-counts-in-android

- eigene MAC fälschen hiden

 */

package com.mcuhq.ple_v3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

//https://www.youtube.com/watch?v=kknBxoCOYXI&pbjreload=101
import android.view.Menu;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private final String TAG = MainActivity.class.getSimpleName();
    static Handler mHandler; // Our main handler that will receive callback notifications

    // GUI Components
    private TextView mBluetoothStatus0;
    private TextView mBluetoothStatus1;
    private TextView mBluetoothStatus2;
    private TextView mReadBuffer;

    private Button mListPairedDevicesBtn;
    private Button mBtn_BarChart;       // wifi chart
    private Button mBtn_Extra;
    private Button mBtn_blScan2;        // BL chart
    private Button mBtn_combiChart;        // BL and wifi in one Chart
    private Button mBtn_visitorHistoryChart;
    private Button mBtn_GeoMapView;  // is calling the HeatMapActivity1
    private Button mBtn_Freeze; // is calling the FREEZE ALERT SITE

    // ListViews
    private ListView mSniffDataListView;
    private ListView mNearByDevicesListView;
    private ListView mLV_exposureBeacons;

    // BT Adapter to connect ESP32
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;

    private Boolean mEsIstEtwasInDerListe = false;


    // #defines for identifying shared types between calling functions
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    //ESP SNIFFER-ADDR & NAME

    public static final String esp32Name = "ESP32test";
    //ESP32 normales Dev Board
    //public static final String esp32Addr =  "24:0A:C4:60:A8:36"; // ESP BT MAC Address
    //M5STICKC
    //public static final String esp32Addr =  "D8:A0:1D:57:45:16"; // ESP BT MAC Address:

    // Flags
    public boolean mBTconnectionOK = false;

    // Setup-Parameters ( müssen via dataBaseHelper.getStringSettingsFromDb  von db befüllt werden )

    public static String _ESPname;
    public static String _ESPmac;
    public static int _Esp32IsAvailable;  // Checkbox if ES is available at all
    public static int _Ep32TriggerTimer;
    public static int _wifi_BarChart_Timer;
    public static int _BT_BarChart_Timer;
    public static int _WBT_DoubleChart_Timer;
    public static int _SelectTimeFilter;

    // Array
    ArrayAdapter snifferArrayAdapter;

    // CLASS
    DataBaseHelper dataBaseHelper;
    BluetoothConnectionService mBluetoothConnection;
    ToolsClass mToolsClass;


    // GEO GEO GEO
    private BroadcastReceiver _geoLocationBroadcastReceiver;
    LocationManager mLocationManager;  // for permission check
    String mProvider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    int PERMISSION_ID = 44;
    static Double _LAT = 0.0;
    static Double _LON = 0.0;
    static String _PROVIDER = "";
    static float _ACCURANCY = 0;
    static String _SatellitesInView = "";
    static String _SatellitesInUse = "";
    static String _distanceInMeters = "";
    static String _GeoStateTxt = "";

    //GeoLocationListenerService mService;
    boolean mBound = false;

    // EXPOSURE NOTIFICATION Beacon stuff  +  Array for my LV
    private BeaconManager mBeaconManager;
    ArrayList<String> _exposureBeaconArray;
    double _NearestTarget = 100.0f;

    AlertDialog.Builder mSocialAlertDialog;

    //Toast
    public void myToastShow(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mToolsClass = new ToolsClass(this);

        //Status & Buffer
        mBluetoothStatus0 = (TextView) findViewById(R.id.bluetoothStatus0);
        mBluetoothStatus1 = (TextView) findViewById(R.id.bluetoothStatus1);
        mBluetoothStatus2 = (TextView) findViewById(R.id.bluetoothStatus2);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);

        //BUTTONS
        mListPairedDevicesBtn = (Button) findViewById(R.id.PairedBtn);
        mBtn_BarChart = (Button) findViewById(R.id.Btn_BarChart);
        mBtn_Extra = (Button) findViewById(R.id.Btn_Extra);
        mBtn_blScan2 = (Button) findViewById(R.id.btn_blScan2);
        mBtn_combiChart = (Button) findViewById(R.id.btn_combiChart);
        mBtn_visitorHistoryChart = (Button) findViewById(R.id.btn_visitorHistoryChart);
        mBtn_GeoMapView = (Button) findViewById(R.id.btn_GeoMapView);
        mBtn_Freeze = (Button) findViewById(R.id.Btn_freeze);

        //BT
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        //LISTS + Inflate header view  + Add header view to the ListView

        mSniffDataListView = (ListView) findViewById(R.id.snifferDataListView);
        ViewGroup headerView = (ViewGroup)getLayoutInflater().inflate(R.layout.dbm_mac_cnt_header_wifi, mSniffDataListView,false);
        mSniffDataListView.addHeaderView(headerView);

        mLV_exposureBeacons= (ListView)findViewById(R.id.lv_exposureBeacons);
        ViewGroup headerView2 = (ViewGroup)getLayoutInflater().inflate(R.layout.dbm_mac_cnt_header_c19, mLV_exposureBeacons,false);
        mLV_exposureBeacons.addHeaderView(headerView2);





        // Exposure Beacon Stuff
        _exposureBeaconArray = new ArrayList<String>();

        //Textview

        //DataBase sqLite
        dataBaseHelper = new DataBaseHelper(MainActivity.this);
        //   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! showSnifferDatadInListView();

        // Ask for location permission if not already allowed
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocationManager.getBestProvider(new Criteria(), false);
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        PermissionUtils.checkComplete_LocationAndBTpermission(this,mBeaconManager,mLocationManager);

        //SOCIAL DISTANCE ALARM - https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
        mSocialAlertDialog = new AlertDialog.Builder(MainActivity.this);
        mSocialAlertDialog.setMessage("Write your message here.");
        mSocialAlertDialog.setCancelable(true);
        mSocialAlertDialog.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        mSocialAlertDialog.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        //showSocialDistancePopUp();


        //Message handler

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    mReadBuffer.setText(readMessage);
                    Log.i(TAG, "From ESP32: " + readMessage);
                    writeSnifferDataIntoDB(readMessage);
                }
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        mBluetoothStatus1.setText("Connected: " + msg.obj);
                        Log.i(TAG, "Connected to Device: " + msg.obj);
                        mBTconnectionOK = true;
                    } else {
                        mBluetoothStatus1.setText("Connection Failed");
                        Log.i(TAG, "Connection Failed");
                        mBTconnectionOK = false;

                    }
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus1.setText("Status: Bluetooth not found");
        } else {
            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBTconnectionToESP32();  //  BLUETOOTH  CONNECT AND START ESP communication
                }
            });
        }

        mBtn_BarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBarChartActivity();
            }
        });
        mBtn_blScan2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBLscanActivity();
            }
        });
        mBtn_combiChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCombiChartActivity();
            }
        });
        mBtn_visitorHistoryChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistoryActivity();
            }
        });
        mBtn_Extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupMyDB2SDcard();
            }
        });
        mBtn_GeoMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( Math.abs(_LAT )> 0 && Math.abs(_LON ) > 0 ) {  // abs .cause in brasil it might be -8.49, -51.32
                    openHeatMapActivity();
                }else{
                    Log.i(TAG, "Own Location NOT available - Check Satelite" );
                    mBluetoothStatus2.setText("Own Location NOT available - Check Satelite");
                }
            }
        });
        mBtn_Freeze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFreezeActivity();
            }
        });

        // DB HOUSING
        dataBaseHelper.deleteOldData(-1);  // delete db data OLDER than 2 Days

        // beim anlegen ignoreTable mit paar bekanten devices füllen...später automatisieren..mit click auf list element usw

        dataBaseHelper.upDateInsertBLIgnoredDevices("ESP32test", "24:0A:C4:60:A8:36");
        dataBaseHelper.upDateInsertBLIgnoredDevices("ESP32test", "D8:A0:1D:57:45:16");
        dataBaseHelper.upDateInsertBLIgnoredDevices("SmartSolar HQ1935I7CKA", "D1:DD:04:9C:40:99");

        // Hier holen wir uns gleich die abgespeicherten  settings

        _ESPname = dataBaseHelper.getStringSettingsFromDb("espName", "ESP32test");
        _ESPmac = dataBaseHelper.getStringSettingsFromDb("espMAC", "D8:A0:1D:57:45:16");
        _Esp32IsAvailable = dataBaseHelper.getIntSettingsFromDb("esp32IsAvailable", 1); // default ist 1  also true
        _Ep32TriggerTimer = dataBaseHelper.getIntSettingsFromDb("Ep32TriggerTimer", 10);
        _wifi_BarChart_Timer = dataBaseHelper.getIntSettingsFromDb("wifi_BarChart_Timer", 4);
        _BT_BarChart_Timer = dataBaseHelper.getIntSettingsFromDb("BT_BarChart_Timer", 4);
        _WBT_DoubleChart_Timer = dataBaseHelper.getIntSettingsFromDb("WBT_DoubleChart_Timer", 4);
        _SelectTimeFilter = dataBaseHelper.getIntSettingsFromDb("SelectTimeFilter", 4);

        // local BT scan
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // wir legen gleich mit dem scan los:
        mBTAdapter.startDiscovery();

        // AUTOMATICALLY (re)Start the ESP BT connection
        if (_Esp32IsAvailable == 1) {
            mBluetoothStatus1.setText("From Settings: WiFi-Module\n(M5StickC) should be available..");
            startBTconnectionToESP32();  //  BLUETOOTH  CONNECT AND START ESP communication
        } else {
            mBluetoothStatus1.setText("Check Settings: NO WiFi-Module\n(M5StickC) or ext. SCANNER AVAILABLE");
        }

        // To detect proprietary BEACONS, you must add a line like below corresponding to your beacon -
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        long FOREGROUND_SCAN_PERIOD = 10000;  // https://stackoverflow.com/questions/32475081/altbeacon-library-setbackgroundscanperiod-not-working
        long FOREGROUND_BETWEEN_SCAN_PERIOD = 5000;
        long BACKGROUND_SCAN_PERIOD = 10000;
        long BACKGROUND_BETWEEN_SCAN_PERIOD = 5000;

        mBeaconManager.setForegroundScanPeriod(FOREGROUND_SCAN_PERIOD);    //  https://stackoverflow.com/questions/32475081/altbeacon-library-setbackgroundscanperiod-not-working
        mBeaconManager.setForegroundBetweenScanPeriod(FOREGROUND_BETWEEN_SCAN_PERIOD);    //5000 L
        mBeaconManager.setBackgroundScanPeriod(BACKGROUND_SCAN_PERIOD);    //8000 L
        mBeaconManager.setBackgroundBetweenScanPeriod(BACKGROUND_BETWEEN_SCAN_PERIOD);    //300000 L
        try {
            mBeaconManager.updateScanPeriods();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=fd6f,p:-:-59,i:2-17,d:18-21"));
        mBeaconManager.bind(this);
        mBeaconManager.setBackgroundMode(false);

        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("ExposureCovd19TrackingID", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }



        StartTimer4Status_counter();  // TRIGGER TIMER HIER starten
    } // onCreate END

    /*
    * BACKUP of our DB to SD CARD
    * https://stackoverflow.com/questions/19093458/copy-database-file-to-sdcard-in-android
    *
     */



    String sdPath;
    private boolean isSDPresent(Context context) {
        File[] storage = ContextCompat.getExternalFilesDirs(context, null);
        if (storage.length > 1 && storage[0] != null && storage[1] != null) {
            sdPath = storage[1].toString();
            return true;
        }
        else
            return false;
    }
    private boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }
    public boolean backupMyDB2SDcard() {
        if (isContextValid(this))
            try {
                if (!isSDPresent(this)) {
                    Log.e(TAG, "SD is absent!");
                    return false;
                }
                String localDbName =  dataBaseHelper.getDatabaseName();
                String backupDbName = localDbName;

                File sd = new File(sdPath);
                if (sd.canWrite()) {
                    File currentDB = new File("/data/data/" + this.getPackageName() +"/databases/", localDbName);  //currDB /data/data/com.mcuhq.ple_v3/databases/pLE.db
                    File backupDB = new File(sd,  backupDbName);
                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        displayPromptToGetTheDBpath(this, sd.toString());
                    }
                }
                else {
                    Log.e(TAG, "SD can't write data!");
                    return false;
                }
            } catch ( Exception e){
                String err = "backupMyDB2SDcard()()/Exception: " + e.getMessage();
                Log.e(TAG, err);
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
            }
        else {
            Log.e(TAG, "Export DB: Context is not valid!");
            return false;
        }

        return true;
    }


    public void displayPromptToGetTheDBpath(final Activity activity, final String backupDBpath )
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
        builder1.setTitle("PathFinder LocalDB Backup");
        builder1.setMessage("Pathfinder reports that the sqLite DB has been saved \nto SD on this folder (please check with ES explorer, for example!):\n" + backupDBpath);
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Got it",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        /*
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

         */

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }



    /*
        BEACON lopp - scan
    */

    String timeStamp4Beacon = "";
    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                Log.d("beacons", beacons.size() + "");
                if (beacons.size() > 0) {
                    Log.i("beacons", "\n ----------------- \n"   );
                    Date c = Calendar.getInstance().getTime();  // now()
                    SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");// or YYYY-MM-dd HH:MM:SS.SSS
                    timeStamp4Beacon = df.format(c);
                    _exposureBeaconArray = new ArrayList<String>();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Iterator<Beacon> beaconIterator = beacons.iterator();
                            double distance = 10.00f;

                            while (beaconIterator.hasNext()) {
                                Beacon beacon = beaconIterator.next();
                                distance  =   (int)(Math.round(beacon.getDistance() * 10))/10.0;
                                if ( distance <= _NearestTarget)
                                    _NearestTarget = distance;
                                Log.i("beacons", "EXPOSURE MAC:  " + beacon.getBluetoothAddress() + " ID: " + beacon.getId1()  + " Distance: "
                                        + beacon.getDistance()   + " Packets: " + beacon.getPacketCount()   );
                                String row = "MAC: " + beacon.getBluetoothAddress() + "  Distance(m) " +  distance + "  RSSI(dBm): " +  beacon.getRssi() + "\nID: " + beacon.getId1() + "\nPackets: " +  beacon.getPacketCount()  + "\n" ;
                                _exposureBeaconArray.add(row);
                                dataBaseHelper.addBleExposureNotificationBeacon ("tbl_bleExposureScan", timeStamp4Beacon,
                                        beacon.getBluetoothAddress() , beacon.getId1().toString(), beacon.getRssi(), distance , beacon.getPacketCount() ,_LAT,_LON);

                                //mStatus1.setText("Nearest Target: " + _NearestTarget);
                                //_mp.start();  // click sound..task:  search for a better dosimeter sound
                            }
                            // laterm MainTv.setText("Min distance: " + distance);
                        }
                    });
                }
            }

        });
        try {
            mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }  // EXPOSURE BEACON SCAN END


    @Override
    protected void onStart() {
        super.onStart();
        Intent i =new Intent(getApplicationContext(),GeoLocationListenerService.class);
        startService(i);
    }


    @Override
    protected void onResume() {
        super.onResume();
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
                    _GeoStateTxt =  _LAT + "|" + _LON + " / "  + _PROVIDER + " SIU: " + _SatellitesInUse + " SIV: " +  _SatellitesInView
                            + " ACC: " + _ACCURANCY + " Dist(m): " +   _distanceInMeters;

                    mBluetoothStatus2.setText(_GeoStateTxt);
                }
            };
        }
        registerReceiver(_geoLocationBroadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent i =new Intent(getApplicationContext(),GeoLocationListenerService.class);
        /*
        stopService(i);
        if(_geoLocationBroadcastReceiver != null) {
            unregisterReceiver(_geoLocationBroadcastReceiver);
        }

         */
    }

    @Override
    protected void onDestroy() {
            super.onDestroy();
            Intent i =new Intent(getApplicationContext(),GeoLocationListenerService.class);
            stopService(i);
            if (_geoLocationBroadcastReceiver != null) {
                unregisterReceiver(_geoLocationBroadcastReceiver);
            }
     }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //location updates: NOT here!! see GeoLocationListenerService (mLcationManager.requestLocationUpdates(mProvider, 400, 1,  this);)
                    }

                } else {

                    // permission denied, boo! Disable the - functionality that depends on this permission.
                }
                return;
            }

        }
    }


    public static void displayPromptForEnablingGPS(final Activity activity)
    {
        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Do you want open GPS setting?";
        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }



    // MENU  With Options and Help and stuff - https://www.youtube.com/watch?v=kknBxoCOYXI&pbjreload=101
    @Override
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

    public void showSocialDistancePopUp() {
        AlertDialog showSocialDistanceAlarm = mSocialAlertDialog.create();
        showSocialDistanceAlarm.show();
    }

    public void openBarChartActivity(){
        Intent intent = new Intent(this, BarChartActivity.class);
        startActivity(intent);
    }

    public void openBLscanActivity(){
        Intent intent = new Intent(this, BLscanActivity.class);
        startActivity(intent);
    }
    public void openCombiChartActivity(){
        Intent intent = new Intent(this, CombiChartActivity.class);
        startActivity(intent);
    }
    public void openHistoryActivity(){
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    public void openHeatMapActivity(){
        Intent intent = new Intent(this, HeatMapActivity1.class);
        startActivity(intent);
    }
    public void openFreezeActivity(){
        Intent intent = new Intent(this, FreezeActivity.class);
        startActivity(intent);
    }

    // SHOW EXPOSURE BEACON-SCAN RESULT in LV

    // Hold views of the ListView to improve its scrolling performance
    static class ViewHolder {
        public TextView textDbm;
        public TextView textMac;
        public TextView textCount;
    }
    String[] itemList4BLEc19Data;
    private void showExposureBeaconsIn_LV(final String lastXMinusMinutes) {
        ArrayList<String> data = dataBaseHelper.getC19ExposureBeaconDistancesFromLastNminutes(lastXMinusMinutes);
        String[] items = new String[data.size()];
        items = data.toArray(items);
        itemList4BLEc19Data = items;

        if (items != null && items.length > 0 ){
            ArrayAdapter arrayAdapter;
            arrayAdapter= new ArrayAdapter(MainActivity.this, R.layout.dbm_mac_cnt_rows, R.id.tvDbm, itemList4BLEc19Data){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View rowView = convertView;
                    // Inflate the rowlayout.xml file if convertView is null
                    if(rowView==null){
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        rowView= inflater.inflate(R.layout.dbm_mac_cnt_rows, parent, false);
                        ViewHolder viewHolder = new ViewHolder();
                        viewHolder.textDbm= (TextView) rowView.findViewById(R.id.tvDbm);
                        viewHolder.textMac= (TextView) rowView.findViewById(R.id.tvMAC);
                        viewHolder.textCount = (TextView) rowView.findViewById(R.id.tvCount);
                        rowView.setTag(viewHolder);
                    }
                    // Set text to each TextView of ListView item
                    String[] items = itemList4BLEc19Data[position].split("__");
                    ViewHolder holder = (ViewHolder) rowView.getTag();
                    holder.textDbm.setText(items[0]);
                    holder.textMac.setText(items[1]);
                    holder.textCount.setText(items[2]);
                    // Return the view
                    return rowView;
                }
            };
            // try to change the Header-Text
            TextView tv = (TextView) findViewById(R.id.tv_C19_Header_Distance_4DbmMacCnt);
            tv.setText("Distance(Meter)");
            tv = (TextView) findViewById(R.id.tv_C19_Header_MAC_4DbmMacCnt);
            tv.setText("MAC_BLE/C19");
            tv  = (TextView) findViewById(R.id.tv_C19_Header_Count_4DbmMacCnt);
            tv.setText("Count <" + lastXMinusMinutes + " min>");
            mLV_exposureBeacons.setAdapter(arrayAdapter);  // der header wird in onCreate zur liste geklebt
        }else
        {
            Toast.makeText(getApplicationContext(), "showSnifferDatadInListView()/No New Data! " , Toast.LENGTH_SHORT).show();
        }
    }
    // show ESP wifi data
    String[] itemList4ESPdata;
    private void showSnifferDatadInListView(final String lastXMinusMinutes) {
        ArrayList<String> data = dataBaseHelper.getAllSnifferDataAsList(lastXMinusMinutes);
        String[] items = new String[data.size()];
        items = data.toArray(items);
        itemList4ESPdata = items;

        if (items != null && items.length > 0 ){
            ArrayAdapter arrayAdapter;
            arrayAdapter= new ArrayAdapter(MainActivity.this, R.layout.dbm_mac_cnt_rows, R.id.tvDbm, itemList4ESPdata){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    View rowView = convertView;
                    // Inflate the rowlayout.xml file if convertView is null
                    if(rowView==null){
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        rowView= inflater.inflate(R.layout.dbm_mac_cnt_rows, parent, false);
                        ViewHolder viewHolder = new ViewHolder();
                        viewHolder.textDbm= (TextView) rowView.findViewById(R.id.tvDbm);
                        viewHolder.textMac= (TextView) rowView.findViewById(R.id.tvMAC);
                        viewHolder.textCount = (TextView) rowView.findViewById(R.id.tvCount);
                        rowView.setTag(viewHolder);
                    }
                    // Set text to each TextView of ListView item
                    String[] items = itemList4ESPdata[position].split("__");
                    ViewHolder holder = (ViewHolder) rowView.getTag();
                    holder.textDbm.setText(items[0]);
                    holder.textMac.setText(items[1]);
                    holder.textCount.setText(items[2]);
                    // Return the view
                    return rowView;
                }
            };
            // try to change the Header-Text
            TextView tv = (TextView) findViewById(R.id.tv_Header_Distance_4DbmMacCnt);
            tv.setText("Distance(dBm)");
            tv = (TextView) findViewById(R.id.tv_Header_MAC_4DbmMacCnt);
            tv.setText("MAC_WiFi");
            tv = (TextView) findViewById(R.id.tv_Header_Count_4DbmMacCnt);
            tv.setText("Count <" + lastXMinusMinutes + " min>");
            mSniffDataListView.setAdapter(arrayAdapter);  // der header wird in onCreate zur liste geklebt
        }else
        {
            Toast.makeText(getApplicationContext(), "showSnifferDatadInListView()/No New Data! " , Toast.LENGTH_SHORT).show();
        }
    }



/* BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL
 LOCAL BLUETOOTH SCANs   - LOCAL BLUETOOTH SCANs   - LOCAL BLUETOOTH SCANs   - LOCAL BLUETOOTH SCANs   - LOCAL BLUETOOTH SCANs   -
 BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL_BL
 */

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success  = false;
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE); // dBm
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                // Get the BluetoothDevice object from the Intent - name/mac : https://stackoverflow.com/questions/3170805/how-to-scan-for-available-bluetooth-devices-in-range-in-android
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //TimeStamp:
                Date c = Calendar.getInstance().getTime();  // now()
                SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");// or YYYY-MM-dd HH:MM:SS.SSS
                String timeStamp = df.format(c);
                String mac  = device.getAddress();
                String msgTxt = timeStamp + "|" +  name + "|"  + rssi + "|"  + mac  + "\n";
                // update oder insert in die Tabelle
                //mBlScanArray.add (msgTxt);
                mEsIstEtwasInDerListe = true;
                if (rssi != 0){
                    Log.i(TAG, "BL_Scan: " + msgTxt );
                    success = dataBaseHelper.upDateInsertBLscanData (timeStamp,name, mac, rssi,_LAT,_LON); // W R I T E   2    D B / BT
                }
            }
        }
    };

    // sqLite Stuff   ***********************************************************************************
    // toDo:  muss noch in den DB Helper
    private void     writeSnifferDataIntoDB(String sniffData){
        boolean success  = false;
        String sniffStr = "";
        String[] sniffDataArray;
        String[] mac_rssi;
        String mac = "";
        double rssi = 0.0;

        //curr timeDate

        Date c = Calendar.getInstance().getTime();  // now()
        SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");// or YYYY-MM-dd HH:MM:SS.SSS
        String timeStamp = df.format(c);
        String msg = "writeSnifferDataIntoDB()/TimeStamp: " + timeStamp;
        Log.i(TAG, msg);

        // sqLite
        DataBaseHelper dataBaseHelper = new DataBaseHelper(MainActivity.this);

        // sniffer daten kommen so rein: 1,0|8C5E1DEA09C,-96|B827EB9A27D6,-93|F05C7739A8E5,-93|DC94C427F6E,-42

        try{
            //showCurrentGeoLocation();
            sniffDataArray = sniffData.split("\\|");   // splitte ein einzelne pakete: B827EB9A27D6,-93
            for (String item : sniffDataArray)
            {
                mac_rssi = item.split(",");
                mac = mac_rssi[0];
                rssi = Double.parseDouble(mac_rssi[1]);
                //sniffStr = sniffData.replaceAll("(\\r|\\n)", "");
                if (rssi != 0){
                    success = dataBaseHelper.addESPdata_wifi(timeStamp, mac, rssi,_LAT, _LON); // W R I T E   2    D B
                }
                Log.i(TAG, "item: " + item);
            }
        } catch ( Exception e){
            String err = "writeSnifferDataIntoDB()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
        }
        if (success == true){

            // Toast.makeText(getApplicationContext(), "writeSnifferDataIntoDB()/Success: " + success, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "writeSnifferDataIntoDB()/No Data? /Success: " + success, Toast.LENGTH_SHORT).show();
        }
    }

    // Bluetooth Stuff   ***********************************************************************************

    // wir listen NUR ESP32
    private void startBTconnectionToESP32(){
        mBTArrayAdapter.clear();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // suche nach dem gepairten ESP
            for (BluetoothDevice device : mPairedDevices) {
                if (device.getName().contains(_ESPname)){
                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    // ESP ist gepaired..jetzt wird die BT connection aufgebaut
                    Toast.makeText(getApplicationContext(), "Found paired Device : = " + _ESPname + " MAC:" + _ESPmac, Toast.LENGTH_LONG).show();
                    mBluetoothStatus1.setText("Connecting...");
                    // S T a r t   v o n   S o c k e t     t h r e a d
                    mBluetoothConnection = new BluetoothConnectionService(MainActivity.this, _ESPmac , _ESPname);
                    if (mBluetoothConnection != null) {
                        Toast.makeText(getApplicationContext(), "BluetoothConnectionService OK!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "startBTconnectionToESP32() / BT connection OK! ");
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "BluetoothConnectionService Failed to connect!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "startBTconnectionToESP32() / BluetoothConnectionService Failed to connect! ");
                    }
                    break;
                }
            }
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }


    // BL END

    //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
    private Handler handler;
    private Runnable handlerTask;

    void StartTimer4Status_counter(){
        final int[] count = {0};
        handler = new Handler();
        handlerTask = new Runnable()
        {
            @Override
            public void run() {
                //do something

                String lastXMinutes = "-5";

                // T I G G E R  the ESP - ESP will send next Data-Pack to APP..check settings if esp available at all
                if (_Esp32IsAvailable == 1){
                    if (mBTconnectionOK == true ){
                        mBluetoothConnection.triggerCharForESP_Module(); //First check to make sure thread created
                    }else{
                        // AUTOMATICALLY (re)Start the ESP BT connection
                        startBTconnectionToESP32();  //  BLUETOOTH  CONNECT AND START ESP communication
                    }
                }else{
                    mBluetoothStatus1.setText("Check Settings: NO WiFi-Module\n(M5StickC) or ext. SCANNER AVAILABLE. Its Disabled!");
                }

                if (mEsIstEtwasInDerListe){
                    /*
                    Broadcast lifecycle is about 10 sec, you need to start a service and perform task into service
                    https://stackoverflow.com/questions/53722562/broadcast-receiver-stops-automatically-after-some-time
                    wir lassen ihn jetzt ca 10 sekunden sammeln und geben die neuen daten dann zurück
                 */
                    mBTAdapter.startDiscovery();  // hier wegen der 10 s Broad-MaxTime
                }

                showSnifferDatadInListView("-5");
                showExposureBeaconsIn_LV("-5");

                int cntWifi = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_wifiScan","wifiMacAddr",lastXMinutes);
                Log.i("Device counts", "WiFi count in last <" + lastXMinutes + ">  minutes: " + cntWifi);
                int cntBT = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_blScan","blMacAddr",lastXMinutes);
                Log.i("Device counts", "BT/BLE count in last <" + lastXMinutes + ">  minutes: " + cntBT);
                int cntC19 = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_bleExposureScan","bleMacAddr",lastXMinutes);
                Log.i("Device counts", "EXPOSURE (C19/BLE) count in last <" + lastXMinutes + ">  minutes: " + cntC19);

                mBluetoothStatus0.setText("COUNT f. last " + lastXMinutes + " Min.: WiFi= " + cntWifi  + "  BT= " + cntBT +  "  C19= " + cntC19);


                // ANY ALARM-FLAG?
                long t = _Ep32TriggerTimer * 1000;
                handler.postDelayed(handlerTask, t);
            }
        };
        handlerTask.run();
    }

    // ALARM - POP UP ..to do



}
