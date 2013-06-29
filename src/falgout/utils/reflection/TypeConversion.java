package falgout.utils.reflection;

public interface TypeConversion {
	/**
	 * Determines if a conversion {@code from} the given class {@code to} the
	 * other class is allowed via a set of rules.
	 * 
	 * Example:
	 * If this class represented an assignment conversion:
	 * 
	 * <pre>
	 * From f = ...
	 * To t = f;
	 * </pre>
	 * 
	 * If this conversion would result in a compiler error, the method would
	 * return {@code false}. If it is allowed, the method would return
	 * {@code true}.
	 * 
	 * <b>Note:</b> {@code from} may be {@code null} to represent the
	 * {@code null} type, but {@code to} should <i>never</i> be {@code null}.
	 * 
	 * @param from The {@code Class} to convert from
	 * @param to The {@code Class} to convert to
	 * @return Whether or not the conversion is allowed.
	 */
	public boolean convert(Class<?> from, Class<?> to);
}