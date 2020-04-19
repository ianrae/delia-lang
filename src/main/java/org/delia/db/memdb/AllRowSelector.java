package org.delia.db.memdb;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.QuerySpec;
import org.delia.error.ErrorTracker;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class AllRowSelector extends RowSelectorBase {
	@Override
	public void init(ErrorTracker et, QuerySpec spec, DStructType dtype, DTypeRegistry registry) {
		super.init(et, spec, dtype, registry);
	}

	@Override
	public List<DValue> match(List<DValue> list) {
		List<DValue> copy = new ArrayList<>(list);
		return copy;
	}
}