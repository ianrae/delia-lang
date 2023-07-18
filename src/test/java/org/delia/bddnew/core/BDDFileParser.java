package org.delia.bddnew.core;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
                String thenType = parseThenTypeIfPresent(line);
                String expectDVal = parseExpectDValueIfPresent(line);
                Boolean runBGPerTest = parseRunBGPerTest(line);
                if (thenType != null) {
                    currentFeature.expectedType = thenType;
                } else if (runBGPerTest != null) {
                    currentFeature.runBackgroundPerTest = runBGPerTest;
                } else if (expectDVal != null) {
                    //TODO
                } else {
                    bgSnippet.lines.add(line);
                }
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

            currentFeature.backgroundsL = currentFeature.backgroundsL.stream()
                    .filter(snip -> !snip.lines.isEmpty())
                    .collect(Collectors.toList());;
        }

        return currentFeature;
    }

    private String parseThenTypeIfPresent(String line) {
        String target = "thenType:";
        if (! line.contains(target)) {
            return null;
        }
        String s = StringUtils.substringAfter(line, target);
        s = s.trim();
        return s;
    }
    private String parseExpectDValueIfPresent(String line) {
        String target = "expectDVal:";
        if (! line.contains(target)) {
            return null;
        }
        String s = StringUtils.substringAfter(line, target);
        s = s.trim();
        return s;
    }
    private Boolean parseRunBGPerTest(String line) {
        String target = "runBackgroundPerTest:";
        if (! line.contains(target)) {
            return null;
        }
        String s = StringUtils.substringAfter(line, target);
        s = s.trim();
        return Boolean.parseBoolean(s);
    }



    private SnippetType getSnippetType(String line) {
        if (! line.contains("(")) {
            return line.startsWith("then:") ? SnippetType.VALUES : SnippetType.DELIA;
        }
        if (line.contains(";date(")) {
            return SnippetType.DELIA;
        }
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
        List<String> list = Arrays.asList("background(sql):", "background(delia):", "background(seede):", "background(values):", "background:");
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

//            if (line.contains("then:")) {
//                System.out.println("sedf");
//            }

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
            } else if (currentTest != null && line.startsWith("allowSemiColons:")) {
                currentTest.allowSemiColons = true;
            } else if (currentTest != null && line.startsWith("SKIP:")) {
                Boolean b = true; //Boolean.parseBoolean(parseArg(line));
                currentTest.skip = b;
            } else if (currentTest != null && line.startsWith("thenType:")) {
                String thenType = parseThenTypeIfPresent(line);
                currentTest.expectedType = thenType;
            } else if (currentTest != null && line.startsWith("bulkInsert:")) {
                currentTest.bulkInsertEnabled = true;
            } else if (currentTest != null && line.startsWith("expectDVal:")) {
                boolean b = (parseExpectDValueIfPresent(line) != null);
                currentTest.expectDValFlag = b;
            } else if (currentTest != null && line.startsWith("chainNextTest:")) {
                Boolean b = Boolean.parseBoolean(parseArg(line));
                currentTest.chainNextTest = b;
            } else if (inSnippet && !line.startsWith("---")) {
                inSnippet = addLineOrSplitLine(snippet, line, currentTest);
                if (! inSnippet) {
                    currentTest = null;
                }
            } else if (isDefaultingToWhen(currentTest, inSnippet)) {
                snippet = new BDDSnippet();
                snippet.type = getSnippetType(line);
                inSnippet = true;
                addSnippetToTest(currentTest, snippet, "when:");

                inSnippet = addLineOrSplitLine(snippet, line, currentTest);
                if (! inSnippet) {
                    currentTest = null;
                }
            } else {
            }
        }
    }

    private boolean addLineOrSplitLine(BDDSnippet snippet, String line, BDDTest currentTest) {
        //using ; is problematic is actual delia contains it
        //TOD replace ; with something else later
        if (! currentTest.allowSemiColons && line.contains(";")) {
            //single line form: ..delia...;..thenvalue...
            String s = StringUtils.substringBefore(line, ";").trim();
            snippet.lines.add(s);

            s = StringUtils.substringAfter(line, ";").trim();
            BDDSnippet thenSnippet = new BDDSnippet();
            thenSnippet.type = SnippetType.VALUES;
            addSnippetToTest(currentTest, thenSnippet, "then:");
            thenSnippet.lines.add(s);
//            inSnippet = false;
//            currentTest = null;
            return false; //snippet has ended
        } else {
            snippet.lines.add(line);
            return true; //still in snippet
        }

    }

    private boolean isDefaultingToWhen(BDDTest currentTest, boolean inSnippet) {
        return (currentTest != null && ! inSnippet);
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
        List<String> list = Arrays.asList("given(", "when(", "then(", "given:", "when:", "then:");
        for (String s : list) {
            if (line.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
