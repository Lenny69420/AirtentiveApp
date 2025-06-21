package com.example.airtentiveapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.airtentiveapp.R;

import java.util.List;

public class ConnectedDeviceAdapter extends RecyclerView.Adapter<ConnectedDeviceAdapter.ConnectedDeviceViewHolder> {

    private List<ConnectedDeviceInfo> devices;

    public ConnectedDeviceAdapter(List<ConnectedDeviceInfo> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public ConnectedDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_connected_device_info, parent, false);
        return new ConnectedDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectedDeviceViewHolder holder, int position) {
        ConnectedDeviceInfo device = devices.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceSensorData.setText(device.getSensorData());

        // Set the image if available
        if (device.getImageResourceId() != 0) {
            holder.deviceImage.setImageResource(device.getImageResourceId());
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void updateDevices(List<ConnectedDeviceInfo> newDevices) {
        this.devices = newDevices;
        notifyDataSetChanged();
    }

    static class ConnectedDeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceSensorData;
        ImageView deviceImage;

        ConnectedDeviceViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceSensorData = itemView.findViewById(R.id.device_dustsensor_data);
            deviceImage = itemView.findViewById(R.id.device_image);
        }
    }
}
