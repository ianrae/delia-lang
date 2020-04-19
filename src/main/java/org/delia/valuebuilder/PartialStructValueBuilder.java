package org.delia.valuebuilder;

import org.delia.type.DStructType;
import org.delia.type.DValueImpl;


/**
 * An UPDATE statement includes a list of fields to be updated.
 * This may be a partial or full list.
 * @author Ian Rae
 *
 */
public class PartialStructValueBuilder extends StructValueBuilder {

	public PartialStructValueBuilder(DStructType type) {
		super(type);
	}

	@Override
	protected void onFinish() {
		if (wasSuccessful()) {
			//don't check for missing fields.
			newDVal = new DValueImpl(type, map);
		}
	}
}