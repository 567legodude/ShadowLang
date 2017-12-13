package com.ssplugins.shadow2.common;

public class Range {
	
	private int lower;
	private int upper;
	private Type type;
	
	private Range(int lower, int upper) {
		this.lower = lower;
		this.upper = upper;
		type = Type.DEFAULT;
	}
	
	private Range type(Type type) {
		this.type = type;
		return this;
	}
	
	public static Range from(int lower, int upper) {
		if (lower > upper) throw new IllegalArgumentException("Lower bound is greater than upper bound.");
		return new Range(lower, upper);
	}
	
	public static Range single(int number) {
		return new Range(number, 0).type(Type.SINGLE);
	}
	
	public static Range outside(int lower, int upper) {
		return from(lower, upper).type(Type.OUTSIDE);
	}
	
	public static Range any() {
		return new Range(0, 0).type(Type.ANY);
	}
	
	public int getLowerBound() {
		return lower;
	}
	
	public int getUpperBound() {
		return upper;
	}
	
	public boolean inRange(int value) {
		if (type == Type.ANY) return true;
		if (type == Type.SINGLE) return value == lower;
		if (type == Type.OUTSIDE) {
			return value < lower || value > upper;
		}
		else return lower <= value && value <= upper;
	}
	
	public boolean outsideRange(int value) {
		return !inRange(value);
	}
	
	private enum Type {
		DEFAULT,
		SINGLE,
		OUTSIDE,
		ANY
	}
	
	@Override
	public String toString() {
		if (type == Type.ANY) return "any";
		if (type == Type.SINGLE) return String.valueOf(lower);
		if (type == Type.OUTSIDE) return lower + ">n>" + upper;
		else return lower + "-" + upper;
	}
}
