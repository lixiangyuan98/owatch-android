package cn.edu.bupt.owatch.bean;

public class WsRequest {
    private String method;
    private String host;
    private String src;
    private String dest;
    private Integer port;

    public WsRequest(String method, String host, String src, String dest, Integer port) {
        this.method = method;
        this.host = host;
        this.src = src;
        this.dest = dest;
        this.port = port;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
