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
	
	public static Object[] processEach(String[] instructions, Scope scope, ClassFinder finder) {
		List<Object> objects = new ArrayList<>();
		for (String s : instructions) {
			objects.add(process(s, scope, finder));
		}
		return objects.toArray();
	}
	
	/*
	Eval operators:
	< = get var from scope
	? = literal string
	> = cast to type
	: = call method
	= = construct type
	~ = get field from object
	- = parse number, boolean, null, or string (in that order)
	# = new box
	[ = get box
	] = get variable from box
	+ = set box variable (method format)
	 */
	
	public Object process() {
		Debugger.log("--- processing ---");
		Pattern pattern = Pattern.compile("g-\\w+|([^\\w.])?((?:\\W)?[\\w.$ ]+(?:\\([^)]*\\))?)");
		Matcher matcher = pattern.matcher(instruction);
		while (matcher.find()) {
			String o = matcher.group(1);
			String data = matcher.group(2);
			if (o == null || o.equals("<")) {
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
			if (o.equals("?")) {
				Debugger.log("literal string: " + data);
				return data;
			}
			else if (o.equals(">")) castTo(data);
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
				Debugger.log("param is: " + current.toString());
				if (current != null) cClass = unwrap(current.getClass());
				else cClass = null;
			}
			else if (o.equals("#")) {
				Debugger.log("preparing to create box: " + data);
				Matcher m = Pattern.compile("(.+)\\((.+)\\)").matcher(data);
				List<Object> params = new ArrayList<>();
				if (m.find()) {
					data = m.group(1);
					String[] prms = m.group(2).split("(?<!\\\\),\\s?");
					for (String s : prms) params.add(Evaluator.process(s, scope, finder));
				}
				current = newBox(data, params);
				cClass = current == null ? null : Box.class;
			}
			else if (o.equals("[")) {
				Debugger.log("getting box: " + data);
				boolean g = data.startsWith(":");
				if (g) {
					data = data.substring(1);
					current = scope.getGlobalVar(data).map(Variable::getValue).orElse(null);
				}
				else {
					current = scope.getVar(data).map(Variable::getValue).orElse(null);
				}
				if (!(current instanceof Box)) current = null;
				cClass = current == null ? null : Box.class;
			}
			else if (o.equals("]")) {
				Debugger.log("getting box variable: " + data);
				if (!(current instanceof Box)) current = null;
				if (current != null) {
					Debugger.log("current is box, getting var");
					current = ((Box) current).getVar(data).map(Variable::getValue).orElse(null);
				}
				cClass = current == null ? null : unwrap(current.getClass());
			}
			else if (o.equals("+")) {
				Debugger.log("preparing to set box variable: " + data);
				if (!(current instanceof Box)) current = null;
				if (current != null) {
					Matcher m = Pattern.compile("(.+)\\((.+)\\)").matcher(data);
					Object val = null;
					if (m.find()) {
						data = m.group(1);
						String[] prms = m.group(2).split("(?<!\\\\),\\s?");
						if (prms.length > 0) val = Evaluator.process(prms[0], scope, finder);
					}
					Debugger.log("setting var " + data + " to " + (val != null ? val.toString() : "null"));
					((Box) current).setVar(data, val);
				}
				cClass = current == null ? null : unwrap(current.getClass());
			}
			Debugger.log("currently: " + (current == null ? "null" : nameOf(cClass)));
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
	
	private String nameOf(Class<?> clazz) {
		return clazz != null ? clazz.getName() : "null";
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
					if (!p[i].isAssignableFrom(unwrap(params[i].getClass())) && !p[i].isAssignableFrom(params[i].getClass())) return false;
				}
				Class<?> arrayType = p[p.length - 1].getComponentType();
				for (int i = p.length - 1; i < params.length; i++) {
					if (!arrayType.isAssignableFrom(unwrap(params[i].getClass())) && !arrayType.isAssignableFrom(params[i].getClass())) return false;
				}
				ShadowUtil.toVarArgs(params, p.length - 1, arrayType);
				return true;
			}
			if (p.length != params.length) return false;
			for (int i = 0; i < p.length; i++) {
				if (params[i] == null) continue;
				if (!p[i].isAssignableFrom(unwrap(params[i].getClass())) && !p[i].isAssignableFrom(params[i].getClass())) return false;
			}
			return true;
		}).findFirst();
		return op.orElse(null);
	}
	
	private Box newBox(String name, List<Object> params) {
		Optional<BoxPattern> op = scope.getShadow().getBoxPattern(name);
		return op.map(boxPattern -> boxPattern.create(params)).orElse(null);
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
		Debugger.log("parsing param: " + value);
		if (value.matches("p\\{.+}")) {
			Debugger.log("getting private var");
			Optional<Variable> op = scope.getPrivateVar(value.substring(2, value.length() - 1));
			return op.map(Variable::getValue).orElse(null);
		}
		return toObject(value);
	}
	
	public static Object toObject(String value) {
		Debugger.log("converting to primitive");
		if (value.matches("-?[0-9.]+")) {
			Debugger.log("value is number");
			if (value.contains(".")) return Double.valueOf(value);
			return Integer.valueOf(value);
		}
		else if (value.matches("true|false")) {
			Debugger.log("value is boolean");
			return Boolean.valueOf(value);
		}
		else if (value.equals("null")) {
			Debugger.log("value is null");
			return null;
		}
		Debugger.log("returning value as string");
		return value;
	}
}
