package com.nalamchaitanya.sunshine;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Chaitanya on 6/13/2015.
 */
public class LatLong implements Parcelable,Serializable
{
    public double Lat;
    public double Long;

    public LatLong(double Lat,double Long)
    {
        this.Lat = Lat;
        this.Long = Long;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeDouble(Lat);
        dest.writeDouble(Long);
    }

    public static final Creator<LatLong> CREATOR = new Creator<LatLong>() {
        @Override
        public LatLong createFromParcel(Parcel source) {
            return new LatLong(source);
        }

        @Override
        public LatLong[] newArray(int size) {
            return new LatLong[0];
        }
    };

    private LatLong(Parcel in)
    {
        Lat = in.readDouble();
        Long = in.readDouble();
    }
}

