package com.example.airtentiveapp.bluetooth;

public class BluetoothDevice {
    private String name;
    private String address;

    public BluetoothDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}

