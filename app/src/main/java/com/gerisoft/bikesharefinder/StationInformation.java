package com.gerisoft.bikesharefinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class StationInformation extends AppCompatActivity implements OnMapReadyCallback {
    Station s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_info);

        Intent intent = getIntent();
        s = intent.getParcelableExtra("stationItem");

        MapFragment map = (MapFragment)getFragmentManager().findFragmentById(R.id.mapView);
        map.getMapAsync(this);

        TextView name = (TextView)findViewById(R.id.stationName);
        TextView id = (TextView)findViewById(R.id.stationNumber);
        TextView bikes = (TextView)findViewById(R.id.stationBikes);
        TextView update = (TextView)findViewById(R.id.stationUpdate);
        TextView status = (TextView)findViewById(R.id.stationStatus);
        TextView dist = (TextView)findViewById(R.id.stationDistance);

        name.setText(s.name);
        id.setText("#" + s.id);
        bikes.setText(String.format(
                getString(R.string.stat_bikes),
                s.avail, s.total));
        long diff = new Date().getTime() - s.date.getTime();

        if (diff < 24 * 60 * 60 * 1000) {
            DateFormat df = new SimpleDateFormat("K:mm a zzz");
            df.setTimeZone(TimeZone.getDefault());
            DateFormat day = new SimpleDateFormat("d");
            if (day.format(new Date()).equals(day.format(s.date))) {
                update.setText(String.format(
                        getString(R.string.stat_updatedtoday),
                        df.format(s.date)));
            } else {
                update.setText(String.format(
                        getString(R.string.stat_updatedyest),
                        df.format(s.date)));
            }
        } else {
            DateFormat df = new SimpleDateFormat("E, MMM d 'at' K:mm a zzz");
            df.setTimeZone(TimeZone.getDefault());
            update.setText(String.format(
                    getString(R.string.stat_updated),
                    df.format(s.date)));
        }

        if (s.status == 1) status.setText(getString(R.string.stat_inservice));
        else if (s.status == 0) status.setText(getString(R.string.stat_noservice));
        dist.setText(String.format(
                getString(R.string.stat_away),
                s.dist));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(s.location, 15);
        googleMap.addMarker(new MarkerOptions()
                .title("#" + s.id)
                .position(s.location));
        googleMap.moveCamera(cam);
    }

    //http://stackoverflow.com/questions/2662531/launching-google-maps-directions-via-an-intent-on-android
    public void getDirections(View v) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + s.location.latitude + ',' + s.location.longitude));
        startActivity(intent);
    }
}
