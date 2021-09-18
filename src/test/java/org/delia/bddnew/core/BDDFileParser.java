package org.delia.bddnew.core;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BDDFileParser {

    public BDDFeature parse(List<String> lines) {
        BDDFeature feature = parseFeature(lines);
        if (feature == null) {
            return null;
        }

        parseTests(feature, lines);
        return feature;
    }

    private BDDFeature parseFeature(List<String> lines) {
        BDDFeature currentFeature = null;
        boolean inBackground = false;
        BDDSnippet bgSnippet = null;

        int index = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                index++;
                continue;
            }
            if (line.startsWith("FEATURE:")) {
                BDDFeature feature = new BDDFeature();
                feature.name = parseArg(line);
                currentFeature = feature;
            } else if (isBackgroundLine(line)) {
                inBackground = true;
                bgSnippet = new BDDSnippet();
                bgSnippet.type = getSnippetType(line);
                currentFeature.backgroundsL.add(bgSnippet);
            } else if (inBackground && !line.startsWith("---")) {
                bgSnippet.lines.add(line);
            } else {
                if (line.startsWith("---")) {
                    break;
                }
                bgSnippet.lines.add(line);
            }
            index++;
        }

        if (currentFeature != null) {
            currentFeature.startLineIndex = index;
        }

        return currentFeature;
    }

    private SnippetType getSnippetType(String line) {
        String s = StringUtils.substringAfter(line, "(");
        s = StringUtils.substringBefore(s, ")");
        s = s.trim().toUpperCase(Locale.ROOT);
        return SnippetType.valueOf(s);
    }

    private String parseArg(String line) {
        String s = StringUtils.substringAfter(line, ":");
        s = s.trim();
        return s;
    }

    private boolean isBackgroundLine(String line) {
        List<String> list = Arrays.asList("background(sql):", "background(delia):", "background(seede):", "background(values):");
        for (String s : list) {
            if (line.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private void parseTests(BDDFeature feature, List<String> lines) {
        BDDTest currentTest = null;
        BDDSnippet snippet = null;
        boolean inSnippet = false;

        for (int index = feature.startLineIndex; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("---")) {
                currentTest = new BDDTest();
                feature.testsL.add(currentTest);
            } else if (isSnippetStart(line)) {
                snippet = new BDDSnippet();
                snippet.type = getSnippetType(line);
                inSnippet = true;
                addSnippetToTest(currentTest, snippet, line);
                String extra = parseArg(line);
                if (!extra.isEmpty()) {
                    snippet.lines.add(line);
                }
            } else if (currentTest != null && line.startsWith("title:")) {
                currentTest.title = parseArg(line);
            } else if (currentTest != null && line.startsWith("skip:")) {
                Boolean b = Boolean.parseBoolean(parseArg(line));
                currentTest.skip = b;
            } else if (currentTest != null && line.startsWith("chainNextTest:")) {
                Boolean b = Boolean.parseBoolean(parseArg(line));
                currentTest.chainNextTest = b;
            } else if (inSnippet && !line.startsWith("---")) {
                snippet.lines.add(line);
            } else {
            }
        }

    }

    private void addSnippetToTest(BDDTest test, BDDSnippet snippet, String line) {
        if (line.startsWith("given")) {
            test.givenL.add(snippet);
        } else if (line.startsWith("when")) {
            test.whenL.add(snippet);
        } else if (line.startsWith("then")) {
            test.thenL.add(snippet);
        }
    }

    private boolean isSnippetStart(String line) {
        List<String> list = Arrays.asList("given(", "when(", "then(");
        for (String s : list) {
            if (line.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
