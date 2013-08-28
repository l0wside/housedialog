package com.hornbill.housedialog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;

public class HDMain extends Activity {
	
	private HDGui gui;
	private HDControl controltask;
	private HDNetwork networktask;
	private HDSip siptask;	
	private Intent preferenceIntent = null;
	private SharedPreferences preferences;
	private boolean initialStart = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		controltask = new HDControl();
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		logNetworkAddresses();
		
		Log.i("HDMain","Starting network task");
		try {
			networktask = new HDNetwork(this,controltask);
			networktask.start();
		} catch (IOException e) {
			Log.e("HDControl","Terminating due to network error");
		}
		

		boolean use_sip = preferences.getBoolean("pref_key_sip_enable", false);
		Log.i("HDMain","Starting SIP task, value from preferences: " + Boolean.toString(use_sip));
		if (use_sip) {
			try {
				siptask = new HDSip(this,controltask);
				/*			siptask.start(); */
				}  catch (Exception e) {

			Log.e("HDControl","SIP error " + e.toString()); 
			use_sip = false;
			} finally {}
		}

    	Log.i("HDMain","Starting GUI");
        super.onCreate(savedInstanceState);
        gui = new HDGui(this,controltask);

    	Log.i("HDMain","Starting control task");
    	controltask.setInterfaces(gui,networktask,siptask,this); 
		controltask.start();
    
    }
    
    @Override
    public void onResume() {
    	Log.i("HDMain","Resume");
    	try {
    		if (!initialStart) {
    			if (preferences.getString("pref_key_gui_reloadonwake", "").equals(getString(R.string.pref_gui_reloadonwake_mostrecent))) {
    				controltask.onNetworkCmd(getString(R.string.CMD_RELOAD));
    			} else if (preferences.getString("pref_key_gui_reloadonwake", "").equals(getString(R.string.pref_gui_reloadonwake_default))) {
    				controltask.onNetworkCmd(getString(R.string.CMD_SITE) + ":" + preferences.getString("pref_key_gui_defaultsite",""));
    			}
    			initialStart = false;
    		}
    	} catch (InterruptedException e) {
    		Log.e("HDMain","Could not dispatch network command");
    	}
    	super.onResume();
    } 
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.i("HDMain","Menu " + menu.toString());
    	
    	if (preferenceIntent == null) {
        	Log.i("HDMain","Initializing preferenceIntent");
    		preferenceIntent = new Intent(this,HDPreferenceActivity.class);
    	}
    	Log.i("HDMain","preferenceIntent: " + preferenceIntent.toString());
    	startActivity(preferenceIntent);
    	return true;
    }
    
    void logNetworkAddresses() {
    	try {
    		List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
    		for (NetworkInterface intf : interfaces) {
    			List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
    			for (InetAddress addr : addrs) {
    				String sAddr = addr.getHostAddress().toUpperCase();
    				Log.i("HDMain","IP address: " + sAddr);
    			}
    		}
    	} catch (Exception ex) { } // for now eat exceptions

    }
}