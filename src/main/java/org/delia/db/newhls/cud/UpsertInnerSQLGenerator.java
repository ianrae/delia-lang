package org.delia.db.newhls.cud;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hld.HLDField;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.ListWalker;
import org.delia.type.DRelation;
import org.delia.type.DValue;

/**
 * 
 * 
 * single use!!!
 * @author ian
 *
 */
public class UpsertInnerSQLGenerator extends ServiceBase { 

	public UpsertInnerSQLGenerator(FactoryService factorySvc) {
		super(factorySvc);
	}

	private DValue renderValue(DValue inner) {
		if (inner != null && inner.getType().isRelationShape()) {
			DRelation drel = inner.asRelation();
			return drel.getForeignKey(); //better only be one!
		}
		return inner;
	}

	private void outTblName(StrCreator sc, HLDBase hld) {
		sc.o(hld.typeOrTbl.render());
	}

	
	
//  MERGE INTO CustomerAddressAssoc as T USING (SELECT id FROM CUSTOMER) AS S
//  ON T.leftv = s.id WHEN MATCHED THEN UPDATE SET T.rightv = ?
//  WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
	
//	merge into Flight as t0 using (select id from Flight) as t1
//	on t0.id=t1.id
//	//when matched the update set t0.wid=? ...
//	when not matched then insert (wid) values (?)
	public SqlStatement genMergeIntoNoUpdateStatement(HLDUpsert hld) {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("MERGE INTO");
		hld.typeOrTbl.alias = null;
		outTblName(sc, hld);
//		String alias = hld.typeOrTbl.alias;
		String alias2 = "t9"; //TODO fix better
		
		sc.o(" USING (SELECT %s FROM %s) AS %s", hld.mergePKField, hld.typeOrTbl.getTblName(), alias2);
		sc.o(" ON %s = %s.%s", hld.mergePKField, alias2, hld.mergePKField);
		
		sc.o(" WHEN NOT MATCHED THEN INSERT");
		if (hld.fieldL.isEmpty()) {
			sc.o(" DEFAULT VALUES");
			stm.sql = sc.toString();
			return stm;
		}
		
		sc.o(" (");
		ListWalker<HLDField> walker = new ListWalker<>(hld.fieldL);
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			ff.alias = null;
			sc.o(ff.render());
			walker.addIfNotLast(sc, ", ");
		}
		sc.o(")");

		sc.o(" VALUES(");
		ListWalker<DValue> dvalwalker = new ListWalker<>(hld.valueL);
		//no null dvals (they wouldn't be in the list)
		while(dvalwalker.hasNext()) {
			DValue inner = dvalwalker.next();
			stm.paramL.add(renderValue(inner));
			sc.o("?");
			dvalwalker.addIfNotLast(sc, ", ");
		}
		sc.o(")");
		
		stm.sql = sc.toString();
		return stm;
	}
	
}