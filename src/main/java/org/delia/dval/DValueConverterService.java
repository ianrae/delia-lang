package org.delia.dval;

import java.util.Date;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DValue;
import org.delia.type.Shape;
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

		if (input instanceof Date) {
			Date value = (Date) input; 
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
	
}