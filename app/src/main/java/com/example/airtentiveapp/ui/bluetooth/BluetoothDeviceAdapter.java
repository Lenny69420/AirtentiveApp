package com.example.airtentiveapp.ui.bluetooth;
import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.airtentiveapp.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Assuming your item layout is named 'item_bluetooth_device.xml'
// and has TextViews with ids: textViewDeviceName, textViewDeviceAddress

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    private final List<BluetoothDevice> deviceList;
    private final OnDeviceClickListener listener;
    private final Context context;
    // Store data for each device by MAC address
    private final Map<String, String> deviceDataMap = new HashMap<>();

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList, OnDeviceClickListener listener, Context context) {
        this.deviceList = deviceList;
        this.listener = listener;
        this.context = context;
    }

    // Method to update data for a specific device
    public void updateDeviceData(String deviceAddress, String data) {
        deviceDataMap.put(deviceAddress, data);
        // Find the position of the device and notify adapter
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getAddress().equals(deviceAddress)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout. Make sure you have R.layout.item_bluetooth_device
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        String currentData = deviceDataMap.get(device.getAddress());
        holder.bind(device, listener, context, currentData);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        // Example: These should match the IDs in your item_bluetooth_device.xml
        TextView textViewDeviceName;
        TextView textViewDeviceDustSensorData;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Example:
            textViewDeviceName = itemView.findViewById(R.id.device_name);
            textViewDeviceDustSensorData = itemView.findViewById(R.id.device_dustsensor_data);
        }

        public void bind(final BluetoothDevice device, final OnDeviceClickListener listener, Context context, String currentData) {
            String deviceNameStr;
            // Check for BLUETOOTH_CONNECT permission before accessing device name on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                    deviceNameStr = device.getName();
                } else {
                    deviceNameStr = "Name Hidden (No Permission)";
                }
            } else {
                //noinspection MissingPermission
                deviceNameStr = device.getName(); // For older versions, direct access is often fine after discovery
            }

            if (deviceNameStr == null || deviceNameStr.isEmpty()) {
                textViewDeviceName.setText("Unknown Device");
            } else {
                textViewDeviceName.setText(deviceNameStr);
            }

            // Display current data or default message
            if (currentData != null && !currentData.isEmpty()) {
                textViewDeviceDustSensorData.setText(currentData);
            } else {
                textViewDeviceDustSensorData.setText("No data received");
            }

            itemView.setOnClickListener(v -> listener.onDeviceClick(device));
        }
    }
}