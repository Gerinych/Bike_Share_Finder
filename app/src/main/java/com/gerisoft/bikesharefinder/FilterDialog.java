package com.gerisoft.bikesharefinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

// Used to handle the search options dialog
// http://developer.android.com/guide/topics/ui/dialogs.html#CustomLayout
public class FilterDialog extends DialogFragment {
    double radius;
    boolean sort;
    View vi = null;

    SeekBar sbDist;
    RadioButton rdDist;
    RadioButton rdBike;
    TextView txtKm;

    // Get current settings from the main activity
    @Override
    public void setArguments(Bundle savedInstanceState) {
        radius = savedInstanceState.getDouble("radius", 5);
        sort = savedInstanceState.getBoolean("sort", true);
    }

    // Update the dialog UI based on current settings
    private void updateUI() {
        int rad = (int)Math.round(this.radius * 10) - 1;
        Log.i("rad", String.valueOf(rad));
        sbDist.setProgress(rad);

        if (rad < 100) txtKm.setText(String.format("%.1fkm", this.radius));
        else txtKm.setText(getString(R.string.filter_showall));

        if (sort) rdDist.setChecked(true);
        else rdBike.setChecked(true);
    }

    // Create dialog, assign layout, find views, set listeners, update UI
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                else { rdDist.setChecked(false); sort = false;
                }
            }
        };
        rdDist.setOnClickListener(radioClick);
        rdBike.setOnClickListener(radioClick);

        sbDist.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double rad = (double)(progress + 1) / 10;
                if (progress < 100) txtKm.setText(String.format("%.1fkm", rad));
                else txtKm.setText(getString(R.string.filter_showall));
                radius = rad;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //building view
        builder.setView(vi)
                // Pass settings to main using DialogResult.getResult()
                .setPositiveButton(getString(R.string.filter_okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DialogResult act = (DialogResult)getActivity();
                        Bundle b = new Bundle();
                        b.putDouble("radius", radius);
                        b.putBoolean("sort", sort);
                        act.getResult(b);
                    }
                })
                        // Dismiss dialog
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
