package org.delia.rule.rules;

import org.delia.error.ErrorTracker;
import org.delia.rule.*;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

import static java.util.Objects.isNull;

public class SizeofRule extends ShapeSpecificRuleBase {
    private RuleOperand oper1;
    private int sizeofAmount;

    public SizeofRule(RuleGuard guard, RuleOperand oper1, int sizeofAmount) {
        super("sizeof", guard);
        this.oper1 = oper1;
        this.sizeofAmount = sizeofAmount;
    }

    @Override
    public String renderAsDelia(RuleGeneratorContext ctx) {
        return String.format("%ssizeof(%d)", ctx.getOperAsDelia(oper1), sizeofAmount);
    }

    @Override
    public void performCompilerPass4Checks(DType dtype, FieldExistenceService fieldExistSvc, ErrorTracker et) {
        fieldExistSvc.checkRuleOperand(getName(), oper1, et);

        DType typeToCheck = foo(dtype, oper1.getSubject(), "sizeof-unknown-field", et);
        if (isNull(typeToCheck)) {
            return;
        }

        switch (typeToCheck.getShape()) {
            case INTEGER:
                pass4IntCheck(et);
                break;
            case STRING:
                pass4StringCheck(et);
                break;
            default:
                et.add("sizeof-not-allowed", String.format("sizeof rule not supported for type '%s' with shape '%s'", typeToCheck.getName(), typeToCheck.getShape().name()));
                ;
                break;
        }

    }

    private void pass4IntCheck(ErrorTracker et) {
        switch (sizeofAmount) {
            case 8:
            case 16:
            case 32:
            case 64:
                break;
            default:
                String msg = String.format("sizeof int only supports 8,16,32, and 64, not: %d", sizeofAmount);
                et.add("sizeof-wrong-amount", msg);
                break;
        }
    }

    private void pass4StringCheck(ErrorTracker et) {
        if (sizeofAmount <= 0) {
            String msg = String.format("sizeof must be a positive integer: %d", sizeofAmount);
            et.add("sizeof-wrong-amount", msg);
        }
    }


    @Override
    protected boolean onValidate(DValue dval, DRuleContext ctx) {
        String fieldName = oper1.getSubject();
        DValue inner = dval.getType().isStructShape() ? dval.asStruct().getField(fieldName) : dval;

        switch (inner.getType().getShape()) {
            case INTEGER:
                return validateInt(dval, ctx);
//            case LONG:
//                return checkLong(dval, ctx);
            case STRING:
                return validateString(dval, ctx);
            default:
                DeliaExceptionHelper.throwError("rule-wrong-field-type", "sizeof only supported on int and string types");
                break;
        }

        return true;
    }

    private boolean validateInt(DValue dval, DRuleContext ctx) {
        //postgres smallint, integer, and bigint. https://www.postgresql.org/docs/9.5/datatype-numeric.html#DATATYPE-INT
        int min;
        int max;

        switch (sizeofAmount) {
            case 8:
                min = -128;
                max = 127;
                return checkInt(dval, min, max, ctx);
            case 16:
                min = -32768;
                max = 32767;
                return checkInt(dval, min, max, ctx);
            case 32:
                min = Integer.MIN_VALUE; //-2147483648
                max = Integer.MAX_VALUE; //2147483647
                return checkInt(dval, min, max, ctx);
            case 64:
                return checkLong(dval, ctx);
            default:
                DeliaExceptionHelper.throwError("sizeof-wrong-amount", "sizeof int only supports 8,16,32, and 64, not: %d", sizeofAmount);
                break;
        }

        return true;
    }

    private boolean checkLong(DValue dval, DRuleContext ctx) {
        //no way to check if bigger than long, because that's the longest delia supports
        return true;
    }

    private boolean checkInt(DValue dval, int min, int max, DRuleContext ctx) {
        int n = oper1.asInt(dval);

        if (n < min) {
            String msg = String.format("int value %d smaller than sizeof(%d) allows.", n, sizeofAmount);
            ctx.addError(this, msg, oper1);
            return false;
        } else if (n > max) {
            String msg = String.format("int value %d larger than sizeof(%d) allows.", n, sizeofAmount);
            ctx.addError(this, msg, oper1);
            return false;
        }

        return true;
    }

    private boolean validateString(DValue dval, DRuleContext ctx) {
        String s = oper1.asString(dval);

        int maxlen = sizeofAmount;
        if (s.length() > maxlen) {
            String msg = String.format("string exceeds sizeof(%d) characters: string len=%d", maxlen, s.length());
            ctx.addError(this, msg, oper1);
            return false;
        }
        return true;
    }

    @Override
    public boolean dependsOn(String fieldName) {
        return oper1.dependsOn(fieldName);
    }

    @Override
    public String getSubject() {
        return oper1.getSubject();
    }

    public int getSizeofAmount() {
        return sizeofAmount;
    }
}