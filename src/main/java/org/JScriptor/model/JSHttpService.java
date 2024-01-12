package org.JScriptor.model;


public class JSHttpService {
    private String host;
    private int port;
    private boolean secure;

    public JSHttpService() {
    }

    public JSHttpService(String host, int port, boolean secure) {
        this.host = host;
        this.port = port;
        this.secure = secure;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public String toString() {
        return "JSHttpService{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", secure=" + secure +
                '}';
    }
}

