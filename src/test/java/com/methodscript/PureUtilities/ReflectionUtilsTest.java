package com.methodscript.PureUtilities;

import com.methodscript.PureUtilities.ClassLoading.ClassDiscovery;
import com.methodscript.PureUtilities.Common.ReflectionUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 */
public class ReflectionUtilsTest {

	public ReflectionUtilsTest() {
	}

	@Before
	public void setUp() {
		ClassDiscovery.getDefaultInstance()
				.addDiscoveryLocation(ClassDiscovery.GetClassContainer(ReflectionUtilsTest.class))
				.addDiscoveryLocation(ClassDiscovery.GetClassContainer(ReflectionUtils.class));
	}

	class A {

		B bObj = new B();
	}

	class B {

		C cObj = new C();
	}

	class C {

		String obj = "string";
	}

	@Test
	public void testRecursiveGet() {
		A a = new A();
		String result = (String) ReflectionUtils.get(A.class, a, "bObj.cObj.obj");
		assertEquals("string", result);
	}

	@Test
	public void testFuzzyLookup() {
		Class expected = ReflectionUtils.class;
		Class actual = ClassDiscovery.getDefaultInstance().forFuzzyName("com.methodscript.Pur.*", "ReflectionUtils").loadClass();
		assertEquals(expected, actual);
	}

}
