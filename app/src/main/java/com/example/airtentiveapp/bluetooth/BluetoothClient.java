package com.example.airtentiveapp.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class BluetoothClient {

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final Context context;
    private BluetoothSocket socket;
    private BluetoothDataCallback callback;

    public BluetoothClient(Context context) {
        this.context = context;
    }

    public void setCallback(BluetoothDataCallback callback) {
        this.callback = callback;
    }

    public void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("BluetoothClient", "Missing BLUETOOTH_CONNECT permission");
                    return;
                }
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);

                socket.connect();

                if (callback != null) {
                    callback.onConnected();  // ✅ Notify success
                }

                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (callback != null) {
                        callback.onDataReceived(line);
                    }
                }

            } catch (IOException e) {
                Log.e("BluetoothClient", "Connection error", e);
                if (callback != null) {
                    callback.onConnectionFailed(e);  // ✅ Notify failure
                }
            }
        }).start();
    }

}