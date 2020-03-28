package cn.edu.bupt.owatch.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WsResponse {

    private Integer code;
    private String message;
    @SerializedName("data")
    private ArrayList<ServerInfo> serverInfoList;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<ServerInfo> getServerInfoList() {
        return serverInfoList;
    }

    public void setServerInfoList(ArrayList<ServerInfo> serverInfoList) {
        this.serverInfoList = serverInfoList;
    }
}
