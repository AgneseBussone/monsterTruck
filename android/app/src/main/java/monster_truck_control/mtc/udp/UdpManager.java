package monster_truck_control.mtc.udp;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.net.DatagramSocket;

/**
 * Class that manages all the actions related to the udp threads.
 */
public class UdpManager {

    /* Constants for commands to be sent to the car */

    // command types
    public static final short ACCELERATOR = 0;
    public static final short BRAKE = 1;
    public static final short LIGHTS = 2;
    public static final short PRNDL = 3;
    public static final short DIRECTION = 4;
    private static final short SHUTDOWN = 5;

    // command value
    public static final char ACC_BRK_PRESS = 'P';
    public static final char ACC_BRK_RELEASE = 'R';
    public static final char LIGHT_ON = 'A';
    public static final char LIGHT_OFF = 'S';
    public static final char PARK = 'P';
    public static final char REAR = 'R';
    public static final char DRIVE = 'D';
    public static final char LEFT = 'L';
    public static final char RIGHT = 'R';
    public static final char STRAIGHT = 'S';


    // Server info used by the udp thread
    static final int server_port = 9930;
    static DatagramSocket datagramSocket = null;
    static final String server_ip = "192.168.42.1";

    // Handler used by the app to send message to the udp thread
    private static UdpHandler udpHandler = null;

    private static final String LOG_TAG = UdpManager.class.getSimpleName();

    private UdpManager(){}

    public static void initUdp(ConnectivityManager connectivityManager){
        // Create the socket
        if(datagramSocket == null){
            try {
                UdpManager.datagramSocket = new DatagramSocket();

                // Search the wifi network interface
                Network[] nets = connectivityManager.getAllNetworks();
                for (Network n : nets ) {
                    NetworkInfo info = connectivityManager.getNetworkInfo(n);
                    if((info != null) && (info.getType() == ConnectivityManager.TYPE_WIFI)){
                        // Bind the socket to this net, even if it doesn't provide internet
                        // In this way we avoid that the packet is sent over mobile just because wifi is not connected
                        n.bindSocket(UdpManager.datagramSocket);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                UdpManager.datagramSocket = null;
            }
        }

        // Create the sender thread
        if(udpHandler == null){
            if(datagramSocket != null){
                // Create the sender thread with a looper inside it
                HandlerThread udpThread = new HandlerThread("UDP Sender Thread");
                udpThread.start();

                // Get the thread's looper
                Looper udpLooper = udpThread.getLooper();

                //Create a handler associated to the looper
                udpHandler = new UdpHandler(udpLooper);

                Log.i(LOG_TAG, "udp thread running");
            }
            else{
                Log.e(LOG_TAG, "udp setup failed");
            }
        }

    }

    public static void sendMessage(short cmd, char val){
        String data = String.valueOf(cmd) + " " + String.valueOf(val) + "\0";

        // Creates an new Message instance
        Message msg = Message.obtain();

        // Put the string into Message, into "obj" field.
        msg.obj = data;

        // Send the message to the handler
        if(udpHandler != null){
            udpHandler.sendMessage(msg);
        }
        else{
            Log.e(LOG_TAG, "udpHandler doesn't exist");
        }
    }

    public static void shutDown(){

        // Remove all messages
        udpHandler.removeCallbacksAndMessages(null);

        sendMessage(SHUTDOWN, ' ');

        udpHandler.getLooper().quitSafely();
        udpHandler = null;
    }

}
