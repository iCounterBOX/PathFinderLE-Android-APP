

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FreezeActivity extends AppCompatActivity {



    private final String TAG = MainActivity.class.getSimpleName();
    static Handler mHandler; // Our main handler that will receive callback notifications


    // #defines for identifying shared types between calling functions
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    // Status Infos
    private TextView mStatusInfoText1;
    private TextView mStatusInfoText2;
    private TextView mStatusInfoText3;
    private TextView mStatusInfoText4;
    private TextView mReadBuffer;

    private Button mBtn_FreezeStart; // Start the Freeze
    private Button mBtn_FreezeStop;

    private BarChart chart;
    private ListView mNearByDevicesListView;
    AlertDialog.Builder mSocialAlertDialog;

    //DataBase sqLite
    DataBaseHelper dataBaseHelper;

    //Bool
    Boolean FreezeIsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_freeze);

        //DataBase sqLite
        dataBaseHelper = new DataBaseHelper(this);

        //State & buffer
        mStatusInfoText1 = (TextView) findViewById(R.id.tv_state1);
        mStatusInfoText2 = (TextView) findViewById(R.id.tv_state2);
        mStatusInfoText3 = (TextView) findViewById(R.id.tv_state3);
        mStatusInfoText4 = (TextView) findViewById(R.id.tv_state4);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);

        //Buttons
        mBtn_FreezeStart = (Button) findViewById(R.id.Btn_freezeStart);
        mBtn_FreezeStop = (Button) findViewById(R.id.Btn_freezeStop);


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


        //Message handler

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    mReadBuffer.setText(readMessage);
                    Log.i(TAG, "From ESP32: " + readMessage);
                }
                if (msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        mStatusInfoText1.setText("Connected: " + msg.obj);
                        Log.i(TAG, "Connected to Device: " + msg.obj);

                    } else {
                        mStatusInfoText1.setText("Connection Failed");
                        Log.i(TAG, "Connection Failed");
                    }
                }
            }
        };

        mBtn_FreezeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastXMinusMinutes = "-10";
                int cnt = dataBaseHelper.freeze_FillTblWithAvailableMac( lastXMinusMinutes);
                String msg = "Amount of FREEZE-OBJECTS ( " + lastXMinusMinutes + " Minutes ) = " + cnt;
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                mStatusInfoText3.setText("Currently in FREEZE-State (wifi + BT + ExpC19) =  " + cnt + " per (" + lastXMinusMinutes + "Min.)" );
                FreezeIsRunning = true;
            }
        });

        mBtn_FreezeStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "FREEZE = STOP!! ";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                mStatusInfoText4.setText("FREEZE STOPPED! " );
                FreezeIsRunning = false;
            }
        });


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
                String lastXMinutes = "-10";

                int cntWifi = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_wifiScan","wifiMacAddr",lastXMinutes);
                int cntBT = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_blScan","blMacAddr",lastXMinutes);
                int cntC19 = dataBaseHelper.getCountOfAsingleItem_fromLastXMinutes("tbl_bleExposureScan","bleMacAddr",lastXMinutes);
                mStatusInfoText2.setText("Scan from last " + lastXMinutes + " Minutes: WiFi=" + cntWifi  + "  BT=" + cntBT +  "  C19=" + cntC19 + " (unique MAC)");

                if ( FreezeIsRunning){
                    int cntFreezeViolation = dataBaseHelper.freeze_detectViolation("-2");
                    mStatusInfoText4.setText("FREEZE VIOLATION! <  " + cntFreezeViolation + " >  New Objects/Devices since last FREEZE!");
                }


                handler.postDelayed(handlerTask, 4000);
            }
        };
        handlerTask.run();
    }

}
