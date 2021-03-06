package com.gerisoft.bikesharefinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gerisoft.utils.Utilities;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// Main activity, shows the filtered list of all stations
public class MainActivity extends AppCompatActivity implements DialogResult, LocationListener {
    // constants
    final String JsonUrl = "http://www.bikesharetoronto.com/stations/json";
    Criteria crit = new Criteria();

    // Activity variables
    ListView statList = null;
    TextView statusText = null;
    Comparator<Station> distComp = new Comparator<Station>() {
        @Override
        public int compare(Station a, Station b) {return Double.compare(a.dist, b.dist);
        }
    };
    Comparator<Station> bikeComp = new Comparator<Station>() {
        @Override
        public int compare(Station a, Station b) { return new Integer(b.avail).compareTo(a.avail); }
    };
    ArrayList<Station> stations, dispStations;
    LocationManager lm = null;
    String provider = null;
    LatLng location = null;
    SharedPreferences sets;

    //filter settings
    double filter_radius = 5;
    boolean filter_sort = true;

    // Create activity, load prefs, find views, set up location updates, set up station list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Loading shared prefs
        sets = getPreferences(0);
        filter_radius = sets.getFloat("radius", 5);
        filter_sort = sets.getBoolean("sort", true);

        //Finding views used
        statList = (ListView) findViewById(R.id.lstStations);
        statusText = (TextView) findViewById(R.id.txtSubtitle);

        // Location set up
        // https://blog.codecentric.de/en/2014/05/android-gps-positioning-location-strategies/
        crit.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        crit.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        crit.setSpeedRequired(true); // Chose if speed for first location fix is required.
        crit.setAltitudeRequired(false); // Choose if you use altitude.
        crit.setBearingRequired(false); // Choose if you use bearing.
        crit.setCostAllowed(false); // Choose if this provider can waste money :-)

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationSetup(crit);
        Log.i("Location stuff", String.format("Provider set to %s", provider));

        // Getting list of stations
        new GetJson().execute();

