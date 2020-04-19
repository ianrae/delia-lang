package org.delia.valuebuilder;

import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;

public class IntegerValueBuilder extends DValueBuilder {
	public IntegerValueBuilder(DType type) {
		if (!type.isShape(Shape.INTEGER)) {
			addWrongTypeError("expecting int");
			return;
		}
		this.type = type;
	}

	public void buildFromString(String input) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}

		Integer nval = null;
		try {
			nval = Integer.parseInt(input);
			
			//use .valueOf to save memory. it re-uses the same instances for common values.
			nval = Integer.valueOf(nval.intValue());
			
			this.newDVal = new DValueImpl(type, nval);
		} catch (NumberFormatException e) {
			addParsingError(String.format("'%s' is not an integer", input), input);
		}
	}
	public void buildFrom(Integer lval) {
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