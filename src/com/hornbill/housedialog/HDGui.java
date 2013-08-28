package com.hornbill.housedialog;

import java.io.InputStream;

import org.jwebsocket.api.WebSocketClientTokenListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;

import com.hornbill.housedialog.R;


@SuppressLint("SetJavaScriptEnabled")
public class HDGui {
	Activity activity;
	HDController controller;
	WebView webview;
	HDWebSocket websocket;
	private String CMD_RELOAD;
	private String CMD_SITE;
	private String CMD_DIALOG;
	
	private Dialog tel_dialog;
	
	private SharedPreferences preferences;
	
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		public void handleMessage(Message msg) {

		String action =  msg.getData().getString("action");
		if (action != null) {
			Log.i("HDGui","Handler called: " + action);
			if (action.startsWith(CMD_RELOAD)) {
				Log.i("HDGui","Reloading");
				webview.reload();
			} else if (action.startsWith(CMD_SITE + ":")) {
				String site = action.substring(CMD_SITE.length()+1);
				Log.i("HDGui","Loading " + site);
				webview.loadUrl(site);
			} else if (action.startsWith(CMD_DIALOG + ":")) {
				String value = action.substring(CMD_DIALOG.length()+1);
				if (value.startsWith("1")) {
					Log.i("HDGui","Showing Call Dialog");
					tel_dialog.show();
				} else {
					Log.i("HDGui","Hiding Call Dialog");
					tel_dialog.hide();
				}
			} else if (action.startsWith("WS:")) {
				webview.loadUrl("javascript:" + action.substring(3));
			} else {
				Log.e("HDGui","Unknown command " + action);
			}
			
		} else {
			Log.e("HDGui","Empty action");
		}
		 
		}
	};
	
	@SuppressWarnings("deprecation")
	public HDGui(Activity activity, HDController controller) {
		this.activity = activity;
		this.controller = controller;
		
		CMD_RELOAD = activity.getString(R.string.CMD_RELOAD);
		CMD_SITE = activity.getString(R.string.CMD_SITE);
		CMD_DIALOG = activity.getString(R.string.CMD_DIALOG);
		
    	activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    	    	
    	tel_dialog = new Dialog(activity);
    	tel_dialog.setContentView(R.layout.tel_dialog);
    	tel_dialog.setTitle(R.string.call_label);
    	
    	webview = new WebView(activity);
    	webview.getSettings().setJavaScriptEnabled(true);
    	webview.setWebChromeClient(new WebChromeClient());
    	webview.setWebViewClient(new WebViewClient() {
    		@Override
    		public void onPageStarted (WebView view, String url, Bitmap favicon) {
    			Log.i("HDGui","onPageStarted: " + url);

    			super.onPageStarted(view,url,favicon);
    			if (url.startsWith("http")) {
//    				view.loadUrl(websocket.jsCode());
    				Log.i("HDGui",websocket.jsCode());
    			}
    		}

    		@Override
    		public void onPageFinished (WebView view, String url) {
        		Log.i("HDGui","onPageFinished: " + url);

        		super.onPageFinished(view,url);
        	}


        });
        websocket = new HDWebSocket(activity,controller);
        websocket.start();
        webview.addJavascriptInterface(websocket, "HDWebSocket");

        activity.setContentView(webview);
        webview.loadUrl(websocket.jsCode());
        webview.loadUrl("javascript:window.location.href=\"http://hausmeister/sh8\";");
//        webview.loadUrl("http://bzo.bosch.com/");
		
	}
	
	public void onCommand(String command) {
		Message msg = handler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putString("action", command);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}
	
	class HDWebResourceResponse extends WebResourceResponse {
		public HDWebResourceResponse(String mimeType, String encoding, InputStream data) {

			super(mimeType, encoding, data);
		}

		public InputStream getData() {
			Log.i("HDGui","WRR.getData()");
			return super.getData();
		}
		
		public void setData(InputStream data) {
			Log.i("HDGui","WRR.setData()");
			super.setData(data);
		}


		
		class HDWRInputStream extends InputStream {
			InputStream origStream;
			String prepend;
			
			public HDWRInputStream(InputStream origStream, String prepend) {
				this.origStream = origStream;
				this.prepend = prepend;
			}
			
			public int read(byte[] data) {
				Log.i("HDGui","InputStream.read(byte[])");
				return -1;
			}
			
			public int read() {
				Log.i("HDGui","InputStream.read()");
				return -1;
			}
		}
	}

}
