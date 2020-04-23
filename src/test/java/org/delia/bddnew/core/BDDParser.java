package org.delia.bddnew.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class BDDParser {
	private static enum LineType {
		GIVEN,
		WHEN,
		THEN
	}
	public List<BDDTest> parse(List<String> lines) {
		List<BDDFeature> features = parseFeatures(lines);
		
		List<BDDTest> allTests = new ArrayList<>();
		for(BDDFeature feature: features) {
			List<BDDTest> tests = parse2(feature.bglines, feature.lines);
			int index = 0;
			for(BDDTest test: tests) {
				test.feature = feature;
				if (test.title == null) {
					test.title = String.format("Test%d", index);
				}
				index++;
			}
			allTests.addAll(tests);
		}
		return allTests;
	}
	
	
	private List<BDDFeature> parseFeatures(List<String> lines) {
		List<BDDFeature> features = new ArrayList<>();
		
		BDDFeature currentFeature = null;
		boolean inBackground = false;
		for(String line: lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			if (line.startsWith("FEATURE:")) {
				BDDFeature feature = new BDDFeature();
				feature.feature = parseArg(line);
				features.add(feature);
				currentFeature = feature;
			} else if (line.startsWith("background:")) {
				inBackground = true;
			} else if (inBackground && !line.startsWith("---")) {
				currentFeature.bglines.add(line);
			} else {
				if (line.startsWith("---")) {
					inBackground = false;
				}
				currentFeature.lines.add(line);
			}
		}
		return features;
	}


	public List<BDDTest> parse2(List<String> backgroundL, List<String> lines) {
		List<BDDTest> tests = new ArrayList<>();

		BDDTest currentTest = null;
		BDDParser.LineType lineType = null;
		
		for(String line: lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			if (line.startsWith("---")) {
				if (currentTest != null) {
				}
				currentTest = new BDDTest();
				tests.add(currentTest);
				
				lineType = LineType.GIVEN;
				for(String s: backgroundL) {
					lineType = parseTestLine(currentTest, s, lineType);
				}
				lineType = LineType.WHEN;
			} else if (currentTest != null) {
				lineType = parseTestLine(currentTest, line, lineType);
			}
		}

		return tests;
	}
	
	private LineType parseTestLine(BDDTest currentTest, String line, LineType lineType) {
		
		if (line.startsWith("title:")) {
			currentTest.title = parseArg(line);
			lineType = LineType.WHEN;
		} else if (line.startsWith("SKIP:")) {
			currentTest.skip = true; //parseArg(line);
			lineType = LineType.WHEN;
		} else if (line.startsWith("thenType:")) {
			currentTest.expectedType = parseArg(line);
			lineType = LineType.WHEN;
		} else if (line.startsWith("expectDVal:")) {
			currentTest.expectDVal = Boolean.valueOf(parseArg(line));
			lineType = LineType.WHEN;
		} else if (line.startsWith("chainNextTest:")) {
			currentTest.chainNextTest = true;
			lineType = LineType.WHEN;
		} else if (line.startsWith("allowSemiColons:")) {
			currentTest.allowSemiColons = true;
		} else if (line.startsWith("cleanTables:")) {
			currentTest.cleanTables = parseArg(line);
		} else if (line.startsWith("useSafeMigrationPolicy:")) {
			currentTest.useSafeMigrationPolicy = line.contains("true");
		} else if (line.startsWith("given:")) {
			lineType = LineType.GIVEN;
		} else if (line.startsWith("when:")) {
			lineType = LineType.WHEN;
		} else if (line.startsWith("then:")) {
			lineType = LineType.THEN;
		} else {
			switch(lineType) {
			case GIVEN:
				currentTest.givenL.add(line);
				break;
			case WHEN:
				if (currentTest.allowSemiColons) {
					currentTest.whenL.add(line);
				} else if (line.contains(";")) {
					String s1 = StringUtils.substringBefore(line, ";");
					String s2 = StringUtils.substringAfter(line, ";");
					currentTest.whenL.add(s1.trim());
					currentTest.thenL.add(s2.trim());
				} else {
					currentTest.whenL.add(line);
				}
				break;
			case THEN:
				currentTest.thenL.add(line);
				break;
			default:
				break;
			}
		}
		return lineType;
	}

	private String parseArg(String line) {
		String s = StringUtils.substringAfter(line, ":");
		s = s.trim();
		return s;
	}
}