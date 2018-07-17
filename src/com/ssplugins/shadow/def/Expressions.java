package com.ssplugins.shadow.def;

import com.ssplugins.shadow.Scope;
import com.ssplugins.shadow.ShadowTools;
import com.ssplugins.shadow.element.Reference;
import com.ssplugins.shadow.element.ShadowSection;
import com.ssplugins.shadow.exceptions.ShadowException;
import com.ssplugins.shadow.exceptions.ShadowExecutionException;

import java.util.function.BiFunction;

public final class Expressions {
	
	private static ShadowSection mathOp(ShadowSection left, ShadowSection right, Scope scope, BiFunction<Integer, Integer, Integer> ints, BiFunction<Double, Double, Double> doubles) {
		Object l = ShadowTools.asObject(left, scope).orElseThrow(ShadowException.sectionConvert());
		Object r = ShadowTools.asObject(right, scope).orElseThrow(ShadowException.sectionConvert());
		if (l instanceof Number && r instanceof Number) {
			Number ln = (Number) l;
			Number rn = (Number) r;
			if (ln instanceof Double || rn instanceof Double) return new Reference(doubles.apply(ln.doubleValue(), rn.doubleValue()));
			else return new Reference(ints.apply(ln.intValue(), rn.intValue()));
		}
		throw new ShadowExecutionException("Invalid operands. Numbers only.");
	}
	
	private static ShadowSection compareOp(ShadowSection left, ShadowSection right, Scope scope, BiFunction<Integer, Integer, Boolean> ints, BiFunction<Double, Double, Boolean> doubles) {
		Object l = ShadowTools.asObject(left, scope).orElseThrow(ShadowException.sectionConvert());
		Object r = ShadowTools.asObject(right, scope).orElseThrow(ShadowException.sectionConvert());
		if (l instanceof Number && r instanceof Number) {
			Number ln = (Number) l;
			Number rn = (Number) r;
			if (ln instanceof Double || rn instanceof Double) return new Reference(doubles.apply(ln.doubleValue(), rn.doubleValue()));
			else return new Reference(ints.apply(ln.intValue(), rn.intValue()));
		}
		throw new ShadowExecutionException("Invalid operands. Numbers only.");
	}
	
	public static ShadowSection add(ShadowSection left, ShadowSection right, Scope scope) {
		Object l = ShadowTools.asObject(left, scope).orElseThrow(ShadowException.sectionConvert());
		Object r = ShadowTools.asObject(right, scope).orElseThrow(ShadowException.sectionConvert());
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
		return new Reference(left.toString() + right.toString());
	}
	
	public static ShadowSection subtract(ShadowSection left, ShadowSection right, Scope scope) {
		return mathOp(left, right, scope, (integer, integer2) -> integer - integer2, (aDouble, aDouble2) -> aDouble - aDouble2);
	}
	
	public static ShadowSection multiply(ShadowSection left, ShadowSection right, Scope scope) {
		return mathOp(left, right, scope, (integer, integer2) -> integer * integer2, (aDouble, aDouble2) -> aDouble * aDouble2);
	}
	
	public static ShadowSection divide(ShadowSection left, ShadowSection right, Scope scope) {
		return mathOp(left, right, scope, (integer, integer2) -> integer / integer2, (aDouble, aDouble2) -> aDouble / aDouble2);
	}
	
	public static ShadowSection mod(ShadowSection left, ShadowSection right, Scope scope) {
		return mathOp(left, right, scope, (integer, integer2) -> integer % integer2, (aDouble, aDouble2) -> aDouble % aDouble2);
	}
	
	public static ShadowSection lessThan(ShadowSection left, ShadowSection right, Scope scope) {
		return compareOp(left, right, scope, (integer, integer2) -> integer < integer2, (aDouble, aDouble2) -> aDouble < aDouble2);
	}
	
	public static ShadowSection lessThanEqual(ShadowSection left, ShadowSection right, Scope scope) {
		return compareOp(left, right, scope, (integer, integer2) -> integer <= integer2, (aDouble, aDouble2) -> aDouble <= aDouble2);
	}
	
	public static ShadowSection greaterThan(ShadowSection left, ShadowSection right, Scope scope) {
		return compareOp(left, right, scope, (integer, integer2) -> integer > integer2, (aDouble, aDouble2) -> aDouble > aDouble2);
	}
	
	public static ShadowSection greaterThanEqual(ShadowSection left, ShadowSection right, Scope scope) {
		return compareOp(left, right, scope, (integer, integer2) -> integer >= integer2, (aDouble, aDouble2) -> aDouble >= aDouble2);
	}
	
	public static ShadowSection equals(ShadowSection left, ShadowSection right, Scope scope) {
		Object l = ShadowTools.asObject(left, scope).orElseThrow(ShadowException.sectionConvert());
		Object r = ShadowTools.asObject(right, scope).orElseThrow(ShadowException.sectionConvert());
		if (l instanceof String || r instanceof String) {
			return new Reference(l.equals(r));
		}
		return new Reference(l == r);
	}
	
	public static ShadowSection notEquals(ShadowSection left, ShadowSection right, Scope scope) {
		Object l = ShadowTools.asObject(left, scope).orElseThrow(ShadowException.sectionConvert());
		Object r = ShadowTools.asObject(right, scope).orElseThrow(ShadowException.sectionConvert());
		if (l instanceof String || r instanceof String) {
			return new Reference(!l.equals(r));
		}
		return new Reference(l != r);
	}
	
	public static ShadowSection and(ShadowSection left, ShadowSection right, Scope scope) {
		Object l = ShadowTools.asObject(left, scope).orElseThrow(ShadowException.sectionConvert());
		Object r = ShadowTools.asObject(right, scope).orElseThrow(ShadowException.sectionConvert());
		if (l instanceof Boolean && r instanceof Boolean) {
			return new Reference((Boolean) l && (Boolean) r);
		}
		throw new ShadowExecutionException("Invalid operands. Booleans only.");
	}
	
	public static ShadowSection or(ShadowSection left, ShadowSection right, Scope scope) {
		Object l = ShadowTools.asObject(left, scope).orElseThrow(ShadowException.sectionConvert());
		Object r = ShadowTools.asObject(right, scope).orElseThrow(ShadowException.sectionConvert());
		if (l instanceof Boolean && r instanceof Boolean) {
			return new Reference((Boolean) l || (Boolean) r);
		}
		throw new ShadowExecutionException("Invalid operands. Booleans only.");
	}
	
}
