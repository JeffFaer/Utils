package falgout.utils.reflection;

import static falgout.utils.reflection.BasicTypeConversion.BOXING;
import static falgout.utils.reflection.BasicTypeConversion.IDENTITY;
import static falgout.utils.reflection.BasicTypeConversion.UNBOXING;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_AND_NARROWING_PRIMITIVE;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_PRIMITIVE;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_REFERENCE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class BasicTypeConversionTest {
	@Test
	public void IdentityTest() {
		assertTrue(IDENTITY.convert(String.class, String.class));
		assertFalse(IDENTITY.convert(Object.class, String.class));
		assertFalse(IDENTITY.convert(String.class, Object.class));
		assertFalse(IDENTITY.convert(null, String.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void IdentityFailsWhenToIsNull() {
		IDENTITY.convert(String.class, null);
	}
	
	@Test
	public void WideningPrimitiveTest() {
		List<Class<?>> primitives = Arrays.<Class<?>> asList(byte.class, short.class, int.class, long.class,
				float.class, double.class);
		for (int x = 0; x < primitives.size(); x++) {
			assertTrue(WIDENING_PRIMITIVE.convert(primitives.get(x), primitives.get(x)));
			for (int y = x + 1; y < primitives.size(); y++) {
				assertTrue(WIDENING_PRIMITIVE.convert(primitives.get(x), primitives.get(y)));
			}
		}
		
		for (int x = primitives.indexOf(int.class); x < primitives.size(); x++) {
			assertTrue(WIDENING_PRIMITIVE.convert(char.class, primitives.get(x)));
		}
		
		assertFalse(WIDENING_PRIMITIVE.convert(null, int.class));
		assertFalse(WIDENING_PRIMITIVE.convert(byte.class, char.class));
		assertFalse(WIDENING_PRIMITIVE.convert(double.class, byte.class));
		assertFalse(WIDENING_PRIMITIVE.convert(boolean.class, int.class));
		assertFalse(WIDENING_PRIMITIVE.convert(char.class, short.class));
		
		assertFalse(WIDENING_PRIMITIVE.convert(Integer.class, long.class));
		assertFalse(WIDENING_PRIMITIVE.convert(byte.class, Integer.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void WideningPrimitiveFailsWhenToIsNull() {
		WIDENING_PRIMITIVE.convert(int.class, null);
	}
	
	@Test
	public void WideningAndNarrowingPrimitiveTest() {
		assertTrue(WIDENING_AND_NARROWING_PRIMITIVE.convert(byte.class, char.class));
		assertFalse(WIDENING_AND_NARROWING_PRIMITIVE.convert(null, char.class));
		assertFalse(WIDENING_AND_NARROWING_PRIMITIVE.convert(short.class, char.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void WideningAndNarrowingPrimitiveFailsWhenToIsNull() {
		WIDENING_AND_NARROWING_PRIMITIVE.convert(byte.class, null);
	}
	
	@Test
	public void WideningReferenceTest() {
		assertTrue(WIDENING_REFERENCE.convert(String.class, Object.class));
		assertTrue(WIDENING_REFERENCE.convert(null, Object.class));
		assertTrue(WIDENING_REFERENCE.convert(null, String.class));
		assertTrue(WIDENING_REFERENCE.convert(String.class, String.class));
		assertFalse(WIDENING_REFERENCE.convert(Object.class, String.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void WideningReferenceFailsWhenToIsNull() {
		WIDENING_REFERENCE.convert(String.class, null);
	}
	
	@Test
	public void BoxingTest() {
		assertTrue(BOXING.convert(boolean.class, Boolean.class));
		assertTrue(BOXING.convert(byte.class, Byte.class));
		assertTrue(BOXING.convert(short.class, Short.class));
		assertTrue(BOXING.convert(char.class, Character.class));
		assertTrue(BOXING.convert(int.class, Integer.class));
		assertTrue(BOXING.convert(long.class, Long.class));
		assertTrue(BOXING.convert(float.class, Float.class));
		assertTrue(BOXING.convert(double.class, Double.class));
		
		assertTrue(BOXING.convert(byte.class, Number.class));
		assertTrue(BOXING.convert(double.class, Object.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void BoxingFailsWhenToIsNull() {
		BOXING.convert(int.class, null);
	}
	
	@Test
	public void UnboxingTest() {
		assertTrue(UNBOXING.convert(Boolean.class, boolean.class));
		assertTrue(UNBOXING.convert(Byte.class, byte.class));
		assertTrue(UNBOXING.convert(Short.class, short.class));
		assertTrue(UNBOXING.convert(Character.class, char.class));
		assertTrue(UNBOXING.convert(Integer.class, int.class));
		assertTrue(UNBOXING.convert(Long.class, long.class));
		assertTrue(UNBOXING.convert(Float.class, float.class));
		assertTrue(UNBOXING.convert(Double.class, double.class));
		
		assertTrue(UNBOXING.convert(Byte.class, long.class));
		assertTrue(UNBOXING.convert(Integer.class, double.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void UnboxingFailsWhenToIsNull() {
		UNBOXING.convert(Integer.class, null);
	}
}