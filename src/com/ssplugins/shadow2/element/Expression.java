package com.ssplugins.shadow2.element;

public class Expression extends ShadowSection {
	
	private ShadowSection left;
	private String operator;
	private ShadowSection right;
	
	public Expression(ShadowSection left, String operator, ShadowSection right) {
		this.left = left;
		this.operator = operator;
		this.right = right;
	}
	
	public ShadowSection getLeft() {
		return left;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public ShadowSection getRight() {
		return right;
	}
	
	@Override
	public String toString() {
		return left.toString() + " " + operator + " " + right.toString();
	}
	
}
