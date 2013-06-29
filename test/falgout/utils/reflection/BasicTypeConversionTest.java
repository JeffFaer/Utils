package falgout.utils.reflection;

import static falgout.utils.reflection.BasicTypeConversion.IDENTITY;
import static falgout.utils.reflection.BasicTypeConversion.WIDENING_REFERENCE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class BasicTypeConversionTest {
	@Test
	public void IdentityConversionTest() {
		assertTrue(IDENTITY.convert(String.class, String.class));
		assertFalse(IDENTITY.convert(Object.class, String.class));
		assertFalse(IDENTITY.convert(String.class, Object.class));
		assertFalse(IDENTITY.convert(null, String.class));
	}
	
	@Test
	public void WideningPrimitiveTest() {
		fail();
	}
	
	@Test
	public void NarrowingPrimitiveTest() {
		fail();
	}
	
	@Test
	public void WideningAndNarrowingPrimitiveTest() {
		fail();
	}
	
	@Test
	public void WideningReferenceTest() {
		assertTrue(WIDENING_REFERENCE.convert(String.class, Object.class));
		assertTrue(WIDENING_REFERENCE.convert(null, Object.class));
		assertTrue(WIDENING_REFERENCE.convert(null, String.class));
		assertFalse(WIDENING_REFERENCE.convert(Object.class, String.class));
	}
	
	@Test
	public void BoxingTest() {
		fail();
	}
	
	@Test
	public void UnboxingTest() {
		fail();
	}
}