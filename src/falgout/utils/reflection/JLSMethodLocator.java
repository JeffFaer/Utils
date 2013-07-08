package falgout.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import falgout.utils.temp.Predicate;

class JLSMethodLocator extends MethodLocator {
	@Override
	protected Method findMethod(Collection<? extends Method> methods, Class<?> clazz, String name, Class<?>... args)
			throws AmbiguousDeclarationException, NoSuchMethodException {
		return findParameterized(convertMethods(methods), clazz, name, args).getMethod();
	}
	
	@Override
	protected Set<Method> findMethods(Collection<? extends Method> methods, Class<?> clazz, String name,
			Class<?>... args) throws NoSuchMethodException {
		Set<Parameterized.Method> found = findParameterizeds(convertMethods(methods), clazz, name, args);
		
		Set<Method> meths = new LinkedHashSet<>(found.size());
		for (Parameterized.Method m : found) {
			meths.add(m.getMethod());
		}
		
		return meths;
	}
	
	@Override
	protected <T> Constructor<T> findConstructor(Collection<? extends Constructor<T>> constructors, Class<T> clazz,
			Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException {
		return findParameterized(convertConstructors(constructors), clazz, "<init>", args).getConstructor();
	}
	
	@Override
	protected <T> Set<Constructor<T>> findConstructors(Collection<? extends Constructor<T>> constructors,
			Class<T> clazz, Class<?>... args) throws NoSuchMethodException {
		Set<Parameterized.Constructor<T>> found = findParameterizeds(convertConstructors(constructors), clazz,
				"<init>", args);
		
		Set<Constructor<T>> ctors = new LinkedHashSet<>(found.size());
		for (Parameterized.Constructor<T> c : found) {
			ctors.add(c.getConstructor());
		}
		
		return ctors;
	}
	
	private List<Parameterized.Method> convertMethods(Collection<? extends Method> methods) {
		List<Parameterized.Method> converted = new ArrayList<>(methods.size());
		for (Method m : methods) {
			converted.add(new Parameterized.Method(m));
		}
		return converted;
	}
	
	private <T> List<Parameterized.Constructor<T>> convertConstructors(Collection<? extends Constructor<T>> constructors) {
		List<Parameterized.Constructor<T>> converted = new ArrayList<>(constructors.size());
		for (Constructor<T> c : constructors) {
			converted.add(new Parameterized.Constructor<>(c));
		}
		return converted;
	}
	
	private <P extends Parameterized> P findParameterized(Collection<? extends P> parameterizeds, Class<?> clazz,
			String name, Class<?>... args) throws AmbiguousDeclarationException, NoSuchMethodException {
		Set<P> found = findParameterizeds(parameterizeds, clazz, name, args);
		if (found.size() > 1) {
			throw new AmbiguousDeclarationException(found.toString());
		}
		return found.iterator().next();
	}
	
	private <P extends Parameterized> Set<P> findParameterizeds(Collection<? extends P> parameterizeds, Class<?> clazz,
			String name, Class<?>... args) throws NoSuchMethodException {
		Set<P> potentiallyApplicable = new LinkedHashSet<>();
		Predicate<Parameterized> filter = new PotentiallyApplicable(name, args);
		for (P p : parameterizeds) {
			if (filter.test(p)) {
				potentiallyApplicable.add(p);
			}
		}
		
		for (Phase phase : Phase.values()) {
			Set<P> applicable = new LinkedHashSet<>();
			
			for (P p : potentiallyApplicable) {
				if (phase.isApplicable(args, p)) {
					applicable.add(p);
				}
			}
			
			if (!applicable.isEmpty()) {
				P max = Collections.max(applicable, MethodSpecificity.INSTANCE);
				Set<P> found = new LinkedHashSet<>();
				found.add(max);
				
				for (P p : applicable) {
					if (MethodSpecificity.INSTANCE.compare(p, max) == 0) {
						found.add(p);
					}
				}
				
				return found;
			}
		}
		
		throw new NoSuchMethodException(createMessage(clazz, name, args));
	}
	
	private String createMessage(Class<?> clazz, String name, Class<?>... args) {
		StringBuilder b = new StringBuilder();
		b.append(clazz.getName()).append(".").append(name).append("(");
		for (int x = 0; x < args.length; x++) {
			if (x != 0) {
				b.append(", ");
			}
			b.append(toHumanReadableName(args[x]));
		}
		b.append(")");
		return b.toString();
	}
	
	private String toHumanReadableName(Class<?> clazz) {
		return clazz.isArray() ? toHumanReadableName(clazz.getComponentType()) + "[]" : clazz.getName();
	}
}