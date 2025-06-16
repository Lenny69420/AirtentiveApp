package com.example.airtentiveapp.bluetooth;
import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Assuming you use TextViews
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.airtentiveapp.R;

import java.util.List;

// Assuming your item layout is named 'item_bluetooth_device.xml'
// and has TextViews with ids: textViewDeviceName, textViewDeviceAddress

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    private final List<BluetoothDevice> deviceList;
    private final OnDeviceClickListener listener;
    private final Context context; // Store context for permission checks

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public BluetoothDeviceAdapter(List<BluetoothDevice> deviceList, OnDeviceClickListener listener, Context context) {
        this.deviceList = deviceList;
        this.listener = listener;
        this.context = context; // Initialize context
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
        holder.bind(device, listener, context);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        // Example: These should match the IDs in your item_bluetooth_device.xml
        TextView textViewDeviceName;
        TextView textViewDeviceAddress;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            // Example:
            textViewDeviceName = itemView.findViewById(R.id.device_name);
            textViewDeviceAddress = itemView.findViewById(R.id.device_address);
        }

        public void bind(final BluetoothDevice device, final OnDeviceClickListener listener, Context context) {
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
            textViewDeviceAddress.setText(device.getAddress());

            itemView.setOnClickListener(v -> listener.onDeviceClick(device));
        }
    }
}