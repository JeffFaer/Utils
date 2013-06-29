package falgout.utils.reflection;

public enum BasicTypeConversion implements TypeConversion {
	/**
	 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.1"
	 */
	IDENTITY {
		@Override
		protected boolean doConvert(Class<?> from, Class<?> to) {
			return to.equals(from);
		}
	},
	/**
	 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.2"
	 */
	WIDENING_PRIMITIVE {
		@Override
		protected boolean doConvert(Class<?> from, Class<?> to) {
			return false;
		}
	},
	/**
	 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.3"
	 */
	NARROWING_PRIMITIVE {
		@Override
		protected boolean doConvert(Class<?> from, Class<?> to) {
			return false;
		}
	},
	/**
	 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.4"
	 */
	WIDENING_AND_NARROWING_PRIMITIVE {
		@Override
		protected boolean doConvert(Class<?> from, Class<?> to) {
			return false;
		}
	},
	/**
	 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.5"
	 */
	WIDENING_REFERENCE {
		@Override
		protected boolean doConvert(Class<?> from, Class<?> to) {
			return from == null || to.isAssignableFrom(from);
		}
	},
	/**
	 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.7"
	 */
	BOXING {
		@Override
		protected boolean doConvert(Class<?> from, Class<?> to) {
			return false;
		}
	},
	/**
	 * "http://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.8"
	 */
	UNBOXING {
		@Override
		protected boolean doConvert(Class<?> from, Class<?> to) {
			return false;
		}
	};
	
	@Override
	public final boolean convert(Class<?> from, Class<?> to) {
		if (to == null) {
			throw new IllegalArgumentException("to == null");
		}
		
		return doConvert(from, to);
	}
	
	protected abstract boolean doConvert(Class<?> from, Class<?> to);
}