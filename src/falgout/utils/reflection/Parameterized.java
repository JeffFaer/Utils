package falgout.utils.reflection;

interface Parameterized {
	public static class Method implements Parameterized {
		private final java.lang.reflect.Method m;
		
		public Method(java.lang.reflect.Method m) {
			this.m = m;
		}
		
		@Override
		public Class<?>[] getParameterTypes() {
			return m.getParameterTypes();
		}
		
		@Override
		public boolean isVarArgs() {
			return m.isVarArgs();
		}
		
		@Override
		public String getName() {
			return m.getName();
		}
		
		public java.lang.reflect.Method getMethod() {
			return m;
		}
	}
	
	public static class Constructor<T> implements Parameterized {
		private final java.lang.reflect.Constructor<T> c;
		
		public Constructor(java.lang.reflect.Constructor<T> c) {
			this.c = c;
		}
		
		@Override
		public Class<?>[] getParameterTypes() {
			return c.getParameterTypes();
		}
		
		@Override
		public boolean isVarArgs() {
			return c.isVarArgs();
		}
		
		@Override
		public String getName() {
			return c.getName();
		}
		
		public java.lang.reflect.Constructor<T> getConstructor() {
			return c;
		}
	}
	
	public Class<?>[] getParameterTypes();
	
	public boolean isVarArgs();
	
	public String getName();
}