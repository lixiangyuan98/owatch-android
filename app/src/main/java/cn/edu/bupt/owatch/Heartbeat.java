package cn.edu.bupt.owatch;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

class Heartbeat {

    private Timer timer;
    private DatagramSocket socket;
    private DatagramPacket packet;

    Heartbeat() {
        timer = new Timer();
    }

    void start(String serverIP, int serverPort) {
        try {
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            socket = new DatagramSocket();
            packet = new DatagramPacket(new byte[1], 1, serverAddress, serverPort);
        } catch (IOException e) {
            Log.e("HEARTBEAT", e.toString());
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }
}
