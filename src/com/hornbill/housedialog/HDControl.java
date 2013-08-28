package com.hornbill.housedialog;

import java.util.concurrent.LinkedBlockingQueue;
import com.hornbill.housedialog.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

/* Any incoming command from the other modules (GUI, network, SIP) is handled here.
 * The other modules put their requests/state changes into cmdQueue.
 * run() processes them in a separate thread.
 * 
 * All requests are strings (for the sake of simplicity). The first three letters 
 * designate the receiver (NWK for network, SIP for telephony, GUI for GUI and 
 * SYS for system, i.e. processed by the HDController itself).
 */

public class HDControl extends Thread implements HDController {
	private final LinkedBlockingQueue<String> cmdQueue = new LinkedBlockingQueue<String>();
	private HDGui gui;
	private HDNetwork network;
	private HDSip sip;
	private String CMD_ONOFF;
	private String CMD_RELOAD;
	private String CMD_SITE;
	private SharedPreferences preferences;
	private PowerManager powermanager;
	private PowerManager.WakeLock wakelock;
	
	/* HDControl contains references to all other modules, which in turn contain references to the HDControl
	 * instance. See HDMain for details. 
	 */
	
	@SuppressWarnings("deprecation")
	public void setInterfaces(HDGui gui, HDNetwork network, HDSip sip, Activity activity) {
		this.gui = gui; 
		this.network = network;
		this.sip = sip;
		preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		powermanager = (PowerManager)activity.getSystemService(Context.POWER_SERVICE);
		wakelock = powermanager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"");

		CMD_ONOFF = activity.getString(R.string.CMD_ONOFF);
		CMD_RELOAD = activity.getString(R.string.CMD_RELOAD);
		CMD_SITE = activity.getString(R.string.CMD_SITE);
	}
	
	
	
    public void run() {
    	String nextCmd, destination, content;
    	try {
    		while (true) {
    			Log.i("HDControl","Waiting");
    			nextCmd = cmdQueue.take(); /* Blocking */
    			Log.i("HDCOntrol","Got data");
    			destination = nextCmd.substring(0,3);
    			content = nextCmd.substring(4);
    			
    			
    			if (destination.contentEquals("NWK")) {
    				Log.i("HDControl","Processing NWK (" + content + ")");
    				network.onStatusChanged(content);
    			} else if (destination.contentEquals("SIP")) {
    				Log.i("HDControl","Processing SIP (" + content + ")");
    				
    			} else if (destination.contentEquals("GUI")) {
    				Log.i("HDControl","Processing GUI (" + content + ")");
    				gui.onCommand(content);
    				
    			} else if (destination.contentEquals("SYS")) {
    				Log.i("HDControl","Processing SYS (" + content + ")");
    				onSystemCommand(content);
    			} else {
    				Log.e("HDControl","Unknown destination " + destination);
    			}
    			Log.i("HDControl","returning");
    		}
    	} catch (Exception E) {
    		Log.e("HDControl",E.getMessage());
    	}
    }
    
    /* Interface implementations, running in other threads.
     * Incoming requests are checked for the proper receiver, prepended
     * with the proper code (see comment above) and put into cmdQueue */
	public void onUISiteChanged(String site) throws InterruptedException {
		cmdQueue.put("NWK:SITE:"+site);
	}

	public void onUITelEvent(String event) throws InterruptedException {
		Log.i("HDControl","UI Telephony Event: " + event);
		
		cmdQueue.put("SIP:"+event);
	}

	public void onNetworkCmd(String packet) throws InterruptedException {
		Log.i("HDControl","onNetworkCmd: " + packet);
		if (packet.startsWith(CMD_ONOFF)) {
			cmdQueue.put("SYS:" + packet);
		} else if (packet.startsWith(CMD_RELOAD)) {
			cmdQueue.put("GUI:" + packet);
		} else if (packet.startsWith(CMD_SITE)) {
			cmdQueue.put("GUI:" + packet);
		} else {
/* Preliminary, remove in release */			
cmdQueue.put("GUI:" + packet);
			Log.e("HDControl","Unknown network command " + packet);
		}
		Log.i("HDControl","Elements in queue now " + Integer.toString(cmdQueue.size()));
	}

	public void onSIPEvent(String event) throws InterruptedException {
		Log.i("HDControl","SIP Event " + event);
	}
	
	public void onSystemCommand(String command) {
		if (command.startsWith(CMD_ONOFF)) {
			String value = command.substring(CMD_ONOFF.length()+1);
			if (value.startsWith("1")) {
				Log.i("HDGui","Waking up");
				wakelock.acquire();
			} else {
				Log.i("HDGui","Prepare to sleep");
				wakelock.release();
			}		
		}
	}
	
	public void onWebSocketEvent(String event) throws InterruptedException {
		cmdQueue.put("GUI:WS:" + event);
	}
}