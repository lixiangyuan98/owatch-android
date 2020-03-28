package cn.edu.bupt.owatch.bean;

import java.io.Serializable;
import java.util.List;

public class ServerInfo implements Serializable {
    private String host;
    private String status;
    private List<String> files;

    public String getHost() {
        return this.host;
    }

    public String getStatus() {
        return this.status;
    }

    public List<String> getFiles() {
        return this.files;
    }
}
