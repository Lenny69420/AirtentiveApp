package com.example.airtentiveapp.ui.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
                List<String> requiredPermissions = new ArrayList<>();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
                    requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT); // For device name and connection
                } else {
                    requiredPermissions.add(Manifest.permission.BLUETOOTH);
                    requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
                }
                requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION); // Always for classic BT discovery

                List<String> missingPermissions = new ArrayList<>();

                for (String permission : requiredPermissions) {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        missingPermissions.add(permission);
                    }
                }

                if (!missingPermissions.isEmpty()) {
                    Log.e("BluetoothClient", "Missing permissions: " + TextUtils.join(", ", missingPermissions));
                    return;
                }

                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);

                socket.connect();

                if (callback != null) {
                    callback.onConnected(device.getAddress());  // ✅ Notify success with device address
                }

                // Đọc dữ liệu từ Bluetooth
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    if (callback != null) {
                        // Gọi callback đẩy data về frontend với device address
                        callback.onDataReceived(device.getAddress(), line);
                    }
                }

            } catch (IOException e) {
                Log.e("BluetoothClient", "Connection error", e);
                if (callback != null) {
                    callback.onConnectionFailed(device.getAddress(), e);  // ✅ Notify failure with device address
                }
            }
        }).start();
    }

}