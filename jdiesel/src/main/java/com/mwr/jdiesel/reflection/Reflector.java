package com.mwr.jdiesel.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.mwr.jdiesel.reflection.types.ReflectedType;

public class Reflector {
	
	public static Object construct(Class<?> klass, ReflectedType[] arguments) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		Constructor<?> constructor = null;
		
		if(arguments.length == 0)
			constructor = klass.getConstructor();
		else
			constructor = getConstructor(klass, arguments);
		
		if(constructor != null)
			return constructor.newInstance(getNativeArguments(arguments));
		else
			throw new NoSuchMethodException();
	}
	
	private static Constructor<?> getConstructor(Class<?> object, ReflectedType[] arguments) {
		for(Constructor<?> constructor : object.getConstructors()) {
			if(hasCompatibleSignatures(constructor.getParameterTypes(), getNativeArguments(arguments)))
				return constructor;
		}
		
		return null;
	}
	
	private static Field getField(Object object, String property) throws NoSuchFieldException {
		if(object instanceof Class)
			return ((Class<?>)object).getField(property);
		else
			return object.getClass().getField(property);
	}
	
	private static Method getMethod(Object object, String method_name, ReflectedType[] arguments) throws NoSuchMethodException {
		if(object instanceof Class) {
			Method m = lookupMethod((Class<?>)object, method_name, arguments);
			
			if(!Modifier.isStatic(m.getModifiers()))
				return null;
			else
				return m;
		}
		else
			return lookupMethod(object.getClass(), method_name, arguments);
	}
	
	private static Object[] getNativeArguments(ReflectedType[] arguments) {
		Object[] natives = new Object[arguments.length];
		
		for(int i=0; i<arguments.length; i++)
			natives[i] = arguments[i].getNative();
		
		return natives;
	}
	
	public static Object getProperty(Object object, String property) throws IllegalAccessException, NoSuchFieldException {
		return getField(object, property).get(object instanceof Class ? null : object);
	}
	
	private static boolean hasCompatibleSignatures(Class<?>[] argument_types, Object[] arguments) {
		if(argument_types.length == arguments.length) {
			boolean correct = true;
			
			for (int i=0; i<arguments.length; i++)
				correct = correct & isCompatible(arguments[i], argument_types[i]);
			
			if(correct)
				return true;
		}
		
		return false;
	}
	
	public static Object invoke(Object object, String method_name, ReflectedType[] arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		if(arguments.length == 0)
			return getMethod(object, method_name, arguments).invoke(object, (Object[])null);
		else
			return getMethod(object, method_name, arguments).invoke(object, getNativeArguments(arguments));
	}
	
	private static boolean isCompatible(Object object, Class<?> type) {
		return object == null && !type.isPrimitive() ||
				type.isInstance(object) ||
				type.isPrimitive() && isWrapperTypeOf(object.getClass(), type);
	}
	
	public static boolean isMethodReturnPrimitive(Object object, String method_name, ReflectedType[] arguments) throws NoSuchMethodException {
		Method method = getMethod(object, method_name, arguments);
		
		if(method != null) {
			Class<?> type = method.getReturnType();
			
			return type != null ? type.isPrimitive() : null;
		}
		else {
			throw new NoSuchMethodException(method_name);
		}
	}
	
	public static boolean isPropertyPrimitive(Object object, String name) throws NoSuchFieldException {
		return getField(object, name).getType().isPrimitive();
	}
	
	private static boolean isWrapperTypeOf(Class<?> klass, Class<?> primitive) {
		try {
			return !klass.isPrimitive() && klass.getDeclaredField("TYPE").get(null).equals(primitive);
		}
		catch(IllegalAccessException e) {
			return false;
		}
		catch(NoSuchFieldException e) {
			return false;
		}
	}
	
	private static Method lookupMethod(Class<?> klass, String method_name, ReflectedType[] arguments) throws NoSuchMethodException {
		for(Method method: klass.getMethods()) {
			if(method_name.equals(method.getName())) {
				if(hasCompatibleSignatures(method.getParameterTypes(), getNativeArguments(arguments)))
					return method;
			}
		}
		
		throw new NoSuchMethodException(method_name + " for " + klass.toString());
	}
	
	public static Class<?> resolve(String className) {
		try {
			return Class.forName(className);
		}
		catch(ClassNotFoundException e) {
			return null;
		}
	}
	
	public static void setProperty(Object object, String property, ReflectedType value) throws IllegalAccessException, NoSuchFieldException {
		getField(object, property).set(object, value.getNative());
	}

}
