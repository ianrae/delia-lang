package org.delia.other;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.delia.bddnew.NewBDDBase.BDDGroup;
import org.delia.bddnew.NewBDDBase.FileHelper;
import org.junit.Test;

public class OtherTests {
	
	static class Shape {
		public int x;
		public int y;
	}
	
	static class Circle extends Shape {
		public int radius;
	}

	@Test
	public void test() {
		Class<?> clazzShape = Shape.class;
		Class<?> clazzCir = Circle.class;
		
		boolean b = clazzShape.isAssignableFrom(clazzCir);
		assertEquals(true, b);
		b = clazzCir.isAssignableFrom(clazzShape);
		assertEquals(false, b);
	}
	
	@Test
	public void testFiles() {
		FileHelper fileHelper = new FileHelper();
		
		String dir = fileHelper.getDir(BDDGroup.R1600_let_fetch);
		File file = new File(dir);       
		Collection<File> files = FileUtils.listFiles(file, null, false);     
		for(File file2 : files){
		    System.out.println(file2.getName());            
		}		
	}
}
