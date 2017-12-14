package com.ssplugins.shadow2;

public final class Debug {
	
	private static boolean enabled = false;
	
	public static void log(String msg) {
		if (enabled) System.out.println(msg);
	}
	
	public static void setEnabled(boolean enabled) {
		Debug.enabled = enabled;
	}
	
}
