package monster_truck_control.mtc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.InputStream;
import monster_truck_control.mtc.graphics.BackgroundDrawable;
import monster_truck_control.mtc.graphics.SpeedChangeListener;
import monster_truck_control.mtc.sensor.MotionManager;
import monster_truck_control.mtc.udp.UdpManager;
import monster_truck_control.mtc.wifi.WifiSetup;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;

    private SensorManager sensorManager;
    private MotionManager motionManager;
    private MainReceiver main_receiver = new MainReceiver();
    private IntentFilter main_filter = new IntentFilter();
    private SpeedChangeListener speedChangeListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the app has all the dangerous permission it needs
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an alert box with an explanation
                AlertDialog.Builder explanation = new AlertDialog.Builder(MainActivity.this);
                explanation.setTitle(R.string.wifi_perm_label);
                explanation.setMessage(R.string.wifi_perm_descr);
                PermissionDialog listener = new PermissionDialog();
                explanation.setPositiveButton("OK", listener);
                explanation.setNegativeButton("CANCEL", listener);
                explanation.show();
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                                                  new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                  MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        }
        else{
            /* Wifi setup */
            wifiSetup(true);
        }

        /* Make system bar invisible */
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                      | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                      | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                      | View.SYSTEM_UI_FLAG_FULLSCREEN
                                      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        /* Set the background image */
        InputStream resource = getResources().openRawResource(R.raw.background);
        RelativeLayout base = (RelativeLayout)findViewById(R.id.baseLayout);
        Bitmap bitmap = BitmapFactory.decodeStream(resource);
        if(base != null)
            base.setBackground(new BackgroundDrawable(bitmap));

        /* Steering wheel setup */
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        motionManager = new MotionManager(sensorManager);

        /* Battery setup */
        main_filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        /* Speed setup */
        SeekBar speed = (SeekBar)findViewById(R.id.speed);
        if(speed != null) {
            speedChangeListener = new SpeedChangeListener();
            speed.setOnSeekBarChangeListener(speedChangeListener);
        }

        /* UDP setup*/
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        UdpManager.initUdp(connectivityManager);
    }

    @Override
    protected void onResume(){
        super.onResume();
        motionManager.registerMotion(sensorManager);
        registerReceiver(main_receiver, main_filter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        motionManager.unregisterMotion(sensorManager);
        unregisterReceiver(main_receiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void wifiSetup(boolean activate){
        if(activate) {
            WifiSetup wifiTask = new WifiSetup((WifiManager)(getApplicationContext().getSystemService(Context.WIFI_SERVICE)),
                    (TextView) findViewById(R.id.wifiIcon),
                    R.drawable.ic_wifi_green,
                    R.drawable.ic_wifi_orange,
                    R.drawable.ic_wifi_red);
            main_receiver.setWifiTask(wifiTask);
            main_filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            wifiTask.execute();
        }
        else {
            TextView wifi_view = (TextView) findViewById(R.id.wifiIcon);
            if (wifi_view != null) {
                wifi_view.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_wifi_red, 0, 0, 0);
                wifi_view.setText(R.string.wifi_perm_denied);
            }
        }
    }


    /*********** Permission request management ****************/

    /* Callback called by ActivityCompat.requestPermissions */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Activate the wifi feature
                    wifiSetup(true);
                } else {
                    // Disable wifi feature
                    wifiSetup(false);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /* Listener for the explanation dialog */
    private class PermissionDialog implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                // Re-prompt the request to the user
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            } else {
                // Disable wifi feature
                wifiSetup(false);
            }
        }
    }


    /*********** Event handlers defined in the XML ****************/

    public void stopEngineClick(View view) {
        UdpManager.shutDown();
        finish();
    }

    public void lightBtnClick(View view) {
        boolean checked = ((ToggleButton) view).isChecked();
        if (checked)
            UdpManager.sendMessage(UdpManager.LIGHTS, UdpManager.LIGHT_ON);
        else
            UdpManager.sendMessage(UdpManager.LIGHTS, UdpManager.LIGHT_OFF);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.drive:
                if (checked && speedChangeListener != null)
                    speedChangeListener.setDirection(UdpManager.DRIVE);
                break;
            case R.id.rear:
                if (checked && speedChangeListener != null)
                    speedChangeListener.setDirection(UdpManager.REAR);
                break;
        }
    }
}