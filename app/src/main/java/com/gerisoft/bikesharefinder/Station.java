package com.gerisoft.bikesharefinder;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// Station class, defines a station and its properties
public class Station implements Parcelable {
    // Member variables
    double dist = -1;
    int bearing;
    String name;
    int avail;
    int total;
    LatLng location;
    int id;
    Date date = new Date();
    int status;

    // Initialize object based on passed parameters
    public Station(int id, String name, LatLng location, int avail, int total, int status) {
        this.name = name;
        this.location = location;
        this.avail = avail;
        this.total = total;
        this.id = id;
        this.status = status;
    }

    // For passing object between activities
    public Station(Parcel in) {
        dist = in.readDouble();
        name = in.readString();
        status = in.readInt();
        avail = in.readInt();
        total = in.readInt();
        id = in.readInt();
        date = new Date(in.readLong());
        location = new LatLng(in.readDouble(), in.readDouble());
        bearing = in.readInt();
    }

    // Parsing date from the JSON object
    // It is such a bad idea to include both 24-hour clock and AM/PM markers
    public void setDate(String time) throws ParseException {
        //http://stackoverflow.com/questions/4216745/java-string-to-date-conversion
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:m:s");
        date = df.parse(time);
    }

    // Calculating distance and bearing from current location, store in dist
    //http://rosettacode.org/wiki/Haversine_formula#Java
    //http://stackoverflow.com/questions/3209899/determine-compass-direction-from-one-lat-lon-to-the-other
    public double getDistance(LatLng me) {
        // Distance
        double lat1 = location.latitude,
                lat2 = me.latitude,
                lon1 = location.longitude,
                lon2 = me.longitude;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        dist = 6372.8 * c;

        // Bearing
        double y = Math.sin(dLon) * Math.cos(lat1);
        double x = Math.cos(lat2)*Math.sin(lat1) -
                Math.sin(lat2)*Math.cos(lat1)*Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));

        double index = brng - 22.5;
        if (index < 0) index += 360;
        bearing = (int)Math.floor(index / 45);

        return dist;
    }

    // Parcel logic
    //http://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(dist);
        dest.writeString(name);
        dest.writeInt(status);
        dest.writeInt(avail);
        dest.writeInt(total);
        dest.writeInt(id);
        dest.writeLong(date.getTime());
        dest.writeDouble(location.latitude);
        dest.writeDouble(location.longitude);
        dest.writeInt(bearing);
    }

    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        public Station[] newArray(int size) {
            return new Station[size];
        }
    };
}
