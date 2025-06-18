package com.example.airtentiveapp.ui.home;

public class ConnectedDeviceInfo {
    private String name;
    private String sensorData;
    private int imageResourceId;

    public ConnectedDeviceInfo(String name, String sensorData, int imageResourceId) {
        this.name = name;
        this.sensorData = sensorData;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public String getSensorData() {
        return sensorData;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
