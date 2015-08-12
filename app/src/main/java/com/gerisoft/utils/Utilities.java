package com.gerisoft.utils;

import android.util.Log;

import com.gerisoft.bikesharefinder.Station;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class Utilities {
    // Reading JSON text from URL, returning as string
    public static String ReadURL(String urlText) throws IOException {
        URL url = new URL(urlText);
        Scanner s = new Scanner(url.openStream());
        StringBuilder str = new StringBuilder();
        while (s.hasNext()) {
            str.append(s.nextLine());
        }
        Log.i("ReadURL", "Json read");
        return str.toString();
    }

    // Converting JSON string to Station list
    public static ArrayList<Station> ParseStations(String url) throws IOException {
        ArrayList<Station> ret = new ArrayList<>();

        try {
            String json = ReadURL(url);
            JSONObject mainJson = new JSONObject(json);

            JSONArray sArray = mainJson.getJSONArray("stationBeanList");
            for (int i = 0; i < sArray.length(); i++) {
                JSONObject j = sArray.getJSONObject(i);
                Station stat = new Station(
                        j.getInt("landMark"),
                        j.getString("stationName"),
                        new LatLng(j.getDouble("latitude"), j.getDouble("longitude")),
                        j.getInt("availableBikes"),
                        j.getInt("totalDocks"),
                        j.getInt("statusKey"));
                try {
                    String date = j.getString("lastCommunicationTime");
                    stat.setDate(date);
                } catch (ParseException e) {
                    Log.e("JSONParser", e.getMessage());
                }

                ret.add(stat);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
