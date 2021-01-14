package org.delia.db.newhls.cud;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.newhls.HLDField;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.HLDSQLGenerator;
import org.delia.db.newhls.simple.SimpleBase;
import org.delia.db.newhls.simple.SimpleSqlGenerator;
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
	private HLDSQLGenerator otherSqlGen;
	private SimpleSqlGenerator simpleSqlGenerator;

	public InsertInnerSQLGenerator(FactoryService factorySvc, DTypeRegistry registry, HLDSQLGenerator otherSqlGen) {
		super(factorySvc);
		this.registry = registry;
		this.otherSqlGen = otherSqlGen;
		this.simpleSqlGenerator = new SimpleSqlGenerator(registry, factorySvc);
	}

	public SqlStatementGroup generate(HLDInsertStatement hldins) {
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		
		SqlStatement stm = genInsertStatement(hldins.hldinsert);
		stmgrp.add(stm);
		
		for(HLDUpdate hld: hldins.updateL) {
			SqlStatement stmx = genUpdateStatement(hld);
			stmgrp.add(stmx);
		}
		
		for(HLDInsert hld: hldins.assocInsertL) {
			SqlStatement stmx = genInsertStatement(hld);
			stmgrp.add(stmx);
		}
		return stmgrp;
	}
	public SqlStatementGroup generate(HLDUpdateStatement hldupdate) {
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		
		SqlStatement stm = genUpdateStatement(hldupdate.hldupdate);
		stmgrp.add(stm);
		
		for(HLDUpdate hld: hldupdate.updateL) {
			SqlStatement stmx = genUpdateStatement(hld);
			stmgrp.add(stmx);
		}
		
		for(AssocBundle bundle: hldupdate.assocBundleL) {
			if (bundle.hlddelete != null) {
				if (bundle.hlddelete.useDeleteIn) {
					SqlStatement stmx = genDeleteInStatement(bundle.hlddelete);
					stmgrp.add(stmx);
				} else {
					SqlStatement stmx = genDeleteStatement(bundle.hlddelete);
					stmgrp.add(stmx);
				}
			}
			if (bundle.hldupdate != null) {
				if (bundle.hldupdate.isMergeInto) {
					SqlStatement stmx = genMergeIntoStatement(bundle.hldupdate);
					stmgrp.add(stmx);
				} else if (bundle.hldupdate.isMergeAllInto) {
					SqlStatement stmx = genMergeAllIntoStatement(bundle.hldupdate);
					stmgrp.add(stmx);
				} else if (bundle.hldupdate.isMergeCTE) {
					SqlStatement stmx = genMergeIntoCTEStatement(bundle.hldupdate);
					stmgrp.add(stmx);
				} else {
					SqlStatement stmx = genUpdateStatement(bundle.hldupdate);
					stmgrp.add(stmx);
				}
			}
		}
		return stmgrp;
	}
	public SqlStatementGroup generate(HLDDeleteStatement hld) {
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		
		for(HLDDelete hldd: hld.deleteL) {
			SqlStatement stmx = genDeleteStatement(hldd);
			stmgrp.add(stmx);
		}
		for(SimpleBase simple: hld.moreL) {
			SqlStatement stmx = new SqlStatement();
			stmx.sql = simpleSqlGenerator.genAny(simple, stmx);
			stmgrp.add(stmx);
		}
		
		//do actual delete last
		SqlStatement stm = genDeleteStatement(hld.hlddelete);
		stmgrp.add(stm);
		
		return stmgrp;
	}
	
	private SqlStatement genInsertStatement(HLDInsert hldins) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO");
		outTblName(sc, hldins);
		
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
	
	private void outTblName(StrCreator sc, HLDBase hld) {
		sc.o(" %s as %s", hld.typeOrTbl.getTblName(), hld.typeOrTbl.alias);
	}

	private SqlStatement genUpdateStatement(HLDUpdate hld) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("UPDATE");
		outTblName(sc, hld);
		
		if (hld.fieldL.isEmpty()) {
			stm.sql = sc.toString();
			return stm;
		}
		
		sc.o(" SET ");
		int index = 0;
		ListWalker<HLDField> walker = new ListWalker<>(hld.fieldL);
		String conditionStr = null;
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			DValue inner = hld.valueL.get(index);
			stm.paramL.add(inner);
			
			conditionStr = String.format("%s = %s", renderSetField(ff), "?");
			sc.o(conditionStr);
			walker.addIfNotLast(sc, ", ");
			index++;
		}
		
		if (hld.isSubSelect) {
			addSubSelectWhere(sc, hld.hld, stm, conditionStr);
		} else {
			addWhereIfNeeded(sc, hld.hld, stm);
		}

//		renderIfPresent(sc, orderByFrag);
//		renderIfPresent(sc, limitFrag);  TODO is this needed?
		
		stm.sql = sc.toString();
		return stm;
	}
	private void addSubSelectWhere(StrCreator sc, HLDQuery hld, SqlStatement stm, String conditionStr) {
//		WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 WHERE t2.x > ?", "10");

		conditionStr = StringUtils.substringBefore(conditionStr, "=").trim();
		sc.o(" WHERE %s IN ", conditionStr);
		String alias = "t9"; //TODO fix later
		sc.o("(SELECT %s.cid FROM %s as %s WHERE", alias, hld.fromType.getName(), alias);
		String whereStr = otherSqlGen.generateSqlWhere(hld, stm);

		String s1 = "t9.";
		String source = String.format("%s.", hld.fromAlias);
		whereStr = whereStr.replace(source, s1);
		sc.o("%s", whereStr);
	}

	
