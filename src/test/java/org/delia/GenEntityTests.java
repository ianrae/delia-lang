package org.delia;

import org.apache.commons.lang3.StringUtils;
import org.delia.util.TextFileReader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GenEntityTests {

    public static class Info {
        String rawName;
        String camelCaseName;

        public Info(String rawName, String camelCaseName) {
            this.rawName = rawName;
            this.camelCaseName = camelCaseName;
        }
    }

    @Test
    public void testEmployeeSubscriptionMap() {
        assertEquals(1, 1);
        String path = "C:\\DAILY\\2024\\jan9\\tbl1.txt";

        TextFileReader r = new TextFileReader();
        List<String> lines = r.readFile(path);

        boolean inFields = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("`id`")) {
                inFields = true;
            } else if (line.startsWith("PRIMARY KEY")) {
                inFields = false;
            }


            if (inFields) {
//                log(line);

                Info info = convertToCamelCase(line);
                log(String.format("//%s", line));
                log(String.format("private %s;", info.camelCaseName));
                log("");
            }
        }
    }

    @Test
    public void testSubscriptionPlan() {
        assertEquals(1, 1);
        String path = "C:\\DAILY\\2024\\jan9\\tbl2.txt";

        TextFileReader r = new TextFileReader();
        List<String> lines = r.readFile(path);

        boolean inFields = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("`id`")) {
                inFields = true;
            } else if (line.startsWith("PRIMARY KEY")) {
                inFields = false;
            }


            if (inFields) {
//                log(line);

                Info info = convertToCamelCase(line);
                log(String.format("//%s", line));
                log(String.format("private %s;", info.camelCaseName));
                log("");
            }
        }
    }


    @Test
    public void test1() {
        Info info = convertToCamelCase("`id`");
        assertEquals("id", info.rawName);
        assertEquals("id", info.camelCaseName);

        info = convertToCamelCase("`subscription_plan_id`");
        assertEquals("subscription_plan_id", info.rawName);
        assertEquals("subscriptionPlanId", info.camelCaseName);
    }

    private Info convertToCamelCase(String text) {
        String s = StringUtils.substringAfter(text, "`");
        s = StringUtils.substringBefore(s, "`");

        String[] words = s.split("[\\W_]+");
        if (words.length == 1) {
            return new Info(s,s );
        }

        Info info = new Info(s, null);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                word = word.isEmpty() ? word : word.toLowerCase();
            } else {
                word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            }
            builder.append(word);
        }

        info.camelCaseName = builder.toString();
        return info;
    }

    //---
    private void log(String s) {
        System.out.println(s);
    }
}
