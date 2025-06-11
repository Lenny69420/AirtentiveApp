package com.example.airtentiveapp.bluetooth;

public interface BluetoothDataCallback {
    void onDataReceived(String data);
    void onConnected();
    void onConnectionFailed(Exception e);
}

