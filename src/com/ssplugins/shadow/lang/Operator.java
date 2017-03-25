package com.ssplugins.shadow.lang;

public class Operator {
	
	public static int plus(int a, int b) {
		return a + b;
	}
	
	public static double plus(double a, double b) {
		return a + b;
	}
	
	public static String plus(String a, String b) {
		return a + b;
	}
	
	public static Object plus(Object a, Object b) {
		if (a instanceof String || b instanceof String) return plus(a.toString(), b.toString());
		else if (a instanceof Number && b instanceof Number) {
			if (a instanceof Double || b instanceof Double) return plus(Double.valueOf(a.toString()), Double.valueOf(b.toString()));
			else return plus(Integer.valueOf(a.toString()), Integer.valueOf(b.toString()));
		}
		else return plus(a.toString(), b.toString());
	}
	
	public static int minus(int a, int b) {
		return a - b;
	}
	
	public static double minus(double a, double b) {
		return a - b;
	}
	
	public static int multiply(int a, int b) {
		return a * b;
	}
	
	public static double multiply(double a, double b) {
		return a * b;
	}
	
	public static int divide(int a, int b) {
		return a / b;
	}
	
	public static double divide(double a, double b) {
		return a / b;
	}
	
	public static boolean equals(Object a, Object b) {
		return a.equals(b);
	}
	
	public static boolean nequals(Object a, Object b) {
		return !equals(a, b);
	}
	
}
