package com.ssplugins.shadow;

import com.ssplugins.shadow.common.TypeReference;
import com.ssplugins.shadow.element.ShadowSection;
import com.ssplugins.shadow.exceptions.ShadowExecutionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class Reflect {

	private TypeReference ref;
	private Scope scope;
	
	private Reflect(TypeReference ref, Scope scope) {
		this.ref = ref;
		this.scope = scope;
	}
	
	public static Reflect of(TypeReference ref, Scope scope) {
		return new Reflect(ref, scope);
	}
	
	public void method(String methodName, List<ShadowSection> params) {
		Optional<Object[]> args = ShadowTools.asObjectArray(params, scope);
		if (!args.isPresent()) throw new ShadowExecutionException("Parameters could not be converted to objects.");
		Optional<Method> method = ReflectionTools.findMethod(ref, methodName, args.get());
		if (!method.isPresent()) throw new ShadowExecutionException("Method \"" + methodName + "\" not found in current object.");
		ReflectionTools.callMethod(ref, method.get(), args.get());
	}
	
	public void construct(String className, List<ShadowSection> params) {
		Optional<Object[]> args = ShadowTools.asObjectArray(params, scope);
		if (!args.isPresent()) throw new ShadowExecutionException("Parameters could not be converted to objects.");
		Optional<Class<?>> type = scope.getContext().findClass(className);
		if (!type.isPresent()) throw new ShadowExecutionException("No class found from name " + className);
		Optional<Constructor<?>> con = ReflectionTools.findConstructor(type.get(), args.get());
		if (!con.isPresent()) throw new ShadowExecutionException("Constructor for \"" + className + "\" not found.");
		ReflectionTools.callConstructor(ref, con.get(), args.get());
	}
	
	public void field(String fieldName) {
		Optional<Field> field = ReflectionTools.findField(ref, fieldName);
		if (!field.isPresent()) throw new ShadowExecutionException("Field " + fieldName + " not found in current object.");
		ReflectionTools.getField(ref, field.get());
	}
	
}
