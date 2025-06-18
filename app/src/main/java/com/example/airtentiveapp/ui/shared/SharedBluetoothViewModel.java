package com.example.airtentiveapp.ui.shared;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedBluetoothViewModel extends ViewModel {
    private final MutableLiveData<String> bluetoothData = new MutableLiveData<>();

    public void setBluetoothData(String data) {
        // Append new data to existing data
        String currentData = bluetoothData.getValue();
        if (currentData == null || currentData.isEmpty()) {
            bluetoothData.setValue(data);
        } else {
            bluetoothData.setValue(currentData + "\n" + data);
        }
    }

    public LiveData<String> getBluetoothData() {
        return bluetoothData;
    }

    public void clearData() {
        bluetoothData.setValue("");
    }
}
