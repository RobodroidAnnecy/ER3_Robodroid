package com.example.admin.RoboDroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;



public class Connexion extends AppCompatActivity implements View.OnClickListener {
    public Button mButConnect =null;
    public static BlueT mBluetooth;// Bluetooth var
    public static boolean bFlag = false;//Used to set mbtConnected only once

    private WebView mWebView; //Used to display camera

    //To receive data
    public static String strCapteur = new String("");

    //Auto mode
    static public StateMachine mEtat;
    static public String strIrSensor= new String("");
    static public String strUltrasonicSensor= "000";
    public static TextView mTextView2 = null;
    public static TextView mTextView3 = null;
    public static int iFlagStop=0;//Flag to activate/deactivate Auto mode
    public static int iFlagThreadStop=0;//Flag to stop the thread loop

    //For sending purposes
    private Thread mThreadEnvoi = null;


    static public Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            strCapteur=(String) msg.obj;
            String[] str = strCapteur.split("/");//Separate the message
            strIrSensor=str[0];//Variable in which we place the Infrared sensor
            strUltrasonicSensor=str[1];//Variable in which we place the Ultrasonic sensor
            mTextView2.setText(strIrSensor);
            mTextView3.setText(strUltrasonicSensor);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_connexion);
        this.mTextView2 = (TextView)findViewById(R.id.textView2);
        this.mTextView3 = (TextView)findViewById(R.id.textView3);
        this.mButConnect = (Button) findViewById(R.id.buttonConnect2);
        this.mButConnect.setOnClickListener(this);

        //For the thread
        iFlagStop=0;
        iFlagThreadStop=0;

        //Webview connects to a website to display camera
        mWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("http://192.168.43.193:8080/browserfs.html");//adress

        /* Instantiates only once mBluetooth -- this way mbtConnected stays true
        Otherwise it's initialized at 0.*/
        if(bFlag == false) {
            this.mBluetooth = new BlueT(this, mHandler);
            bFlag = true;
        }

        //Auto
        this.mEtat=new StateMachine();

        // thread to send periodically a message to the device
        this.mThreadEnvoi = new Thread(new Runnable() {
            @Override
            public void run() {
                String strOrder = new String("");
                while (iFlagStop != 1) {
                    if (mBluetooth.mbtConnected == true) {
                        strOrder = mEtat.Evolve(strIrSensor, strUltrasonicSensor);//Place in the order variable the order found with Sensors
                        if (iFlagThreadStop != 1) {
                            mBluetooth.envoi(strOrder + "\0");//Send the order generate by the StateMachine
                        }
                        else {
                            mBluetooth.envoi("S" + "\0");//Send the message "S" for stop the robot before past to Manual Mode
                            iFlagStop = 1;//Stop the loop of Thread
                        }
                    }
                    try {
                        Thread.sleep(500, 0);//Send the message every 500ms to not overflow the reception of the robot
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        );
        mThreadEnvoi.start();
    }
    public void onClick(View v) {
        switch(v.getId()) { // who click ?
            //Bluetooth connexion Button
            case R.id.buttonConnect2:
                this.mBluetooth.connexion();
                break;
        }
    }
    //Manual Button in which we stop sending Automatic intructions and we intent the manual activity
    public void ButtonManu(View view){
        iFlagThreadStop=1;
        Intent myIntent= new Intent();
        myIntent.setClass(this, ModeManu.class);
        startActivityForResult(myIntent, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy(); // nothing special

    }
}
