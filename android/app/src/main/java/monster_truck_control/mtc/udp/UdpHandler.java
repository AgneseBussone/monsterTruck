package monster_truck_control.mtc.udp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Handler associated with the sender thread.
 * The main application use the handler to insert messages into the queue of the thread.
 * The callback method polls the message from the queue, reads it and sends it to the car using the socket.
 * It does not matter if the wifi is active or not, if the app is linked to the correct net....
 */
class UdpHandler extends Handler{

    private static final String LOG_TAG = UdpHandler.class.getSimpleName();

    private int pck_cnt = 0;

    // Create a handler associated to the thread looper
    public UdpHandler(Looper looper){
        super(looper);
    }

    // Send the message to the car
    @Override
    public void handleMessage(Message msg){

        // Get the message data
        String data = (String) msg.obj;

        // Attach a counter to the packet
        String message = String.valueOf(pck_cnt) + " " + data;

        Log.i(LOG_TAG, message);

        // Send the message through the socket
        try {
            // Create a datagram packet
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(),
                    InetAddress.getByName(UdpManager.server_ip),
                    UdpManager.server_port);

            // Send the packet
            if (UdpManager.datagramSocket != null)
                UdpManager.datagramSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pck_cnt++;
    }


}
