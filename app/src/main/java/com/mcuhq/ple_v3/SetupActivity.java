package com.mcuhq.ple_v3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;

public class SetupActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private TextInputLayout mEspName;
    private TextInputLayout mEspMacAddr;
    private TextInputLayout mEp32TriggerTimer;
    private TextInputLayout mWifi_BarChart_Timer;
    private TextInputLayout mBT_BarChart_Timer;
    private TextInputLayout mWBT_DoubleChart_Timer;
    private TextInputLayout mSelectTimeFilter;
    private CheckBox mCB_esp32IsAvailable;

    //DataBase sqLite
    DataBaseHelper dataBaseHelper;

    ToolsClass mToolsClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //DataBase sqLite
        dataBaseHelper = new DataBaseHelper(this);

        mEspName = findViewById(R.id.EspName);
        mEspMacAddr = findViewById(R.id.EspMacAddr);
        mCB_esp32IsAvailable = (CheckBox)findViewById(R.id.CB_esp32IsAvailable);  // Check Box
        mEp32TriggerTimer = findViewById(R.id.Ep32TriggerTimer);
        mWifi_BarChart_Timer = findViewById(R.id.Wifi_BarChart_Timer);
        mBT_BarChart_Timer = findViewById(R.id.BT_BarChart_Timer);
        mWBT_DoubleChart_Timer= findViewById(R.id.WBT_DoubleChart_Timer);
        mSelectTimeFilter= findViewById(R.id.SelectTimeFilter);

        mToolsClass = new ToolsClass();

        getPathFinderSettings();

    }

    // MENU (die 3 punkte ) With Options and Help and stuff - https://www.youtube.com/watch?v=kknBxoCOYXI&pbjreload=101
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.storage_menu,menu);
        return true;
    }
    // MENU ..on options listener  - https://www.youtube.com/watch?v=zwabHRv2taA
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.storeSettings){
            confirmInput ();
            //Toast.makeText(getApplicationContext(), "onOptionsItemSelected()/SETTINGS! " , Toast.LENGTH_SHORT).show();
        } else  if(item.getItemId() == R.id.help){
            Toast.makeText(getApplicationContext(), "onOptionsItemSelected()/HELP! " , Toast.LENGTH_SHORT).show();
        } else{
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
    // END Options-Menu


    // text auslesen und prüfen  / https://www.youtube.com/watch?v=veOZTvAdzJ8
    private boolean validateESP_MAC_Addr(){
        String espMac = mEspMacAddr.getEditText().getText().toString().trim();
        //  hier die MAC validieren...
        if (espMac.isEmpty()){
            mEspMacAddr.setError("MAC can't be empty");
            return false;
        }else{
            mEspMacAddr.setError(null);
            mEspMacAddr.setErrorEnabled(false);
            return true;
        }
    }

    // CONFIRM STORE BUTTON
    public void confirmInput (View v) {
        confirmInput();
    }

    // CONFIRM STORE Function
    private void confirmInput () {
        if (!validateESP_MAC_Addr()){
            Toast.makeText(getApplicationContext(), "Please correct your input data!" , Toast.LENGTH_SHORT).show();
            return;
        }

        /*
        String input = "ESPname:"  + mEspName.getEditText().getText().toString();
        input += "\n";
        input += "ESPmac:"  + mEspMacAddr.getEditText().getText().toString();
        input += "\n";
        input += "ep32TriggerTimer:"  + mEp32TriggerTimer.getEditText().getText().toString();
        input += "\n";
        input += "Wifi_BarChart_Timer:"  +  mWifi_BarChart_Timer.getEditText().getText().toString();
        input += "\n";
        input += "BT_BarChart_Timer:"  +  mBT_BarChart_Timer.getEditText().getText().toString();
        input += "\n";
        input += "WBT_DoubleChart_Timer:"  +  mWBT_DoubleChart_Timer.getEditText().getText().toString();
        input += "\n";
        input += "SelectTimeFilter:"  +  mSelectTimeFilter.getEditText().getText().toString();

        Toast.makeText(getApplicationContext(), input , Toast.LENGTH_SHORT).show();
*/
        // store this into DB/table
        deleteSetupTable ();

        // Achtung: in der app und in der db verarbeiten wir esp32IsAvailable als int.. aber die CheckBox gibt true/false zurück
        // deshalb ? 1:0
        updateInsertSetupData (
                mEspName.getEditText().getText().toString(),
                mEspMacAddr.getEditText().getText().toString(),
                mCB_esp32IsAvailable.isChecked() ? 1 : 0,
                Integer.parseInt(mEp32TriggerTimer.getEditText().getText().toString()),
                Integer.parseInt(mWifi_BarChart_Timer.getEditText().getText().toString()),
                Integer.parseInt(mBT_BarChart_Timer.getEditText().getText().toString()),
                Integer.parseInt(mWBT_DoubleChart_Timer.getEditText().getText().toString()),
                Integer.parseInt(mSelectTimeFilter.getEditText().getText().toString()) );

        // AFTER RESTART the Activity - mit dem aufRuf der MainActivity werden dann auch die NEUEN Parameter gleich frisch eingelesen!!
        Toast.makeText(getApplicationContext(), "Setup()/Changes Done + pLE restarted! " , Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /* sqLite DB ACCESS
     einfach mit cnpaste vom sqlite studio in den string :-)
     zZ befindet sich NUR 1 DS in der tabelle ..eben die settings DER pLE APP.
     Deshalb löschen wir bevor der insert or update kommt!!
     */

    public boolean deleteSetupTable (){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        try{
            String qs = "DELETE FROM tbl_Settings;";
            db.execSQL(qs);
            db.close();
        }catch ( Exception e){
            String err = "deleteSetupTable()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            Toast.makeText(getApplicationContext(),  err , Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(getApplicationContext(), "deleteSetupTable()/ OK" , Toast.LENGTH_SHORT).show();
        return true;
    }


    public boolean updateInsertSetupData (String espName , String espMAC, int esp32IsAvailable, int esp32TriggerTimer, int wifi_BarChart_Timer, int bt_BarChart_Timer, int wbt_DoubleChart_Timer , int selectTimeFilter){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        try{
            //try first to insert..if not existing

            String qs =
                    "INSERT OR REPLACE INTO tbl_Settings VALUES (\n" +
                    "                                        '" + espName + "',\n" +
                    "                                        '" + espMAC + "',\n" +
                    "                                        '" + esp32IsAvailable + "',\n" +
                    "                                        " + esp32TriggerTimer + ",\n" +
                    "                                        " + wifi_BarChart_Timer + ",\n" +
                    "                                        " + bt_BarChart_Timer + ",\n" +
                    "                                        " + wbt_DoubleChart_Timer + ",\n" +
                    "                                        " + selectTimeFilter + "\n" +
                    "                                    );";

            db.execSQL(qs);
            db.close();
        }catch ( Exception e){
            String err = "updateInsertSetupData()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            Toast.makeText(getApplicationContext(),  err , Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(getApplicationContext(), "updateInsertSetupData()/ OK" , Toast.LENGTH_SHORT).show();
        return true;
    }

    private void getPathFinderSettings(){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursorData;

        try{
            String qs = "select * from tbl_Settings;";
            cursorData = db.rawQuery(qs,null);
            if (cursorData.getCount() == 0){
                Log.i(TAG, "getPathFinderSettings() - No Data Found! " );
                return;
            }
        }catch ( Exception e){
            String err = "getPathFinderSettings() ()/Exception: " + e.getMessage();
            Log.i(TAG, err);
            return;
        }

        // loop results  create new cursor objects
        while (cursorData.moveToNext()){
            String s = cursorData.getString(0);
            mEspName.getEditText().setText(s);
            mEspMacAddr.getEditText().setText(cursorData.getString(1) );
            mCB_esp32IsAvailable.setChecked( mToolsClass.convertIntToBoolean( cursorData.getInt(2)) );
            mEp32TriggerTimer.getEditText().setText(cursorData.getString(3));
            mWifi_BarChart_Timer.getEditText().setText(cursorData.getString(4) );
            mBT_BarChart_Timer.getEditText().setText(cursorData.getString(5) );
            mWBT_DoubleChart_Timer.getEditText().setText(cursorData.getString(6) );
            mSelectTimeFilter.getEditText().setText(cursorData.getString(7) );
        }

        return;
    }


}