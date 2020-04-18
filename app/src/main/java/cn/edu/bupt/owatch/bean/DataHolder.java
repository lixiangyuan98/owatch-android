package cn.edu.bupt.owatch.bean;

import android.app.Application;

import okhttp3.WebSocket;

public class DataHolder extends Application {
    private ServerInfo info;
    private WebSocket ws;

    public ServerInfo getInfo() {
        return info;
    }

    public void setInfo(ServerInfo info) {
        this.info = info;
    }

    public WebSocket getWs() {
        return ws;
    }

    public void setWs(WebSocket ws) {
        this.ws = ws;
    }
}
