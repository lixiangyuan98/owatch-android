package cn.edu.bupt.owatch;

import java.util.Locale;

class Heartbeat {

    private byte[] message;

    private Heartbeat() {}

    Heartbeat(int ip) {
        byte[] ipBytes = String.format(Locale.getDefault(), "%d.%d.%d.%d",
                ip & 0xff, ip >> 8 & 0xff, ip >> 16 & 0xff, ip >> 24 & 0xff).getBytes();
        this.message = new byte[ipBytes.length + 8];
        System.arraycopy(ipBytes, 0, message, 0, ipBytes.length);
    }

    /** 获取心跳包的bytes */
    byte[] getMessage() {
        long timestamp = System.currentTimeMillis();

        for (int i = message.length - 1; i >= message.length - 8; i--) {
            message[i] = (byte)(timestamp & 0x00000000000000FFL);
            timestamp >>= 8;
        }
        return message;
    }

    String getString() {
        byte[] message = getMessage();
        StringBuilder sb = new StringBuilder();
        for (byte b : message) {
            sb.append(String.format(Locale.getDefault(), "%02x", b));
        }
        return sb.toString();
    }
}
