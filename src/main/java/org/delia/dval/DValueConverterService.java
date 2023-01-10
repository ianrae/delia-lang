package org.delia.dval;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.*;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.time.ZonedDateTime;

public class DValueConverterService extends ServiceBase {

    private DateFormatService fmtSvc;

    public DValueConverterService(FactoryService factorySvc) {
        super(factorySvc);
        this.fmtSvc = factorySvc.getDateFormatService();
    }

    public DValue buildDefaultValue(Shape shape, ScalarValueBuilder builder) {
        DValue inner = null;
        switch (shape) {
            case INTEGER:
                inner = buildInt("0", builder);
                break;
//            case LONG:
//                inner = buildLong("0", builder);
//                break;
            case NUMBER:
                inner = buildNumber("0.0", builder);
                break;
            case BOOLEAN:
                inner = buildBoolean("false", builder);
                break;
            case STRING:
                inner = buildString("", builder);
                break;
            case DATE:
                inner = buildDate("1970", builder);
                break;
            case BLOB:
                inner = buildBlob("", builder);
                break;
            default:
                //err not supported
                break;
        }
        return inner;
    }

    public DValue buildFromObject(Object input, Shape shape, ScalarValueBuilder builder, DType hintType) {
        DValue inner = null;
        switch (shape) {
            case INTEGER:
                inner = buildInt(input, builder, hintType);
                break;
//            case LONG:
//                inner = buildLong(input, builder, hintType);
//                break;
            case NUMBER:
                inner = buildNumber(input, builder, hintType);
                break;
            case BOOLEAN:
                inner = buildBoolean(input, builder, hintType);
                break;
            case STRING:
                inner = buildString(input, builder, hintType);
                break;
            case DATE:
                inner = buildDate(input, builder, hintType);
                break;
            case BLOB:
                inner = buildBlob("", builder, hintType);
                break;
            default:
                //err not supported
                break;
        }
        return inner;
    }

    private DValue buildInt(Object input, ScalarValueBuilder builder) {
        return buildInt(input, builder, builder.getTypeForShape(Shape.INTEGER));
    }

    private DValue buildInt(Object input, ScalarValueBuilder builder, DType hintType) {
        if (input == null) {
            return null;
        }

        if (input instanceof Integer) {
            Integer value = (Integer) input;
            return builder.buildInt(value, hintType);
        } else if (input instanceof Long) {
            Long value = (Long) input;
            return builder.buildInt(value, hintType);
        } else {
            String s = input.toString();
            return builder.buildInt(s, hintType);
        }
    }

//    private DValue buildLong(Object input, ScalarValueBuilder builder) {
//        return buildLong(input, builder, builder.getTypeForShape(Shape.LONG));
//    }
//    private DValue buildLong(Object input, ScalarValueBuilder builder, DType hintType) {
//        if (input == null) {
//            return null;
//        }
//
//        if (input instanceof Long) {
//            Long value = (Long) input;
//            return builder.buildLong(value, hintType);
//        } else {
//            String s = input.toString();
//            return builder.buildLong(s, hintType);
//        }
//    }

    private DValue buildNumber(Object input, ScalarValueBuilder builder) {
        return buildNumber(input, builder, builder.getTypeForShape(Shape.NUMBER));
    }

    private DValue buildNumber(Object input, ScalarValueBuilder builder, DType hintType) {
        if (input == null) {
            return null;
        }

        if (input instanceof Double) {
            Double value = (Double) input;
            return builder.buildNumber(value, hintType);
        } else {
            String s = input.toString();
            return builder.buildNumber(s, hintType);
        }
    }

    private DValue buildBoolean(Object input, ScalarValueBuilder builder) {
        return buildBoolean(input, builder, builder.getTypeForShape(Shape.BOOLEAN));
    }

