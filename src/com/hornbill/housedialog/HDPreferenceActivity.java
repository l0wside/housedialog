package com.hornbill.housedialog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class HDPreferenceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i("HDPreference","onCreate()");
        super.onCreate(savedInstanceState);
        
    	Log.i("HDPreference","setContentView()");
        setContentView(R.layout.activity_main);

        // Display the fragment as the main content.
    	Log.i("HDPreference","getFragmentManager()");
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new HDPreferenceFragment())
                .commit();
    	Log.i("HDPreference","getFragmentManager() done");
        
    }

}
