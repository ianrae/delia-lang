package org.delia.dbimpl.mem.impl.filter.filterfn;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.tok.Tok;
import org.delia.type.BuiltInTypes;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class FilterFunctionService extends ServiceBase {

    private final DTypeRegistry registry;
    private final ScalarValueBuilder valueBuilder;

    public FilterFunctionService(FactoryService factorySvc, DTypeRegistry registry) {
        super(factorySvc);
        this.registry = registry;
        this.valueBuilder = factorySvc.createScalarValueBuilder(registry);
    }

    public DType getFunctionType(Tok.FieldTok fexp) {
        int n = fexp.funcL.size();
        Tok.FunctionTok fnTok = fexp.funcL.get(n - 1);
        return getFunctionType(fnTok);
    }

    public DType getFunctionType(Tok.FunctionTok fexp) {
        String fnName = fexp.fnName;
        DType stringType = registry.getType(BuiltInTypes.STRING_SHAPE);
        switch (fnName) {
            case "year":
            case "month":
            case "day":
            case "hour":
            case "minute":
            case "second":
                return stringType;
            default: {
                DeliaExceptionHelper.throwError("filterfn-unknown", "unknown filterfn '%s'", fnName);
            }
        }

        return null;
    }

    public DValue exec(DValue outerVal, DValue dval, List<Tok.FunctionTok> funcs) {
        DValue resultVal = null;
        for (Tok.FunctionTok fntok : funcs) {
            resultVal = execFn(dval, fntok);  //TODO fix for more than one func. eg .year().min()
        }
        return resultVal;
    }

    private DValue execFn(DValue fieldval, Tok.FunctionTok fntok) {
        String fnName = fntok.fnName;

        switch (fnName) {
            case "year":
                return execYear(fntok, fieldval);
            case "month":
                return execMonth(fntok, fieldval);
            case "day":
                return execDate(fntok, fieldval);
            case "hour":
                return execHour(fntok, fieldval);
            case "minute":
                return execMinute(fntok, fieldval);
            case "second":
                return execSecond(fntok, fieldval);
            default:
                return null; //already handled earlier
        }

    }


    private DValue execMinute(Tok.FunctionTok fntok, DValue fieldval) {
        LocalDateTime ldt = convertDate(fieldval);
        int n = ldt.getMinute();
        return buildIntDVal(n);
    }

    private DValue execSecond(Tok.FunctionTok fntok, DValue fieldval) {
        LocalDateTime ldt = convertDate(fieldval);
        int n = ldt.getSecond();
        return buildIntDVal(n);
    }

    private DValue execHour(Tok.FunctionTok fntok, DValue fieldval) {
        LocalDateTime ldt = convertDate(fieldval);
        int n = ldt.getHour(); //1-24
        return buildIntDVal(n);
    }

    private DValue execMonth(Tok.FunctionTok fntok, DValue fieldval) {
        LocalDateTime ldt = convertDate(fieldval);
        int n = ldt.getMonthValue();
        return buildIntDVal(n);
    }

    private DValue execYear(Tok.FunctionTok fntok, DValue fieldval) {
        LocalDateTime ldt = convertDate(fieldval);
        int n = ldt.getYear();
        return buildIntDVal(n);
    }

    private DValue execDate(Tok.FunctionTok fntok, DValue fieldval) {
        LocalDateTime ldt = convertDate(fieldval);
        int n = ldt.getDayOfMonth();
        return buildIntDVal(n);
    }

    private DValue buildIntDVal(int n) {
        return valueBuilder.buildInt(n);
    }

    private LocalDateTime convertDate(DValue fieldval) {
        ZonedDateTime zdt = fieldval.asDate();
        return zdt.toLocalDateTime();
    }


}
