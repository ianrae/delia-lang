package org.delia.valuebuilder;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.log.LoggableBlob;
import org.delia.type.*;
import org.delia.util.DeliaExceptionHelper;

import java.time.ZonedDateTime;
import java.util.Date;

public class ScalarValueBuilder extends ServiceBase {

	protected DTypeRegistry registry;

	public ScalarValueBuilder(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}

	public DType getTypeForShape(Shape shape) {
		return registry.getType(BuiltInTypes.getBuiltInTypeOf(shape));
	}

	public DValue buildInt(String input) {
		return buildInt(input, registry.getType(BuiltInTypes.INTEGER_SHAPE));
	}
	public DValue buildInt(Integer value) {
		return buildInt(value, registry.getType(BuiltInTypes.INTEGER_SHAPE));
	}
	public DValue buildInt(Long value) {
		return buildInt(value, registry.getType(BuiltInTypes.INTEGER_SHAPE));
	}
	public DValue buildInt(String input, DType dtype) {
		IntegerValueBuilder builder = new IntegerValueBuilder(dtype);
		builder.buildFromString(input); 
		return finish(builder, "int", input);
	}
	public DValue buildInt(Integer value, DType dtype) {
		IntegerValueBuilder builder = new IntegerValueBuilder(dtype);
		builder.buildFrom(value); 
		return finish(builder, "int", value);
	}
	public DValue buildInt(Long value, DType dtype) {
		IntegerValueBuilder builder = new IntegerValueBuilder(dtype);
		builder.buildFrom(value);
		return finish(builder, "int", value);
	}

//	public DValue buildLong(String input) {
//		return buildLong(input, registry.getType(BuiltInTypes.LONG_SHAPE));
//	}
//	public DValue buildLong(Long value) {
//		return buildLong(value, registry.getType(BuiltInTypes.LONG_SHAPE));
//	}
//	public DValue buildLong(String input, DType dtype) {
//		LongValueBuilder builder = new LongValueBuilder(dtype);
//		builder.buildFromString(input);
//		return finish(builder, "long", input);
//	}
//	public DValue buildLong(Long value, DType dtype) {
//		LongValueBuilder builder = new LongValueBuilder(dtype);
//		builder.buildFrom(value);
//		return finish(builder, "long", value);
//	}

	public DValue buildNumber(String input) {
		return buildNumber(input, registry.getType(BuiltInTypes.NUMBER_SHAPE));
	}
	public DValue buildNumber(Double value) {
		return buildNumber(value, registry.getType(BuiltInTypes.NUMBER_SHAPE));
	}
	public DValue buildNumber(String input, DType dtype) {
		NumberValueBuilder builder = new NumberValueBuilder(dtype);
		builder.buildFromString(input); 
		return finish(builder, "number", input);
	}
	public DValue buildNumber(Double value, DType dtype) {
		NumberValueBuilder builder = new NumberValueBuilder(dtype);
		builder.buildFrom(value); 
		return finish(builder, "number", value);
	}
	
	public DValue buildDate(String input) {
		return buildDate(input, registry.getType(BuiltInTypes.DATE_SHAPE));
	}
	public DValue buildLegacyDate(Date value) {
		return buildLegacyDate(value, registry.getType(BuiltInTypes.DATE_SHAPE));
	}
	public DValue buildDate(String input, DType dtype) {
		DateValueBuilder builder = new DateValueBuilder(factorySvc, dtype);
		builder.buildFromString(input); 
		return finish(builder, "date", input);
	}
	public DValue buildLegacyDate(Date value, DType dtype) {
		DateValueBuilder builder = new DateValueBuilder(factorySvc, dtype);
		builder.buildFromLegacy(value);
		return finish(builder, "date", value);
	}
	public DValue buildDate(ZonedDateTime value) {
		return buildDate(value, registry.getType(BuiltInTypes.DATE_SHAPE));
	}
	public DValue buildDate(ZonedDateTime value, DType dtype) {
		DateValueBuilder builder = new DateValueBuilder(factorySvc, dtype);
		builder.buildFrom(value);
		return finish(builder, "date", value);
	}
	//TODO note. blob is not a scalar type. move this later
	public DValue buildBlob(String input, DType dtype) {
		BlobValueBuilder builder = new BlobValueBuilder(factorySvc, dtype);
		builder.buildFromString(input);
		return finish(builder, "blob", new LoggableBlob(input));
	}
	public DValue buildBlob(byte[] byteArr, DType dtype) {
		BlobValueBuilder builder = new BlobValueBuilder(factorySvc, dtype);
		builder.buildFromByteArray(byteArr);
		return finish(builder, "blob", new LoggableBlob(byteArr));
	}

	public DValue buildBoolean(String input) {
		return buildBoolean(input, registry.getType(BuiltInTypes.BOOLEAN_SHAPE));
	}
	public DValue buildBoolean(Boolean value) {
		return buildBoolean(value, registry.getType(BuiltInTypes.BOOLEAN_SHAPE));
	}
	public DValue buildBoolean(String input, DType dtype) {
		BooleanValueBuilder builder = new BooleanValueBuilder(dtype);
		builder.buildFromString(input); 
		return finish(builder, "boolean", input);
	}
	public DValue buildBoolean(Boolean value, DType dtype) {
		BooleanValueBuilder builder = new BooleanValueBuilder(dtype);
		builder.buildFrom(value); 
		return finish(builder, "boolean", value);
	}
	
	public DValue buildString(String input) {
		return buildString(input, registry.getType(BuiltInTypes.STRING_SHAPE));
	}
	public DValue buildString(String input, DType dtype) {
		StringValueBuilder builder = new StringValueBuilder(dtype);
		builder.buildFromString(input); 
		return finish(builder, "string", input);
	}

	protected DValue finish(DValueBuilder builder, String typeStr, Object value) {
		boolean b = builder.finish();
		if (!b) {
			throwOnFail(builder, typeStr, value);
		}
		DValue dval = builder.getDValue();
		return dval;
	}

	protected void throwOnFail(DValueBuilder builder, String typeStr, Object value) {
		//FUTURE propogate errors from inner builder
		String s = value == null ? "NULL" : value.toString();
		DeliaExceptionHelper.throwError("value-builder-failed", "Failed to create %s from '%s'", typeStr, s);
	}

	public boolean checkIntegerEffectiveShape(DValue dval) {
		EffectiveShape effectiveShape = dval.getType().getEffectiveShape();
		if (EffectiveShape.EFFECTIVE_INT.equals(effectiveShape)) {
			long longVal = dval.asLong();
			int nval = dval.asInt();
			if (nval != longVal) { //doesn't fit in int?
				return false;
			}
		}
		return true;
	}

	public DTypeRegistry getRegistry() {
		return registry;
	}
}