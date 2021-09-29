package com.mcuhq.ple_v3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BluetoothConnectionService {

    private final String TAG = MainActivity.class.getSimpleName();
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    public ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;

    private String mDeviceAddr;
    private String mDeviceName;

    public BluetoothConnectionService(Context context, String DeviceAddr , String DeviceName) {
        mDeviceAddr = DeviceAddr;
        mDeviceName = DeviceName;
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }



    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
                    boolean fail = false;
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MainActivity._ESPmac);

        public AcceptThread(){
            try {
                mBTSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                fail = true;
                Toast.makeText(mContext, "Socket creation failed", Toast.LENGTH_SHORT).show();
            }
            // Establish the Bluetooth socket connection.
            try {
                mBTSocket.connect();
            } catch (IOException e) {
                try {
                    fail = true;
                    mBTSocket.close();
                    MainActivity.mHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    try {
                        FreezeActivity.mHandler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    } catch (Exception e3){
                        // do nothing.. ToDo: sowas muss in broadCast receiver
                    };

                } catch (IOException e2) {
                    //insert code to deal with this
                    Log.e(TAG, "Socket creation failed",e);
                    Toast.makeText(mContext, "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
            }
            // HURRA  Connection via BT OK
            if(fail == false) {
                mConnectedThread = new ConnectedThread(mBTSocket);
                mConnectedThread.start();
                // connection established
                MainActivity.mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, mDeviceName + "/" + mDeviceAddr)
                        .sendToTarget();
                try {
                    FreezeActivity.mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, mDeviceName + "/" + mDeviceAddr)
                            .sendToTarget();
                } catch (Exception e4) {
                    //Activity ist noch nicht geladen
                }
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        MainActivity.mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                        try {
                            FreezeActivity.mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                    .sendToTarget(); // ToDo: sowas sollte in einen broadcast receiver
                        } catch (Exception e2) {
                            //
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectedThread!= null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void triggerCharForESP_Module() {
        // T I G G E R  the ESP - ESP will send next Data-Pack to APP
        //TimeStamp:
        Date c = Calendar.getInstance().getTime();  // now()
        SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");// or YYYY-MM-dd HH:MM:SS.SSS
        String timeStamp = df.format(c) + "\n";
        if( mConnectedThread != null) //First check to make sure thread created
        {
           // mConnectedThread.write("12345\n");  // or "1"
            mConnectedThread.write(timeStamp);  // or "1"
        }
    }
    // BL END



}
