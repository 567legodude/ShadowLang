package com.ssplugins.shadow.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
		Pattern pattern = Pattern.compile("g-\\w+|(\\W)?(\\w+(\\([^)]*\\))?)");
		Matcher matcher = pattern.matcher(instruction);
		while (matcher.find()) {
			String o = matcher.group(1);
			String data = matcher.group(2);
			if (o == null) {
				Debugger.log("getting " + data + " from scope");
				Optional<Variable> op;
				if (data == null) {
					op = scope.getGlobalVar(matcher.group().substring(2));
				}
				else {
					op = ShadowUtil.getVariable(data, scope);
				}
				if (op.isPresent()) {
					current = op.get().getValue();
					cClass = unwrap(current.getClass());
				}
				else {
					scope.error("Unable to get variable from scope");
					return null;
				}
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
			else if (o.equals("~")) {
				field(data);
			}
			else if (o.equals("-")) {
				current = parseParam(data);
				cClass = unwrap(current.getClass());
			}
			Debugger.log("currently: " + (current == null ? "null" : cClass.getName()));
			if (current == null && cClass == null) {
				scope.info("Result is null");
				return null;
			}
		}
		scope.msg("Eval complete (" + (current == null ? "" : current.getClass().getSimpleName()) + ")");
		return current;
	}
	
	private void fail() {
		current = null;
		cClass = null;
	}
	
	private void castTo(String type) {
		Debugger.log("casting to: " + type);
		try {
			Class<?> clazz = Class.forName(finder.findClass(type));
			Debugger.log("found class: " + clazz.getName());
			if (current == null) {
				cClass = clazz;
			}
			else if (clazz.isAssignableFrom(cClass) || clazz.isInstance(current)) {
				current = clazz.cast(current);
				cClass = clazz;
			}
			else fail();
		} catch (ClassNotFoundException ignored) {
			Debugger.log("type not found");
			scope.error("Unable to find class: " + type);
			fail();
		}
	}
	
	private void construct(String type, Object[] params) {
		Debugger.log("constructing " + type + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : ""));
		try {
			Class<?> clazz = Class.forName(finder.findClass(type));
			Constructor<?> con = clazz.getConstructor(getClasses(params));
			con.setAccessible(true);
			current = con.newInstance(params);
			cClass = clazz;
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
			scope.error("Unable to construct: " + type + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : ""));
			fail();
		}
	}
	
	private void field(String name) {
		Debugger.log("getting field: " + name);
		try {
			Field field = cClass.getDeclaredField(name);
			field.setAccessible(true);
			current = field.get(current);
			cClass = field.getType();
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			scope.error("Unable to get field: " + name);
			fail();
		}
	}
	
	private void method(String method, Object[] params) {
		Debugger.log("calling method " + method + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : ""));
		try {
			Method m = null;
			if (current != null) {
				Class<?> type = current.getClass();
				Class<?> sup = type.getSuperclass();
				while (sup != null) {
					m = methodSmartSearch(sup, method, params);
					if (m == null) sup = sup.getSuperclass();
					else break;
//					try {
//						m = sup.getDeclaredMethod(method, getClasses(params));
//						break;
//					} catch (NoSuchMethodException ignored) {
//						sup = sup.getSuperclass();
//					}
				}
				if (m == null) {
					for (Class<?> i : type.getInterfaces()) {
						m = methodSmartSearch(i, method, params);
						if (m == null) continue;
						break;
//						try {
//							m = i.getDeclaredMethod(method, getClasses(params));
//							break;
//						} catch (NoSuchMethodException ignored) {
//						}
					}
					if (m == null) {
						m = methodSmartSearch(cClass, method, params);
//						m = cClass.getDeclaredMethod(method, getClasses(params));
					}
				}
			}
			else {
				m = methodSmartSearch(cClass, method, params);
//				m = cClass.getDeclaredMethod(method, getClasses(params));
			}
			if (m == null) throw new NoSuchMethodException();
			if (m.isVarArgs()) {
				params = ShadowUtil.trim(params, m.getParameterCount());
			}
			m.setAccessible(true);
			current = m.invoke(current, params);
			cClass = m.getReturnType();
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
			Debugger.log("method " + method + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : "") + " not found in " + (cClass == null ? "null" : cClass.getName()));
			scope.error("Unable to find method: " + method + (params.length > 0 ? " with params " + ShadowUtil.combine(params, ", ") : "") + " in: " + (cClass == null ? "null" : cClass.getName()));
			fail();
		}
	}
	
	private Method methodSmartSearch(Class<?> clazz, String method, Object[] params) {
		Optional<Method> op = Stream.of(clazz.getDeclaredMethods()).filter(method1 -> method1.getName().equals(method)).filter(method1 -> {
			Class<?>[] p = method1.getParameterTypes();
			if (method1.isVarArgs()) {
				if (params.length < p.length) return false;
				for (int i = 0; i < p.length - 1; i++) {
					if (!p[i].isAssignableFrom(unwrap(params[i].getClass()))) return false;
				}
				Class<?> arrayType = p[p.length - 1].getComponentType();
				for (int i = p.length - 1; i < params.length; i++) {
					if (!arrayType.isAssignableFrom(unwrap(params[i].getClass()))) return false;
				}
				ShadowUtil.toVarArgs(params, p.length - 1, arrayType);
				return true;
			}
			if (p.length != params.length) return false;
			for (int i = 0; i < p.length; i++) {
				if (!p[i].isAssignableFrom(unwrap(params[i].getClass()))) return false;
			}
			return true;
		}).findFirst();
		return op.orElse(null);
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
		if (value.matches("p\\{.+}")) {
			Optional<Variable> op = scope.getPrivateVar(value.substring(2, value.length() - 1));
			return op.map(Variable::getValue).orElse(null);
		};
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
		else if (value.equals("null")) {
			return null;
		}
		return value;
	}
}
