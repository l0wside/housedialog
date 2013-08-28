package com.hornbill.housedialog;

public interface HDController {
	public final int UI_TEL_INCOMING = 0;
	public final int UI_TEL_RINGING = 1;
	public final int UI_TEL_ACCEPTED = 2;
	public final int UI_TEL_REJECTED = 3;
	public final int UI_TEL_TERMINATED = 4;
	public final int UI_TEL_DOOROPEN = 5;
	
	public void onUISiteChanged(String site) throws InterruptedException;
	public void onUITelEvent(String event) throws InterruptedException;
	public void onNetworkCmd(String packet) throws InterruptedException;
	public void onSIPEvent(String event) throws InterruptedException;
	public void onWebSocketEvent(String event) throws InterruptedException;
}