    private DValue buildBoolean(Object input, ScalarValueBuilder builder, DType hintType) {
        if (input == null) {
            return null;
        }

        if (input instanceof Boolean) {
            Boolean value = (Boolean) input;
            return builder.buildBoolean(value, hintType);
        } else {
            String s = input.toString();
            return builder.buildBoolean(s, hintType);
        }
    }

    private DValue buildString(Object input, ScalarValueBuilder builder) {
        return buildString(input, builder, builder.getTypeForShape(Shape.STRING));
    }

    private DValue buildString(Object input, ScalarValueBuilder builder, DType hintType) {
        if (input == null) {
            return null;
        }

        String s = input.toString();
        return builder.buildString(s, hintType);
    }

    private DValue buildDate(Object input, ScalarValueBuilder builder) {
        return buildDate(input, builder, builder.getTypeForShape(Shape.DATE));
    }

    private DValue buildDate(Object input, ScalarValueBuilder builder, DType hintType) {
        if (input == null) {
            return null;
        }

        if (input instanceof ZonedDateTime) {
            ZonedDateTime value = (ZonedDateTime) input;
            return builder.buildDate(value, hintType);
        } else {
            String s = input.toString();
            return builder.buildDate(s, hintType);
        }
    }

    private DValue buildBlob(String input, ScalarValueBuilder builder) {
        return buildBlob(input, builder, builder.getTypeForShape(Shape.BLOB));
    }

    private DValue buildBlob(String input, ScalarValueBuilder builder, DType hintType) {
        if (input == null) {
            return null;
        }
        DValue blob = builder.buildBlob(input, hintType);
        return blob;
    }


    public DValue normalizeValue(DValue dval, DType type, ScalarValueBuilder builder) {
        if (dval == null) {
            return null;
        }
        DValue inner = null;
        switch (type.getShape()) {
            case INTEGER:
//                if (dval.getType().isShape(Shape.LONG)) {
//                    long nval = dval.asLong();
//                    if (nval < (long) Integer.MIN_VALUE || nval > (long) Integer.MAX_VALUE) {
//                        throwOnFail(type.getName(), nval);
//                    }
//                }
                inner = buildInt(dval.getObject(), builder, type);
                break;
//            case LONG:
//                inner = buildLong(dval.asLong(), builder, type);
//                break;
            case NUMBER:
                inner = buildNumber(dval.asNumber(), builder, type);
                break;
            case BOOLEAN:
                if (typeMatches(BuiltInTypes.BOOLEAN_SHAPE, type, dval)) {
                    inner = dval;
                } else {
                    inner = buildBoolean(dval.asBoolean(), builder, type);
                }
                break;
            case STRING:
                if (typeMatches(BuiltInTypes.STRING_SHAPE, type, dval)) {
                    inner = dval;
                } else {
                    inner = buildString(dval.asString(), builder, type);
                }
                break;
            case DATE:
                if (typeMatches(BuiltInTypes.DATE_SHAPE, type, dval)) {
                    inner = dval;
                } else {
                    inner = buildDate(dval.asString(), builder, type); //TODO does this work?
                }
                break;
            case BLOB:
                if (typeMatches(BuiltInTypes.BLOB_SHAPE, type, dval)) {
                    inner = dval;
                } else {
                    inner = buildBlob(dval.asString(), builder, type);
                }
                break;
            default:
                //err not supported
                break;
        }
        return inner;
    }

    //return true if dval is dtype's shape and is a native (ie. BuiltInType) type.
    //If is a custom string type, for example, return false so we can build the custom type
    private boolean typeMatches(BuiltInTypes targetBuiltInType, DType type, DValue dval) {
        if (type.getShape() != dval.getType().getShape()) {
            return false;
        }
        BuiltInTypes bit = BuiltInTypes.getAsBuiltInScalarTypeName(type.getName());
        return targetBuiltInType.equals(bit);
    }

    protected void throwOnFail(String typeStr, Object value) {
        //FUTURE propogate errors from inner builder
        String s = value == null ? "NULL" : value.toString();
        String msg = String.format("%s value is not an %s - %s", typeStr, typeStr, s);
        et.add("wrong-type", msg);
        DeliaExceptionHelper.throwError("wrong-type", msg);
    }

