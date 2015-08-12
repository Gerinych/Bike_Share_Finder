package com.gerisoft.bikesharefinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    ArrayList<Station> stations = new ArrayList<>();
    CameraUpdate cam = null;
    GoogleMap gMap = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        stations = intent.getParcelableArrayListExtra("stations");
        Collections.sort(stations, new Comparator<Station>() {
            @Override
            public int compare(Station a, Station b) {
                return Double.compare(a.dist, b.dist);
            }
        });

        MapFragment map = (MapFragment)getFragmentManager().findFragmentById(R.id.mapViewFragment);
        map.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        for (Station s : stations) {
            googleMap.addMarker(new MarkerOptions()
                    .title("#" + s.id)
                    .position(s.location));
        }
        googleMap.setMyLocationEnabled(true);
        cam = CameraUpdateFactory.newLatLngZoom(new LatLng(43.656003,-79.3802945), 14);
        googleMap.moveCamera(cam);
    }

    public void nearestStation(View v) {
        Station s = stations.get(0);
        cam = CameraUpdateFactory.newLatLngZoom(s.location, 18);
        gMap.animateCamera(cam);
    }
}
