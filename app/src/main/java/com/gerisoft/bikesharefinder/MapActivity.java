package com.gerisoft.bikesharefinder;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//I probably took a lot of stuff from here
//https://developers.google.com/maps/documentation/android/marker

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener {
    // Activity variables
    ArrayList<Station> stations = new ArrayList<>();
    CameraUpdate cam = null;
    GoogleMap gMap = null;
    int selectedId = -1;
    Button action = null;

    // Inflate layout, get station list, find views, start map
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        stations = intent.getParcelableArrayListExtra("stations");

        // Sort station list based on distance for the action button
        Collections.sort(stations, new Comparator<Station>() {
            @Override
            public int compare(Station a, Station b) {
                return Double.compare(a.dist, b.dist);
            }
        });

        action = (Button)findViewById(R.id.btnNearest);
        MapFragment map = (MapFragment)getFragmentManager().findFragmentById(R.id.mapViewFragment);
        map.getMapAsync(this);
    }

    // Set the markers when the map loads
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        for (Station s : stations) {
            // Set marker color based on number of bikes
            int marker;
            if (s.avail == 0) marker = R.drawable.ic_marker_gray;
            else if (s.total / s.avail == 1) marker = R.drawable.ic_marker_green;
            else if (s.total / s.avail == 2) marker = R.drawable.ic_marker_yellow;
            else marker = R.drawable.ic_marker_red;

            // Add the marker, gray out if it's not in service
            if (s.status == 0) {
                googleMap.addMarker(new MarkerOptions()
                        .title("#" + s.id)
                        .position(s.location)
                        .snippet(getString(R.string.stat_noservice))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_gray)));
            } else {
                googleMap.addMarker(new MarkerOptions()
                        .title("#" + s.id)
                        .position(s.location)
                        .snippet(String.format(
                                getString(R.string.map_markersub),
                                s.avail, s.total))
                        .icon(BitmapDescriptorFactory.fromResource(marker)));
            }
        }
        // Set up click listeners
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedId = Integer.parseInt(marker.getId().substring(1));
                action.setText(getString(R.string.map_statinfo));
                return false;
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedId = -1;
                action.setText(getString(R.string.map_nearest));
            }
        });

        // Turn on my location thing and move the camera to Yonge and Dundas
        googleMap.setMyLocationEnabled(true);
        cam = CameraUpdateFactory.newLatLngZoom(new LatLng(43.656003,-79.3802945), 14);
        googleMap.moveCamera(cam);
    }

    // Start station information activity or go to nearest location
    public void actionButton(View v) {
        if (selectedId == -1) {
            // The nearest location should be the first one
            Station s = stations.get(0);
            cam = CameraUpdateFactory.newLatLngZoom(s.location, 18);
            gMap.animateCamera(cam);
        } else {
            Intent intent = new Intent(MapActivity.this, StationInformation.class);
            intent.putExtra("stationItem", stations.get(selectedId));
            startActivity(intent);
        }
    }

    // Sort the list based on new location
    @Override
    public void onMyLocationChange(Location location) {
        for (Station s : stations) {
            s.getDistance(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        Collections.sort(stations, new Comparator<Station>() {
            @Override
            public int compare(Station a, Station b) {
                return Double.compare(a.dist, b.dist);
            }
        });
    }
}
