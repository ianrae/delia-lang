package org.delia.db.newhls.cud;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.newhls.HLDField;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

/**
 * Copy of InsertFragmentParser but seperated from SelectFragmentParser.
 * 
 * single use!!!
 * @author ian
 *
 */
public class InsertInnerSQLGenerator extends ServiceBase { 

	private DTypeRegistry registry;

	public InsertInnerSQLGenerator(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}

	public SqlStatementGroup generate(HLDInsert hldins) {
		//SqlParamGenerator paramGen = new SqlParamGenerator(registry, factorySvc); 
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		
		SqlStatement stm = genInsertStatement(hldins);
		stmgrp.add(stm);
		
		return stmgrp;
	}
	
	private SqlStatement genInsertStatement(HLDInsert hldins) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO");
		sc.o(" %s", hldins.typeOrTbl.getTblName());
		
		if (hldins.fieldL.isEmpty()) {
			sc.o(" DEFAULT VALUES");
			stm.sql = sc.toString();
			return stm;
		}
		
		sc.o(" (");
		ListWalker<HLDField> walker = new ListWalker<>(hldins.fieldL);
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			String s = String.format("%s.%s", ff.alias, ff.fieldName); //TODO do asstr later
			sc.o(s);
			walker.addIfNotLast(sc, ", ");
		}
		sc.o(")");

		sc.o(" VALUES(");
		ListWalker<DValue> dvalwalker = new ListWalker<>(hldins.valueL);
		//no null dvals (they wouldn't be in the list)
		while(dvalwalker.hasNext()) {
			DValue inner = dvalwalker.next();
			stm.paramL.add(inner);
			sc.o("?");
			dvalwalker.addIfNotLast(sc, ", ");
		}
		sc.o(")");
		
		stm.sql = sc.toString();
		return stm;
	}
	
}