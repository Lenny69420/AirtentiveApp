package com.example.airtentiveapp.ui.bluetooth;

public interface BluetoothDataCallback {
    void onDataReceived(String deviceAddress, String data);
    void onConnected(String deviceAddress);
    void onConnectionFailed(String deviceAddress, Exception e);
}
