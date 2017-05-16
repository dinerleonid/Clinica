package com.example.leonwork.clinica;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Clinic clinic;
    private Marker marker;
    private Circle circle;
    private SharedPreferences clinicPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent getDetailView = getIntent();
        clinic = getDetailView.getExtras().getParcelable("selectedClinic");
        if (mMap != null) {
            mMap.clear();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
    }

    public void initMap() {
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        LatLng clinicLocation = new LatLng(clinic.getLat(), clinic.getLon());
        LatLng myLocation = new LatLng(clinicPrefs.getFloat("myLat", 0), clinicPrefs.getFloat("myLon", 0));
        mMap.addMarker(new MarkerOptions().position(clinicLocation).title(clinic.getClinicName() +
        "-" + clinic.getClinicAddress()));
        if(marker == null) {
            marker = mMap.addMarker(new MarkerOptions().position(clinicLocation).title(clinic.getClinicName()));
        } else {
            marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(clinicLocation).title(clinic.getClinicName()));
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(clinicLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(clinicLocation, 16));
        circle = mMap.addCircle(new CircleOptions()
                .center(myLocation)
                .radius(clinicPrefs.getFloat("distancefromsettings", 1) * 1000)
                .fillColor((Color.parseColor("#644FC4F6")))
                .strokeColor(Color.TRANSPARENT));
    }
}