        // listview click logic
        statList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Station s = (Station) parent.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, StationInformation.class);
                intent.putExtra("stationItem", s);
                startActivity(intent);
            }
        });
    }

    // Updates listview based on radius and sort method
    public void updateList() {

        // create a new list based on radius
        dispStations = new ArrayList<>();
        for (Station s : stations) {
            if (location == null)
                dispStations.add(s);
            else if (s.getDistance(location) <= filter_radius || filter_radius >= 10.1)
                dispStations.add(s);
        }

        // sort that list
        if (filter_sort) Collections.sort(dispStations, distComp);
        else Collections.sort(dispStations, bikeComp);

        // display message based on how many stations found
        if (dispStations.size() > 0) {
            if (location == null)
                statusText.setText(getString(R.string.main_noloc));
            else if (filter_radius < 10.1)
                statusText.setText(String.format(
                    getString(R.string.main_found),
                    stations.size(), filter_radius));
            else
                statusText.setText(String.format(
                        getString(R.string.main_foundall),
                        stations.size()));
        } else {
            statusText.setText(String.format(
                    getString(R.string.main_notfound),
                    filter_radius));
        }

        // put it all into listview
        statList.setAdapter(new StationAdapter(this, dispStations));
    }

    // standard stuff, inflate menu layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // handling menu actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.refresh) {
            // refresh list, the only thing that should change is the number of bikes

            new GetJson().execute();
            return true;
        } else if (id == R.id.filter) {
            // show the filter dialog, pass current settings

            Bundle b = new Bundle();
            b.putDouble("radius", filter_radius);
            b.putBoolean("sort", filter_sort);

            FilterDialog dialog = new FilterDialog();
            dialog.setArguments(b);
            dialog.show(getFragmentManager(), "filter");
            return true;

        } else if (id == R.id.map) {
            // switch to map view of all stations

            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putParcelableArrayListExtra("stations", stations);
            this.startActivity(intent);
            return true;
        } else if (id == R.id.legal) {
            //show the legal stuff about Google API

            AlertDialog legal = new AlertDialog.Builder(MainActivity.this).create();
            legal.setMessage(getString(R.string.main_aaslegal) + GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
            legal.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Getting settings from the filter dialog
    //http://stackoverflow.com/questions/12622742/get-value-from-dialogfragment
    @Override
    public void getResult(Bundle savedInstanceState) {
        filter_radius = savedInstanceState.getDouble("radius");
        filter_sort = savedInstanceState.getBoolean("sort");

        SharedPreferences.Editor editor = sets.edit();
        editor.putFloat("radius", (float) filter_radius);
        editor.putBoolean("sort", filter_sort);
        editor.apply();

        updateList();
    }

    // Location stuff
    @Override
    public void onLocationChanged(Location location) {
        if (this.location == null) {
            this.location = new LatLng(location.getLatitude(), location.getLongitude());
            updateList();
            return;
        }
        this.location = new LatLng(location.getLatitude(), location.getLongitude());

        // I really hate this piece of code
        //http://stackoverflow.com/questions/3724874/how-can-i-update-a-single-row-in-a-listview
        //http://stackoverflow.com/questions/6740089/android-getting-a-count-of-the-visible-children-in-a-listview
        for (int i = 0; i < dispStations.size(); i++) {
            // Update distance info for the stations loaded into the list
            dispStations.get(i).getDistance(this.location);

            // Update views if they're currently visible
            if (i >= statList.getFirstVisiblePosition() && i <= statList.getLastVisiblePosition()) {
                View v = statList.getChildAt(i - statList.getFirstVisiblePosition());
                TextView dist = null;
                ImageView img = null;
                if (v != null) {
                    dist = (TextView) v.findViewById(R.id.txtDist);
                    img = (ImageView) v.findViewById(R.id.imgBearing);
                }
                if (dist != null) {
                    Utilities.setBearing(dispStations.get(i).bearing, img);
                    dist.setText(String.format("%.1f km", dispStations.get(i).dist));
                }
            }
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {
        if (lm.getBestProvider(crit, true).equals(provider)) {
            this.provider = provider;
            lm.removeUpdates(this);
            locationSetup(crit);
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
        // if current provider gets disabled, use a different one

        if (this.provider.equals(provider)) {
            lm.removeUpdates(this);
            locationSetup(crit);
        }
        Log.i("Location stuff", String.format("Provider set to %s", this.provider));
    }

    // JSON retrieval and parsing in a separate thread
    private class GetJson extends AsyncTask<Void, Void, Void> {
        String err = null;

        @Override
        protected void onPreExecute() {
            statusText.setText(getString(R.string.main_looking));
        }

        // Read the JSON objects from URL and store them in a station list
        @Override
        protected Void doInBackground(Void... params) {
            try {
                stations = Utilities.ParseStations(JsonUrl);
            } catch (Exception e) {
                err = getString(R.string.main_error);
                e.printStackTrace();
            }
            Log.i("AsyncTask", "Running...");
            return null;
        }

        // Update list or display error
        protected void onPostExecute(Void result) {
            Log.i("AsyncTask", "Done");
            if (err != null) statusText.setText(err);
            else updateList();
        }
    }

    // Location handling for pause, resume and stop events
    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        lm.removeUpdates(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        locationSetup(crit);
        Log.i("Location stuff", String.format("Provider set to %s", provider));
    }

    // Gets the best provider, sets up requests and last known location
    // I included criteria in case I wanted to make changes
    // depending on accuracy and power consumption
    private void locationSetup(Criteria c) {
        provider = lm.getBestProvider(c, true);
        lm.requestLocationUpdates(provider, 1000, 2, this);
        Location tmp = lm.getLastKnownLocation(provider);
        if (tmp != null)
            location = new LatLng(tmp.getLatitude(), tmp.getLongitude());
    }
}
