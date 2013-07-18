package falgout.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public abstract class MethodLocator {
	public Method getMethod(Class<?> clazz, String name, Object... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getMethod(clazz, name, ReflectionUtilities.getClasses(args));
	}
	
	public Method getMethod(Class<?> clazz, String name, Class<?>... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getMethod(Arrays.asList(clazz.getMethods()), clazz, name, args);
	}
	
	public Set<Method> getMethods(Class<?> clazz, String name, Object... args) throws NoSuchMethodException {
		return getMethods(clazz, name, ReflectionUtilities.getClasses(args));
	}
	
	public Set<Method> getMethods(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
		return getMethods(Arrays.asList(clazz.getMethods()), clazz, name, args);
	}
	
	public Method getDeclaredMethod(Class<?> clazz, String name, Object... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getDeclaredMethod(clazz, name, ReflectionUtilities.getClasses(args));
	}
	
	public Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getMethod(Arrays.asList(clazz.getDeclaredMethods()), clazz, name, args);
	}
	
	public Set<Method> getDeclaredMethods(Class<?> clazz, String name, Object... args) throws NoSuchMethodException {
		return getDeclaredMethods(clazz, name, ReflectionUtilities.getClasses(args));
	}
	
	public Set<Method> getDeclaredMethods(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
		return getMethods(Arrays.asList(clazz.getDeclaredMethods()), clazz, name, args);
	}
	
	public Method getMethod(Collection<? extends Method> methods, Class<?> clazz, String name, Object... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getMethod(methods, clazz, name, ReflectionUtilities.getClasses(args));
	}
	
	protected abstract Method getMethod(Collection<? extends Method> methods, Class<?> clazz, String name,
			Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException;
	
	public Set<Method> getMethods(Collection<? extends Method> methods, Class<?> clazz, String name, Object... args)
			throws NoSuchMethodException {
		return getMethods(methods, clazz, name, ReflectionUtilities.getClasses(args));
	}
	
	protected abstract Set<Method> getMethods(Collection<? extends Method> methods, Class<?> clazz, String name,
			Class<?>... args) throws NoSuchMethodException;
	
	public <T> Constructor<T> getConstructor(Class<T> clazz, Object... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getConstructor(clazz, ReflectionUtilities.getClasses(args));
	}
	
	public <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getConstructor(ReflectionUtilities.getConstructors(clazz), clazz, args);
	}
	
	public <T> Set<Constructor<T>> getConstructors(Class<T> clazz, Object... args) throws NoSuchMethodException {
		return getConstructors(clazz, ReflectionUtilities.getClasses(args));
	}
	
	public <T> Set<Constructor<T>> getConstructors(Class<T> clazz, Class<?>... args) throws NoSuchMethodException {
		return getConstructors(ReflectionUtilities.getConstructors(clazz), clazz, args);
	}
	
	public <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Object... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getDeclaredConstructor(clazz, ReflectionUtilities.getClasses(args));
	}
	
	public <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getConstructor(ReflectionUtilities.getDeclaredConstructors(clazz), clazz, args);
	}
	
	public <T> Set<Constructor<T>> getDeclaredConstructors(Class<T> clazz, Object... args) throws NoSuchMethodException {
		return getDeclaredConstructors(clazz, ReflectionUtilities.getClasses(args));
	}
	
	public <T> Set<Constructor<T>> getDeclaredConstructors(Class<T> clazz, Class<?>... args)
			throws NoSuchMethodException {
		return getConstructors(ReflectionUtilities.getDeclaredConstructors(clazz), clazz, args);
	}
	
	public <T> Constructor<T> getConstructor(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
			Object... args) throws AmbiguousDeclarationException, NoSuchMethodException {
		return getConstructor(constructors, clazz, ReflectionUtilities.getClasses(args));
	}
	
	protected abstract <T> Constructor<T> getConstructor(Collection<? extends Constructor<T>> constructors,
			Class<T> clazz, Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException;
	
	public <T> Set<Constructor<T>> getConstructors(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
			Object... args) throws NoSuchMethodException {
		return getConstructors(constructors, clazz, ReflectionUtilities.getClasses(args));
	}
	
	protected abstract <T> Set<Constructor<T>> getConstructors(Collection<? extends Constructor<T>> constructors,
			Class<T> clazz, Class<?>... args) throws NoSuchMethodException;
}