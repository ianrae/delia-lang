package org.delia.dval;

import java.time.ZonedDateTime;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hld.cond.FilterCond;
import org.delia.db.hld.cond.IntegerFilterCond;
import org.delia.db.hld.cond.LongFilterCond;
import org.delia.db.hld.cond.StringFilterCond;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

public class DValueConverterService extends ServiceBase {

	private DateFormatService fmtSvc;

	public DValueConverterService(FactoryService factorySvc) {
		super(factorySvc);
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	public DValue buildFromObject(Object input, Shape shape, ScalarValueBuilder builder) {
		DValue inner = null;
		switch(shape) {
		case INTEGER:
			inner = buildInt(input, builder);
			break;
		case LONG:
			inner = buildLong(input, builder);
			break;
		case NUMBER:
			inner = buildNumber(input, builder);
			break;
		case BOOLEAN:
			inner = buildBoolean(input, builder);
			break;
		case STRING:
			inner = buildString(input, builder);
			break;
		case DATE:
			inner = buildDate(input, builder);
			break;
		default:
			//err not supported
			break;
		}
		return inner;
	}

	private DValue buildInt(Object input, ScalarValueBuilder builder) {
		if (input == null) {
			return null;
		}

		if (input instanceof Integer) {
			Integer value = (Integer) input; 
			return builder.buildInt(value);
		} else {
			String s = input.toString();
			return builder.buildInt(s);
		}
	}
	private DValue buildLong(Object input, ScalarValueBuilder builder) {
		if (input == null) {
			return null;
		}

		if (input instanceof Long) {
			Long value = (Long) input; 
			return builder.buildLong(value);
		} else {
			String s = input.toString();
			return builder.buildLong(s);
		}
	}
	private DValue buildNumber(Object input, ScalarValueBuilder builder) {
		if (input == null) {
			return null;
		}

		if (input instanceof Double) {
			Double value = (Double) input; 
			return builder.buildNumber(value);
		} else {
			String s = input.toString();
			return builder.buildNumber(s);
		}
	}
	private DValue buildBoolean(Object input, ScalarValueBuilder builder) {
		if (input == null) {
			return null;
		}

		if (input instanceof Boolean) {
			Boolean value = (Boolean) input; 
			return builder.buildBoolean(value);
		} else {
			String s = input.toString();
			return builder.buildBoolean(s);
		}
	}
	private DValue buildString(Object input, ScalarValueBuilder builder) {
		if (input == null) {
			return null;
		}

		String s = input.toString();
		return builder.buildString(s);
	}
	private DValue buildDate(Object input, ScalarValueBuilder builder) {
		if (input == null) {
			return null;
		}

		if (input instanceof ZonedDateTime) {
			ZonedDateTime value = (ZonedDateTime) input; 
			return builder.buildDate(value);
		} else {
			String s = input.toString();
			return builder.buildDate(s);
		}
	}
	
	
	public Exp createExpFor(DValue inner) {
		switch(inner.getType().getShape()) {
		case INTEGER:
			return new IntegerExp(inner.asInt());
		case LONG:
			return new LongExp(inner.asLong());
		case NUMBER:
			return new NumberExp(inner.asNumber());
		case STRING:
			return new StringExp(inner.asString());
		case BOOLEAN:
			return new BooleanExp(inner.asBoolean());
		case DATE:
		{
			String s = fmtSvc.format(inner.asDate());
			return new StringExp(s);
		}
		default:
			//err
			return null;
		}
	}
	public DValue createDValueFrom(FilterCond cond, ScalarValueBuilder builder, boolean dontThrowException) {
		if (cond instanceof IntegerFilterCond) {
			IntegerFilterCond cc = (IntegerFilterCond) cond;
			return builder.buildInt(cc.asInt());
		} else if (cond instanceof LongFilterCond) {
			LongFilterCond cc = (LongFilterCond) cond;
			return builder.buildLong(cc.asLong());
		} else if (cond instanceof StringFilterCond) {
			StringFilterCond cc = (StringFilterCond) cond;
			return builder.buildString(cc.asString());
		} else {
			if (!dontThrowException) {
				DeliaExceptionHelper.throwError("invalid-filter-value-type", "can't convert filter");
			}
			return null;
		}
	}
	
	public Object extractObj(Exp exp) {
		if (exp instanceof NullExp) {
			return null;
		} else if (exp instanceof IntegerExp) {
			Integer n = ((IntegerExp)exp).val;
			return n;
		} else if (exp instanceof LongExp) {
			Long n = ((LongExp)exp).val;
			return n;
		} else if (exp instanceof NumberExp) {
			Double n = ((NumberExp)exp).val;
			return n;
		} else if (exp instanceof BooleanExp) {
			Boolean n = ((BooleanExp)exp).val;
			return n;
		} else if (exp instanceof StringExp) {
			return exp.strValue();
		} else {
			//date and relation should be handled in another layer
			return exp.strValue();
		}
	}

	
	public DValue createDValFromExp(Exp valueExp, ScalarValueBuilder builder) {
		return createDValFromExp(valueExp, builder, true);
	}
	public DValue createDValFromExp(Exp valueExp, ScalarValueBuilder builder, boolean treatUnknownAsString) {
		if (valueExp instanceof IntegerExp) {
			IntegerExp exp = (IntegerExp) valueExp;
			return builder.buildInt(exp.val);
		} else if (valueExp instanceof LongExp) {
			LongExp exp = (LongExp) valueExp;
			return builder.buildLong(exp.val);
		} else if (valueExp instanceof BooleanExp) {
			BooleanExp exp = (BooleanExp) valueExp;
			return builder.buildBoolean(exp.val);
		} else if (valueExp instanceof NumberExp) {
			NumberExp exp = (NumberExp) valueExp;
			return builder.buildNumber(exp.val);
			//note. date _must_ be explicit type (since it's formatted as a string
		} else if (valueExp instanceof StringExp) {
			StringExp exp = (StringExp) valueExp;
			return builder.buildString(exp.val);
		} else if (valueExp instanceof NullExp) {
			return null; 
		} else { //treat as string
			if (treatUnknownAsString) {
				return builder.buildString(valueExp.strValue());
			} else {
				return null;
			}
		}
	}
	
}