//    MERGE INTO CustomerAddressAssoc as T USING (SELECT id FROM CUSTOMER) AS S
//    ON T.leftv = s.id WHEN MATCHED THEN UPDATE SET T.rightv = ?
//    WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
	private SqlStatement genMergeAllIntoStatement(HLDUpdate hld) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("MERGE INTO");
		outTblName(sc, hld);
		String alias = hld.typeOrTbl.alias;
		
		//USING (SELECT id FROM CUSTOMER) AS S ON T.leftv = s.id
		sc.o(" USING (SELECT %s FROM %s) AS S", hld.mergePKField, hld.mergeType);
		sc.o(" ON %s.%s = s.%s", alias, hld.mergeKey, hld.mergePKField);
		
		//WHEN MATCHED THEN UPDATE SET T.rightv = ?
		sc.o(" WHEN MATCHED THEN UPDATE SET %s.%s = ?", alias, hld.mergeKeyOther);
		DValue inner = findFirstNonNullValue(hld.valueL); 
		stm.paramL.add(inner);
		stm.paramL.add(inner);
		
		//WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
		sc.o(" WHEN NOT MATCHED THEN INSERT (leftv, rightv)", hld.mergeKey, hld.mergeKeyOther);
		sc.o(" VALUES(s.%s, ?)", hld.mergePKField);
		
		stm.sql = sc.toString();
		return stm;
	}
	private DValue findFirstNonNullValue(List<DValue> valueL) {
		Optional<DValue> opt = valueL.stream().filter(x -> x != null).findFirst();
		return opt.orElse(null);
	}

//  WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1
	private SqlStatement genMergeIntoCTEStatement(HLDUpdate hld) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("WITH cte1 AS (SELECT ? as leftv, id as rightv");
		stm.paramL.add(hld.dvalCTE);
		
		sc.o(" FROM %s", hld.mergeType);
		sc.o("INSERT INTO");		
		outTblName(sc, hld);
		String alias = hld.typeOrTbl.alias;
		
		sc.o(" SELECT * from cte1");		
		
		
		stm.sql = sc.toString();
		return stm;
	}

//  merge into CustomerAddressAssoc key(leftv) values(55,100) //only works if 1 record updated/inserted
	private SqlStatement genMergeIntoStatement(HLDUpdate hld) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("MERGE INTO");
		outTblName(sc, hld);
		
		sc.o(" KEY(%s)", hld.mergeKey);
		
		sc.o(" VALUES ");
		int index = 0;
		ListWalker<HLDField> walker = new ListWalker<>(hld.fieldL);
		while(walker.hasNext()) {
			HLDField ff = walker.next();
			DValue inner = hld.valueL.get(index);
			stm.paramL.add(inner);
			
			sc.o("?");
			walker.addIfNotLast(sc, ", ");
			index++;
		}
		
		stm.sql = sc.toString();
		return stm;
	}
	
	
	private String renderSetField(HLDField ff) {
		String s = String.format("%s.%s", ff.alias, ff.fieldName); //TODO do asstr later
		return s;
	}

	//DELETE FROM table_name WHERE condition;
	private SqlStatement genDeleteStatement(HLDDelete hld) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("DELETE FROM");
		outTblName(sc, hld);
		
		addWhereIfNeeded(sc, hld.hld, stm);

		stm.sql = sc.toString();
		return stm;
	}
	//DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv <> ? AND leftv IN (SELECT rightv FROM Customer as a WHERE  t1.rightv = ?) -- 999,55,55
	//          delete CustomerAddressAssoc where leftv <> 100 and rightv in (SELECT id FROM Address as a WHERE a.z > ?)
	//delete CustomerAddressAssoc where rightv <> 100 and leftv in (SELECT id FROM Address as a WHERE a.z > ?)
	private SqlStatement genDeleteInStatement(HLDDelete hld) {
		SqlStatement stm = new SqlStatement();
		StrCreator sc = new StrCreator();
		sc.o("DELETE FROM");
		outTblName(sc, hld);
		String alias = hld.typeOrTbl.alias;
		
		int n = stm.paramL.size();
		String whereStr = otherSqlGen.generateSqlWhere(hld.hld, stm);
		DValue dval = hld.deleteInDVal;
		DValue sav = stm.paramL.remove(n);
		stm.paramL.add(dval);
		stm.paramL.add(sav);
		
		whereStr = whereStr.replace(String.format("%s.", alias), "a.");
		String s2 = String.format("%s.%s <> ?",  alias, hld.mergeOtherKey); //whereStr.substring(pos1 + 5);

		String s = String.format(" %s AND %s.%s IN (SELECT %s FROM %s as a WHERE%s)", s2, alias, hld.mergeKey, hld.mergePKField, hld.mergeType, whereStr);
		sc.o(" WHERE%s", s);

		stm.sql = sc.toString();
		return stm;
	}

	private void addWhereIfNeeded(StrCreator sc, HLDQuery hld, SqlStatement stm) {
		String whereStr = otherSqlGen.generateSqlWhere(hld, stm);
		if (whereStr != null) {
			sc.o(" WHERE%s", whereStr);
		}
	}
}