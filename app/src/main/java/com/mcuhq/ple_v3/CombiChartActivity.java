package com.mcuhq.ple_v3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class CombiChartActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();


    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    // ListViews:

    // Status Infos
    private TextView mStatusInfoText1;
    private TextView mStatusInfoText2;

    // Social Distancing Violation
    private Boolean mSDV_BL = false;
    private Boolean mSDV_wifi = false;

    // Bluetooth CHart
    private BarChart chart;
    private ListView mNearByBTdevicesListView;  // BT CHART


    ArrayList mBlScanArray = new ArrayList<String>();

    //private Boolean mEsIstEtwasInDerListe = false;

    //DataBase sqLite
    DataBaseHelper dataBaseHelper;

    ToolsClass mToolsClass;

    protected Typeface tfLight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combi_chart);

        //DataBase sqLite
        dataBaseHelper = new DataBaseHelper(this);

        mToolsClass = new ToolsClass();

        mStatusInfoText1 = (TextView) findViewById(R.id.tv_combiState1);
        mStatusInfoText2 = (TextView) findViewById(R.id.tv_combiState2);


        String header = "TS|Name|RSSI|MAC";
        mBlScanArray.add(header);

        // wir legen gleich mit dem scan los:
        //BTAdapter.startDiscovery();

        // B a r C h a r t   einrichten und konfigurieren

        chart= (BarChart) findViewById(R.id.barchartCombi);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        final String[] RSSI = new String[]{"10", "20", "30", "40", "50","60", "70", "80", "90", "100"};
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(RSSI);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        chart.setFitBars(true);
        chart.getDescription().setText("PathFinder.LE");  //change labe text  https://geekstocode.com/create-pie-chart-in-android-studio/

        showBarChart("-5");


        // Timer um den Inhalt anzuzeigen:

        StartTimer4BLscan();
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




