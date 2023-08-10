package org.delia.rule.rules;

import org.delia.error.ErrorTracker;
import org.delia.rule.*;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.StringUtil;
import org.delia.core.QueryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SqlTableNameRule extends DRuleBase {
    private String tableName;

    public SqlTableNameRule(RuleGuard guard, String tableName) {
        super("sqlTableName", guard);
        this.tableName = tableName;
    }
    @Override
    public String renderAsDelia(RuleGeneratorContext ctx) {
        return String.format(""); //not used
    }

    @Override
    public void performCompilerPass4Checks(DType dtype, FieldExistenceService fieldExistSvc, ErrorTracker et) {
//        for (RuleOperand oper : operL) {
//            fieldExistSvc.checkRuleOperand(getName(), oper, et);
//        }
    }

    @Override
    protected boolean onValidate(DValue dval, DRuleContext ctx) {
        return true; //nothing to do
    }


    @Override
    public boolean dependsOn(String fieldName) {
        return false;
    }

    @Override
    public String getSubject() {
        return null;
    }

}