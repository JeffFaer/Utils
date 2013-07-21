package falgout.utils.reflection;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

public class JLSMethodLocatorTest {
    private static final Class<?> CLAZZ = JLSMethodLocatorTest.class;
    private static final String NAME = "foo";
    
    public static void foo(String o1, String o2, String... o3) {}
    
    public static void foo(String o1, String o2, String o3) {}
    
    public static void foo(Object o1, Object o2, String o3) {}
    
    public static void foo(String o1, Object o2, Object o3) {}
    
    private static final Method m1;
    private static final Method m2;
    private static final Method m3;
    private static final Method m4;
    static {
        try {
            m1 = CLAZZ.getMethod(NAME, String.class, String.class, String[].class);
            m2 = CLAZZ.getMethod(NAME, String.class, String.class, String.class);
            m3 = CLAZZ.getMethod(NAME, Object.class, Object.class, String.class);
            m4 = CLAZZ.getMethod(NAME, String.class, Object.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }
    
    private static MethodLocator l = new JLSMethodLocator();
    
    @Test(expected = NoSuchMethodException.class)
    public void NonExistentMethodTest() throws AmbiguousDeclarationException, NoSuchMethodException {
        l.getMethod(Collections.<Method> emptyList(), Class.class, "foo");
    }
    
    @Test(expected = NoSuchMethodException.class)
    public void NonExistentMethodWithNullArguments() throws AmbiguousDeclarationException, NoSuchMethodException {
        l.getMethod(Collections.<Method> emptyList(), Class.class, "foo", null, null);
    }
    
    @Test(expected = AmbiguousDeclarationException.class)
    public void AmbiguousDeclarationTest() throws AmbiguousDeclarationException, NoSuchMethodException {
        l.getMethod(CLAZZ, NAME, String.class, Object.class, String.class);
    }
    
    @Test
    public void CanRetrieveAllAmbiguousDeclarations() throws NoSuchMethodException {
        assertEquals(new HashSet<>(Arrays.asList(m3, m4)),
                l.getMethods(CLAZZ, NAME, String.class, Object.class, String.class));
    }
    
    @Test
    public void PicksVarArgsLast() throws NoSuchMethodException, AmbiguousDeclarationException {
        assertEquals(m2, l.getMethod(CLAZZ, NAME, String.class, String.class, String.class));
    }
    
    @Test
    public void PicksVarArgsIfOnlyPresent() throws AmbiguousDeclarationException, NoSuchMethodException {
        assertEquals(m1, l.getMethod(Arrays.asList(m1), CLAZZ, NAME, String.class, String.class));
    }
    
    @Test
    public void PicksVarArgsWithManyArguments() throws AmbiguousDeclarationException, NoSuchMethodException {
        assertEquals(m1, l.getMethod(CLAZZ, NAME, "", "", "", "", "", "", "", ""));
    }
    
    @Test
    public void VarArgsWithNullArray() throws AmbiguousDeclarationException, NoSuchMethodException {
        assertEquals(m1, l.getMethod(Arrays.asList(m1), CLAZZ, NAME, String.class, String.class, null));
    }
}
