package org.delia.db.sql.fragment;

import org.delia.core.FactoryService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

//single use!!!
public class MiniSelectFragmentParser extends MiniFragmentParserBase {

	public MiniSelectFragmentParser(FactoryService factorySvc, DTypeRegistry registry, WhereFragmentGenerator whereGen) {
		super(factorySvc, registry, whereGen);
	}

	public SelectStatementFragment parseSelect(QuerySpec spec, QueryDetails details) {
		SelectStatementFragment selectFrag = new SelectStatementFragment();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, selectFrag);
		selectFrag.tblFrag = tblFrag;

		initWhere(spec, structType, selectFrag);

		return selectFrag;
	}


	public String renderSelect(SelectStatementFragment selectFrag) {
		selectFrag.statement.sql = selectFrag.render();
		return selectFrag.statement.sql;
	}
}