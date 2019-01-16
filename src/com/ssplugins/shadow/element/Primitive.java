package com.ssplugins.shadow.element;

import java.util.Optional;

public class Primitive extends ShadowSection {
	
	private Object value;
	private Type type;
	
	private Primitive() {}
	
	public Primitive(String value) {
		try {
			type = Type.NUMBER;
			if (value.contains(".")) this.value = Double.parseDouble(value);
			else this.value = Integer.parseInt(value);
			return;
		} catch (NumberFormatException ignored) {}
		type = Type.BOOLEAN;
		if (value.equalsIgnoreCase("true")) {
			this.value = true;
			return;
		}
		else if (value.equalsIgnoreCase("false")) {
			this.value = false;
			return;
		}
		else if (value.equalsIgnoreCase("null")) {
			this.value = null;
			type = Type.NULL;
			return;
		}
		this.value = value;
		type = Type.STRING;
	}
	
	public static Primitive string(String s) {
		Primitive p = new Primitive();
		p.value = s;
		p.type = Type.STRING;
		return p;
	}
    
    public static Primitive integer(int i) {
        Primitive p = new Primitive();
        p.value = i;
        p.type = Type.NUMBER;
        return p;
    }
    
    public static Primitive decimal(double d) {
        Primitive p = new Primitive();
        p.value = d;
        p.type = Type.NUMBER;
        return p;
    }
	
	public boolean isString() {
		return type == Type.STRING;
	}
	
	public boolean isBoolean() {
		return type == Type.BOOLEAN;
	}
	
	public boolean isNumber() {
		return type == Type.NUMBER;
	}
	
	public boolean isInt() {
        return value instanceof Integer;
    }
    
    public boolean isDouble() {
        return value instanceof Double;
    }
	
	public boolean isNull() {
		return type ==Type.NULL;
	}
	
	public Object get() {
		return value;
	}
	
	public String asString() {
		return value == null ? "null" : value.toString();
	}
	
	public Optional<Integer> asInt() {
		if (isNumber()) return Optional.of((int) value);
		return Optional.empty();
	}
	
	public Optional<Double> asDouble() {
		if (isNumber()) return Optional.of((double) value);
		return Optional.empty();
	}
	
	public Optional<Boolean> asBoolean() {
		if (isBoolean()) return Optional.of((boolean) value);
		return Optional.empty();
	}
	
	@Override
	public String toString() {
		return asString();
	}
	
	private enum Type {
		STRING,
		NUMBER,
		BOOLEAN,
		NULL;
	}
	
}
