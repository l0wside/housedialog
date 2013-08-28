package com.hornbill.housedialog;

import java.io.*;
import java.net.*;

import com.hornbill.housedialog.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/* Handles all incoming and outgoing UDP data (except telephony).
 * Runs in a separate thread. Incoming data is forwarded to HDController, while
 * outgoing data is sent to the address specified in XXX. 
 */
public class HDNetwork extends Thread {
	private int udp_localport;
	private HDController controller; 
	private Activity activity;

	private DatagramSocket socket_in;
	byte[] buf = new byte[2048];
	DatagramPacket packet;
	
	private SharedPreferences preferences;

	/* Constructor. Creates the UDP socket to wait on. */
	HDNetwork(Activity activity, HDController controller) throws IOException {
		this.controller = controller;
		this.activity = activity;
		
		udp_localport = activity.getResources().getInteger(R.integer.udp_localport);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		
    	packet = new DatagramPacket(buf, buf.length);
        try {
            Log.i("HDNetwork", "Constructor: Creating socket on port " + Integer.toString(udp_localport));
            socket_in = new DatagramSocket(udp_localport);
            socket_in.setSoTimeout(0); 
            socket_in.setBroadcast(true);
            socket_in.setReuseAddress(true);            
        } catch (IOException e) {
            Log.e("HDNetwork", "Constructor failed: " + e.toString());
        	throw(e);
        }
        Log.i("HDNetwork", "Constructor: Socket created on port " + Integer.toString(socket_in.getLocalPort()));
	}
	
	/* Network thread. Waits for incoming UDP messages and sends them to HDControl. */
	@Override
    public void run() { //This runs on a different thread
        try {
            while (true) {
            	Log.i("HDNetwork", "Waiting for UDP packet on port " + Integer.toString(socket_in.getLocalPort()));
            	packet.setLength(buf.length);
            	socket_in.receive(packet);
            	String content = new String(packet.getData(),0,packet.getLength());
            	Log.i("HDNetwork","UDP packet received: " + content);
            	controller.onNetworkCmd(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("HDNetwork", "run(): IOException");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("HDNetwork", "run(): Exception");
        } finally {
            Log.i("HDNetwork", "run(): Terminating");
        	socket_in.close();
        	try {
        		controller.onNetworkCmd(activity.getString(R.string.INT_NETWORKERROR));
        	} catch (InterruptedException e) {
        		Log.e("HDNetwork","Cannot put data into command queue");
        		e.printStackTrace();
        	}
        }
    }
	
	public void onStatusChanged(String status) {
		Log.i("HDNetwork","Transmitting new status " + status);
	}

}
