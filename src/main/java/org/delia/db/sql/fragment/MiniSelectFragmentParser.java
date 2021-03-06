//package org.delia.db.sql.fragment;
//
//import org.delia.core.FactoryService;
//import org.delia.db.QueryDetails;
//import org.delia.db.QuerySpec;
//import org.delia.db.sql.StrCreator;
//import org.delia.type.DStructType;
//import org.delia.type.DTypeRegistry;
//
///**
// * A mini version of SelectFragmentParser, for use by HLS
// * -single use!
// * 
// * 
// * @author Ian Rae
// *
// */
//public class MiniSelectFragmentParser extends MiniFragmentParserBase {
//
//	public MiniSelectFragmentParser(FactoryService factorySvc, DTypeRegistry registry, WhereFragmentGenerator whereGen, AliasCreator aliasCreator) {
//		super(factorySvc, registry, whereGen, aliasCreator);
//	}
//
//	public SelectStatementFragment parseSelect(QuerySpec spec, QueryDetails details) {
//		whereGen.tableFragmentMaker = this;
//		
//		SelectStatementFragment selectFrag = new SelectStatementFragment();
//
//		//init tbl
//		DStructType structType = getMainType(spec); 
//		TableFragment tblFrag = createTable(structType, selectFrag);
//		selectFrag.tblFrag = tblFrag;
//
//		initWhere(spec, structType, selectFrag);
//
//		return selectFrag;
//	}
//
//
//	public String renderSelect(SelectStatementFragment selectFrag) {
//		selectFrag.statement.sql = selectFrag.render();
//		return selectFrag.statement.sql;
//	}
//
//	public String renderSelectWherePartOnly(SelectStatementFragment selectFrag) {
//		StrCreator sc = new StrCreator();
//		sc.o("WHERE");
//		selectFrag.renderWhereL(sc);
//		return sc.toString();
//	}
//}