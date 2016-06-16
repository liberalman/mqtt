package com.seaofheart.app.chat;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Parcel;
import android.os.Parcelable;

public class LocationMessageBody extends MessageBody implements Parcelable {
    String address;
    double latitude;
    double longitude;
    public static final Creator<LocationMessageBody> CREATOR = new Creator() {
        public LocationMessageBody createFromParcel(Parcel var1) {
            //return new LocationMessageBody(var1, (LocationMessageBody)null);
            return new LocationMessageBody(var1);
        }

        public LocationMessageBody[] newArray(int var1) {
            return new LocationMessageBody[var1];
        }
    };

    public LocationMessageBody(String var1, double var2, double var4) {
        this.address = var1;
        this.latitude = var2;
        this.longitude = var4;
    }

    public String getAddress() {
        return this.address;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String toString() {
        return "location:" + this.address + ",lat:" + this.latitude + ",lng:" + this.longitude;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
        var1.writeString(this.address);
        var1.writeDouble(this.latitude);
        var1.writeDouble(this.longitude);
    }

    private LocationMessageBody(Parcel var1) {
        this.address = var1.readString();
        this.latitude = var1.readDouble();
        this.longitude = var1.readDouble();
    }
}

