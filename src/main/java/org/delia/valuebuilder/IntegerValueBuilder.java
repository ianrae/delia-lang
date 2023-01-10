package org.delia.valuebuilder;

import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.EffectiveShape;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;

public class IntegerValueBuilder extends DValueBuilder {
	public IntegerValueBuilder(DType type) {
		if (!type.isShape(Shape.INTEGER)) {
			addWrongTypeError("expecting int");
			return;
		}
		this.type = type;
	}

	public void buildFromString(String input) {
		EffectiveShape effectiveShape = getEffectiveShape();
		buildFromStringEx(input, effectiveShape);
	}

	public void buildFromStringEx(String input, EffectiveShape effectiveShape) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}

		switch(effectiveShape) {
			case EFFECTIVE_INT: {
				Integer nval = null;
				try {
					nval = Integer.parseInt(input);

					//use .valueOf to save memory. it re-uses the same instances for common values.
					nval = Integer.valueOf(nval.intValue());

					this.newDVal = new DValueImpl(type, nval);
				} catch (NumberFormatException e) {
					addParsingError(String.format("'%s' is not an integer. (int)", input), input);
				}
			}
			break;
			case EFFECTIVE_LONG: {
				Long nval = null;
				try {
					nval = Long.parseLong(input);

					//use .valueOf to save memory. it re-uses the same instances for common values.
					nval = Long.valueOf(nval.longValue());

					this.newDVal = new DValueImpl(type, nval);
				} catch (NumberFormatException e) {
					addParsingError(String.format("'%s' is not an integer. (long)", input), input);
				}
			}
			break;
			default:
				DeliaExceptionHelper.throwNotImplementedError("unknown effective shape");
				break;
		}
	}

	private EffectiveShape getEffectiveShape() {
		if (type.getEffectiveShape() == null || type.getEffectiveShape().equals(EffectiveShape.EFFECTIVE_INT)) {
			return EffectiveShape.EFFECTIVE_INT;
		}
		return EffectiveShape.EFFECTIVE_LONG;
	}

	public void buildFrom(Integer lval) {
		if (lval == null) {
			addNoDataError("no data");
			return;
		}
		this.newDVal = new DValueImpl(type, lval);
	}
	public void buildFrom(Long lval) {
		if (lval == null) {
			addNoDataError("no data");
			return;
		}
		this.newDVal = new DValueImpl(type, lval);
	}

	@Override
	protected void onFinish() {
	}
}