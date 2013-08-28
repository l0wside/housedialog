package com.hornbill.housedialog;
import com.hornbill.housedialog.HDController;
import java.security.AccessControlException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.sip.*;
import android.preference.PreferenceManager;
import android.util.Log;

public class HDSip extends BroadcastReceiver {
	private Activity activity;
	private HDController controller;

	private String domain;
	private String username;
	private String password; 
	
	private SipManager sipManager = null;
	private SipProfile sipProfile = null;
	private Intent sipIntent = null;
	
	private SharedPreferences preferences;
	
	public void onReceive(Context c,Intent i) {
		
	}

	HDSip(Activity activity, HDController controller) throws AccessControlException {
		this.activity = activity;
		this.controller = controller;
		
/*		preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		domain = preferences.getString("pref_key_sip_domain", "");
		username = preferences.getString("pref_key_sip_user", "");
		password = preferences.getString("pref_key_sip_password", "");
		
		Log.i("HDSip","Init: domain " + domain + ", username: " + username + ", pwd: " + password);
		
		/* Initialize profile (i.e. account information) */
/*		try {
			SipProfile.Builder builder = new SipProfile.Builder(username, domain);
			builder.setPassword(password);
			sipProfile = builder.build();
		} catch(Exception e) {
			throw(new AccessControlException("Cannot build SIP profile"));
		}
		
		sipManager = SipManager.newInstance(activity);
		
		/* Register as the handler for an incoming SIP call */
/*		sipIntent = new Intent();
		sipIntent.setAction("com.hornbill.housedialog.INCOMING_CALL");
//		PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, this, 0, sipIntent, Intent.FILL_IN_DATA);
		sipManager.open(sipProfile, pendingIntent, null);
*/
	}
	
	void reqAccept() {
		
	}
	
	void reqHangup() {
		
	}
	
	void reqReject() {
		
	}
	
	void reqSendCode() {
		
	}
}
