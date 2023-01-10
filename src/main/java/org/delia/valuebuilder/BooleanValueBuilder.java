package org.delia.valuebuilder;

import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;

public class BooleanValueBuilder extends DValueBuilder {

	public BooleanValueBuilder(DType type) {
		if (!type.isShape(Shape.BOOLEAN)) {
			addWrongTypeError("expection boolean");
			return;
		}
		this.type = type;
	}

	public void buildFromString(String input) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}

		Boolean bool = null;
		try {
			String target = "true";
			String target2 = "false";
			if (target.equalsIgnoreCase(input) || target2.equalsIgnoreCase(input)) {
				bool = Boolean.parseBoolean(input);
				this.newDVal = new DValueImpl(type, bool);
			} else {
				addParsingError(String.format("'%s' is not an boolean", input), input);
			}
		} catch (NumberFormatException e) {
			addParsingError(String.format("'%s' is not an boolean", input), input);
		}
	}
	public void buildFrom(Boolean bool) {
		if (bool == null) {
			addNoDataError("no data");
			return;
		}
		this.newDVal = new DValueImpl(type, bool);
	}

	@Override
	protected void onFinish() {
	}
}