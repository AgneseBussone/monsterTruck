package monster_truck_control.mtc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.TextView;
import java.util.Locale;

import monster_truck_control.mtc.wifi.WifiSetup;


/**
 * Listener that handle graphics updates basing on the system status
 */
public class MainReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = MainReceiver.class.getSimpleName();

    private WifiSetup wifi_task;

    public MainReceiver(){}

    public void setWifiTask(WifiSetup wifiTask){
        wifi_task = wifiTask;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
            // Is charging?
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING);

            // Determine the battery level
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = (level / (float)scale)*100;
            Log.i(LOG_TAG, "Battery: " + batteryPct + " %");
            // 0-33: red
            // 33-66: yellow
            // 66-100: green
            TextView battery_view = (TextView) ((Activity) context).findViewById(R.id.batteryIcon);
            battery_view.setText(String.format(Locale.ENGLISH, "%.0f%%", batteryPct));
            if(batteryPct < 33){
                if(isCharging)
                    battery_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_battery_red_charge, 0);
                else
                    battery_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_battery_red, 0);
            }else if(batteryPct < 66){
                if(isCharging)
                    battery_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,R.drawable.ic_battery_yellow_charge, 0);
                else
                    battery_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,R.drawable.ic_battery_yellow, 0);
            }else
                if(isCharging)
                    battery_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_battery_green_charge, 0);
                else
                    battery_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_battery_green, 0);
        }
        else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = info.getState();
            TextView wifi_view = (TextView) ((Activity) context).findViewById(R.id.wifiIcon);
            switch (state){
                case DISCONNECTED:
                    if(wifi_task != null && wifi_task.getStatus() == AsyncTask.Status.FINISHED){
                        wifi_task = null;
                        wifi_task = new WifiSetup((WifiManager)(context.getSystemService(Context.WIFI_SERVICE)),
                                wifi_view,
                                R.drawable.ic_wifi_green,
                                R.drawable.ic_wifi_orange,
                                R.drawable.ic_wifi_red);
                        wifi_task.execute();
                    }

                    break;
                case CONNECTED:
                    WifiInfo wifi_info = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if(wifi_info.getSSID().equals(WifiSetup.net)){
                        // connected to the right net
                        wifi_view.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_wifi_green, 0, 0, 0);
                        wifi_view.setText(wifi_info.getSSID());
                    }
                    else{
                        // Try to connect to the correct net
                        if(wifi_task != null && wifi_task.getStatus() == AsyncTask.Status.FINISHED) {
                            wifi_task = null;
                            wifi_task = new WifiSetup((WifiManager)(context.getSystemService(Context.WIFI_SERVICE)),
                                                        wifi_view,
                                                        R.drawable.ic_wifi_green,
                                                        R.drawable.ic_wifi_orange,
                                                        R.drawable.ic_wifi_red);
                            wifi_task.execute();
                        }
                    }
                    break;
                case CONNECTING:
                case DISCONNECTING:
                    wifi_view.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_wifi_orange, 0, 0, 0);
                    break;
                default:
                    Log.i(LOG_TAG, "wifi state not handled: " + state.toString());
                    break;
            }
        }
    }

}
