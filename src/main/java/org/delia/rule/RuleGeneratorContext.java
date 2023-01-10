package org.delia.rule;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class RuleGeneratorContext {
    public Map<String,String> replaceMap = new HashMap<>(); //oldName,newName

    public String getOperAsDelia(RuleOperand oper1) {
        String name = oper1.getSubject();
        if (isNull(name)) {
            return "";
        }
        if (replaceMap.containsKey(name)) {
            name = replaceMap.get(name);
        }
        return String.format("%s.", name);
    }
}
