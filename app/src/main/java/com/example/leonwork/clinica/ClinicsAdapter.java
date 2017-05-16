package com.example.leonwork.clinica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import it.sephiroth.android.library.picasso.Picasso;


public class ClinicsAdapter extends RecyclerView.Adapter<ClinicsAdapter.ClinicsHolder>{

    private Context context;
    private ArrayList<Clinic> clinics = new ArrayList<>();
    private SharedPreferences clinicPrefs;

    private final static String API_KEY = "&key=AIzaSyBWR3S7bcVnysNY49SXQBBapuFsPD_jALk";
    private final static String BASE_URL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=50&photoreference=";


    public ClinicsAdapter(Context context, ArrayList<Clinic> clinics) {
        this.context = context;
        this.clinics = clinics;
    }

    @Override
    public ClinicsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.clinic_view, parent, false);
        return new ClinicsHolder(view);
    }

    @Override
    public void onBindViewHolder(ClinicsAdapter.ClinicsHolder holder, int position) {
        holder.bind(clinics.get(position));
    }

    @Override
    public int getItemCount() {
        return clinics.size();
    }

    public class ClinicsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView clinicName, clinicAddress, clinicDistance;
        private ImageView clinicImage;
        private Clinic clinic;
        private String imagePath;
        private String forDistance;

        public ClinicsHolder(View clinicView) {
            super(clinicView);
            clinicName = (TextView) clinicView.findViewById(R.id.clinicName);
            clinicAddress = (TextView) clinicView.findViewById(R.id.clinicAddress);
            clinicDistance = (TextView) clinicView.findViewById(R.id.clinicDistance);
            clinicImage = (ImageView) clinicView.findViewById(R.id.imageView);
            clinicView.setOnClickListener(this);
        }

        public void bind(Clinic clinic) {
            this.clinic = clinic;
            clinicPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            double distance = clinic.getDistance();
            DecimalFormat formater = new DecimalFormat("0.00");
            clinicName.setText(clinic.getClinicName());
            clinicAddress.setText(clinic.getClinicAddress());
            if (distance > 1) {
                clinicDistance.setText(String.valueOf(formater.format(distance)) + " Km. from you");
                forDistance = String.valueOf(formater.format(distance)) + " Km. from you";
            } else {
                clinicDistance.setText(String.valueOf(formater.format(distance * 1000)) + " m. from you");
                forDistance = String.valueOf(formater.format(distance)) + " m. from you";
            }
            clinicPrefs.edit().putString("distance", forDistance).commit();
            imagePath = BASE_URL + clinic.getClinicImagePath() + API_KEY;
            Picasso.with(context).load(imagePath).into((clinicImage));
        }

        @Override
        public void onClick(View v) {
            switch (getAdapterPosition()){
                default:
                    clinicPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    clinicPrefs.edit().putString("distanceSelected",String.valueOf(clinicDistance.getText())).commit();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("clinic", clinic);
                    Intent detailView = new Intent(context, ClinicDetail.class);
                    detailView.putExtras(bundle);
                    context.startActivity(detailView);
                    break;
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }
}