/*
BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART
NOW we do the MultiDataSetChart: https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/BarChartActivityMultiDataset.java
// (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"
// (0.2 + 0.03) * 2 + 0,54 = 1.00 -> interval per "group"
// (0.2 + 0.03) * 3 + 0,31 = 1.00 -> interval per "group"
*/


    // Multiple Charts
    private void showBarChart(String lastXMinusMinutes) {

        // CHART - BAR CHART

        float groupSpace = 0.31f;  // für 4 : 0.08f   ... für 2 : 0.54f...für 3: 0.31fv
        float barSpace = 0.03f; // x4 DataSet
        float barWidth = 0.2f; // x4 DataSet

        int groupCount = 10;  // dBm 0 bis  -97  in 10 schritten
        int start_dBm = 0;
        int endYear = start_dBm + groupCount;


        ArrayList<BarEntry> values1 =  getNewNearByBTdataOnBarChart(dataBaseHelper, lastXMinusMinutes);  // i.d.r. "-5" ..letzte 5 minuten
        ArrayList<BarEntry> values2 =  getNewNearByWiFiDataOnBarChart(dataBaseHelper, lastXMinusMinutes);
        ArrayList<BarEntry> values3 =  getNewNearByEXPOSUREdataOnBarChart(dataBaseHelper, lastXMinusMinutes);



        if (mSDV_wifi == true || mSDV_BL == true  ) {
            mStatusInfoText2.setBackgroundColor(Color.RED);
            mStatusInfoText2.setTextColor(Color.YELLOW);
            mStatusInfoText2.setText("CORONA\nSDV ALERT");
            // showSocialAlert();  // funktioniert
        }else
        {
            mStatusInfoText2.setBackgroundColor(Color.GREEN);
            mStatusInfoText2.setTextColor(Color.BLUE);
            mStatusInfoText2.setText("SDV State\n-SAVE-" );
        }

        BarDataSet set1, set2, set3;
        set1 = new BarDataSet(values1, "BL / BLE Scan");
        set1.setColor(Color.rgb(0, 89, 179));  // blue
        set2 = new BarDataSet(values2, "WiFi Scan");
        set2.setColor(Color.rgb(153, 204, 0));  // Green
        set3 = new BarDataSet(values3, "C19 Scan");
        set3.setColor(Color.rgb(255, 102, 0));  // ORANGE
        BarData data = new BarData(set1, set2, set3);
        data.setValueFormatter(new LargeValueFormatter());
        data.setValueTypeface(tfLight);

        chart.setData(data);
        chart.animateXY(1000, 1000);

        // specify the width each bar should have
        chart.getBarData().setBarWidth(barWidth);

        // restrict the x-axis range
        chart.getXAxis().setAxisMinimum(start_dBm);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        chart.getXAxis().setAxisMaximum(start_dBm + chart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        chart.groupBars(start_dBm, groupSpace, barSpace);
        chart.invalidate();
    }


    // get sqLite data..get BT devices near by
    public ArrayList<BarEntry> getNewNearByBTdataOnBarChart(DataBaseHelper dataBaseHelper, String lastXMinusMinutes) {
        String qs = "with nx as\n" +
                "(SELECT  s.blRSSI, s.blMacAddr, s.blRSSI as rssiAligned \n" +
                "FROM tbl_blScan s WHERE s.TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime')\n" +
                "and blMacAddr not in (select blMacAddr from tbl_blScanIgnoredDevices) GROUP BY s.blRSSI, s.blMacAddr\n" +
                ")\n" +
                "select nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned;";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);
        ArrayList<BarEntry> entries = new ArrayList<>();
        if (cursorData.getCount() != 0){
            entries = getBarEntries(cursorData,100,45,102);  // we do NOT the normal BT devices
        }else
            Log.i(TAG, "info / getNewNearByBTdataOnBarChart() - No Data Found! " );
        return entries;  // entries for the Chart
    }


    // get sqLite data..get EXPOSURE NOTIFICATION DATA
    public ArrayList<BarEntry> getNewNearByEXPOSUREdataOnBarChart(DataBaseHelper dataBaseHelper, String lastXMinusMinutes) {
        String qs = "with nx as\n" +
                "(SELECT  s.bleRSSI as rssiAligned ,  s.bleMacAddr\n" +
                "FROM tbl_bleExposureScan s WHERE s.TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') GROUP BY s.bleRSSI, s.bleMacAddr\n" +
                ")\n" +
                "select nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned;\n";
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);
        ArrayList<BarEntry> entries = new ArrayList<>();
        if (cursorData.getCount() != 0){
            entries = getBarEntries(cursorData,100,60,102);
        }else
            Log.i(TAG, "info / getNewNearByEXPOSUREdataOnBarChart() - No Data Found! " );
        return entries;  // entries for the Chart
    }

    // WIFI - get sqLite data
    public ArrayList<BarEntry> getNewNearByWiFiDataOnBarChart(DataBaseHelper dataBaseHelper, String lastXMinusMinutes) {

        String qs = "with nx as\n" +
                "(\n" +
                "SELECT  s.wifiRSSI,s.wifiMacAddr, s.wifiRSSI as rssiAligned\n" +
                "FROM tbl_wifiScan s WHERE s.TS >= Datetime('now', '" + lastXMinusMinutes + " minutes', 'localtime') GROUP BY s.wifiRSSI, s.wifiMacAddr\n" +
                ")\n" +
                "select nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);

        ArrayList<BarEntry> entries = new ArrayList<>();
        if (cursorData.getCount() != 0){
            entries = getBarEntries(cursorData,100,10,99);
        }else
            Log.i(TAG, "info / getNewNearByBTdataOnBarChart() - No Data Found! " );
        return entries;  // entries for the Chart
    }


    private ArrayList<BarEntry> getBarEntries(Cursor cursorData, int numLevel, int minRssi, int maxRssi) {
        int rssiAligned = 0;
        ArrayList<BarEntry> entries = new ArrayList<>();
        // dürfen NUR max die 10 sein die auch im chart anzeigt werden
        for (int i = 0; i < 10; i++) {
            entries.add(new BarEntry(i*10, 0));
        }
        // loop results  create new cursor objects
        int idx = 0;
        while (cursorData.moveToNext()){
            rssiAligned = Math.abs((int)(cursorData.getDouble(0)));
            float deviceCount = cursorData.getFloat(1);
            rssiAligned = mToolsClass.calculateSignalLevel(rssiAligned, numLevel,minRssi,maxRssi);

            // Von db der rssi raw nehmen..dieser wird in calculateSignalLevel() normalisiert ( aligned zw BT and wifi )
            // auf die 10 elemente (x-achse ) verteilen...und dabei die werte AUFADDIEREN!

            if ( rssiAligned  >= 0 &&  rssiAligned  < 20 )  entries.set(0,new BarEntry(10f, entries.get(0).getY() + deviceCount));
            if ( rssiAligned  >= 20 &&  rssiAligned  < 30 ) entries.set(1,new BarEntry(20f, entries.get(1).getY() + deviceCount));
            if ( rssiAligned  >= 30 &&  rssiAligned  < 40 ) entries.set(2,new BarEntry(30f, entries.get(2).getY() + deviceCount));
            if ( rssiAligned  >= 40 &&  rssiAligned  < 50 ) entries.set(3,new BarEntry(40f, entries.get(3).getY() + deviceCount));
            if ( rssiAligned  >= 50 &&  rssiAligned  < 60 ) entries.set(4,new BarEntry(50f, entries.get(4).getY() + deviceCount));
            if ( rssiAligned  >= 60 &&  rssiAligned  < 70 ) entries.set(5,new BarEntry(60f, entries.get(5).getY() + deviceCount));
            if ( rssiAligned  >= 70 &&  rssiAligned  < 80 ) entries.set(6,new BarEntry(70f, entries.get(6).getY() + deviceCount));
            if ( rssiAligned  >= 80 &&  rssiAligned  < 90 ) entries.set(7,new BarEntry(80f, entries.get(7).getY() + deviceCount));
            if ( rssiAligned  >= 90 &&  rssiAligned  < 100 ) entries.set(8,new BarEntry(90f,entries.get(8).getY() + deviceCount));
            if ( rssiAligned  >= 100 &&  rssiAligned  < 120 ) entries.set(9,new BarEntry(100f,entries.get(9).getY() + deviceCount));

        }
        if (rssiAligned >= 1 && rssiAligned <= 50 ) {
            mSDV_BL = true;
        }else
        {
            mSDV_BL = false;
        }
        return entries;
    }



// local timer

    //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
    private Handler handler;
    private Runnable handlerTask;

    void StartTimer4BLscan(){
        final int[] count = {0};
        handler = new Handler();
        handlerTask = new Runnable()
        {
            @Override
            public void run() {
                //do something

                String lastXMinutes = "-5";
                showBarChart(lastXMinutes);
                int cntWifi = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_wifiScan","wifiMacAddr",lastXMinutes);
                int cntBT = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_blScan","blMacAddr",lastXMinutes);
                int cntC19 = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_bleExposureScan","bleMacAddr",lastXMinutes);
                mStatusInfoText1.setText("Scan from last " + lastXMinutes + " Minutes: \nWiFi=" + cntWifi  + "  BT=" + cntBT +  "  C19=" + cntC19 + " (unique MAC)");
                handler.postDelayed(handlerTask, 4000);
            }
        };
        handlerTask.run();
    }
}