package com.example.airtentiveapp.ui.home;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.airtentiveapp.R;
import com.example.airtentiveapp.databinding.FragmentHomeBinding;
import com.example.airtentiveapp.ui.shared.SharedBluetoothViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedBluetoothViewModel sharedBluetoothViewModel;
    private ConnectedDeviceAdapter deviceAdapter;
    private List<ConnectedDeviceInfo> connectedDevices = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // Get the shared Bluetooth ViewModel (shared across fragments)
        sharedBluetoothViewModel = new ViewModelProvider(requireActivity()).get(SharedBluetoothViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize fake data (removed it to test Bluetooth functionality)
        populateFakeDevices();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up the received data TextView

        //binding.textViewReceivedData.setMovementMethod(new ScrollingMovementMethod());

        // Observe the Bluetooth data from the shared ViewModel
    /*    sharedBluetoothViewModel.getBluetoothData().observe(getViewLifecycleOwner(), data -> {
            binding.textViewReceivedData.setText(data);
        });*/

        return root;
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.recyclerViewConnectedDevices;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        deviceAdapter = new ConnectedDeviceAdapter(connectedDevices);
        recyclerView.setAdapter(deviceAdapter);
    }

    private void populateFakeDevices() {
        // Clear any existing devices
        connectedDevices.clear();

        // Add fake devices for testing
        connectedDevices.add(new ConnectedDeviceInfo("Bedroom Sensor", "23.5 µg/m³", R.drawable.airtentive));
        connectedDevices.add(new ConnectedDeviceInfo("Living Room", "18.2 µg/m³", R.drawable.airtentive));
        connectedDevices.add(new ConnectedDeviceInfo("Kitchen", "32.7 µg/m³", R.drawable.airtentive));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}