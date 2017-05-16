package com.example.leonwork.clinica;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class ClinicsHandler {

    private static final String TAG = ClinicsHandler.class.getSimpleName();
    private SharedPreferences clinicsPrefs;
    private ArrayList<Clinic> clinicsList = new ArrayList<>();
    private Context context;

    protected ArrayList<Clinic> getClinics(Context context) {
        this.context = context;
        String json = loadJsonFromAsset();
        createClinics(json);
        return clinicsList;
    }

    //filter clinics by distance and by search name
    public ArrayList<Clinic> filterClinics(float selectedDistance, String searchWord) {
        ArrayList<Clinic> filteredList = new ArrayList<>();
        Clinic  currentClinic;
        double distance;
        String clinicName;
        clinicsPrefs = PreferenceManager.getDefaultSharedPreferences(context);
       // double myLat = clinicsPrefs.getFloat("myLat", 0);
      // double myLon = clinicsPrefs.getFloat("myLon", 0);
        for (int i=0; i< clinicsList.size(); i++){
            currentClinic = clinicsList.get(i);
          //  distance = calcDistance(myLat, currentClinic.getLat(), myLon, currentClinic.getLon());   //calculate my distance from the clinic
            distance = currentClinic.getDistance();
            clinicName = currentClinic.getClinicName();
            //filter by distance and by searched value
            if (distance <= selectedDistance &&
                    (searchWord.toLowerCase().equalsIgnoreCase("") || clinicName.toLowerCase().contains(searchWord.toLowerCase()))) {
                filteredList.add(currentClinic);
            }
        }
        return filteredList;
    }

    private String loadJsonFromAsset() {
        String json = null;
        try{
            InputStream inputStream = context.getAssets().open("clinicslist.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public void createClinics(String jsonItems){
        clinicsPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.e(TAG, "Response from json: " + jsonItems);
        if(jsonItems != null){
            try {
                JSONObject jsonObj = new JSONObject(jsonItems);
                JSONArray clinics = jsonObj.getJSONArray("clinics");
                double distance;
                for (int i = 0; i < clinics.length(); i++) {
                    JSONObject clinicObj = clinics.getJSONObject(i);
                    String name = clinicObj.getString("name");
                    String phone = clinicObj.getString("phone");
                    String rating = clinicObj.getString("rating");
                    String address = clinicObj.getString("vicinity");
                    String clinicPhoto = clinicObj.getString("photo_reference");
                    JSONObject opening_hours = clinicObj.getJSONObject("opening_hours");
                    boolean open_now = opening_hours.getBoolean("open_now");
                    String typesList = clinicObj.getString("types");

                    JSONObject location = clinicObj.getJSONObject("geometry").getJSONObject("location");
                    String clinicLat = location.getString("lat");
                    String clinicLon = location.getString("lng");
                    double myLat = clinicsPrefs.getFloat("myLat", 0);
                    double myLon = clinicsPrefs.getFloat("myLon", 0);
                    distance = calcDistance(myLat, Float.parseFloat(clinicLat), myLon, Float.parseFloat(clinicLon));   //calculate my distance from the clinic

                    Clinic clinic = new Clinic(Double.parseDouble(clinicLat), Double.parseDouble(clinicLon), name, address, phone, clinicPhoto, rating, typesList, open_now);
                    clinic.setDistance(distance); //set the distance from my location to clinic
                    clinicsList.add(clinic);
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                };
            }
        } else {
            Log.e(TAG, "Couldn't get json from server.");
            new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            };
        }
    }

    public double calcDistance(double myLat, double clinicLat, double myLon, double clinicLon) {
        final int R = 6371; // Radius of the earth
        Double latDistance = Math.toRadians(clinicLat - myLat);
        Double lonDistance = Math.toRadians(clinicLon - myLon);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(myLat)) * Math.cos(Math.toRadians(clinicLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c ; // convert to meters
        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }
}

