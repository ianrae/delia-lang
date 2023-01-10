package org.delia;

import java.util.List;

import org.delia.util.DirectoryUtil;
import org.delia.util.TextFileReader;
import org.junit.Before;
import org.junit.Test;

/**
 * I often comment out a file as part of refactoring.
 * This test identifies them so i can later delete them.
 * 
 * @author Ian Rae
 *
 */
public class FindEmptyClassesTests  { 
	
	@Test
	public void test1() {
		String dir = "src/main/java";
		
		List<String> list = DirectoryUtil.getFilesInRecursive(dir, "java"); 
		//assertEquals(22, list.size());
		
		log("--list--");
		for(String path: list) {
			if (fileIsCommentedOut(path)) {
//				log(path);
			}
		}
	}	
	
	private boolean fileIsCommentedOut(String path) {
		TextFileReader r = new TextFileReader();
		List<String> list = r.readFile(path);
		int adjacentCount = 0;
		int maxAdjacentCount = 0;
		int state = 0;
		for(String line: list) {
			line = line.trim();
			
			if (state == 0 ) {
				if (line.startsWith("//")) {
					state = 1;
					adjacentCount = 1;
				} else {
					adjacentCount = 0;
				}
			} else {
				if (line.startsWith("//")) {
					adjacentCount++;
				} else {
					maxAdjacentCount = Integer.max(maxAdjacentCount, adjacentCount);
					state = 0;
				}
			}
		}
		
		maxAdjacentCount = Integer.max(maxAdjacentCount, adjacentCount);


		boolean tooMany = maxAdjacentCount > 20;
		if (tooMany) {
			log(String.format("%s:  %d", path, maxAdjacentCount));
		}
		return tooMany;
	}

	@Before
	public void init() {
	}

	private void log(String s) {
		System.out.println(s);
	}
}
