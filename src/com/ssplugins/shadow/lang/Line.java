package com.ssplugins.shadow.lang;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Line extends ShadowComponent {
	
	private String line;
	private String keyword;
	private String[] args;
	private int lineNum = 0;
	
	private Method method;
	
	Line(Shadow shadow, String line, int lineNum) {
		super(shadow);
		line = line.trim();
		this.line = line;
		String[] t = ShadowUtil.getParts(line);
		keyword = t[0];
		args = Arrays.copyOfRange(t, 1, t.length);
		this.lineNum = lineNum;
	}
	
	public String getLine() {
		return line;
	}
	
	public String keyword() {
		return keyword;
	}
	
	public String[] getArguments() {
		return Arrays.copyOf(args, args.length);
	}
	
	public int getLineNum() {
		return lineNum;
	}
	
	void setMethod(Method method) {
		this.method = method;
	}
	
	boolean methodStored() {
		return method != null;
	}
	
	Method getMethod() {
		return method;
	}
}
