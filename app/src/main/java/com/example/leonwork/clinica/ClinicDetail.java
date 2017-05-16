package com.example.leonwork.clinica;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.sephiroth.android.library.picasso.Picasso;

public class ClinicDetail extends AppCompatActivity implements View.OnClickListener {

    private TextView detailName, detailAddress, detailDistance, detailMoreInfo;
    private Button btnCall, btnMap, btnNavigate;
    private SharedPreferences clinicPrefs;
    private ImageView detailImage;
    private String imagePath;
    private Clinic clinic;
    private static final int CALL_PHONE_PERMISSION = 89;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_detail);
        detailName = (TextView) findViewById(R.id.detailName);
        detailAddress = (TextView) findViewById(R.id.detailAddress);
        detailDistance = (TextView) findViewById(R.id.detailDistance);
        detailMoreInfo = (TextView) findViewById(R.id.detailMoreInfo);
        btnCall = (Button) findViewById(R.id.btnCall);
        btnCall.setOnClickListener(this);
        btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(this);
        btnNavigate = (Button) findViewById(R.id.btnNavigate);
        btnNavigate.setOnClickListener(this);
        detailImage = (ImageView) findViewById(R.id.detailImage);
        clinicPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent getDetailView = getIntent();
        clinic = getDetailView.getExtras().getParcelable("clinic");

        if (clinic != null) {
            displayClinicInfo(detailName, "Name: " + clinic.getClinicName());
            displayClinicInfo(detailAddress, "Address: " + clinic.getClinicAddress());
            displayClinicInfo(detailDistance, "Distance to clinic - " + clinicPrefs.getString("distanceSelected", ""));

            if (clinic.isOpen_now()) {
                displayClinicInfo(detailMoreInfo, "This clinic is open now, it's type: " + clinic.getTypesList() + "\n" + "Clinic is rated: " + clinic.getRating());
            } else {
                displayClinicInfo(detailMoreInfo, "This clinic is closed now it's type: " + clinic.getTypesList() + "\n" + "Clinic is rated: " + clinic.getRating());
            }
            imagePath = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference=" + clinic.getClinicImagePath() +
                    "&key=AIzaSyBWR3S7bcVnysNY49SXQBBapuFsPD_jALk";
            Picasso.with(this).load(imagePath).into(detailImage);
        }
    }

    private void displayClinicInfo(TextView textView, String message){
        textView.setText(message);
        textView.setTextColor(Color.BLUE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.btnCall):
                onCall();
                break;
            case (R.id.btnMap):
                Bundle bundle = new Bundle();
                bundle.putParcelable("selectedClinic", clinic);
                Intent goToMap = new Intent(this, MapsActivity.class);
                goToMap.putExtras(bundle);
                startActivity(goToMap);
                break;
            case (R.id.btnNavigate):
                String uri = "geo:" + clinic.getLat() + "," + clinic.getLon() + "?q="
                        + clinic.getClinicAddress();
                Intent navigate = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                navigate.setData(Uri.parse(uri));
                PackageManager packageManager = getPackageManager();
                List activities = packageManager.queryIntentActivities(navigate,
                        PackageManager.MATCH_DEFAULT_ONLY);
                String title = "How would you like to navigate there?";
                // Create intent to show chooser
                Intent chooser = Intent.createChooser(navigate, title);
                boolean isIntentSafe = activities.size() > 0;
                // Verify the intent will resolve to at least one activity
                if (navigate.resolveActivity(getPackageManager()) != null) {
                    if (isIntentSafe) {
                        startActivity(chooser);
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CALL_PHONE_PERMISSION:
                if ((grantResults.length > 0) && (grantResults[0]
                        == PackageManager.PERMISSION_GRANTED)) {
                    onCall();
                } else {
                    Log.d("TAG", "Call Permission Not Granted");
                }
                break;
            default:
                break;
        }
    }

    public void onCall(){
        int permissionCheck = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE);
        String phoneNumber = clinic.getClinicPhone();
        Intent callNumber = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null));
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_PERMISSION);
        } else {
            startActivity(callNumber);
        }
    }
}