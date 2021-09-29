package com.mcuhq.ple_v3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/*
History-Chart
statt MP AndroidChart mal den AnyChart testen
MP machte probleme mit den TimeStamps

Neu:
https://github.com/AnyChart/AnyChart-Android/blob/master/sample/src/main/res/layout/activity_chart_common.xml

 */

public class HistoryActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    // ListViews:
    private ListView mQueryResultLV ;

    // Status Infos
    private TextView mStatusInfoText1;

    // Progress Bar
    private ProgressBar mProgressBar;

    // Social Distancing Violation
    private Boolean mSDV_BL = false;
    private Boolean mSDV_wifi = false;

    // RangeSlider for MinusMinutesX

    Slider mSlider;
    float mSliderValue = 1;

    // Bluetooth CHart / anyChart - https://github.com/AnyChart/AnyChart-Android/blob/master/sample/src/main/res/layout/activity_chart_common.xml
    AnyChartView mAnyChartView;
    List<DataEntry> mSeriesData;
    private Set set;

    //DataBase sqLite  & TOOLS
    DataBaseHelper dataBaseHelper;
    ToolsClass mToolsClass;

    //Chart
    Cartesian mCartesian;

    String lastXMinutes =   "-50";   //"-1444";  // 1 tag hat 1440 minuten

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // DataBase sqLite
        dataBaseHelper = new DataBaseHelper(this);

        // other classes
        mToolsClass = new ToolsClass();

        // GUI
        mStatusInfoText1 = (TextView) findViewById(R.id.tv_state1);

        // Überschrift d. Activity
        setTitle("PathFinderLE/History");

        //Slider f. lastXMinutes

        mSlider = findViewById((R.id.slider));
        /*
        mSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                // hier kann zB $ mit gegeben werden im slider  - https://www.youtube.com/watch?v=IibybM4oM1w
                return null;
            }
        });

         */
        mSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                mSliderValue = value;
                lastXMinutes = Float.toString(-1440 * mSliderValue);  // 1 Minute * sliderTage
            }
        });

        /*
        LINE C h a r t   einrichten und konfigurieren
        Example with 3 Lines :
        https://github.com/AnyChart/AnyChart-Android/blob/master/sample/src/main/java/com/anychart/sample/charts/LineChartActivity.java
         */
        mAnyChartView = findViewById(R.id.lineChartHistory);
        mProgressBar = findViewById(R.id.progress_bar);
        mAnyChartView.setProgressBar(mProgressBar);

        mCartesian = AnyChart.line();
        mCartesian.animation(true);
        mCartesian.padding(10d, 20d, 5d, 20d);
        mCartesian.crosshair().enabled(true);
        mCartesian.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        mCartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        mCartesian.title("xAxis: e.g. 1916 = 19.Day i. Month 16 oClock");
        mCartesian.yAxis(0).title("wifi + BT/BLE (Count)");
        mCartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        mSeriesData = new ArrayList<>();
        mSeriesData.add(new CustomDataEntry(" ", 0,0,0));

        set = Set.instantiate();  // ohne diesen schritt und ohne den dummy eintrag schein unten dann keine eingabe möglich??
        set.data(mSeriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
        Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");

        Line series1 = mCartesian.line(series1Mapping);
        series1.name("Wifi+BT");
        series1.stroke("4 red");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series2 = mCartesian.line(series2Mapping);
        series2.name("Wifi");
        series2.stroke("2 gray");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series3 = mCartesian.line(series3Mapping);
        series3.name("BT/BLE");
        series3.stroke("2 blue");
        series3.hovered().markers().enabled(true);
        series3.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series3.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        mCartesian.legend().enabled(true);
        mCartesian.legend().fontSize(13d);
        mCartesian.legend().padding(0d, 0d, 10d, 0d);

        mAnyChartView.setChart(mCartesian);

        //  END AnyChart Def... LineChart


       // showLineChart(dataBaseHelper,lastXMinutes );


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





    // show the BL scans from last x minutes..aggregated..only the amount of MDevices in a specific distance



/*
BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART - BAR CHART
LineChart over Time  - lastXdays    zb  -5

ACHTUNG:  AN DEN MONATS_ÜBERGÄNGEN WIRD ES NICHT GANZ STIMMEN:::TO DO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

NEW:

drop table if exists tmpTS;
CREATE TEMPORARY TABLE if not exists tmpTS   (TSHR text, TSX TEXT );
INSERT OR replace INTO tmpTS  SELECT distinct  STRFTIME('%d%H', TS) TSHR, STRFTIME('%Y/%m/%d %H', TS) TSX_BT from  TBL_BLSCAN where TS BETWEEN DATETIME('now', 'localtime', '-2444 days') AND DATETIME('now', 'localtime')  ;
select * from  tmpTS;
INSERT OR replace INTO tmpTS  SELECT distinct STRFTIME('%d%H', TS) TSHR, STRFTIME('%Y/%m/%d %H', TS) TSX_wifi from  TBL_WIFISCAN where TS BETWEEN DATETIME('now', 'localtime', '-2444 days') AND DATETIME('now', 'localtime')  ;
select * from  tmpTS;
INSERT OR replace INTO tmpTS  SELECT distinct STRFTIME('%d%H', TS) TSHR, STRFTIME('%Y/%m/%d %H', TS) TSX_C19 from  tbl_bleExposureScan where TS BETWEEN DATETIME('now', 'localtime', '-2444 days') AND DATETIME('now', 'localtime') ;
--select distinct  * from  tmpTS  order by TSX desc;
SELECT distinct TSHR, TSX
      ,(select count(distinct WIFIMACADDR)  from TBL_WIFISCAN where STRFTIME('%Y/%m/%d %H', TS)  = tmpTS.TSX    group by STRFTIME('%Y/%m/%d %H', TS)
       ) AS WiFiCnt
      ,(select count(distinct BLMACADDR)  from TBL_BLSCAN where STRFTIME('%Y/%m/%d %H', TS)  = tmpTS.TSX    group by STRFTIME('%Y/%m/%d %H', TS)
      ) AS BlCnt
       ,(select count(distinct bleMacAddr) from tbl_bleExposureScan where STRFTIME('%Y/%m/%d %H', TS)  = tmpTS.TSX   group by STRFTIME('%Y/%m/%d %H', TS)
      ) AS C19Cnt
from  tmpTS order by TSX desc ;


*/

    private void showLineChart(DataBaseHelper dataBaseHelper, String lastXdays) {
        //String lastXdays = "-2";
        mSeriesData = new ArrayList<>();
        mStatusInfoText1.setText("Data from last < "  + lastXdays + " > Days");

        Cursor cursorData;
        int cnt_HistoryData = 0;
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        try {

            /*
            Transaction um die temp Tabelle für die Scan-TimeStamps aufzubauen. Nach langen test die beste methode, da es sein kann
            dass zb WiFi-tabelle leer ist ( esp!)  somit sind JOINs nicht möglich...tmpTS enthält ALLE möglichen timestamps aller 3 Tabellen
            und dient somit als referenz für mögliche scan-Hits
             */
            db.execSQL("drop table if exists tmpTS");
            db.execSQL("CREATE TEMPORARY TABLE if not exists tmpTS  (TSHR text, TSX TEXT )");

            // fill with WiFi ( if available )
            String qs ="INSERT OR replace INTO tmpTS  SELECT distinct STRFTIME('%d%H', TS) TSHR, STRFTIME('%Y/%m/%d %H', TS) TSX_wifi from  TBL_WIFISCAN \n" +
                    "where TS BETWEEN DATETIME('now', 'localtime', '" + lastXdays + " days') AND DATETIME('now', 'localtime');";
            db.beginTransaction();
            db.execSQL(qs);
            db.setTransactionSuccessful();
            db.endTransaction();

            // fill with BT and BLE Data
            qs ="INSERT OR replace INTO tmpTS  SELECT distinct  STRFTIME('%d%H', TS) TSHR, STRFTIME('%Y/%m/%d %H', TS) TSX_BT from  TBL_BLSCAN \n" +
                    "where TS BETWEEN DATETIME('now', 'localtime', '" + lastXdays + " days') AND DATETIME('now', 'localtime') ;";
            db.beginTransaction();
            db.execSQL(qs);
            db.setTransactionSuccessful();
            db.endTransaction();

            // fill with Covid19 Exposure Beacon ( BLE ) Data
            qs ="INSERT OR replace INTO tmpTS  SELECT distinct STRFTIME('%d%H', TS) TSHR, STRFTIME('%Y/%m/%d %H', TS) TSX_C19 from  tbl_bleExposureScan \n" +
                    "where TS BETWEEN DATETIME('now', 'localtime', '" + lastXdays + " days')  AND DATETIME('now', 'localtime') ;";
            db.beginTransaction();
            db.execSQL(qs);
            db.setTransactionSuccessful();
            db.endTransaction();

            /*
            die history-result Table
            TSHR	TSX	WiFiCnt	BlCnt	C19Cnt
            2112	2020/11/21 12	30	3	3
            2109	2020/11/21 09	1	1	1
            2108	2020/11/21 08	21	9	4
            2107	2020/11/21 07	16	7
            2106	2020/11/21 06	8	1
            2105	2020/11/21 05	6
            2104	2020/11/21 04	7
             */

            qs ="SELECT distinct TSHR, TSX    \n" +
                    "      ,(select count(distinct WIFIMACADDR)  from TBL_WIFISCAN where STRFTIME('%Y/%m/%d %H', TS)  = tmpTS.TSX    group by STRFTIME('%Y/%m/%d %H', TS) \n" +
                    "       ) AS WiFiCnt\n" +
                    "      ,(select count(distinct BLMACADDR)  from TBL_BLSCAN where STRFTIME('%Y/%m/%d %H', TS)  = tmpTS.TSX    group by STRFTIME('%Y/%m/%d %H', TS)\n" +
                    "      ) AS BlCnt    \n" +
                    "       ,(select count(distinct bleMacAddr) from tbl_bleExposureScan where STRFTIME('%Y/%m/%d %H', TS)  = tmpTS.TSX   group by STRFTIME('%Y/%m/%d %H', TS)\n" +
                    "      ) AS C19Cnt    \n" +
                    "from  tmpTS order by TSX asc ;";

            cursorData = db.rawQuery(qs,null);

            if (cursorData.getCount() != 0){
                // loop results  create new cursor objects
                while (cursorData.moveToNext()){
                    String tsHr = cursorData.getString(0);  // 2005 .. 20.06.20 5 Uhr .. 20.Tag i monat 5 uhr
                    String TimeStamp = cursorData.getString(1); // datum tsx  2020/06/18 16
                    TimeStamp = TimeStamp.replace("2020/","");
                    int cntWifi = cursorData.getInt(2);  // cntWifi
                    int cntBT = cursorData.getInt(3);  // cntBT
                    int cntC19 = cursorData.getInt(4);  // cntC19
                    int cntSum = cntWifi + cntBT+ cntC19;

                    //Reload Data: https://github.com/AnyChart/AnyChart-Android/issues/28
                    mSeriesData.add(new CustomDataEntry(tsHr, cntSum,cntWifi,cntBT));
                }
            }else {
                Toast.makeText(this, "showLineChart()/No Data", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "showLineChart() - No Data Found! ");
            }
            // create a dataset and give it a type
            set.data(mSeriesData);

        } catch (Exception e) {
                Toast.makeText(this, "showLineChart()/ERR", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "EXCEPTION/showLineChart()/",e);
        }
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
               // showLineChart(dataBaseHelper,"-1");
                showLineChart(dataBaseHelper,Float.toString(-1 * mSliderValue));  // e.g. "-3.5"

                int cntWifi = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_wifiScan","wifiMacAddr",lastXMinutes);
                int cntBT = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_blScan","blMacAddr",lastXMinutes);
                int cntC19 = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_bleExposureScan","bleMacAddr",lastXMinutes);
                mStatusInfoText1.setText("Scan from last " + lastXMinutes + " Minutes: \nWiFi=" + cntWifi  + "  BT=" + cntBT +  "  C19=" + cntC19 + " (unique MAC) / slider= "  +  mSliderValue);

                handler.postDelayed(handlerTask, 10000);
            }
        };
        handlerTask.run();
    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2, Number value3) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
        }

    }
}