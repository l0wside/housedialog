package com.hornbill.housedialog;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class HDWebSocket extends Thread {
	Activity activity;
	HDController controller;
	
	static Map<String, SocketChannel> sockets = new HashMap<String, SocketChannel>();
	static Selector socketSelector = null;
	static SocketChannel addingSocketChannel = null;
	static String addingHandler = null;

	String jsCode_ = "";
	
	@Override
	public void run() {
		while (true) {
			if (socketSelector == null) {
				try {
					socketSelector = Selector.open();
				} catch (IOException e) {
					Log.e("JS","IOException upon selector open()");
					return;
				}
			}
			try {
				socketSelector.select();
			} catch (IOException e) {
				Log.e("JS","IOException upon select()");
			} catch (ClosedSelectorException e) {
				Log.e("JS","select(): Selector closed");
				return;
			}
			
			if (addingSocketChannel != null) {
				try {
					Log.i("JS","Adding socket channel");
					addingSocketChannel.register(socketSelector,SelectionKey.OP_READ);
					sockets.put(addingHandler,addingSocketChannel);
					controller.onWebSocketEvent("WebSocket._onEvent(\"" + addingHandler + "\",\"open\",\"\")");
				} catch (ClosedChannelException e) {
					Log.e("JS","Trying to add closed channel to select set"); 
				} catch (InterruptedException e) { 
					Log.e("JS",e.getMessage());
				} finally {
					addingSocketChannel = null;
				}
			}
			
			for (SelectionKey key : socketSelector.selectedKeys()) {
				SocketChannel s = (SocketChannel)key.channel();
				
			}
		}
	}

	public HDWebSocket(Activity activity, HDController controller) {
		this.activity = activity;
		this.controller = controller;
		
		/* Read JavaScript code from assets */
		try {
			InputStream assetStream = activity.getAssets().open(activity.getString(R.string.websocket_filename));
			byte chunk[] = new byte[1024];
			int len;
			while ((len = assetStream.read(chunk)) > 0) {
				String line = new String(chunk);
				line = line.substring(0,len);
				jsCode_ += line;
			}
			assetStream.close();
		} catch (IOException e) {
			Log.e("JS","Cannot read WebSocket JS code");
			jsCode_ = "";
		}
		jsCode_.replace("\n", " ");
		jsCode_.replace("\r", "");
	}
	
	public String jsCode() {
		return "javascript:" + jsCode_.replace('\n', ' ').replace('\r',' ');
	}
	
	@JavascriptInterface
	public String getHandler(String uri, String protocols) {
		String proto = null, path = null, port_s = null, server_s = null;
		String[] uri_elem;
		Log.i("JS","getHandler(" + uri + ")");
		/* Split URI; one single regexp did not work */
		/* Protocol */
		uri_elem = uri.split("://");
		if (uri_elem.length == 0) {
			Log.e("JS","Could not split URI");
			return "";
		}
		proto = uri_elem[0];
		uri = uri_elem[1];
		
		/* Path */
		uri_elem = (uri+"@").split("/");
		if (uri_elem.length < 2) {
			path = "/";
		} else {
			path = uri.substring(uri_elem[0].length());
			uri = uri_elem[0];
		}
		
		/* Port */
		uri_elem = uri.split(":");
		if (uri_elem.length < 2) {
			port_s = "";
		} else {
			uri = uri_elem[0];
			port_s = uri_elem[1];
		}
		
		/* Remaining part is server */
		server_s = uri;
		
		Log.i("JS",proto + "|" + server_s + "|" + port_s + "|" + path);
		
		
		
		/* Generate handler string */
		String handler = UUID.randomUUID().toString();
		
		/* Connect to server */
		int port;
		if (port_s.equals("")) {
			port = R.integer.ws_defaultport;
		} else {
			try {
				port = Integer.parseInt(port_s);
			} catch (NumberFormatException e) {
				Log.e("JS","Cannot parse WS port number " + port_s);
				return "";
			}
		}
		
		InetAddress server;
		try {
			server = InetAddress.getByName(server_s);
		} catch (UnknownHostException e) {
			Log.e("JS","Cannot resolve host " + server_s);
			return "";
		}
		
		try {
			addingSocketChannel = SocketChannel.open();
			addingSocketChannel.connect(new InetSocketAddress(server,port));
			addingSocketChannel.configureBlocking(false);
			addingHandler = handler;
		} catch (ClosedChannelException e) {
			Log.e("JS","Cannot register socket");
			return "";
		} catch (IOException e) {
			Log.e("JS","Cannot connect to " + server + ":" + Integer.toString(port));
			return "";
		}
		
		socketSelector.wakeup();
			
		
		Log.i("JS","Returning handler");
		
		return handler;
	}
	
	@JavascriptInterface
	public void log(String text) {
		Log.i("JS",text);
	}

	@JavascriptInterface
	void send(String arg) {
		Log.i("JS","send " + arg);
	}
}
