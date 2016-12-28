package com.ssplugins.shadow.lang;

public class Debugger {
	
	public static boolean enabled = false;
	
	public static void log(String msg) {
		if (!enabled) return;
		System.out.println(msg);
	}
	
	public static void setEnabled(boolean enabled) {
		Debugger.enabled = enabled;
	}
}
