package org.delia.hld.cud;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.sql.StrCreator;
import org.delia.db.sqlgen.*;
import org.delia.hld.simple.SimpleBase;
import org.delia.hld.simple.SimpleSqlGenerator;
import org.delia.type.DTypeRegistry;

/**
 * 
 * 
 * single use!!!
 * @author ian
 *
 */
public class HLDToSQLConverterImpl extends ServiceBase implements HLDToSQLConverter { 

//	private DTypeRegistry registry;
	private SimpleSqlGenerator simpleSqlGenerator;

	private SqlGeneratorFactory sqlFactory;
	private DBType dbType;

	public HLDToSQLConverterImpl(FactoryService factorySvc, DTypeRegistry registry, DBType dbType, SqlGeneratorFactory sqlgen) {
		super(factorySvc);
		this.simpleSqlGenerator = new SimpleSqlGenerator(registry, factorySvc);
		this.sqlFactory = sqlgen;
		this.dbType = dbType;
	}

	/* (non-Javadoc)
	 * @see org.delia.hld.cud.HLDSQLGenerator#generate(org.delia.hld.cud.HLDInsertStatement)
	 */
	@Override
	public SqlStatementGroup generate(HLDInsertStatement hldins) {
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		
		SqlStatement stm = genInsertStatement(hldins.hldinsert);
		stmgrp.add(stm);
		
		for(SimpleBase simple: hldins.moreL) {
			SqlStatement stmx = new SqlStatement(simple);
			stmx.sql = simpleSqlGenerator.genAny(simple, stmx);
			stmgrp.add(stmx);
		}
		
		return stmgrp;
	}
	/* (non-Javadoc)
	 * @see org.delia.hld.cud.HLDSQLGenerator#generate(org.delia.hld.cud.HLDUpdateStatement)
	 */
	@Override
	public SqlStatementGroup generate(HLDUpdateStatement hldupdate) {
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		
		SqlStatement stm = genUpdateStatement(hldupdate.hldupdate);
		stmgrp.add(stm);
		
		for(SimpleBase simple: hldupdate.moreL) {
			SqlStatement stmx = new SqlStatement(simple);
			stmx.sql = simpleSqlGenerator.genAny(simple, stmx);
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
	/* (non-Javadoc)
	 * @see org.delia.hld.cud.HLDSQLGenerator#generate(org.delia.hld.cud.HLDUpsertStatement)
	 */
	@Override
	public SqlStatementGroup generate(HLDUpsertStatement hldupsert) {
		SqlStatementGroup stmgrp = new SqlStatementGroup();

		SqlStatement stm = genUpsertStatement((HLDUpsert) hldupsert.hldupdate);
		stmgrp.add(stm);

		for (SimpleBase simple : hldupsert.moreL) {
			SqlStatement stmx = new SqlStatement(simple);
			stmx.sql = simpleSqlGenerator.genAny(simple, stmx);
			stmgrp.add(stmx);
		}

		//hack hack hack
		int maxNumDeletes = hldupsert.assocBundleL.size();
		if (DBType.POSTGRES.equals(dbType)) {
			maxNumDeletes = 1; //only delete from DAT once per upsert
		}

		for (AssocBundle bundle : hldupsert.assocBundleL) {
			if (bundle.hlddelete != null) {
				if (maxNumDeletes-- > 0) {
					SqlStatement stmx = genDeleteStatement(bundle.hlddelete);
					stmgrp.add(stmx);
				}
			}
			if (bundle.hldupdate != null) {
				SqlStatement stmx;
				//hack hack hack. Postgres should use INSERT ON CONFLICT
				//produces this:INSERT INTO CustomerAddressDat1 VALUES(?, ?) ON CONFLICT(leftv,rightv) DO UPDATE SET rightv = ?
				//TODO this would be more efficient INSERT INTO CustomerAddressDat1 VALUES(?, ?) ON CONFLICT(leftv,rightv) DO NOTHING
				if (DBType.POSTGRES.equals(dbType)) {
					SqlMergeIntoStatement sqlMergeInto = sqlFactory.createMergeInto();
					sqlMergeInto.init(bundle.hldupdate);
					stmx = sqlMergeInto.render();
				} else {
					stmx = genUpdateStatement(bundle.hldupdate);
				}
				stmgrp.add(stmx);
			}
		}
		return stmgrp;
	}
	
	
	private SqlStatement genUpsertStatement(HLDUpsert hld) {
		if (hld.noUpdateFlag) {
			SqlMergeUsingStatement sqlMergeInto = sqlFactory.createMergeUsing();
			sqlMergeInto.init(hld);
			return sqlMergeInto.render();
		}
		
		SqlMergeIntoStatement sqlMergeInto = sqlFactory.createMergeInto();
		sqlMergeInto.init(hld);
		return sqlMergeInto.render();
	}

	/* (non-Javadoc)
	 * @see org.delia.hld.cud.HLDSQLGenerator#generate(org.delia.hld.cud.HLDDeleteStatement)
	 */
	@Override
	public SqlStatementGroup generate(HLDDeleteStatement hld) {
		SqlStatementGroup stmgrp = new SqlStatementGroup();
		
		for(SimpleBase simple: hld.moreL) {
			SqlStatement stmx = new SqlStatement(simple);
			stmx.sql = simpleSqlGenerator.genAny(simple, stmx);
			stmgrp.add(stmx);
		}
		
		
		//do actual delete last
		SqlStatement stm = genDeleteStatement(hld.hlddelete);
		stmgrp.add(stm);
		
		return stmgrp;
	}
	
	private SqlStatement genInsertStatement(HLDInsert hldins) {
		SqlInsertStatement sqlMergeInto = sqlFactory.createInsert();
		sqlMergeInto.init(hldins);
		return sqlMergeInto.render();
	}
	
	private void outTblName(StrCreator sc, HLDBase hld) {
		sc.o(hld.typeOrTbl.render());
	}

	private SqlStatement genUpdateStatement(HLDUpdate hld) {
		SqlUpdateStatement updateStmt = sqlFactory.createUpdate();
		updateStmt.init(hld);
		return updateStmt.render();
	}

	
//    MERGE INTO CustomerAddressAssoc as T USING (SELECT id FROM CUSTOMER) AS S
//    ON T.leftv = s.id WHEN MATCHED THEN UPDATE SET T.rightv = ?
//    WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
	private SqlStatement genMergeAllIntoStatement(HLDUpdate hld) {
		SqlMergeAllIntoStatement updateStmt = sqlFactory.createMergeAllInto();
		updateStmt.init(hld);
		return updateStmt.render();
	}

//  WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1
	private SqlStatement genMergeIntoCTEStatement(HLDUpdate hld) {
		SqlStatement stm = new SqlStatement(hld);
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
		SqlMergeIntoStatement updateStmt = sqlFactory.createMergeInto();
		updateStmt.init(hld);
		return updateStmt.render();
	}
	
	//DELETE FROM table_name WHERE condition;
	private SqlStatement genDeleteStatement(HLDDelete hld) {
		SqlDeleteStatement delStmt = sqlFactory.createDelete();
		delStmt.init(hld);
		return delStmt.render();
	}
	//DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv <> ? AND leftv IN (SELECT rightv FROM Customer as a WHERE  t1.rightv = ?) -- 999,55,55
	//          delete CustomerAddressAssoc where leftv <> 100 and rightv in (SELECT id FROM Address as a WHERE a.z > ?)
	//delete CustomerAddressAssoc where rightv <> 100 and leftv in (SELECT id FROM Address as a WHERE a.z > ?)
	private SqlStatement genDeleteInStatement(HLDDelete hld) {
		SqlDeleteStatement delStmt = sqlFactory.createDelete();
		sqlFactory.useDeleteIn(delStmt);
		delStmt.init(hld);
		return delStmt.render();
	}

	@Override
	public SqlGeneratorFactory getSqlGeneratorFactory() {
		return this.sqlFactory;
	}

}