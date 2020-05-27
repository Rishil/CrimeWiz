package com.github.rishil.crimewiz.core.util.clustering;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class CrimeClusterIcon implements ClusterItem, Parcelable {
    private final LatLng mPosition;
    private final String mTitle;
    private String mSnippet, mLocation, mDate;

    public CrimeClusterIcon(double lat, double lng, String title, String snippet, String location,
                            String date) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
        mLocation = location;
        mDate = date;
    }

    private CrimeClusterIcon(Parcel in) {
        mPosition = in.readParcelable(LatLng.class.getClassLoader());
        mTitle = in.readString();
        mSnippet = in.readString();
        mLocation = in.readString();
        mDate = in.readString();
    }

    public static final Creator<CrimeClusterIcon> CREATOR = new Creator<CrimeClusterIcon>() {
        @Override
        public CrimeClusterIcon createFromParcel(Parcel in) {
            return new CrimeClusterIcon(in);
        }

        @Override
        public CrimeClusterIcon[] newArray(int size) {
            return new CrimeClusterIcon[size];
        }
    };

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public String getDate() { return mDate;}

    public String getLocation() { return mLocation;}


    @Override
    public String getSnippet() {
        return mSnippet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mPosition, i);
        parcel.writeString(mTitle);
        parcel.writeString(mSnippet);
        parcel.writeString(mLocation);
        parcel.writeString(mDate);


    }
}

