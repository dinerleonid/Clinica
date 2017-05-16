package com.example.leonwork.clinica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, LocationListener{

    private ClinicsAdapter adapter;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private LocationManager locationManager;
    private boolean gotLocation = false;
    private Timer timer;
    private SharedPreferences clinicPrefs;
    private String providerName;
    private boolean clickedToExit = false;
    private ProgressBar progressBar;
    private  ArrayList<Clinic> clinics;
    private final static int DEFAULT_DISTANCE = 5;
    private ClinicsHandler clinicHandler = new ClinicsHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        searchView.clearFocus();
        if(!clinicPrefs.getString("query", "").equals("") ){
            //remove the old search value for the next entry
            clinicPrefs.edit().remove("query");
        }
        clinicPrefs.edit().putBoolean("entered", false).commit();
        clinicPrefs.edit().putFloat("distancefromsettings", DEFAULT_DISTANCE);
        askForPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuItems = getMenuInflater();
        menuItems.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                Intent goToSettings = new Intent(this, SettingsActivity.class);
                startActivity(goToSettings);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        searchView.clearFocus();
        if (clinicPrefs.getBoolean("entered", true)) {
            filterClinics(clinicPrefs.getString("query", "").toString());
        }
    }

    public void getAllClinics(){
        //clear the clinics list
        if (clinics != null) {
            clinics.clear();
        }
        clinics = clinicHandler.getClinics(getApplication());
        bindClinicsToRecyclerView();
    }

    public void filterClinics(String searchWord){
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        float selectedDistance = clinicPrefs.getFloat("distancefromsettings", DEFAULT_DISTANCE);
        //filter clinics by the selected distance and searched value
        clinics = clinicHandler.filterClinics(selectedDistance ,searchWord);
        bindClinicsToRecyclerView();
    }

    public void bindClinicsToRecyclerView(){
        adapter = new ClinicsAdapter(this, clinics);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void askForPermissions() {
        checkLocationServiceOn();
        boolean hasPermissions = PermissionsHandler.requestPermissions(this);
        if (hasPermissions) {
            progressBar.setVisibility(View.VISIBLE);
            initLocation();
            getAllClinics();
        }
    }
    public void checkLocationServiceOn(){
        LocationOnOffChecker checkLocationSettings = new LocationOnOffChecker();
        checkLocationSettings.isLocationServiceEnabled(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            initLocation();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void initLocation(){
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        providerName = LocationManager.GPS_PROVIDER;
        try {
            locationManager.requestLocationUpdates(providerName, 1000, 100, (LocationListener) this);
        } catch (SecurityException e) {
            Log.e("Location", e.getMessage());
        }
        // create timer object
        timer = new Timer("provider");
        // create TimerTask implementation - it will run on a new thread!
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // if we do not have a location yet
                if (gotLocation == false) {
                    try {
                        // remove old location provider(gps)
                        locationManager.removeUpdates(MainActivity.this);
                        // change provider name to NETWORK
                        providerName = LocationManager.NETWORK_PROVIDER;

                        // start listening to location again on the main thread
                        if (MainActivity.this != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        locationManager.requestLocationUpdates(providerName, 1000, 100, MainActivity.this);
                                    } catch (SecurityException e) {
                                    }
                                }
                            });
                        }
                    } catch (SecurityException e) {
                        Log.e("Location", e.getMessage());
                    }
                }
            }
        };
        timer.schedule(task, new Date(System.currentTimeMillis() + 1000));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clinicPrefs.edit().putString("query", query).commit();
        filterClinics(query);
        progressBar.setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clinicPrefs.edit().putString("query", newText).commit();
        filterClinics(newText);
        progressBar.setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        float myLat = (float) location.getLatitude();
        float myLon = (float) location.getLongitude();
        clinicPrefs.edit().putFloat("myLat", myLat).putFloat("myLon", myLon).commit();
        progressBar.setVisibility(View.INVISIBLE);
        if (!clinicPrefs.getBoolean("entered", false)) {
            getAllClinics();
            clinicPrefs.edit().putBoolean("entered", true).commit();
        }
    }

    @Override
    public void onBackPressed(){
        if(!clickedToExit){
            Toast.makeText(this, "Click 'Back' button again to exit", Toast.LENGTH_SHORT).show();
            clickedToExit = true;
        }else{
            locationManager.removeUpdates(this);
            finish();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}
