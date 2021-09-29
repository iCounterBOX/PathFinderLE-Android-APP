package com.mcuhq.ple_v3;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;

import java.util.ArrayList;

// https://www.youtube.com/watch?v=hDSVInZ2JCs

public class DataBaseHelper  extends SQLiteOpenHelper {

    private final String TAG = MainActivity.class.getSimpleName();

    public static final String DATABASE_NAME = "pLE.db";
    public static final String TBL_PLE_WIFI_SCAN = "tbl_wifiScan";
    public static final String TBL_PLE_BL_SCAN = "tbl_blScan";
    public static final String TBL_FREEZE = "tbl_freeze";

    public static final String TBL_PLE_BL_IGNORE  = "tbl_blScanIgnoredDevices";
    public static final String TBL_PLE_SETTINGS = "tbl_Settings";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null,1);
    }

    // this called first time db is accessed .. here is code  to CREATE the DB
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + TBL_PLE_WIFI_SCAN  + " ( TS STRING, wifiMacAddr Text, wifiMacAddrStr Text,  wifiRSSI float, lat DOUBLE, lon DOUBLE)";
        db.execSQL(createTableStatement);
        createTableStatement = "CREATE TABLE " + TBL_PLE_BL_SCAN  + " ( TS STRING, blName Text,  blMacAddr Text, blRSSI float , lat DOUBLE, lon DOUBLE , PRIMARY KEY ( blName, blMacAddr ) )";
        db.execSQL(createTableStatement);
        createTableStatement = "CREATE TABLE tbl_bleExposureScan ( TS STRING, bleMacAddr Text, exposureID  Text, bleRSSI  float, distance DOUBLE,  packets INTEGER,  lat DOUBLE, lon DOUBLE )";
        db.execSQL(createTableStatement);
        createTableStatement = "CREATE TABLE TBL_FREEZE (  MAC Text, SenderType Text,  exposureID  Text, lastRssi  float, lastDistance DOUBLE )";
        db.execSQL(createTableStatement);
        createTableStatement = "CREATE TABLE " + TBL_PLE_BL_IGNORE  + " ( blName Text,  blMacAddr Text , PRIMARY KEY (  blMacAddr ) )";
        db.execSQL(createTableStatement);
        createTableStatement = "CREATE TABLE " + TBL_PLE_SETTINGS  + " ( espName Text, espMAC Text, esp32IsAvailable  INTEGER, Ep32TriggerTimer INTEGER,  wifi_BarChart_Timer INTEGER  , BT_BarChart_Timer  INTEGER, WBT_DoubleChart_Timer INTEGER , SelectTimeFilter  INTEGER, PRIMARY KEY ( espName , espMAC))";
        db.execSQL(createTableStatement);
    }

    // called if DB will be updated later .. it prevent previous user apps from breaking when u change the db design
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // ADD NEW Data

    // Wifi sniffer Data from ESP Module  - esp data into db - schreibe esp32 daten:

    public boolean addESPdata_wifi (String timeStamp, String wifiMac, double RSSI, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long insert;

        // ToDo: check this issue on esp side...sometime we get crap mac like : 200000  or 1E6C5095C5E
        if (wifiMac.length() < 12){
            String err = "addESPdata_wifi()/Error: " + wifiMac + " is too SHORT!!";
            Log.i(TAG, err);
            // erst noch durch lassen für die analyser weshalb die kommen
            return false;
        }
        // we add : to the mac
        String wifiMacWithDoublePoint="";
        for(int i=0;i<wifiMac.length();i=i+2)
        {
            if((i+2)<wifiMac.length())
                wifiMacWithDoublePoint+=wifiMac.substring(i, i+2)+":";
            if((i+2)==wifiMac.length())
            {
                wifiMacWithDoublePoint+=wifiMac.substring(i, i+2);
            }
        }

        try{
            cv.put("TS",timeStamp); //For example, 2016-5-23 10:20:05.123  - YYYY-MM-DD HH:MM:SS.SSS
            cv.put("wifiMacAddr",wifiMac);
            cv.put("wifiMacAddrStr",wifiMacWithDoublePoint);
            cv.put("wifiRSSI",RSSI);
            cv.put("lat",lat);
            cv.put("lon",lon);
            insert  = db.insert(TBL_PLE_WIFI_SCAN ,null,cv);
        }catch ( Exception e){
            String err = "addESPdata_wifi()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            return false;
        }
        if (insert == -1){
            return false;
        }else{
            return true;
        }
    }

    // Wifi sniffer Data from ESP Module  - esp data into db - schreibe esp32 daten:

    public boolean addBleExposureNotificationBeacon (String tblName, String timeStamp, String bleMacAddr , String exposureID , float bleRSSI , double distance , int packets , double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long insert;
        try{
            cv.put("TS",timeStamp); //For example, 2016-5-23 10:20:05.123  - YYYY-MM-DD HH:MM:SS.SSS
            cv.put("bleMacAddr",bleMacAddr);
            cv.put("exposureID",exposureID);
            cv.put("bleRSSI",bleRSSI);
            cv.put("distance",distance);
            cv.put("packets",packets);
            cv.put("lat",lat);
            cv.put("lon",lon);
            insert  = db.insert(tblName ,null,cv);
        }catch ( Exception e){
            String err = "addBleExposureNotificationBeacon()/Exception: " + e.getMessage();
            Log.e(TAG, err);
            return false;
        }
        if (insert == -1){
            return false;
        }else{
            return true;
        }
    }


    /*
    select the pathfinder DB
    *.* : SELECT * FROM " + TBL_PLE_WIFI_SCAN  + " ORDER BY TS DESC

    UC:  Wieviele Devices in den letzten 5 Minuten geordnet nach db(m)/rssi?

with nx as
(
SELECT s.wifiRSSI, s.wifiMacAddr,
Case
    when abs(s.wifiRSSI) <= 100 and  abs(s.wifiRSSI) > 80 then -97
    when abs(s.wifiRSSI) <= 80 and  abs(s.wifiRSSI) > 60 then -80
    when abs(s.wifiRSSI) <= 60 and  abs(s.wifiRSSI) > 40 then -60
    when abs(s.wifiRSSI) <= 40 and  abs(s.wifiRSSI) > 20 then -40
    when abs(s.wifiRSSI) <= 20 and  abs(s.wifiRSSI) > 00 then -20
END rssiAligned
FROM tbl_wifiScan s WHERE s.TS >= Datetime('now', '-5000 minutes', 'localtime') GROUP BY s.wifiRSSI, s.wifiMacAddr)
select nx.rssiAligned ,Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned

ergibt: innere query
-96	30E37A46C76	-97
-96	E4B31884E7A2	-97
-95	30E37A46C76	-97
-95	54FCF089C4EF	-97
..
gesamte query:
rssiAligned	mobileDeviceCnt
-97	        140
-80	        15
-60	        20
-40	        9

1:07 / https://www.youtube.com/watch?v=hDSVInZ2JCs
*/

    /*
    DELETE OLD Data from DB
    DELETE FROM tbl_wifiScan WHERE TS <= date('now','-2 day')
    call: deleteOldData(-2)  - will delete data older than 2 days
     */

    public void deleteOldData(int days){

        SQLiteDatabase db = this.getWritableDatabase();
        String delDays = "-2";
        String qs = "DELETE FROM tbl_wifiScan WHERE TS <= date('now','" + delDays + " day')";
        db.execSQL(qs);
        db.close();
    }


    public ArrayList<String> getAllSnifferDataAsList(final String lastXMinusMinutes){

        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5
        ArrayList buffer = new ArrayList<String>();


        String qs = "with nx as\n" +
                "(\n" +
                "SELECT  s.wifiRSSI,s.wifiMacAddr as mac,\n" +
                "Case\n" +
                "    when abs(s.wifiRSSI) <= 100 and  abs(s.wifiRSSI) > 90 then -97\n" +
                "    when abs(s.wifiRSSI) <= 90 and  abs(s.wifiRSSI) > 80 then -90\n" +
                "    when abs(s.wifiRSSI) <= 80 and  abs(s.wifiRSSI) > 70 then -80\n" +
                "    when abs(s.wifiRSSI) <= 70 and  abs(s.wifiRSSI) > 60 then -70\n" +
                "    when abs(s.wifiRSSI) <= 60 and  abs(s.wifiRSSI) > 50 then -60\n" +
                "    when abs(s.wifiRSSI) <= 50 and  abs(s.wifiRSSI) > 40 then -50\n" +
                "    when abs(s.wifiRSSI) <= 40 and  abs(s.wifiRSSI) > 30 then -40\n" +
                "    when abs(s.wifiRSSI) <= 30 and  abs(s.wifiRSSI) > 20 then -30\n" +
                "    when abs(s.wifiRSSI) <= 20 and  abs(s.wifiRSSI) > 10 then -20\n" +
                "    when abs(s.wifiRSSI) <= 10 and  abs(s.wifiRSSI) > 0 then -10\n" +
                "END rssiAligned\n" +
                "FROM tbl_wifiScan s WHERE s.TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') GROUP BY s.wifiRSSI, s.wifiMacAddr\n" +
                ")\n" +
                "select nx.rssiAligned, nx.mac as MAC_WiFi,  Count (nx.rssiAligned ) as mobileDeviceCnt  from nx group by nx.rssiAligned, nx.mac order by nx.rssiAligned desc";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);

        if (cursorData.getCount() == 0){
            Log.i(TAG, "ERR / getAllSnifferDataAsList() - No Data Found! " );
            return buffer;
        }

        String header = "Distance(dBm) |          MAC_Wifi    | Device Cnt (" + lastXMinusMinutes + " min)";
       // buffer.add(header);
        // loop results  create new cursor objects
        while (cursorData.moveToNext()){
            String row = (int)( cursorData.getDouble(0)) + "__" + (cursorData.getString(1) ) + "__" + (cursorData.getString(2) );
            buffer.add(row);
        }
        // close and return the list
        return buffer;
    }

    /*
    * retrieve a single value from query -      lastXminutes    zb  -5
    * e.g. SELECT count(DISTINCT bleMacAddr) FROM tbl_bleExposureScan WHERE TS >= Datetime('now', '-20  minutes', 'localtime');
    * result:
    * count(DISTINCT bleMacAddr)
                51
     */
    public int getCountOfAsingleItem_fromLastXMinutes(String tblName, String  fieldName,  final String lastXMinusMinutes ){
        int resCount = 0;

        String qs = "SELECT count(DISTINCT " + fieldName + ") FROM " + tblName + " WHERE TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime');";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);

       // https://stackoverflow.com/questions/11480171/fetching-single-value-from-sqlite-in-android
        if (cursorData.getCount() == 1){
            cursorData.moveToFirst();
            resCount = cursorData.getInt(0);
        }else{
            Log.i(TAG, "getCountOfAsingleItem_fromLastXMinutes() - No Data Found! " );

        }
        return resCount;
    }

    // get the NearBy-Devices
    // not via dbModel  https://www.youtube.com/watch?v=eYJq1TO07y4
    //OUTPUT: nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt

    public ArrayList<String> getNearByDevicesFromLastNminutes(final String lastXMinusMinutes){
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5

        ArrayList buffer = new ArrayList<String>();
        String qs = "with nx as\n" +
                "(\n" +
                "SELECT  s.wifiRSSI,s.wifiMacAddr,\n" +
                "Case\n" +
                "    when abs(s.wifiRSSI) <= 100 and  abs(s.wifiRSSI) > 90 then -97\n" +
                "    when abs(s.wifiRSSI) <= 90 and  abs(s.wifiRSSI) > 80 then -90\n" +
                "    when abs(s.wifiRSSI) <= 80 and  abs(s.wifiRSSI) > 70 then -80\n" +
                "    when abs(s.wifiRSSI) <= 70 and  abs(s.wifiRSSI) > 60 then -70\n" +
                "    when abs(s.wifiRSSI) <= 60 and  abs(s.wifiRSSI) > 50 then -60\n" +
                "    when abs(s.wifiRSSI) <= 50 and  abs(s.wifiRSSI) > 40 then -50\n" +
                "    when abs(s.wifiRSSI) <= 40 and  abs(s.wifiRSSI) > 30 then -40\n" +
                "    when abs(s.wifiRSSI) <= 30 and  abs(s.wifiRSSI) > 20 then -30\n" +
                "    when abs(s.wifiRSSI) <= 20 and  abs(s.wifiRSSI) > 10 then -20\n" +
                "    when abs(s.wifiRSSI) <= 10 and  abs(s.wifiRSSI) > 0 then -10\n" +
                "END rssiAligned\n" +
                "FROM tbl_wifiScan s WHERE s.TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') GROUP BY s.wifiRSSI, s.wifiMacAddr\n" +
                ")\n" +
                "select nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned order by nx.rssiAligned desc";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);

        if (cursorData.getCount() == 0){
            Log.i(TAG, "ERR / getNearByDevicesFromLastNminutes() - No Data Found! " );
            return buffer;
        }
        String header = "Distance(dBm) | Wifi Device Cnt (" + lastXMinusMinutes + " min)";
        buffer.add(header);
        // loop results  create new cursor objects
        while (cursorData.moveToNext()){
                String row = (int)( cursorData.getDouble(0)) + " | " + (int)(cursorData.getDouble(1) )+ "\n" ;
                buffer.add(row);
                if (Math.abs((int)( cursorData.getDouble(0))) <= 70 ){
                        // social alarm
                }
        }
        // close and return the list
        return buffer;
    }

    // COVID-19 - BLE EXPOSURE NOTIFICATION BEACONs

    public ArrayList<String> getC19ExposureBeaconDistancesFromLastNminutes(final String lastXMinusMinutes){
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5

        ArrayList buffer = new ArrayList<String>();
        String qs = "SELECT round(s.distance,0) as meter, s.bleMacAddr as mac, count(round(s.distance,0)) as cnt\n" +
                "FROM tbl_bleExposureScan s WHERE s.TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') GROUP BY s.bleMacAddr order by meter asc;";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);

        if (cursorData.getCount() == 0){
            Log.i(TAG, "ERR / getC19ExposureBeaconDistancesFromLastNminutes() - No Data Found! " );
            return buffer;
        }

        // loop results  create new cursor objects
        while (cursorData.moveToNext()){
            String row = (int)( cursorData.getDouble(0))  + "__" + (cursorData.getString(1) )  + "__" + (int)(cursorData.getDouble(2));
            buffer.add(row);
            if (Math.abs((int)( cursorData.getDouble(0))) <= 3 ){
                // social alarm
            }
        }
        // close and return the list
        return buffer;
    }


    /*  BLUETOOTH ---  BLUETOOTH ---  BLUETOOTH --- BLUETOOTH  ---  BLUETOOTH --- BLUETOOTH

    BL BLE Data from the MobilePhone-Scan

    wir brauchen für Upadate Insert einen unique key ( contrain ( mac u name )
    http://zetcode.com/db/sqlite/datamanipulation/

     */

    public boolean upDateInsertBLscanData (String timeStamp,  String BlDeviceName, String BlDeviceMac, double blRSSI, double lat, double lon){
        SQLiteDatabase db = this.getWritableDatabase();
        // einfach mit cnpaste vom sqlite studio in den string :-)
        try{
            //try first to insert..if not existing
            String ts = timeStamp;
            String name = BlDeviceName;
            String mac = BlDeviceMac;
            double rssi = blRSSI;
            String qs = "INSERT OR REPLACE INTO tbl_blScan VALUES('" + ts + "', '" + name + "','" + mac + "' ," + rssi + "," + lat + "," + lon + "   );";
            db.execSQL(qs);
            db.close();
        }catch ( Exception e){
            String err = "upDateInsertBLscanData()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            return false;
        }
        return true;
    }

    public boolean upDateInsertBLIgnoredDevices (  String BlDeviceName, String BlDeviceMac){
        SQLiteDatabase db = this.getWritableDatabase();
        // einfach mit cnpaste vom sqlite studio in den string :-)
        try{
            //try first to insert..if not existing
            String name = BlDeviceName;
            String mac = BlDeviceMac;
            String qs = "INSERT OR REPLACE INTO tbl_blScanIgnoredDevices  VALUES( '" + name + "','" + mac + "' );";
            db.execSQL(qs);
            db.close();
        }catch ( Exception e){
            String err = "upDateInsertBLscanData()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            return false;
        }
        return true;
    }



    public ArrayList<String> getAllBldevices(final String lastXMinusMinutes){
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5  als string

        ArrayList buffer = new ArrayList<String>();
        Cursor cursorData;

        try{
            String minusminutes = lastXMinusMinutes;  // "-500";
            String qs = "select * from tbl_blScan where TS >= Datetime('now', '" + minusminutes + " minutes', 'localtime') and blMacAddr not in (select blMacAddr from tbl_blScanIgnoredDevices) order by blRSSI desc;";


            SQLiteDatabase db = this.getReadableDatabase();
            cursorData = db.rawQuery(qs,null);

            if (cursorData.getCount() == 0){
                Log.i(TAG, "ERR / getNearByDevicesFromLastNminutes() - No Data Found! " );
                return buffer;
            }
        }catch ( Exception e){
            String err = "getAllBldevices()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            return buffer;
        }
        String header = "BL-BLE: TimeStamp     |  Name  | MAC  | RSSI";
        buffer.add(header);
        // loop results  create new cursor objects
        while (cursorData.moveToNext()){
            String row =  cursorData.getString(0) + " | " +  cursorData.getString(1) + " | " +  cursorData.getString(2) + " | " + (int)(cursorData.getDouble(3))  +"\n" ;
            buffer.add(row);

        }
        // close and return the list
        return buffer;
    }

    public ArrayList<String> getNearByBluetoothDevicesFromLastNminutes(final String lastXMinusMinutes){
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5

        ArrayList buffer = new ArrayList<String>();
        String minusMinutes = lastXMinusMinutes;


        String qs = "with nx as\n" +
                "(SELECT  s.blRSSI, s.blMacAddr,\n" +
                "Case\n" +
                "    when abs(s.blRSSI) <= 110 and  abs(s.blRSSI) > 90 then -97\n" +
                "    when abs(s.blRSSI) <= 90 and  abs(s.blRSSI) > 80 then -90\n" +
                "    when abs(s.blRSSI) <= 80 and  abs(s.blRSSI) > 70 then -80\n" +
                "    when abs(s.blRSSI) <= 70 and  abs(s.blRSSI) > 60 then -70\n" +
                "    when abs(s.blRSSI) <= 60 and  abs(s.blRSSI) > 50 then -60\n" +
                "    when abs(s.blRSSI) <= 50 and  abs(s.blRSSI) > 40 then -50\n" +
                "    when abs(s.blRSSI) <= 40 and  abs(s.blRSSI) > 30 then -40\n" +
                "    when abs(s.blRSSI) <= 30 and  abs(s.blRSSI) > 20 then -30\n" +
                "    when abs(s.blRSSI) <= 20 and  abs(s.blRSSI) > 10 then -20\n" +
                "    when abs(s.blRSSI) <= 10 and  abs(s.blRSSI) > 0 then -10\n" +
                "END rssiAligned\n" +
                "FROM tbl_blScan s WHERE s.TS >= Datetime('now', '" + minusMinutes + " minutes', 'localtime')\n" +
                "and blMacAddr not in (select blMacAddr from tbl_blScanIgnoredDevices) GROUP BY s.blRSSI, s.blMacAddr\n" +
                ")\n" +
                "select nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned order by nx.rssiAligned desc;";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);

        if (cursorData.getCount() == 0){
            Log.i(TAG, "ERR / getNearByBluetoothDevicesFromLastNminutes() - No Data Found! " );
            return buffer;
        }
        String header = "Distance | BL Devices";
        buffer.add(header);
        // loop results  create new cursor objects
        while (cursorData.moveToNext()){
            String row = (int)( cursorData.getDouble(0)) + " | " + (int)(cursorData.getDouble(1) )+ "\n" ;
            buffer.add(row);
            if (Math.abs((int)( cursorData.getDouble(0))) <= 70 ){
                // social alarm ... SOCIAL Distancing Violation
            }
        }
        // close and return the list
        return buffer;
    }

    /*
     get the stored Settings - https://stackoverflow.com/questions/11480171/fetching-single-value-from-sqlite-in-android

     !!:
     wenn KEINE DB vorhanden ist, dann knallt die abfrage...das ist jetzt im tryCatch.
     Geben dann erste einmal einen default wert zurück

     */
    public String getStringSettingsFromDb(  String selectName, String defaultValue){
        SQLiteDatabase db = this.getWritableDatabase();

        try{
            String result = DatabaseUtils.stringForQuery(db,
                    "SELECT " + selectName + " FROM tbl_Settings  LIMIT 1", null);
            return result;
        }catch ( Exception e){
            String err = "getStringSettingsFromDb() ()/Exception: " + e.getMessage();
            Log.e(TAG, err);
        }
        String errMsg = "NO " + selectName + " defined/Stored in DB";
        return defaultValue;
    }

    public int getIntSettingsFromDb(  String selectName, int defaultValue){
        SQLiteDatabase db = this.getWritableDatabase();

        try{
            long result = DatabaseUtils.longForQuery(db,
                    "SELECT " + selectName + " FROM tbl_Settings  LIMIT 1", null);
            return (int)result;
        }catch ( Exception e){
            String err = "getStringSettingsFromDb() ()/Exception: " + e.getMessage();
            Log.e(TAG, err);
        }
        String errMsg = "NO " + selectName + " defined/Stored in DB";
        return defaultValue;
    }


    /*
    F.R.E.E.Z.E

    UC: FREEZE is storing ALL the MAC from ALL available Devices from the last X Minutes
    - Delete all Data from tbl_freeze
    - insert all available Scanner Data in freeze
    - get the current freezed count
     */

    ArrayList freezedDevicesBuffer = new ArrayList<String>();

    public int freeze_FillTblWithAvailableMac(final String lastXMinusMinutes){
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5
        freezedDevicesBuffer.clear();

        Cursor cursorData;
        int cnt_FreezedData = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        try{
            db.delete("tbl_freeze;", null, null);

            String qs =    "INSERT INTO tbl_freeze (\n" +
                    "                           MAC,\n" +
                    "                           SenderType,\n" +
                    "                           lastRssi                          \n" +
                    "                       )\n" +
                    "                       SELECT distinct  wifiMacAddrStr AS MAC,\n" +
                    "                              'WiFi' AS SenderType,\n" +
                    "                              MAX(wifiRSSI) AS lastRssi\n" +
                    "                         FROM  tbl_wifiScan\n" +
                    "                        WHERE TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') group by wifiMacAddr order by TS desc; ";

            db.beginTransaction();   // https://www.codota.com/code/java/methods/android.database.sqlite.SQLiteDatabase/rawQuery
            db.execSQL(qs);
            db.setTransactionSuccessful();
            db.endTransaction();

            qs =    "INSERT INTO tbl_freeze (\n" +
                    "                           MAC,\n" +
                    "                           SenderType,\n" +
                    "                           lastRssi                          \n" +
                    "                       )\n" +
                    "                       SELECT distinct  blMacAddr AS MAC,\n" +
                    "                              'BtBle' AS SenderType,\n" +
                    "                              MAX(blRSSI) AS lastRssi\n" +
                    "                         FROM  tbl_blScan\n" +
                    "                        WHERE TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') group by blMacAddr order by TS desc; ";

            db.beginTransaction();   // https://www.codota.com/code/java/methods/android.database.sqlite.SQLiteDatabase/rawQuery
            db.execSQL(qs);
            db.setTransactionSuccessful();
            db.endTransaction();

            /*
            qs =  "INSERT INTO tbl_freeze (\n" +
                    "                           MAC,\n" +
                    "                           SenderType,\n" +
                    "                           lastRssi,\n" +
                    "                           exposureID,\n" +
                    "                           lastDistance                          \n" +
                    "                       )\n" +
                    "                       SELECT distinct  bleMacAddr AS MAC,\n" +
                    "                              'ExpBle' AS SenderType,\n" +
                    "                              MAX(bleRSSI) AS lastRssi,\n" +
                    "                              exposureID,\n" +
                    "                              distance as lastDistance                               \n" +
                    "                         FROM  tbl_bleExposureScan\n" +
                    "                        WHERE TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') group by bleMacAddr; ";

            db.beginTransaction();   // https://www.codota.com/code/java/methods/android.database.sqlite.SQLiteDatabase/rawQuery
            db.execSQL(qs);
            db.setTransactionSuccessful();
            db.endTransaction();

             */

            cursorData = db.rawQuery("select distinct MAC  from  tbl_freeze order by MAC;",null);
            cnt_FreezedData = cursorData.getCount();

            String header = "freezeMAC";
            freezedDevicesBuffer.add(header);
            // loop results  create new cursor objects
            while (cursorData.moveToNext()){
                String row = (cursorData.getString(0) ) ;
                freezedDevicesBuffer.add(row);
            }
            cursorData.close();

        }  catch ( Exception e){
            String err = "freeze_FillTblWithAvailableMac()/Exception: " + e.getMessage();
            Log.e(TAG, err);
        }
        // close and return the list
        return cnt_FreezedData ;
    }

