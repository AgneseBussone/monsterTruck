package monster_truck_control.mtc.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Create the udp socket and the worker thread
 */
class UdpSetup implements Runnable {

    private static final String LOG_TAG = UdpSetup.class.getSimpleName();

    // server information
    private static final String server_ip = "192.168.42.1";

    @Override
    public void run() {
        // Create udp socket
        try {
//            UdpManager.server_addr = InetAddress.getByName(server_ip);
            UdpManager.datagramSocket = new DatagramSocket();
//            String test = "ciao";
//            DatagramPacket packet = new DatagramPacket(test.getBytes(), test.length(),
//                    UdpManager.server_addr,
//                    UdpManager.server_port);
//            UdpManager.datagramSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
            UdpManager.datagramSocket = null;
        }
    }
}
