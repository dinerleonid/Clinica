package com.example.leonwork.clinica;

import android.os.Parcel;
import android.os.Parcelable;

class Clinic implements Parcelable {

    // data members
    private double lat, lon, distance;
    private String clinicName, clinicAddress, clinicPhone, clinicImagePath, rating, typesList;
    private boolean open_now;

    public Clinic(double lat,
                  double lon,
                  String clinicName,
                  String clinicAddress,
                  String clinicPhone,
                  String clinicImagePath,
                  String rating,
                  String typesList,
                  boolean open_now) {
        this.lat = lat;
        this.lon = lon;
        this.clinicName = clinicName;
        this.clinicAddress = clinicAddress;
        this.clinicPhone = clinicPhone;
        this.clinicImagePath = clinicImagePath;
        this.rating = rating;
        this.typesList = typesList;
        this.open_now = open_now;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
        parcel.writeString(clinicName);
        parcel.writeString(clinicAddress);
        parcel.writeString(clinicPhone);
        parcel.writeString(clinicImagePath);
        parcel.writeString(rating);
        parcel.writeString(typesList);
        parcel.writeByte((byte) (open_now ? 1 : 0));
    }

    public static final Creator<Clinic> CREATOR = new Creator<Clinic>() {
        @Override
        public Clinic createFromParcel(Parcel in) {
            return new Clinic(in);
        }

        @Override
        public Clinic[] newArray(int size) {
            return new Clinic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected Clinic(Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
        clinicName = in.readString();
        clinicAddress = in.readString();
        clinicPhone = in.readString();
        clinicImagePath = in.readString();
        rating = in.readString();
        typesList = in.readString();
        open_now = in.readByte() != 0;
    }

    public double getLat() { return lat; }

    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }

    public void setLon(double lon) { this.lon = lon; }

    public String getClinicName() { return clinicName; }

    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public String getClinicAddress() { return clinicAddress; }

    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }

    public String getClinicPhone() { return clinicPhone; }

    public void setClinicPhone(String clinicPhone) { this.clinicPhone = clinicPhone; }

    public String getClinicImagePath() { return clinicImagePath; }

    public void setClinicImagePath(String clinicImagePath) { this.clinicImagePath = clinicImagePath; }

    public String getRating() { return rating; }

    public void setRating(String rating) { this.rating = rating; }

    public String getTypesList() { return typesList; }

    public void setTypesList(String typesList) { this.typesList = typesList; }

    public boolean isOpen_now() { return open_now; }

    public void setOpen_now(boolean open_now) { this.open_now = open_now; }

    public void setDistance(double distance) { this.distance = distance; }

    public double getDistance() { return this.distance; }

}
