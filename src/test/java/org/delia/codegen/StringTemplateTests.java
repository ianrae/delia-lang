package org.delia.codegen;


import static org.junit.Assert.assertEquals;

import org.delia.app.DaoTestBase;
import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;


public class StringTemplateTests extends DaoTestBase {


	@Test
	public void test1() {
		ST hello = new ST("Hello, <name>");
		hello.add("name", "World");
		System.out.println(hello.render());
	}

	@Test
	public void test2() {
		STGroup g = new STGroupFile("templates/test.stg");
//		ST hello = new ST("Hello, <name>");
		ST hello = g.getInstanceOf("t2");
		hello.add("name1", "Sue");
		hello.add("name2", "Welch");
		
		String s = hello.render();
		System.out.println(s);
		assertEquals("hello Sue and Welch.", s);
	}

	//---

	@Before
	public void init() {
	}

}
