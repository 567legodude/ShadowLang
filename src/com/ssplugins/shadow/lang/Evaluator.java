package com.ssplugins.shadow.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Evaluator {
	
	private String instruction;
	private Scope scope;
	private ClassFinder finder;
	
	private Object current = null;
	private Class<?> cClass = null;
	
	public Evaluator(String instruction, Scope scope, ClassFinder finder) {
		this.instruction = instruction;
		this.scope = scope;
		this.finder = finder;
	}
	
	public static Object process(String instruction, Scope scope, ClassFinder finder) {
		return new Evaluator(instruction, scope, finder).process();
	}
	
	public Object process() {
		Debugger.log("processing");
		Pattern pattern = Pattern.compile("(\\W)?([\\w()\\s.]+)");
		Matcher matcher = pattern.matcher(instruction);
		while (matcher.find()) {
			String o = matcher.group(1);
			String data = matcher.group(2);
			if (o == null) {
				Debugger.log("getting " + data + " from scope");
				Optional<Variable> op = scope.getVar(data);
				if (op.isPresent()) {
					current = op.get().getValue();
					cClass = unwrap(current.getClass());
				}
				else return null;
				Debugger.log("starting as: " + (cClass == null ? "null" : cClass.getName()));
				continue;
			}
			if (o.equals(">")) castTo(data);
			else if (o.equals(":")) {
				Debugger.log("preparing to call method: " + data);
				Matcher m = Pattern.compile("(.+)\\((.+)\\)").matcher(data);
				List<Object> params = new ArrayList<>();
				if (m.find()) {
					Debugger.log("found match");
					data = m.group(1);
					String[] prms = m.group(2).split("(?<!\\\\),\\s?");
					for (String s : prms) params.add(parseParam(s));
				}
				method(data, params.toArray(new Object[params.size()]));
			}
			else if (o.equals("=")) {
				Debugger.log("preparing to construct: " + data);
				Matcher m = Pattern.compile("(.+)\\((.+)\\)").matcher(data);
				List<Object> params = new ArrayList<>();
				if (m.find()) {
					Debugger.log("found match");
					data = m.group(1);
					String[] prms = m.group(2).split("(?<!\\\\),\\s?");
					for (String s : prms) params.add(parseParam(s));
				}
				construct(data, params.toArray(new Object[params.size()]));
			}
			else if (o.equals("-")) {
				current = parseParam(data);
				cClass = unwrap(current.getClass());
			}
			Debugger.log("currently: " + (current == null ? "null" : cClass.getName()));
			if (current == null) return null;
		}
		return current;
	}
	
	private void castTo(String type) {
		Debugger.log("casting to: " + type);
		try {
			Class<?> clazz = Class.forName(finder.findClass(type));
			Debugger.log("found class: " + clazz.getName());
			if (clazz.isAssignableFrom(cClass) || clazz.isInstance(current)) {
				current = clazz.cast(current);
				cClass = clazz;
			}
			else current = null;
		} catch (ClassNotFoundException ignored) {
			Debugger.log("type not found");
			current = null;
		}
	}
	
	private void construct(String type, Object[] params) {
		Debugger.log("constructing " + type + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : ""));
		try {
			Class<?> clazz = Class.forName(finder.findClass(type));
			Constructor<?> con = clazz.getConstructor(getClasses(params));
			current = con.newInstance(params);
			cClass = clazz;
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
			current = null;
		}
	}
	
	private void method(String method, Object[] params) {
		Debugger.log("calling method " + method + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : ""));
		try {
			Method m = cClass.getDeclaredMethod(method, getClasses(params));
			current = m.invoke(current, params);
			cClass = m.getReturnType();
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
			Debugger.log("method " + method + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : "") + " not found in " + (cClass == null ? "null" : cClass.getName()));
			current = null;
		}
	}
	
	private Class<?> unwrap(Class<?> clazz) {
		if (clazz.equals(Integer.class)) return int.class;
		else if (clazz.equals(Double.class)) return double.class;
		else if (clazz.equals(Boolean.class)) return boolean.class;
		return clazz;
	}
	
	private Class<?>[] getClasses(Object[] params) {
		List<Class<?>> classes = new ArrayList<>();
		for (Object o : params) {
			classes.add(unwrap(o.getClass()));
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}
	
	private Object parseParam(String value) {
		if (value.matches("o\\{.+}")) return process(value.substring(2, value.length() - 1), scope, finder);
		return toObject(value);
	}
	
	public static Object toObject(String value) {
		if (value.matches("-?[0-9.]+")) {
			if (value.contains(".")) return Double.valueOf(value);
			return Integer.valueOf(value);
		}
		else if (value.matches("true|false")) {
			return Boolean.valueOf(value);
		}
		return value;
	}
}
