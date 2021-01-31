package org.delia.valuebuilder;

import org.delia.core.FactoryService;
import org.delia.type.DType;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;
import org.delia.type.WrappedBlob;
import org.delia.util.BlobUtils;

public class BlobValueBuilder extends DValueBuilder {
	

	public BlobValueBuilder(FactoryService factorySvc, DType type) {
		if (!type.isShape(Shape.BLOB)) {
			addWrongTypeError("expecting blob");
			return;
		}
		this.type = type;
	}

	public void buildFromString(String input) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}

		byte[] byteArr = BlobUtils.fromBase64(input);
		WrappedBlob wblob = new WrappedBlob(byteArr);
		this.newDVal = new DValueImpl(type, wblob);
	}

	@Override
	protected void onFinish() {
	}
}