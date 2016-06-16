package com.seaofheart.app.util;

import android.os.Parcel;
import android.os.Parcelable;

public class LatLng implements Parcelable {
    public double latitude;
    public double longitude;

    public LatLng(double var1, double var3) {
        this.latitude = var1;
        this.longitude = var3;
    }

    public LatLng(Parcel var1) {
        this.latitude = var1.readDouble();
        this.longitude = var1.readDouble();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeDouble(this.latitude);
        var1.writeDouble(this.longitude);
    }
}