package org.delia.valuebuilder;

import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;

public class StringValueBuilder extends DValueBuilder {

	public StringValueBuilder(DType type) {
		if (!type.isShape(Shape.STRING)) {
			addWrongTypeError("expecting string");
			return;
		}
		this.type = type;
	}

	public void buildFromString(String input) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}

		this.newDVal = new DValueImpl(type, input);
	}
	

	@Override
	protected void onFinish() {
	}
}