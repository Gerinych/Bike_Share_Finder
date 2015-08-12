package com.gerisoft.bikesharefinder;

import android.os.Bundle;

// This is interface is used to interact with results sent by a dialog
// This is meant to be inherited only by Activities
// Eg. in dialog's positive button OnClick event
// DialogResult result = (DialogResult)getActivity();

public interface DialogResult {
    void getResult(Bundle savedInstanceState);
}
