package com.gerisoft.bikesharefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class FilterDialog extends DialogFragment {
    int radius;
    boolean sort;
    Context context;
    View vi = null;

    SeekBar sbDist;
    RadioButton rdDist;
    RadioButton rdBike;
    TextView txtKm;

    @Override
    public void setArguments(Bundle savedInstanceState) {
        radius = savedInstanceState.getInt("radius", 10);
        sort = savedInstanceState.getBoolean("sort", true);
    }

    private void updateUI() {
        sbDist.setProgress(this.radius - 1);
        txtKm.setText(this.radius + " km");
        if (sort) rdDist.setChecked(true);
        else rdBike.setChecked(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        vi = inflater.inflate(R.layout.dialog_filter, null);

        //finding elements
        sbDist = (SeekBar)vi.findViewById(R.id.sbDist);
        rdDist = (RadioButton)vi.findViewById(R.id.rdDist);
        rdBike = (RadioButton)vi.findViewById(R.id.rdBike);
        txtKm = (TextView)vi.findViewById(R.id.txtKm);

        //listener registration
        View.OnClickListener radioClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == rdDist) { rdBike.setChecked(false); sort = true; }
                else { rdDist.setChecked(false); sort = false; }
            }
        };
        rdDist.setOnClickListener(radioClick);
        rdBike.setOnClickListener(radioClick);

        sbDist.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtKm.setText(progress + 1 + " km");
                radius = progress + 1;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //building view
        builder.setView(vi)
                .setPositiveButton(getString(R.string.filter_okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DialogResult act = (DialogResult)getActivity();
                        Bundle b = new Bundle();
                        b.putInt("radius", radius);
                        b.putBoolean("sort", sort);
                        act.getResult(b);
                    }
                })
                .setNegativeButton(getString(R.string.filter_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FilterDialog.this.getDialog().cancel();
                    }
                });

        //setting elements to display current settings
        updateUI();

        return builder.create();
    }
}
