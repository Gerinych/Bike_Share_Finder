package com.gerisoft.bikesharefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gerisoft.utils.Utilities;

import java.util.List;

// Adapter logic for the listview items
//http://stackoverflow.com/questions/15832335/android-custom-row-item-for-listview
public class StationAdapter extends BaseAdapter {

    Context context;
    List<Station> stats;
    private static LayoutInflater inflater = null;

    // Initialize adapter
    public StationAdapter(Context context, List<Station> stats) {
        this.context = context;
        this.stats = stats;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Get count
    public int getCount() {
        return stats.size();
    }

    // Get station based on position
    public Object getItem(int position) {
        return stats.get(position);
    }

    // Get ID (seems pointless)
    public long getItemId(int position) {
        return position;
    }

    // Fill out the textviews in the list item based on position
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.station_list_item, null);
        TextView tName = (TextView) vi.findViewById(R.id.txtName);
        TextView tDist = (TextView) vi.findViewById(R.id.txtDist);
        TextView tAvail = (TextView) vi.findViewById(R.id.txtAvailable);
        TextView tTotal = (TextView) vi.findViewById(R.id.txtTotal);
        ImageView iDir = (ImageView) vi.findViewById(R.id.imgBearing);

        Station temp = stats.get(position);
        tName.setText(temp.name);
        if (temp.dist >= 0) {
            Utilities.setBearing(temp.bearing, iDir);
            tDist.setText(String.format("%.1fkm", temp.dist));
        }
        tAvail.setText(Integer.toString(temp.avail));
        tTotal.setText("/" + Integer.toString(temp.total));
        return vi;
    }
}
