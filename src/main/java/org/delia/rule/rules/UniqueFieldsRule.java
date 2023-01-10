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

public class UniqueFieldsRule extends DRuleBase {
    private List<RuleOperand> operL;

    public UniqueFieldsRule(RuleGuard guard, List<RuleOperand> operL) {
        super("uniqueFields", guard);
        this.operL = operL;
    }
    @Override
    public String renderAsDelia(RuleGeneratorContext ctx) {
        return String.format(""); //not used
    }

    @Override
    public void performCompilerPass4Checks(DType dtype, FieldExistenceService fieldExistSvc, ErrorTracker et) {
        for (RuleOperand oper : operL) {
            fieldExistSvc.checkRuleOperand(getName(), oper, et);
        }
    }

    @Override
    protected boolean onValidate(DValue dval, DRuleContext ctx) {
        if (ctx.getDBCapabilities().supportsUniqueConstraint()) {
            return true; //db will do this validation
        }

        if (operL.isEmpty()) {
            return true;
        }

        Map<String, String> map = new HashMap<>();
        if (ctx.isInsertFlag()) {
            String key = buildKey(dval);
            map.put(key, "");
        }

        //MEM
        QueryService querySvc = ctx.getQueryService();
//		DBExecutor dbexecutor = ctx.getDbInterface().createExecutor();
//		dbexecutor.init1(ctx.getRegistry());
        try {
            List<DValue> dvalList = querySvc.queryAll((DStructType) dval.getType());

            ctx.getLog().logDebug("xxxxxxzz " + dvalList.size());
            for (DValue inner : dvalList) {
                String key = buildKey(inner);
                if (map.containsKey(key)) {
                    List<String> nameL = operL.stream().map(x -> x.getSubject()).collect(Collectors.toList());
                    String msg = String.format("fields (%s) are not unique", StringUtil.flatten(nameL));
                    ctx.addError(this, msg, operL.get(0));
                    return false;
                }
                map.put(key, "");
            }
            ctx.getLog().logDebug("xxxxxxszz " + map.size());

        } finally {
        }
        return true;
    }

    private String buildKey(DValue dval) {
        //TODO: improve this algorithm. need proper way to make key from many dvalue fields
        StringJoiner joiner = new StringJoiner(",");
        for (RuleOperand oper : operL) {
            String fieldName = oper.getSubject();
            DValue inner = dval.asStruct().getField(fieldName);
            if (inner == null) {
                joiner.add("___NULL___");
            } else {
                joiner.add(inner.asString());
            }
        }

        return joiner.toString();
    }

    @Override
    public boolean dependsOn(String fieldName) {
        for (RuleOperand oper : operL) {
            if (oper.dependsOn(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSubject() {
        if (operL.isEmpty()) return null;
        return operL.get(0).getSubject(); //TODO: is this ok
    }

    public List<RuleOperand> getOperList() {
        return operL;
    }
}