// detect a Freeze violation..means there are NEW Targets since last FREEZE

    public int freeze_detectViolation(final String lastXMinusMinutes){
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5

        Cursor cursorData;
        int cnt_FreezedData = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList bufferWifi = new ArrayList<String>();  // delMe..nur zum testen des algorythmus
        ArrayList bufferBlBle = new ArrayList<String>();
        ArrayList bufferC19 = new ArrayList<String>();

        try{

            String qs = "";
            String header ="";

           // funktioniert jetzt..fehler war ein group über die wifiMacs!!

            qs = "select distinct wifiMacAddrStr  FROM  tbl_wifiScan\n" +
                    "where TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime')  and \n" +
                    " not  EXISTS (SELECT  MAC\n" +
                    "                  FROM tbl_freeze\n" +
                    "                  WHERE tbl_wifiScan.wifiMacAddrStr = tbl_freeze.MAC and SenderType = 'WiFi' );\n";
            cursorData = db.rawQuery(qs,null);
            cnt_FreezedData = cursorData.getCount();
            // delMe..nur zum testen des algorythmus - hier darf NUR der neuer gescante wifiBeacon erscheinen
            header = "MACwifi_alert";
            bufferWifi.add(header);
            while (cursorData.moveToNext()){
                String row = (cursorData.getString(0) ) ;
                bufferWifi.add(row);
            }

            qs = "select distinct blMacAddr FROM  tbl_blScan\n" +
                    "where TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime')  and \n" +
                    "  not EXISTS (SELECT MAC FROM tbl_freeze  WHERE tbl_blScan.blMacAddr  = tbl_freeze.MAC and SenderType = 'BtBle');";

            cursorData = db.rawQuery(qs,null);
            cnt_FreezedData = cnt_FreezedData + cursorData.getCount();
            // delMe..nur zum testen des algorythmus - hier darf NUR der neuer gescante wifiBeacon erscheinen
            header = "MACbt_alert";
            bufferBlBle.add(header);
            while (cursorData.moveToNext()){
                String row = (cursorData.getString(0) ) ;
                bufferBlBle.add(row);
            }

            /*  Exposure ist BLE deshalb hier nicht auswerten
            qs = "select distinct bleMacAddr   FROM  tbl_bleExposureScan\n" +
                    "where TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime')  and \n" +
                    " not EXISTS (SELECT MAC FROM tbl_freeze  WHERE tbl_bleExposureScan.bleMacAddr  = tbl_freeze.MAC);";
            cursorData = db.rawQuery(qs,null);
            cnt_FreezedData = cnt_FreezedData + cursorData.getCount();
            // delMe..nur zum testen des algorythmus - hier darf NUR der neuer gescante wifiBeacon erscheinen
            header = "MACc19_alert";
            bufferC19.add(header);
            while (cursorData.moveToNext()){
                String row = (cursorData.getString(0) ) ;
                bufferC19.add(row);
            }

             */

            cursorData.close();

        }  catch ( Exception e){
            String err = "detectFreezeViolation()/Exception: " + e.getMessage();
            Log.e(TAG, err);
        }
        // close and return the list
        return cnt_FreezedData ;
    }


}
