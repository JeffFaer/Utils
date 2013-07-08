package falgout.utils.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;

/**
 * 
 * @author jeffrey
 * 
 * @param <M> As of JDK 1.7, only Constructor and Method fulfill the boundary
 *        requirements.
 */
abstract class Parameterized<M extends AccessibleObject & GenericDeclaration & Member> {
	protected final M member;
	
	public Parameterized(M member) {
		this.member = member;
	}
	
	public static class Method extends Parameterized<java.lang.reflect.Method> {
		public Method(java.lang.reflect.Method member) {
			super(member);
		}
		
		@Override
		public Class<?>[] getParameterTypes() {
			return member.getParameterTypes();
		}
		
		@Override
		public boolean isVarArgs() {
			return member.isVarArgs();
		}
	}
	
	public static class Constructor<T> extends Parameterized<java.lang.reflect.Constructor<T>> {
		public Constructor(java.lang.reflect.Constructor<T> member) {
			super(member);
		}
		
		@Override
		public Class<?>[] getParameterTypes() {
			return member.getParameterTypes();
		}
		
		@Override
		public boolean isVarArgs() {
			return member.isVarArgs();
		}
		
		@Override
		public String getName() {
			// Constructor.getName returns the name of the class, but we need
			// "<init>" for a NoSuchMethodException error message
			return "<init>";
		}
	}
	
	public abstract Class<?>[] getParameterTypes();
	
	public abstract boolean isVarArgs();
	
	public String getName() {
		return member.getName();
	}
	
	public M getMember() {
		return member;
	}
}