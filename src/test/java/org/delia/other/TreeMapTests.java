package org.delia.other;

import static org.junit.Assert.*;

import java.util.TreeMap;

import org.junit.Test;

public class TreeMapTests {
	
	@Test
	public void test() {
		TreeMap<String,String> map = new TreeMap<>();

		map.put("1", "1");
		map.put("4", "1a");
		map.put("2", "1b");
		map.put("10", "1c");
		map.put("7", "1c");
		
		for(String key: map.keySet()) {
			log(key);
		}
	}

	private void log(String key) {
		System.out.println(key);
		// TODO Auto-generated method stub
		
	}
}
