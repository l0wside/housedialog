package com.hornbill.housedialog;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class HDPreferenceFragment extends PreferenceFragment {
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.xml.preferences);
 
    }


}
