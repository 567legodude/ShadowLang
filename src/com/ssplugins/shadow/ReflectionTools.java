package com.ssplugins.shadow;

import com.ssplugins.shadow.common.TypeReference;
import com.ssplugins.shadow.exceptions.ShadowExecutionException;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public final class ReflectionTools {
	
	public static Optional<Method> findMethod(TypeReference ref, String methodName, Object[] params) {
		Optional<Method> method = Optional.empty();
		Class<?> toCheck;
		if (ref.getValue() != null) toCheck = ref.getValue().getClass();
		else toCheck = ref.getType();
		while (toCheck != null) {
			method = methodSearch(toCheck, methodName, params);
			if (method.isPresent()) break;
			for (Class<?> i : toCheck.getInterfaces()) {
				method = methodSearch(i, methodName, params);
				if (method.isPresent()) break;
			}
			if (method.isPresent()) break;
			toCheck = toCheck.getSuperclass();
		}
		return method;
	}
	
	public static void callMethod(TypeReference ref, Method method, Object[] params) {
		if (method.isVarArgs()) {
			Class<?>[] types = method.getParameterTypes();
			params = toVarArgs(params, method.getParameterCount() - 1, types[types.length - 1].getComponentType());
		}
		method.setAccessible(true);
		try {
			ref.setValue(method.invoke(ref.getValue(), params));
			ref.setType(method.getReturnType());
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new ShadowExecutionException("Unable to call method " + methodToString(method));
		}
	}
	
	public static Optional<Constructor<?>> findConstructor(Class<?> type, Object[] params) {
		return Stream.of(type.getDeclaredConstructors())
					.filter(constructor -> {
						Class<?>[] paramTypes = constructor.getParameterTypes();
						if (constructor.isVarArgs()) {
							if (params.length < paramTypes.length) return false;
							for (int i = 0; i < paramTypes.length; i++) {
								Class<?> p = params[i].getClass();
								Class<?> pt = paramTypes[i];
								if (!pt.isAssignableFrom(unwrap(p)) && !pt.isAssignableFrom(p)) return false;
							}
							Class<?> arrayType = paramTypes[paramTypes.length - 1].getComponentType();
							for (int i = paramTypes.length - 1; i < params.length; i++) {
								Class<?> p = params[i].getClass();
								if (!arrayType.isAssignableFrom(unwrap(p)) && !arrayType.isAssignableFrom(p)) return false;
							}
							return true;
						}
						if (paramTypes.length != params.length) return false;
						for (int i = 0; i < paramTypes.length; i++) {
							Class<?> p = params[i].getClass();
							Class<?> pt = paramTypes[i];
							if (!pt.isAssignableFrom(unwrap(p)) && !pt.isAssignableFrom(p)) return false;
						}
						return true;
					})
					.findFirst();
	}
	
	public static void callConstructor(TypeReference ref, Constructor<?> con, Object[] params) {
		if (con.isVarArgs()) {
			Class<?>[] types = con.getParameterTypes();
			params = toVarArgs(params, con.getParameterCount() - 1, types[types.length - 1].getComponentType());
		}
		con.setAccessible(true);
		try {
			ref.set(con.newInstance(params));
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new ShadowExecutionException("Unable to call constructor " + constructorToString(con));
		}
	}
	
	public static Optional<Field> findField(TypeReference ref, String fieldName) {
		try {
			return Optional.of(ref.getType().getDeclaredField(fieldName));
		} catch (NoSuchFieldException e) {
			return Optional.empty();
		}
	}
	
	public static void getField(TypeReference ref, Field field) {
		field.setAccessible(true);
		try {
			ref.setValue(field.get(ref.getValue()));
			ref.setType(field.getType());
		} catch (IllegalAccessException e) {
			throw new ShadowExecutionException("Unable to get field " + fieldToString(field));
		}
	}
	
	private static Object[] toVarArgs(Object[] src, int index, Class<?> baseType) {
		int len = src.length - index;
		Object arr = Array.newInstance(baseType, len);
		for (int i = 0; i < len; i++) {
			Array.set(arr, i, src[i + index]);
		}
		src[index] = arr;
		return Arrays.copyOf(src, index + 1);
	}
	
	private static Optional<Method> methodSearch(Class<?> type, String methodName, Object[] params) {
		return Stream.of(type.getDeclaredMethods())
				.filter(method -> method.getName().equals(methodName))
				.filter(method -> {
					Class<?>[] paramTypes = method.getParameterTypes();
					if (method.isVarArgs()) {
						if (params.length < paramTypes.length) return false;
						for (int i = 0; i < paramTypes.length; i++) {
							Class<?> p = params[i].getClass();
							Class<?> pt = paramTypes[i];
							if (!pt.isAssignableFrom(unwrap(p)) && !pt.isAssignableFrom(p)) return false;
						}
						Class<?> arrayType = paramTypes[paramTypes.length - 1].getComponentType();
						for (int i = paramTypes.length - 1; i < params.length; i++) {
							Class<?> p = params[i].getClass();
							if (!arrayType.isAssignableFrom(unwrap(p)) && !arrayType.isAssignableFrom(p)) return false;
						}
						return true;
					}
					if (paramTypes.length != params.length) return false;
					for (int i = 0; i < paramTypes.length; i++) {
						Class<?> p = params[i].getClass();
						Class<?> pt = paramTypes[i];
						if (!pt.isAssignableFrom(unwrap(p)) && !pt.isAssignableFrom(p)) return false;
					}
					return true;
				})
				.findFirst();
	}
	
    private static Class<?> unwrap(Class<?> type) {
        if (type.equals(Integer.class)) return int.class;
        else if (type.equals(Double.class)) return double.class;
        else if (type.equals(Boolean.class)) return boolean.class;
        else if (type.equals(Long.class)) return long.class;
        else if (type.equals(Character.class)) return char.class;
        else if (type.equals(Byte.class)) return byte.class;
        else if (type.equals(Short.class)) return short.class;
        else if (type.equals(Float.class)) return float.class;
        return type;
    }
	
	private static String methodToString(Method method) {
		StringBuilder builder = new StringBuilder();
		builder.append(method.getDeclaringClass().getSimpleName())
				.append("#")
				.append(method.getName())
				.append("(");
		Class<?>[] types = method.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			Class<?> c = types[i];
			builder.append(c.getSimpleName());
			if (i < types.length - 1) builder.append(", ");
		}
		if (method.isVarArgs()) builder.append("...");
		builder.append(")");
		return builder.toString();
	}
	
	private static String constructorToString(Constructor<?> con) {
		StringBuilder builder = new StringBuilder();
		builder.append(con.getDeclaringClass().getSimpleName())
			   .append("(");
		Class<?>[] types = con.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			Class<?> c = types[i];
			builder.append(c.getSimpleName());
			if (i < types.length - 1) builder.append(", ");
		}
		if (con.isVarArgs()) builder.append("...");
		builder.append(")");
		return builder.toString();
	}
	
	private static String fieldToString(Field field) {
		return field.getDeclaringClass().getSimpleName() + "#" + field.getName();
	}
	
}
