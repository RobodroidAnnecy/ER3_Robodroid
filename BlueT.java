package com.example.admin.RoboDroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;



    public class BlueT {
    public static final int DEMANDE_AUTH_ACT_BT = 1;
    public static final int N_DEMANDE_AUTH_ACT_BT = 0;
    private static final String TAG = "BTT";

    BluetoothAdapter mbtAdapt; //BT adapter of the phone
    Activity mActivity; //main activity who instantiate blueT -> association
    boolean mbtActif = false;	//state of the association

    private Set<BluetoothDevice> mDevices; //liste of mDevices
    private BluetoothDevice[]mPairedDevices;// table of known devices

    int mDeviceSelected = -1; //the device choosen by the phone
    String[] mstrDeviceName;
    int miBlc = 0;//used by connection
    boolean mbtConnected = false;


    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // dummy UUID
    private BluetoothSocket mSocket;
    private OutputStream mOutStream;	//mSocket for communication
    private InputStream mInStream;		//mSocket for communication

    public Handler mHandler;
    private Thread mThreadReception =null;	//thread that receives data from device
    private String mstrData = "";
    private String mstrDataTampon ="";
    private String mstrOldData = new String(""); // Initialized here only 1 time (and not everytime we run send();)


    public String mstrRecu = " ";

    byte mbBuffer[] = new byte[200]; // large buffer !
    int iPos=0;

    public BlueT(Activity Activity)
    {
        this.mActivity = Activity;
        this.Verif();
    }
    public BlueT(Activity Activity, Handler Handler)
    {
        this.mActivity = Activity;
        this.mHandler = Handler;
        this.Verif();
        mThreadReception = new Thread(new Runnable() { //create Thread for reception
            @Override
            public void run() {

                while(true)
                {
                    if(mbtAdapt != null) {
                        if(mbtAdapt.isEnabled())
                        {
                            mbtActif = true;
                        }
                        else
                        {
                            mbtActif = false;
                        }
                    }

                    if(mbtConnected == true) // reception of data when connected
                    {

                        mstrRecu = reception();
                        if (!mstrRecu.equals("")) { // if there is something -> send message to the handler of the activity
                            Message msg = mHandler.obtainMessage();
                            msg.obj = mstrRecu;
                            mHandler.sendMessage(msg);
                        }
                    }
                    try {
                        Thread.sleep(20, 0);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mThreadReception.start(); //start thread
    }

    public void Verif() // Verification of BT adapter
    {
        mbtAdapt = BluetoothAdapter.getDefaultAdapter(); // recover BT informations on adapter
        if(mbtAdapt == null) {
        }
        else {
        }
    }

    public void connexion() // connection to device
    {
        Verif();
        this.Device_Connu(); //recover informations for each connected devices
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(mActivity);//pop up off known devices
        adBuilder.setSingleChoiceItems(mstrDeviceName, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeviceSelected = which;
                dialog.dismiss();
                tryconnect(); //connection to the chosen device
            }
        }
        );

        AlertDialog adb = adBuilder.create();
        adb.show();
    }

    public void Device_Connu() // recover all known devices
    {
        this.mDevices = mbtAdapt.getBondedDevices(); //recover the devices in a tab
        this.miBlc = mDevices.size(); // number of known devices
        this.mstrDeviceName = new String[this.miBlc]; //table will be given to pop up menu
        this.miBlc = 0;
        for(BluetoothDevice dev : this.mDevices) {
            this.mstrDeviceName[this.miBlc] = dev.getName();
            this.miBlc = this.miBlc + 1;
        }
        this.mPairedDevices = (BluetoothDevice[]) this.mDevices.toArray(new BluetoothDevice[this.mDevices.size()]); //cast of set in array.
    }

    public void tryconnect()
    {
        try {
            this.mSocket =this.mPairedDevices[this.mDeviceSelected].createRfcommSocketToServiceRecord(MY_UUID); //connection to vhchoosen device via Socket, mUUID: id of BT on device of the target
            this.mSocket.connect();
            Toast.makeText(this.mActivity, "Connected", Toast.LENGTH_SHORT).show();
            this.mbtConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.mActivity, "Try again", Toast.LENGTH_SHORT).show();
            try {
                mSocket.close();
            }
            catch(Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public Boolean envoi(String strOrdre) // false -> error; true -> ok
    {
        try	{
            this.mOutStream = this.mSocket.getOutputStream(); //open output stream

            byte[] trame = strOrdre.getBytes();

            this.mOutStream.write(trame); //send frame via output stream
            this.mOutStream.flush();
        }
        catch(Exception e2) {
            tryconnect();
            try {
                this.mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mbtConnected = false;
        }
        return this.mbtConnected;
    }
    public String reception()
    {
        int iNbLu;
        mstrData = new String("");
        mstrDataTampon = new String("");

        try {
            this.mInStream = this.mSocket.getInputStream();// input stream

            if(this.mInStream.available() > 0 ) {
                // inBLu = number of characters
                // the following part has to be improved
                iNbLu=mInStream.read(mbBuffer,iPos,20); // be aware -> a complete frame is not received
                mstrDataTampon = new String(mbBuffer,0,iNbLu); //create a string using byte received
                mstrOldData = mstrOldData.concat(mstrDataTampon); //stock DataTampon in OldData

                int iIndex = mstrOldData.indexOf("\0");// get the index of "\0" in OldData
                while(iIndex!=-1){ // Test if there's a "\0" left
                    this.mstrData=mstrOldData.substring(0,iIndex); //get the first part in mstrData
                    mstrOldData=mstrOldData.substring(iIndex+1); //put the following part of '\0' in OldData
                    iIndex = mstrOldData.indexOf("\0"); // get the index of "\0" in OldData
                }

            }
        }
        catch (Exception e) {
            try {
                mSocket.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            this.mbtConnected = false;
        }
        return mstrData;
    }
}
