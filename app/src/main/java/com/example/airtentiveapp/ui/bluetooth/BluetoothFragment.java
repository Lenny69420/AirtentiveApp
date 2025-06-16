package com.example.airtentiveapp.ui.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.airtentiveapp.R;
import com.example.airtentiveapp.databinding.FragmentBluetoothBinding;

import java.util.ArrayList;
import java.util.List;

public class BluetoothFragment extends Fragment {

    private FragmentBluetoothBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private ActivityResultLauncher<Intent> requestBluetoothEnableLauncher;
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private TextView statusTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BluetoothViewModel bluetoothViewModel =
                new ViewModelProvider(this).get(BluetoothViewModel.class);

        binding = FragmentBluetoothBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Bluetooth adapter
        BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        // Find and set up the status TextView
        statusTextView = root.findViewById(R.id.text_view_status);
        if (statusTextView == null) {
            // If the TextView isn't found, log an error
            Toast.makeText(requireContext(), "Status TextView not found in layout", Toast.LENGTH_SHORT).show();
        } else {
            statusTextView.setText("Status: Not Connected");
        }

        // Initialize activity result launchers
        initializeActivityResultLaunchers();

        // Set up the button click listener
        binding.buttonScan.setOnClickListener(v -> checkPermissionsAndInitiateScan());

        return root;
    }

    private void initializeActivityResultLaunchers() {
        // Launcher for Bluetooth enable request
        requestBluetoothEnableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1) { // RESULT_OK
                        Toast.makeText(requireContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                        startScanningForDevices();
                    } else {
                        Toast.makeText(requireContext(), "Bluetooth enabling denied", Toast.LENGTH_SHORT).show();
                        updateStatus("Status: Bluetooth not enabled");
                    }
                });

        // Launcher for permission requests
        requestMultiplePermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allPermissionsGranted = true;
                    for (Boolean granted : permissions.values()) {
                        if (Boolean.FALSE.equals(granted)) {
                            allPermissionsGranted = false;
                            break;
                        }
                    }
                    if (allPermissionsGranted) {
                        Toast.makeText(requireContext(), "Permissions Granted", Toast.LENGTH_SHORT).show();
                        checkAndEnableBluetooth();
                    } else {
                        Toast.makeText(requireContext(), "Some permissions were denied. Cannot scan.", Toast.LENGTH_LONG).show();
                        updateStatus("Status: Permissions denied");
                    }
                });
    }

    private void checkPermissionsAndInitiateScan() {
        List<String> requiredPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            requiredPermissions.add(Manifest.permission.BLUETOOTH);
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            requestMultiplePermissionsLauncher.launch(missingPermissions.toArray(new String[0]));
        } else {
            checkAndEnableBluetooth();
        }
    }

    private void checkAndEnableBluetooth() {
        if (bluetoothAdapter == null) {
            updateStatus("Status: Bluetooth not supported");
            Toast.makeText(requireContext(), "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothEnableLauncher.launch(enableBtIntent);
                } else {
                    Toast.makeText(requireContext(), "BLUETOOTH_CONNECT permission needed to enable Bluetooth", Toast.LENGTH_LONG).show();
                    updateStatus("Status: Permission needed to enable BT");
                }
            } else {
                requestBluetoothEnableLauncher.launch(enableBtIntent);
            }
        } else {
            startScanningForDevices();
        }
    }

    private void startScanningForDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            if (statusTextView != null) {
                statusTextView.setText("Status: Bluetooth not enabled for scan");
            }
            Toast.makeText(requireContext(), "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for necessary scan permissions before starting discovery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (statusTextView != null) {
                    statusTextView.setText("Status: Scan permission missing");
                }
                Toast.makeText(requireContext(), "BLUETOOTH_SCAN permission needed to scan.", Toast.LENGTH_LONG).show();
                // Optionally, re-trigger permission request
                return;
            }
        } else { // For older versions, BLUETOOTH_ADMIN is the primary concern for starting discovery
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                if (statusTextView != null) {
                    statusTextView.setText("Status: Admin permission missing for scan");
                }
                Toast.makeText(requireContext(), "BLUETOOTH_ADMIN permission needed to scan.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        // Also ensure location permission is granted for discovery
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (statusTextView != null) {
                statusTextView.setText("Status: Location permission missing for scan");
            }
            Toast.makeText(requireContext(), "Location permission needed for Bluetooth scanning.", Toast.LENGTH_LONG).show();
            // Optionally, re-trigger permission request
            return;
        }

        ensureLocationEnabled();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery(); // Always cancel ongoing discovery before starting a new one
        }

        boolean discoveryStarted = bluetoothAdapter.startDiscovery();
        if (discoveryStarted) {
            if (statusTextView != null) {
                statusTextView.setText("Status: Starting scan...");
            }
            Log.i("BluetoothFragment", "Attempting to start discovery...");
        } else {
            if (statusTextView != null) {
                statusTextView.setText("Status: Failed to start scan");
            }
            Log.e("BluetoothFragment", "Failed to start discovery. Check permissions and BT state carefully.");
        }
    }

    private void updateStatus(String status) {
        if (statusTextView != null) {
            statusTextView.setText(status);
        }
    }

    private void ensureLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationServicesEnabled = LocationManagerCompat.isLocationEnabled(locationManager);
        if (!isLocationServicesEnabled) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Enable Location")
                    .setMessage("Location services are required for Bluetooth scanning. Enable now?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        updateStatus("Status: Location disabled");
                    })
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}