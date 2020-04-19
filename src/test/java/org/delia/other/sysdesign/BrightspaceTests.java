package org.delia.other.sysdesign;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.delia.other.sysdesign.BrightspaceTests.TestGenerator.Question;
import org.delia.util.TextFileReader;
import org.junit.Test;

public class BrightspaceTests {
	
	public static class TestGenerator {
		private static enum State {
			START,
			IN_QUESTIONS,
			AFTER
		}
		
		public static class Question {
			private String question;
			private List<String> ansL = new ArrayList<>();
		}
		
		public TestGenerator() {
		}
		
	    public List<Question> process(String path) {
	    	TextFileReader r = new TextFileReader();
	    	List<String> list = r.readFile(path);
	    	
	    	List<Question> questL = new ArrayList<>();
	    	
	    	State state = State.START;
	    	Question quest = null;
	    	
	    	for(String line: list) {
	    		line = line.trim();
//	    		log(line);
	    		switch(state) {
	    		case START:
	    		{
	    			if (line.contains("START_QUESTIONS")) {
	    				state = State.IN_QUESTIONS;
	    			}
	    		}
	    			break;
	    		case IN_QUESTIONS:
	    			if (line.startsWith("---------------------")) {
	    				state = State.AFTER;
	    			} else {
//	    				log(line);
	    				if (line.isEmpty()) {
	    					if (quest != null) {
	    						questL.add(quest);
	    						quest = null;
	    					}
	    					continue;
	    				}
	    				if (line.startsWith("lect ")) {
	    					continue;
	    				}
	    				
	    				if (line.startsWith("q. ")) {
	    					quest = new Question();
	    					quest.question = line.substring(2);
	    				} else if (quest != null) {
	    					quest.ansL.add(line);
	    				}
	    			}
	    			break;
	    		case AFTER:
	    			break;
	    		}
	    	}
	    	log("end.");
	    	return questL;
	    }
	    
	    private void log(String line) {
	    	System.out.println(line);
		}

		public List<String> gen(List<Question> list) {
			log("---------");
			
			int errCount = 0;
			
			int questNum = 1;
			for(Question quest: list) {
				log(" ");
				String s = String.format("%d. %s", questNum, quest.question);
				log(s);
				char ch = 'a';
				int correctCount = 0;
				for(String ans: quest.ansL) {
					boolean correct = (ans.contains("*"));
					if (correct) {
						correctCount++;
					}
					String strCorrect = correct ? "*" : "";
					String tmp = ans.trim().replace("*", "");
					s = String.format("%s%c) %s", strCorrect, ch++, tmp);
					log(s);
				}
				
				if (correctCount != 1) {
					log("ERROR: no  correct ans!!!");
					errCount++;
				}
				
				questNum++;
			}
			
			if (errCount > 0) {
				log("THERE WERE ERRORS!");
			}
			
			return null;
		}

	 
	}
	
	@Test
	public void test() {
		String path = "C:/Users/ian/Documents/GitHub/basis/algonquin/sysdesign/final-exam.txt";
		TestGenerator svc = new TestGenerator();
		List<Question> list = svc.process(path);
//		assertEquals(31, list.size());
		
		List<String> lines = svc.gen(list);
	}

}
