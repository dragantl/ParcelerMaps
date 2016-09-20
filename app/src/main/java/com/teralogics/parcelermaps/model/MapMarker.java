package com.teralogics.parcelermaps.model;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class MapMarker {
    private String title;
    private float latitude;
    private float longitude;

    @ParcelConstructor
    public MapMarker(@NonNull final String title, final float latitude, final float longitude) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLatitude(final float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(final float longitude) {
        this.longitude = longitude;
    }

    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }
}
