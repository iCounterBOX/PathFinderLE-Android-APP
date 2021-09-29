/*

etwas basic aber geht

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".TestActivity">

    <com.github.mikephil.charting.charts.BarChart
        android:id="@+id/barchart"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/nearByDevicesListViewCh"></com.github.mikephil.charting.charts.BarChart>

    <ListView
        android:id="@+id/testListview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</RelativeLayout>

 */

package com.mcuhq.ple_v3;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.util.ArrayList;

public class BarChartActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();


    // Status Infos
    private TextView mStatusInfoText1;
    private TextView mStatusInfoText2;


    private BarChart chart;
    private ListView mNearByDevicesListView;
    AlertDialog.Builder mSocialAlertDialog;

    //DataBase sqLite
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        //DataBase sqLite
        dataBaseHelper = new DataBaseHelper(this);


        mStatusInfoText1 = (TextView) findViewById(R.id.tv_state1);
        mStatusInfoText2 = (TextView) findViewById(R.id.tv_state2);

        // ALARM

        //SOCIAL DISTANCE ALARM - https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
        mSocialAlertDialog = new AlertDialog.Builder(this);
        mSocialAlertDialog.setTitle("SOCIAL-DISTANCING-ALARM!");
        mSocialAlertDialog.setMessage("Do you want to RECORD the Devices nearby?");
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

        // B a r C h a r t   einrichten und konfigurieren

        chart= (BarChart) findViewById(R.id.barchart);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        final String[] RSSI = new String[]{"10", "20", "30", "40", "50","60", "70", "80", "90", "100"};
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(RSSI);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        chart.setFitBars(true);
        chart.getDescription().setText("PathFinder.LE");  //change labe text  https://geekstocode.com/create-pie-chart-in-android-studio/

        showBarChart("-5");
        StartTimer4BarChart();
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

    private void showBarChart(String lastXMinusMinutes) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        ArrayList<String> data = dataBaseHelper.getNearByDevicesFromLastNminutes("-5");
        if (data.size() > 0) {
            ArrayAdapter arrayAdapter;
            arrayAdapter = new ArrayAdapter( this, android.R.layout.simple_expandable_list_item_1,data) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    /// Get the Item from ListView
                    View view = super.getView(position, convertView, parent);
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    // Set the text size 25 dip for ListView each item
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                    tv.setBackgroundColor(Color.parseColor("olive"));
                    //https://android--code.blogspot.com/2015/08/android-listview-itemrow-height.html
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    // Set the height of the Item View
                    params.height = 35;
                    view.setLayoutParams(params);
                    // Return the view
                    return view;
                }
            };
            mNearByDevicesListView = (ListView)findViewById(R.id.nearByDevicesListViewCh);
            mNearByDevicesListView.setAdapter(arrayAdapter);
        }

        // CHART - BAR CHART

        ArrayList<BarEntry> entries =  getNewNearByDataOnBarChart(dataBaseHelper, lastXMinusMinutes);
        final BarDataSet barDataSet = new BarDataSet(entries, "Mobile Devices (X=rssi/Power  Y=MobileDevice)");
        barDataSet.setBarBorderWidth(0.9f);
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        int[] colors = new int[] { Color.RED, Color.RED, Color.BLACK, Color.DKGRAY , Color.MAGENTA , Color.CYAN, Color.BLUE, Color.LTGRAY, Color.YELLOW, Color.GREEN};
        barDataSet.setColors(colors);
        final BarData barData = new BarData(barDataSet);
        chart.setData(barData);
        chart.animateXY(1000, 1000);
        chart.invalidate();
    }


    // get sqLite data
    public ArrayList<BarEntry> getNewNearByDataOnBarChart(DataBaseHelper dataBaseHelper, String lastXMinusMinutes) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        String[] RSSI4Xaxis = new String[100];

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
                "select nx.rssiAligned, Count (nx.rssiAligned ) as mobileDeviceCnt from nx group by nx.rssiAligned";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursorData = db.rawQuery(qs,null);
        int rssiAligned = 0;

        if (cursorData.getCount() != 0){
            // loop results  create new cursor objects
            int idx = 0;
            while (cursorData.moveToNext()){
                float deviceCount = cursorData.getFloat(1);
                rssiAligned = Math.abs((int)(cursorData.getDouble(0)));

                if (rssiAligned  == 10)
                    entries.add(new BarEntry(0f, deviceCount));
                else
                    entries.add(new BarEntry(0f, 0));

                if (rssiAligned  == 20)
                    entries.add(new BarEntry(1f, deviceCount));
                else
                    entries.add(new BarEntry(1f, 0));

                if (rssiAligned  == 30)
                    entries.add(new BarEntry(2f, deviceCount));
                else
                    entries.add(new BarEntry(2f, 0));

                if (rssiAligned  == 40)
                    entries.add(new BarEntry(3f, deviceCount));
                else
                    entries.add(new BarEntry(3f, 0));

                if (rssiAligned  == 50)
                    entries.add(new BarEntry(4f, deviceCount));
                else
                    entries.add(new BarEntry(4f, 0));

                if (rssiAligned  == 60)
                    entries.add(new BarEntry(5f, deviceCount));
                else
                    entries.add(new BarEntry(5f, 0));

                if (rssiAligned  == 70)
                    entries.add(new BarEntry(6f, deviceCount));
                else
                    entries.add(new BarEntry(6f, 0));

                if (rssiAligned  == 80)
                    entries.add(new BarEntry(7f, deviceCount));
                else
                    entries.add(new BarEntry(7f, 0));

                if (rssiAligned  == 90)
                    entries.add(new BarEntry(8f, deviceCount));
                else
                    entries.add(new BarEntry(8f, 0));

                if (rssiAligned  == 97)
                    entries.add(new BarEntry(9f, deviceCount));
                else
                    entries.add(new BarEntry(9f, 0));
            }
        }else
            Log.i(TAG, "ERR / showNewNearByDataOnBarChart() - No Data Found! " );

        if (rssiAligned == 10 || rssiAligned == 20 || rssiAligned == 30 || rssiAligned == 40 ) {
            // ALERT
            // showSocialAlert();  // funktioniert
        }
        return entries;
    }

    private void showSocialAlert() {
        // https://stackoverflow.com/questions/18459122/play-sound-on-button-click-android
        // wichtig: rawfolder und dort einen sound ablegen
        final MediaPlayer mp = MediaPlayer.create(this,R.raw.alarm1);
        mp.start();
        AlertDialog showSocialDistanceAlarm = mSocialAlertDialog.create();
        showSocialDistanceAlarm.show();
    }

    // local timer

    //https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi
    private Handler handler;
    private Runnable handlerTask;

    void StartTimer4BarChart(){
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
