package org.delia.dval;

import java.time.ZonedDateTime;
import java.util.Date;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.BuiltInTypes;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.valuebuilder.ScalarValueBuilder;

public class DValueExConverter extends ServiceBase {

	private DateFormatService fmtSvc;
	private DTypeRegistry registry;
	private ScalarValueBuilder builder;

	public DValueExConverter(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.fmtSvc = factorySvc.getDateFormatService();
		this.registry = registry;
		this.builder = factorySvc.createScalarValueBuilder(registry);
	}
	
	/**
	 * Builds a dvalue using built-in types.
	 * @param input input string or java primitive
	 * @param shape shape of dvalue to bulid
	 * @return dvalue
	 */
	public DValue buildFromObject(Object input, Shape shape) {
		DType dtype = null;
		switch(shape) {
		case INTEGER:
			dtype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
			break;
		case LONG:
			dtype = registry.getType(BuiltInTypes.LONG_SHAPE);
			break;
		case NUMBER:
			dtype = registry.getType(BuiltInTypes.NUMBER_SHAPE);
			break;
		case BOOLEAN:
			dtype = registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
			break;
		case STRING:
			dtype = registry.getType(BuiltInTypes.STRING_SHAPE);
			break;
		case DATE:
			dtype = registry.getType(BuiltInTypes.DATE_SHAPE);
			break;
		default:
			//will get an NPE
			break;
		}
		return buildFromObject(input, dtype);
	}

	/**
	 * handles custom scalar types as well as built-in types.
	 * @param input input string or java primitive
	 * @param shape shape of dvalue to bulid
	 * @param dtype type or null (if want built-in type)
	 * @return dvalue
	 */
	public DValue buildFromObject(Object input, DType dtype) {
		DValue inner = null;
		switch(dtype.getShape()) {
		case INTEGER:
			inner = buildInt(input, dtype);
			break;
		case LONG:
			inner = buildLong(input, dtype);
			break;
		case NUMBER:
			inner = buildNumber(input, dtype);
			break;
		case BOOLEAN:
			inner = buildBoolean(input, dtype);
			break;
		case STRING:
			inner = buildString(input, dtype);
			break;
		case DATE:
			inner = buildDate(input, dtype);
			break;
		default:
			//err not supported
			break;
		}
		return inner;
	}

	private DValue buildInt(Object input, DType dtype) {
		if (input == null) {
			return null;
		}

		if (input instanceof Integer) {
			Integer value = (Integer) input; 
			return builder.buildInt(value, dtype);
		} else {
			String s = input.toString();
			return builder.buildInt(s, dtype);
		}
	}
	private DValue buildLong(Object input, DType dtype) {
		if (input == null) {
			return null;
		}

		if (input instanceof Long) {
			Long value = (Long) input; 
			return builder.buildLong(value, dtype);
		} else {
			String s = input.toString();
			return builder.buildLong(s, dtype);
		}
	}
	private DValue buildNumber(Object input, DType dtype) {
		if (input == null) {
			return null;
		}

		if (input instanceof Double) {
			Double value = (Double) input; 
			return builder.buildNumber(value, dtype);
		} else {
			String s = input.toString();
			return builder.buildNumber(s, dtype);
		}
	}
	private DValue buildBoolean(Object input, DType dtype) {
		if (input == null) {
			return null;
		}

		if (input instanceof Boolean) {
			Boolean value = (Boolean) input; 
			return builder.buildBoolean(value, dtype);
		} else {
			String s = input.toString();
			return builder.buildBoolean(s, dtype);
		}
	}
	private DValue buildString(Object input, DType dtype) {
		if (input == null) {
			return null;
		}

		String s = input.toString();
		return builder.buildString(s, dtype);
	}
	private DValue buildDate(Object input, DType dtype) {
		if (input == null) {
			return null;
		}

		if (input instanceof Date) {
			Date value = (Date) input; 
			return builder.buildLegacyDate(value, dtype);
		} else if (input instanceof ZonedDateTime) {
			ZonedDateTime value = (ZonedDateTime) input; 
			return builder.buildDate(value, dtype);
		} else {
			String s = input.toString();
			return builder.buildDate(s, dtype);
		}
	}
	
	
	
}