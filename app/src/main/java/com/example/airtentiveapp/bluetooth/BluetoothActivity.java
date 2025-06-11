package com.example.airtentiveapp.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.airtentiveapp.databinding.BluetoothBinding;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    private BluetoothBinding binding;
    private BluetoothAdapter bluetoothAdapter;
    private ActivityResultLauncher<Intent> requestBluetoothEnableLauncher;
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private final List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothDeviceAdapter deviceAdapter;
    private BluetoothClient bluetoothClient;

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.i(TAG, "Receiver triggered with action: " + action);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                } else {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                }

                if (device != null) {
                    if (ContextCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                            || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {

                        String deviceName = device.getName();
                        String deviceAddress = device.getAddress();

                        // Avoid duplicates by address
                        boolean alreadyFound = false;
                        for (BluetoothDevice d : devices) {
                            if (d.getAddress().equals(deviceAddress)) {
                                alreadyFound = true;
                                break;
                            }
                        }

                        if (!alreadyFound) {
                            devices.add(device);
                            Log.i(TAG, "Found device: " + deviceName + " - " + deviceAddress);
                            // Notify adapter of data change to refresh RecyclerView
                            deviceAdapter.notifyDataSetChanged();
                        }

                    } else {
                        // If permission denied, just add with address only if not already added
                        boolean alreadyFound = false;
                        for (BluetoothDevice d : devices) {
                            if (d.getAddress().equals(device.getAddress())) {
                                alreadyFound = true;
                                break;
                            }
                        }

                        if (!alreadyFound) {
                            devices.add(device);
                            Log.w(TAG, "BLUETOOTH_CONNECT permission not granted, can't get name for " + device.getAddress());
                            deviceAdapter.notifyDataSetChanged();
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "Discovery Started...");
                binding.textViewStatus.setText("Status: Scanning...");
                binding.buttonScan.setEnabled(false);
                devices.clear();  // Clear device list before new scan
                deviceAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "Discovery Finished.");
                binding.textViewStatus.setText("Status: Scan finished.");
                binding.buttonScan.setEnabled(true);

                if (devices.isEmpty()) {
                    binding.textViewStatus.append("\nNo devices found.");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BluetoothManager bluetoothManager;
        super.onCreate(savedInstanceState);
        binding = BluetoothBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerViewDevices.setLayoutManager(new LinearLayoutManager(this));
        // Pass the list of devices to your adapter's constructor
        deviceAdapter = new BluetoothDeviceAdapter(devices, this::onDeviceClicked, this);
        binding.recyclerViewDevices.setAdapter(deviceAdapter);

        binding.textViewReceivedData.setMovementMethod(new ScrollingMovementMethod()); // Make it scrollable

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        initializeActivityResultLaunchers();

        binding.buttonScan.setOnClickListener(v -> checkPermissionsAndInitiateScan());

        // Register the BroadcastReceiver for Bluetooth discovery
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.registerReceiver(this, discoveryReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED); // For Android 13+
            registerReceiver(discoveryReceiver, filter, Context.RECEIVER_EXPORTED); // For Android 13+
        } else {
            registerReceiver(discoveryReceiver, filter);
        }
    }

    private void onDeviceClicked(BluetoothDevice device) {
        Toast.makeText(this, "Clicked: " + getDeviceNameSafe(device), Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            binding.textViewStatus.setText("Status: Scan permission missing");
            Toast.makeText(this, "BLUETOOTH_SCAN permission needed to scan.", Toast.LENGTH_LONG).show();
            // Optionally, re-trigger permission request
            return;
        }
        // Cancel before connecting
        if (bluetoothAdapter.isDiscovering()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.cancelDiscovery();
                } else {
                    Log.w(TAG, "Missing BLUETOOTH_SCAN permission to cancel discovery.");
                    // You might want to inform the user or handle this.
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.cancelDiscovery();
                } else {
                    Log.w(TAG, "Missing BLUETOOTH_ADMIN permission to cancel discovery.");
                }
            }
        }


        // Connect to device using BluetoothClient
        bluetoothClient = new BluetoothClient(this);
        bluetoothClient.setCallback(new BluetoothDataCallback() {
            @Override
            public void onDataReceived(String data) {
                runOnUiThread(() -> binding.textViewReceivedData.append("\n" + data));
            }

            @Override
            public void onConnected() {
                runOnUiThread(() -> binding.textViewStatus.setText("Status: Connected"));
            }

            @Override
            public void onConnectionFailed(Exception e) {
                runOnUiThread(() -> binding.textViewStatus.setText("Status: Connection Failed"));
            }
        });
        bluetoothClient.connectToDevice(device);
    }

    private String getDeviceNameSafe(BluetoothDevice device) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            String name = device.getName();
            return (name != null && !name.isEmpty()) ? name : "Unknown Device";
        }
        return "Name Hidden (No Permission)";
    }

    private void initializeActivityResultLaunchers() {
        requestBluetoothEnableLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(BluetoothActivity.this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                startScanningForDevices();
            } else {
                Toast.makeText(BluetoothActivity.this, "Bluetooth enabling denied", Toast.LENGTH_SHORT).show();
                binding.textViewStatus.setText("Status: Bluetooth not enabled");
            }
        });

        requestMultiplePermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allPermissionsGranted = true;
            for (Boolean granted : permissions.values()) {
                if (Boolean.FALSE.equals(granted)) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                Toast.makeText(BluetoothActivity.this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                checkAndEnableBluetooth();
            } else {
                Toast.makeText(BluetoothActivity.this, "Some permissions were denied. Cannot scan.", Toast.LENGTH_LONG).show();
                binding.textViewStatus.setText("Status: Permissions denied");
            }
        });
    }

    private void checkPermissionsAndInitiateScan() {
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
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
            binding.textViewStatus.setText("Status: Bluetooth not supported");
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothEnableLauncher.launch(enableBtIntent);
                } else {
                    Toast.makeText(this, "BLUETOOTH_CONNECT permission needed to enable Bluetooth", Toast.LENGTH_LONG).show();
                    binding.textViewStatus.setText("Status: Permission needed to enable BT");
                    // Optionally, re-trigger permission request here or guide user
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
            binding.textViewStatus.setText("Status: Bluetooth not enabled for scan");
            Toast.makeText(this, "Bluetooth is not enabled.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for necessary scan permissions before starting discovery
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                binding.textViewStatus.setText("Status: Scan permission missing");
                Toast.makeText(this, "BLUETOOTH_SCAN permission needed to scan.", Toast.LENGTH_LONG).show();
                // Optionally, re-trigger permission request
                return;
            }
        } else { // For older versions, BLUETOOTH_ADMIN is the primary concern for starting discovery
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                binding.textViewStatus.setText("Status: Admin permission missing for scan");
                Toast.makeText(this, "BLUETOOTH_ADMIN permission needed to scan.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        // Also ensure location permission is granted for discovery
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            binding.textViewStatus.setText("Status: Location permission missing for scan");
            Toast.makeText(this, "Location permission needed for Bluetooth scanning.", Toast.LENGTH_LONG).show();
            // Optionally, re-trigger permission request
            return;
        }

        ensureLocationEnabled();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery(); // Always cancel ongoing discovery before starting a new one
        }

        boolean discoveryStarted = bluetoothAdapter.startDiscovery();
        if (discoveryStarted) {
            binding.textViewStatus.setText("Status: Starting scan...");
            Log.i(TAG, "Attempting to start discovery...");
        } else {
            binding.textViewStatus.setText("Status: Failed to start scan");
            Log.e(TAG, "Failed to start discovery. Check permissions and BT state carefully.");
            // Re-check permissions and adapter state here. Sometimes happens if not all conditions are met.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Log.e(TAG, "BLUETOOTH_SCAN permission is missing.");
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "ACCESS_FINE_LOCATION permission is missing.");
            }
            if (!bluetoothAdapter.isEnabled()) {
                Log.e(TAG, "Bluetooth Adapter is not enabled.");
            }
        }
    }

    private void ensureLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationServicesEnabled = LocationManagerCompat.isLocationEnabled(locationManager);
        if (!isLocationServicesEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Location")
                    .setMessage("Location services are required for Bluetooth scanning. Enable now?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Take user to Location Settings
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        // Optionally: update UI status
                        binding.textViewStatus.setText("Status: Location disabled");
                    })
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver
        try {
            unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver not registered or already unregistered.", e);
        }

        // Cancel discovery if it's running
        if (bluetoothAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED && bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }

            } else { // For older versions, BLUETOOTH_ADMIN is needed to cancel
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED && bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }

            }
        }
    }
}