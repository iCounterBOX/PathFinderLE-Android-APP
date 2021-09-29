package com.mcuhq.ple_v3;

/*
BL Scan OK erkennt das ESP Modul und den Smart solar

Source:

https://code.tutsplus.com/tutorials/create-a-bluetooth-scanner-with-androids-bluetooth-api--cms-24084
https://stackoverflow.com/questions/42648150/simple-android-ble-scanner

Für RSSI:
https://stackoverflow.com/questions/15312858/get-bluetooth-signal-strength
für die MAC
https://stackoverflow.com/questions/3170805/how-to-scan-for-available-bluetooth-devices-in-range-in-android

ToDo:
BlScanTable in db
TS | Name | MAC | RSSI

nach 10 sec schaltet der broadCast scan AUS...deshalb restarten wir den scan jetzt im timer/scheduler

 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BLscanActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    // ListViews:
    private ListView mBLscanListView;
    private ListView mBLscanListView_distance;

    // Bluetooth CHart
    private BarChart chart;
    private ListView mNearByBTdevicesListView;  // BT CHART


    ArrayList mBlScanArray = new ArrayList<String>();

    //private Boolean mEsIstEtwasInDerListe = false;

    //DataBase sqLite
    DataBaseHelper dataBaseHelper;

    // just some tools
    ToolsClass mToolsClass;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blscan_activity);

        // some tools
        mToolsClass = new ToolsClass();

        //DataBase sqLite
        dataBaseHelper = new DataBaseHelper(this);

        mBLscanListView= (ListView)findViewById(R.id.lv_blScan_raw);
        mBLscanListView_distance = (ListView)findViewById(R.id.lv_blScan_Distance);

        //registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        /*
        Button boton = (Button) findViewById(R.id.btn_blScan2);
        boton.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                BTAdapter.startDiscovery();
            }
        });
         */

        String header = "TS|Name|RSSI|MAC";
        mBlScanArray.add(header);

        // wir legen gleich mit dem scan los:
        //BTAdapter.startDiscovery();

        // B a r C h a r t   einrichten und konfigurieren

        chart= (BarChart) findViewById(R.id.barchartBT);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        final String[] RSSI = new String[]{"10", "20", "30", "40", "50","60", "70", "80", "90", "100"};
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(RSSI);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        chart.setFitBars(true);
        chart.getDescription().setText("PathFinder.LE");  //change labe text  https://geekstocode.com/create-pie-chart-in-android-studio/

        showBarChart();

        //wir listen erst einmal was da ist
        showBLscaninLV_raw();
        showBLscaninLV_distance();

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



    // show the BL scans from last x minutes..raw with TS and MAC etc

    private void showBLscaninLV_raw() {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        ArrayList<String> blScanArray = dataBaseHelper.getAllBldevices("-5");

        if (blScanArray.size() > 0){
            ArrayAdapter arrayAdapter;
            arrayAdapter = new ArrayAdapter( this, android.R.layout.simple_expandable_list_item_1,blScanArray) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    /// Get the Item from ListView
                    View view = super.getView(position, convertView, parent);
                    view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    // Set the text size 25 dip for ListView each item
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                    tv.setBackgroundColor(Color.parseColor("lightgrey"));
                    //https://android--code.blogspot.com/2015/08/android-listview-itemrow-height.html
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    // Set the height of the Item View
                    params.height = 40;
                    view.setLayoutParams(params);
                    // Return the view
                    return view;
                }
            };
            mBLscanListView = (ListView)findViewById(R.id.lv_blScan_raw);
            mBLscanListView.setAdapter(arrayAdapter);
        }else
        {
            Toast.makeText(getApplicationContext(), "showBLscaninLV()/No New Data! " , Toast.LENGTH_SHORT).show();
        }
    }

    // show the BL scans from last x minutes..aggregated..only the amount of MDevices in a specific distance

    private void showBLscaninLV_distance() {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        ArrayList<String> blScanArray = dataBaseHelper.getNearByBluetoothDevicesFromLastNminutes("-5");

        if (blScanArray.size() > 0){
            ArrayAdapter arrayAdapter;
            arrayAdapter = new ArrayAdapter( this, android.R.layout.simple_expandable_list_item_1,blScanArray) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    /// Get the Item from ListView
                    View view = super.getView(position, convertView, parent);
                    view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    // Set the text size 25 dip for ListView each item
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                    tv.setBackgroundColor(Color.parseColor("olive"));
                    //https://android--code.blogspot.com/2015/08/android-listview-itemrow-height.html
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    // Set the height of the Item View
                    params.height = 40;
                    view.setLayoutParams(params);
                    // Return the view
                    return view;
                }
            };
            mBLscanListView_distance = (ListView)findViewById(R.id.lv_blScan_Distance);
            mBLscanListView_distance.setAdapter(arrayAdapter);
        }else
        {
            Toast.makeText(getApplicationContext(), "showBLscaninLV_distance()/No New Data! " , Toast.LENGTH_SHORT).show();
        }
    }

