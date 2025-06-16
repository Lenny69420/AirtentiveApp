package com.example.airtentiveapp;

import android.content.Intent;
import android.os.Bundle;

import com.example.airtentiveapp.bluetooth.BluetoothActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.airtentiveapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_bluetooth, R.id.navigation_notifications)
                .build();

        try {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            try {
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            } catch (Exception e) {
                // If setupActionBarWithNavController fails, the app can still function
                e.printStackTrace();
            }
            NavigationUI.setupWithNavController(binding.navView, navController);
        } catch (Exception e) {
            e.printStackTrace();
        }

       /* // Set up button click listener after navigation setup
        try {
            binding.buttonToBluetoothScreen.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}