package falgout.utils.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.junit.Test;

public class ReflectionUtilsTest {
	@Test
	public void GetClassesTest() {
		Object[] args = { "hi", 5, null };
		Class<?>[] c = ReflectionUtils.getClasses(args);
		
		assertSame(String.class, c[0]);
		assertSame(Integer.class, c[1]);
		assertSame(null, c[2]);
	}
	
	@Test
	public void getConstructorsTest() {
		Set<Constructor<String>> constructors = ReflectionUtils.getConstructors(String.class);
		assertEquals(15, constructors.size());
		
		Set<Constructor<Integer>> ctors2 = ReflectionUtils.getConstructors(Integer.class);
		assertEquals(2, ctors2.size());
	}
	
	@Test
	public void getDeclaredConstructorsTest() {
		Set<Constructor<String>> constructors = ReflectionUtils.getDeclaredConstructors(String.class);
		assertEquals(17, constructors.size());
		
		Set<Constructor<Integer>> ctors2 = ReflectionUtils.getDeclaredConstructors(Integer.class);
		assertEquals(2, ctors2.size());
	}
}