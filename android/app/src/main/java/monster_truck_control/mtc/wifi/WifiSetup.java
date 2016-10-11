package monster_truck_control.mtc.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;
import java.util.List;

/**
 * AsyncTask that handle the connection with the net
 */
public class WifiSetup extends AsyncTask<Void, String, Pair<Integer, String>> { //params, progress, result

    public static final String net = "WifiCmd";

    private final String LOG_TAG = WifiSetup.class.getSimpleName();

    private TextView wifiIcon;
    private int green_icon_wifi;
    private int orange_icon_wifi;
    private int red_icon_wifi;
    private static final String pass = "raspberry";
    private WifiManager wm;

    public WifiSetup(WifiManager wifiManager, TextView view, int green, int orange, int red){
        wm = wifiManager;
        wifiIcon = view;
        green_icon_wifi = green;
        orange_icon_wifi = orange;
        red_icon_wifi = red;
    }

    @Override
    protected Pair<Integer, String> doInBackground(Void... params) {

        // Check if the wifi is enabled and turn it on if it's not
        if(!wm.isWifiEnabled()) {
            Log.w(LOG_TAG, "Turning on wifi");
            publishProgress("activating wifi...");
            if (!wm.setWifiEnabled(true)) {
                Log.e(LOG_TAG, "Failed to turn on wifi");
                return new Pair<>(red_icon_wifi, "wifi disabled");
            }
        }

        // Check if we are already connected to the right net
        WifiInfo info = wm.getConnectionInfo();
        String ssid = info.getSSID().substring(1, info.getSSID().length() - 1);
        if(ssid.equals(net)) {
            //device connected to the right net
            Log.i(LOG_TAG, "connected to " + net);
            return new Pair<>(green_icon_wifi, ssid);
        }
        else {
            // Check if our network is saved
            int net_id = saveNet();

            if (net_id != -1) {

                // Check if the net is reachable
                if(!findNetwork())
                    return new Pair<>(red_icon_wifi, "wifi net not found");

                // Enable our net
                Log.w(LOG_TAG, "Enabling net");
                publishProgress("enabling net...");

                // Enabling the network should trigger a tentative to connect to it and so a change
                // in the wifi status (so a broadcast should be sent)
                if (!wm.enableNetwork(net_id, true)) {
                    Log.e(LOG_TAG, "FAIL: enabling new network");
                    return new Pair<>(red_icon_wifi, "failed to enable the net");
                }
            } else {
                Log.e(LOG_TAG, "Failed to add the net");
                return new Pair<>(red_icon_wifi, "failed to save the net");
            }
        }
        return new Pair<>(orange_icon_wifi, "connecting...");
    }

    @Override
    protected void onPostExecute (Pair<Integer, String> result){
        super.onPostExecute(result);
        wifiIcon.setCompoundDrawablesRelativeWithIntrinsicBounds(result.first, 0, 0, 0);
        wifiIcon.setText(result.second);
    }

    @Override
    protected void onProgressUpdate (String... values){
        super.onProgressUpdate(values);
        wifiIcon.setCompoundDrawablesRelativeWithIntrinsicBounds(orange_icon_wifi, 0, 0, 0);
        wifiIcon.setText(values[0]);
    }

    private int saveNet() {
        int id = -1;

        // Get the list of all saved net on the device
        List<WifiConfiguration> list = wm.getConfiguredNetworks();

        // search if the net is configured
        for(WifiConfiguration n : list){
            if(n.SSID.compareTo(net) == 0){
                id = n.networkId;
                break;
            }
        }

        // If the net is not saved, save it
        if(id == -1) {
            WifiConfiguration net_info = new WifiConfiguration();
            net_info.SSID = net;
            net_info.preSharedKey = "\"" + pass + "\"";
            id = wm.addNetwork(net_info);
        }

        return id;
    }


    private boolean findNetwork(){
        // Check if the net is active
        List<ScanResult> scanResultList = wm.getScanResults();
        for(ScanResult r : scanResultList){
            if(r.SSID.equals(net))
                return true;
        }
        return false;
    }
}
