package com.ssplugins.shadow2.def;

import com.ssplugins.shadow2.Scope;
import com.ssplugins.shadow2.ShadowTools;
import com.ssplugins.shadow2.element.Reference;
import com.ssplugins.shadow2.element.ShadowSection;
import com.ssplugins.shadow2.exceptions.ShadowExecutionException;

public final class Expressions {
	
	public static ShadowSection add(ShadowSection left, ShadowSection right, Scope scope) {
		Object l = ShadowTools.asObject(left, scope);
		Object r = ShadowTools.asObject(right, scope);
		return addIntern(l, r);
	}
	
	private static Reference addIntern(Object left, Object right) {
		if (left == null) left = "null";
		if (right == null) right = "null";
		if (left instanceof Number && right instanceof Number) {
			Number l = (Number) left;
			Number r = (Number) right;
			if (left instanceof Double || right instanceof Double) return new Reference(l.doubleValue() + r.doubleValue());
			else return new Reference(l.intValue() + r.intValue());
		}
		else return new Reference(left.toString() + right.toString());
	}
	
	public static ShadowSection subtract(ShadowSection left, ShadowSection right, Scope scope) {
		Object l = ShadowTools.asObject(left, scope);
		Object r = ShadowTools.asObject(right, scope);
		return subtractIntern(l, r);
	}
	
	private static Reference subtractIntern(Object left, Object right) {
		if (left instanceof Number && right instanceof Number) {
			Number l = (Number) left;
			Number r = (Number) right;
			if (left instanceof Double || right instanceof Double) return new Reference(l.doubleValue() - r.doubleValue());
			else return new Reference(l.intValue() - r.intValue());
		}
		else throw new ShadowExecutionException("Invalid operands for - operator.");
	}
	
}
