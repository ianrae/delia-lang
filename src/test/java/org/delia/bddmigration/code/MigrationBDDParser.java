package org.delia.bddmigration.code;

import org.delia.util.DeliaExceptionHelper;
import org.delia.util.StringUtil;
import org.delia.util.TextFileReader;

import java.util.ArrayList;
import java.util.List;

public class MigrationBDDParser {
    public boolean keepLineFeeds = false;

    public MigrationBDDTest readTest(String path) {
        TextFileReader r = new TextFileReader();
        List<String> lines = r.readFile(path);
        int part = 0;
        MigrationBDDTest bddTest = new MigrationBDDTest();
        List<String> partLines = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("GIVEN1")) {
                part = 1;
            } else if (line.startsWith("GIVEN2")) {
                bddTest.text1 = flattenText(partLines);
                partLines.clear();
                part = 2;
            } else if (line.startsWith("THEN")) {
                bddTest.text2 = flattenText(partLines);
                partLines.clear();
                part = 3;
            } else {
                if (part >= 1 && part <= 3) {
                    partLines.add(line);
                }
            }
        }

        if (part == 2) {
            bddTest.text2 = flattenText(partLines);
        } else if (part == 3) {
            bddTest.text3 = flattenText(partLines);
        } else {
            DeliaExceptionHelper.throwError("bad-migration-bdd-file", path);
        }

        return bddTest;
    }

    private String flattenText(List<String> partLines) {
        if (keepLineFeeds) {
            return StringUtil.flattenEx(partLines, System.lineSeparator());
        }
        return StringUtil.flattenNoComma(partLines);
    }
}