    public DType getType(DType dtype, Shape shape, DTypeRegistry registry) {
        if (dtype != null) return dtype;
        return registry.getType(BuiltInTypes.getBuiltInTypeOf(shape));
    }


//    public Exp createExpFor(DValue inner) {
//        switch (inner.getType().getShape()) {
//            case INTEGER:
//                return new IntegerExp(inner.asInt());
//            case LONG:
//                return new LongExp(inner.asLong());
//            case NUMBER:
//                return new NumberExp(inner.asNumber());
//            case STRING:
//                return new StringExp(inner.asString());
//            case BOOLEAN:
//                return new BooleanExp(inner.asBoolean());
//            case DATE: {
//                String s = fmtSvc.format(inner.asDate());
//                return new StringExp(s);
//            }
//            default:
//                //err
//                return null;
//        }
//    }
//
//    public DValue createDValueFrom(FilterCond cond, ScalarValueBuilder builder, boolean dontThrowException) {
//        if (cond instanceof IntegerFilterCond) {
//            IntegerFilterCond cc = (IntegerFilterCond) cond;
//            return builder.buildInt(cc.asInt());
//        } else if (cond instanceof LongFilterCond) {
//            LongFilterCond cc = (LongFilterCond) cond;
//            return builder.buildLong(cc.asLong());
//        } else if (cond instanceof StringFilterCond) {
//            StringFilterCond cc = (StringFilterCond) cond;
//            return builder.buildString(cc.asString());
//        } else {
//            if (!dontThrowException) {
//                DeliaExceptionHelper.throwError("invalid-filter-value-type", "can't convert filter");
//            }
//            return null;
//        }
//    }
//
//    public Object extractObj(Exp.ElementExp exp) {
//        if (exp instanceof Exp.NullExp) {
//            return null;
//        } else if (exp instanceof IntegerExp) {
//            Integer n = ((IntegerExp) exp).val;
//            return n;
//        } else if (exp instanceof LongExp) {
//            Long n = ((LongExp) exp).val;
//            return n;
//        } else if (exp instanceof NumberExp) {
//            Double n = ((NumberExp) exp).val;
//            return n;
//        } else if (exp instanceof BooleanExp) {
//            Boolean n = ((BooleanExp) exp).val;
//            return n;
//        } else if (exp instanceof StringExp) {
//            return exp.strValue();
//        } else {
//            //date and relation should be handled in another layer
//            return exp.strValue();
//        }
//    }


//    public DValue createDValFromExp(Exp valueExp, ScalarValueBuilder builder) {
//        return createDValFromExp(valueExp, builder, true);
//    }

//    public DValue createDValFromExp(Exp valueExp, ScalarValueBuilder builder, boolean treatUnknownAsString) {
//        if (valueExp instanceof IntegerExp) {
//            IntegerExp exp = (IntegerExp) valueExp;
//            return builder.buildInt(exp.val);
//        } else if (valueExp instanceof LongExp) {
//            LongExp exp = (LongExp) valueExp;
//            return builder.buildLong(exp.val);
//        } else if (valueExp instanceof BooleanExp) {
//            BooleanExp exp = (BooleanExp) valueExp;
//            return builder.buildBoolean(exp.val);
//        } else if (valueExp instanceof NumberExp) {
//            NumberExp exp = (NumberExp) valueExp;
//            return builder.buildNumber(exp.val);
//            //note. date _must_ be explicit type (since it's formatted as a string
//        } else if (valueExp instanceof StringExp) {
//            StringExp exp = (StringExp) valueExp;
//            return builder.buildString(exp.val);
//        } else if (valueExp instanceof NullExp) {
//            return null;
//        } else { //treat as string
//            if (treatUnknownAsString) {
//                return builder.buildString(valueExp.strValue());
//            } else {
//                return null;
//            }
//        }
//    }

}