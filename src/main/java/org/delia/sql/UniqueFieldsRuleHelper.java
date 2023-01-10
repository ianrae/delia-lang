package org.delia.sql;

import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.type.DStructType;

import java.util.List;
import java.util.stream.Collectors;

public class UniqueFieldsRuleHelper {
    public static List<UniqueFieldsRule> buildUniqueFields(DStructType structType) {
        List<UniqueFieldsRule> uniqueFieldsList = structType.getRawRules().stream()
                .filter(x -> x instanceof UniqueFieldsRule)
                .map(x -> (UniqueFieldsRule) x).collect(Collectors.toList());
        return uniqueFieldsList;
    }

}
