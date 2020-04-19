package org.delia.db.memdb;

import org.delia.compiler.ast.FilterExp;
import org.delia.db.QuerySpec;
import org.delia.error.ErrorTracker;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

public abstract class RowSelectorBase implements RowSelector {
	protected FilterExp filter;
	protected DStructType dtype;
	protected String keyField;
	protected ErrorTracker et;
	protected boolean wasError;
	protected MemDBTable tbl;
	protected QuerySpec spec;
	protected DTypeRegistry registry;

	@Override
	public void init(ErrorTracker et, QuerySpec spec, DStructType dtype, DTypeRegistry registry) {
		this.et = et;
		this.filter = spec.queryExp.filter;
		this.spec = spec;
		this.dtype = dtype;
		this.registry = registry;
	}

	@Override
	public boolean wasError() {
		return wasError;
	}

	@Override
	public void setTbl(MemDBTable tbl) {
		this.tbl = tbl;
	}

	@Override
	public MemDBTable getTbl() {
		return tbl;
	}
	
}