/*
BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART

*/

    private void showBarChart() {

        // CHART - BAR CHART

        ArrayList<BarEntry> entries =  getNewNearByBTdataOnBarChart(dataBaseHelper, "-5");
        final BarDataSet barDataSet = new BarDataSet(entries, "BT Devices (X=rssi/Power  Y=BTDevice)");
        barDataSet.setBarBorderWidth(0.9f);
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        int[] colors = new int[] { Color.RED, Color.RED, Color.BLACK, Color.DKGRAY , Color.MAGENTA , Color.CYAN, Color.BLUE, Color.LTGRAY, Color.YELLOW, Color.GREEN};
        barDataSet.setColors(colors);
        final BarData barData = new BarData(barDataSet);
        chart.setData(barData);
        chart.animateXY(1000, 1000);
        chart.invalidate();
    }

    // get sqLite data..get BT devices near by
    public ArrayList<BarEntry> getNewNearByBTdataOnBarChart(DataBaseHelper dataBaseHelper, String lastXMinusMinutes) {
        // get data from DB
        // !!!!!!!!!!!  lastXminutes    zb  -5
        String minusMinutes = lastXMinusMinutes;
        ArrayList<BarEntry> entries = new ArrayList<>();

        // dürfen NUR max die 10 sein die auch im chart anzeigt werden
        for (int i = 0; i < 10; i++) {
            entries.add(new BarEntry(i, 0));  // achtung
        }


        String qs = "with nx as\n" +
                "(SELECT  s.blRSSI, s.blMacAddr, s.blRSSI as rssiAligned \n" +
                "FROM tbl_blScan s WHERE s.TS >= Datetime('now', '" + minusMinutes + " minutes', 'localtime')\n" +
                "and blMacAddr not in (select blMacAddr from tbl_blScanIgnoredDevices) GROUP BY s.blRSSI, s.blMacAddr\n" +
                ")\n" +
                "select nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned;";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);
        int rssiAligned = 0;

        if (cursorData.getCount() != 0){
            // loop results  create new cursor objects
            int idx = 0;
            while (cursorData.moveToNext()){
                rssiAligned = Math.abs((int)(cursorData.getDouble(0)));
                float deviceCount = cursorData.getFloat(1);
                rssiAligned = mToolsClass.calculateSignalLevel(rssiAligned, 100,45,110);

                // Von db der rssi raw nehmen..dieser wird in calculateSignalLevel() normalisiert ( aligned zw BT and wifi )
                // auf die 10 elemente (x-achse ) verteilen...und dabei die werte AUFADDIEREN!

                if ( rssiAligned  >= 0 &&  rssiAligned  < 20 )  entries.set(0,new BarEntry(0f, entries.get(0).getY() + deviceCount));
                if ( rssiAligned  >= 20 &&  rssiAligned  < 30 ) entries.set(1,new BarEntry(1f, entries.get(1).getY() + deviceCount));
                if ( rssiAligned  >= 30 &&  rssiAligned  < 40 ) entries.set(2,new BarEntry(2f, entries.get(2).getY() + deviceCount));
                if ( rssiAligned  >= 40 &&  rssiAligned  < 50 ) entries.set(3,new BarEntry(3f, entries.get(3).getY() + deviceCount));
                if ( rssiAligned  >= 50 &&  rssiAligned  < 60 ) entries.set(4,new BarEntry(4f, entries.get(4).getY() + deviceCount));
                if ( rssiAligned  >= 60 &&  rssiAligned  < 70 ) entries.set(5,new BarEntry(5f, entries.get(5).getY() + deviceCount));
                if ( rssiAligned  >= 70 &&  rssiAligned  < 80 ) entries.set(6,new BarEntry(6f, entries.get(6).getY() + deviceCount));
                if ( rssiAligned  >= 80 &&  rssiAligned  < 90 ) entries.set(7,new BarEntry(7f, entries.get(7).getY() + deviceCount));
                if ( rssiAligned  >= 90 &&  rssiAligned  < 100 ) entries.set(8,new BarEntry(8f,entries.get(8).getY() + deviceCount));
                if ( rssiAligned  >= 100 &&  rssiAligned  < 110 ) entries.set(9,new BarEntry(9f,entries.get(9).getY() + deviceCount));
            }
        }else
            Log.i(TAG, "ERR / getNewNearByBTdataOnBarChart() - No Data Found! " );

        if (rssiAligned == 10 || rssiAligned == 20 || rssiAligned == 30 || rssiAligned == 40 ) {
            // ALERT
            // showSocialAlert();  // funktioniert
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
                 showBLscaninLV_raw();
                 showBLscaninLV_distance();
                 showBarChart();
                 handler.postDelayed(handlerTask, 3000);
            }
        };
        handlerTask.run();
    }
}