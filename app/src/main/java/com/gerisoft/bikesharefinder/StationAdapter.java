package com.gerisoft.bikesharefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

//Source:
//http://stackoverflow.com/questions/15832335/android-custom-row-item-for-listview
public class StationAdapter extends BaseAdapter {

    Context context;
    List<Station> stats;
    private static LayoutInflater inflater = null;

    public StationAdapter(Context context, List<Station> stats) {
        this.context = context;
        this.stats = stats;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return stats.size();
    }

    public Object getItem(int position) {
        return stats.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.station_list_item, null);
        TextView tName = (TextView) vi.findViewById(R.id.txtName);
        TextView tDist = (TextView) vi.findViewById(R.id.txtDist);
        TextView tAvail = (TextView) vi.findViewById(R.id.txtAvailable);
        TextView tTotal = (TextView) vi.findViewById(R.id.txtTotal);

        Station temp = stats.get(position);
        tName.setText(temp.name);
        tDist.setText(String.format("%.1f km", temp.dist));
        tAvail.setText(Integer.toString(temp.avail));
        tTotal.setText("/" + Integer.toString(temp.total));
        return vi;
    }
}
