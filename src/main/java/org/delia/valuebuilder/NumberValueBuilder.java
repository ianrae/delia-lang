package org.delia.valuebuilder;

import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;

public class NumberValueBuilder extends DValueBuilder {
	public NumberValueBuilder(DType type) {
		if (!type.isShape(Shape.NUMBER)) {
			addWrongTypeError("expecting number");
			return;
		}
		this.type = type;
	}

	public void buildFromString(String input) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}

		Double nval = null;
		try {
			nval = Double.parseDouble(input);
			
			//use .valueOf to save memory. it re-uses the same instances for common values.
			nval = Double.valueOf(nval.doubleValue());
			
			this.newDVal = new DValueImpl(type, nval);
		} catch (NumberFormatException e) {
			addParsingError(String.format("'%s' is not a number", input), input);
		}
	}
	public void buildFrom(Double lval) {
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