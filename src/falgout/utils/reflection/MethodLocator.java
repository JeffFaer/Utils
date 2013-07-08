package falgout.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public abstract class MethodLocator {
	public Method getMethod(Class<?> clazz, String name, Object... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getMethod(clazz, name, ReflectionUtils.getClasses(args));
	}
	
	public Method getMethod(Class<?> clazz, String name, Class<?>... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getMethod(Arrays.asList(clazz.getMethods()), clazz, name, args);
	}
	
	public Set<Method> getMethods(Class<?> clazz, String name, Object... args) throws NoSuchMethodException {
		return getMethods(clazz, name, ReflectionUtils.getClasses(args));
	}
	
	public Set<Method> getMethods(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
		return getMethods(Arrays.asList(clazz.getMethods()), clazz, name, args);
	}
	
	public Method getDeclaredMethod(Class<?> clazz, String name, Object... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getDeclaredMethod(clazz, name, ReflectionUtils.getClasses(args));
	}
	
	public Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getMethod(Arrays.asList(clazz.getDeclaredMethods()), clazz, name, args);
	}
	
	public Set<Method> getDeclaredMethods(Class<?> clazz, String name, Object... args) throws NoSuchMethodException {
		return getDeclaredMethods(clazz, name, ReflectionUtils.getClasses(args));
	}
	
	public Set<Method> getDeclaredMethods(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException {
		return getMethods(Arrays.asList(clazz.getDeclaredMethods()), clazz, name, args);
	}
	
	public Method getMethod(Collection<? extends Method> methods, Class<?> clazz, String name, Object... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getMethod(methods, clazz, name, ReflectionUtils.getClasses(args));
	}
	
	public Method getMethod(Collection<? extends Method> methods, Class<?> clazz, String name, Class<?>... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return findMethod(methods, clazz, name, args);
	}
	
	public Set<Method> getMethods(Collection<? extends Method> methods, Class<?> clazz, String name, Object... args)
			throws NoSuchMethodException {
		return getMethods(methods, clazz, name, ReflectionUtils.getClasses(args));
	}
	
	public Set<Method> getMethods(Collection<? extends Method> methods, Class<?> clazz, String name, Class<?>... args)
			throws NoSuchMethodException {
		return findMethods(methods, clazz, name, args);
	}
	
	public <T> Constructor<T> getConstructor(Class<T> clazz, Object... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getConstructor(clazz, ReflectionUtils.getClasses(args));
	}
	
	public <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... args) throws AmbiguousDeclarationException,
			NoSuchMethodException {
		return getConstructor(ReflectionUtils.getConstructors(clazz), clazz, args);
	}
	
	public <T> Set<Constructor<T>> getConstructors(Class<T> clazz, Object... args) throws NoSuchMethodException {
		return getConstructors(clazz, ReflectionUtils.getClasses(args));
	}
	
	public <T> Set<Constructor<T>> getConstructors(Class<T> clazz, Class<?>... args) throws NoSuchMethodException {
		return getConstructors(ReflectionUtils.getConstructors(clazz), clazz, args);
	}
	
	public <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Object... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getDeclaredConstructor(clazz, ReflectionUtils.getClasses(args));
	}
	
	public <T> Constructor<T> getDeclaredConstructor(Class<T> clazz, Class<?>... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return getConstructor(ReflectionUtils.getDeclaredConstructors(clazz), clazz, args);
	}
	
	public <T> Set<Constructor<T>> getDeclaredConstructors(Class<T> clazz, Object... args) throws NoSuchMethodException {
		return getDeclaredConstructors(clazz, ReflectionUtils.getClasses(args));
	}
	
	public <T> Set<Constructor<T>> getDeclaredConstructors(Class<T> clazz, Class<?>... args)
			throws NoSuchMethodException {
		return getConstructors(ReflectionUtils.getDeclaredConstructors(clazz), clazz, args);
	}
	
	public <T> Constructor<T> getConstructor(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
			Object... args) throws AmbiguousDeclarationException, NoSuchMethodException {
		return getConstructor(constructors, clazz, ReflectionUtils.getClasses(args));
	}
	
	public <T> Constructor<T> getConstructor(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
			Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException {
		return findConstructor(constructors, clazz, args);
	}
	
	public <T> Set<Constructor<T>> getConstructors(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
			Object... args) throws NoSuchMethodException {
		return getConstructors(constructors, clazz, ReflectionUtils.getClasses(args));
	}
	
	public <T> Set<Constructor<T>> getConstructors(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
			Class<?>... args) throws NoSuchMethodException {
		return findConstructors(constructors, clazz, args);
	}
	
	protected abstract Method findMethod(Collection<? extends Method> methods, Class<?> clazz, String name,
			Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException;
	
	protected abstract Set<Method> findMethods(Collection<? extends Method> methods, Class<?> clazz, String name,
			Class<?>... args) throws NoSuchMethodException;
	
	protected abstract <T> Constructor<T> findConstructor(Collection<? extends Constructor<T>> constructors,
			Class<T> clazz, Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException;
	
	protected abstract <T> Set<Constructor<T>> findConstructors(Collection<? extends Constructor<T>> constructors,
			Class<T> clazz, Class<?>... args) throws NoSuchMethodException;
}