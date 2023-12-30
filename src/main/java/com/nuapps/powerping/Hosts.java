package com.nuapps.powerping;

public class Hosts {
    private final String location;
    private String hostName;
    private String ipAddress;

    public Hosts(String hostName, String ipAddress, String location) {
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.location = location;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }
}

