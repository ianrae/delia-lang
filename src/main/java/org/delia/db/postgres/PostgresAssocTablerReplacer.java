package org.delia.db.postgres;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.SqlHelperFactory;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.MergeIntoStatementFragment;
import org.delia.db.sql.fragment.RawFragment;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

//single use!!!
public class PostgresAssocTablerReplacer extends AssocTableReplacer {

	private boolean useAliases = true;

	public PostgresAssocTablerReplacer(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
			SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
		super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
		this.isPostgres = true;
	}
	
	
	protected MergeIntoStatementFragment generateMergeUsingAll(UpdateStatementFragment assocUpdateFrag, 
			RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, String subSelectWhere) {
		
		//part 2. 
//		WITH cte1 AS (SELECT 100 as leftv,id as rightv from Customer) INSERT INTO AddressCustomerAssoc as t (leftv,rightv) SELECT * from cte1
		
		MergeIntoStatementFragment mergeIntoFrag = new MergeIntoStatementFragment();
		mergeIntoFrag.prefix = "";
		mergeIntoFrag.afterEarlyPrefix = "INSERT INTO";
		mergeIntoFrag.tblFrag = initTblFrag(assocUpdateFrag);
		mergeIntoFrag.tblFrag.alias = "t";
	
		StrCreator sc = new StrCreator();
//		WITH cte1 AS (SELECT 100 as leftv,id as rightv from Customer) INSERT INTO AddressCustomerAssoc as t (leftv,rightv) SELECT * from cte1
		sc.o("WITH cte1 AS (SELECT ");
		String typeName = info.nearType.getName();
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		String field1;
		String field2;
		if (assocFieldName.equals("leftv")) {
			field1 = "? as leftv";
			field2 =  String.format("%s as rightv FROM %s", pair.name, typeName);
		} else {
			field1 =  String.format("%s as leftv FROM %s", pair.name, typeName);
			field2 = "? as rightv";
		}
		sc.o("%s, %s) ", field1, field2);
		RawFragment rawFrag = new RawFragment(sc.str);
		mergeIntoFrag.earlyL.add(rawFrag);
		
//		SELECT * from cte1
		sc = new StrCreator();
		sc.o(" SELECT * from cte1");
		
		rawFrag = new RawFragment(sc.str);
		mergeIntoFrag.rawFrag = rawFrag;
		return mergeIntoFrag;
	}
	
	protected MergeIntoStatementFragment generateMergeUsing(UpdateStatementFragment assocUpdateFrag, 
			RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, String subSelectWhere) {
		
		//part 2. 
//		INSERT INTO AddressCustomerAssoc as t (leftv,rightv) 
//		VALUES(100, (SELECT s.id FROM Customer as s)) 
//		ON CONFLICT (leftv,rightv) DO UPDATE SET leftv = 100,rightv=55
		
		MergeIntoStatementFragment mergeIntoFrag = new MergeIntoStatementFragment();
		mergeIntoFrag.prefix = "INSERT INTO";
		mergeIntoFrag.tblFrag = initTblFrag(assocUpdateFrag);
		mergeIntoFrag.tblFrag.alias = "t";
		
		StrCreator sc = new StrCreator();
		sc.o(" (leftv,rightv)");
		String typeName = info.nearType.getName();
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		String field1;
		String field2;
		if (assocFieldName.equals("leftv")) {
			field1 = "?";
			field2 =  String.format("(SELECT s.%s FROM %s as %s)", pair.name, typeName, "s");
		} else {
			field1 =  String.format("(SELECT s.%s FROM %s as %s)", pair.name, typeName, "s");
			field2 = "?";
		}

		sc.o(" VALUES(%s,%s)", field1, field2);
		if (assocFieldName.equals("leftv")) {
			field1 = "?";
			field2 =  String.format("s.%s", pair.name);
		} else {
			field2 =  String.format("s.%s", pair.name);
			field2 = "?";
		}
		sc.o(" ON CONFLICT (leftv,rightv) DO UPDATE SET leftv = %s,rightv=%s", field1, field2);
		
		RawFragment rawFrag = new RawFragment(sc.str);
		mergeIntoFrag.rawFrag = rawFrag;
		return mergeIntoFrag;
	}
	
	protected MergeIntoStatementFragment generateMergeForIdOnly(UpdateStatementFragment assocUpdateFrag, RelationInfo info, String assocFieldName, String assocField2, String subSelectWhere) {
		MergeIntoStatementFragment mergeIntoFrag = new MergeIntoStatementFragment();
		mergeIntoFrag.prefix = "INSERT INTO";
		mergeIntoFrag.tblFrag = initTblFrag(assocUpdateFrag);
		mergeIntoFrag.tblFrag.alias = "t";
		
//		INSERT INTO AddressCustomerAssoc as t (leftv,rightv) 
//		VALUES(100, (SELECT s.id FROM Customer as s WHERE s.id = 55)) 
//		ON CONFLICT (leftv,rightv) DO UPDATE SET leftv = 100,rightv=55
		
		StrCreator sc = new StrCreator();
		sc.o(" (leftv,rightv)");
		String typeName = info.nearType.getName();
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		String field1;
		String field2;
		if (assocFieldName.equals("leftv")) {
			field1 = "?";
			field2 =  String.format("(SELECT s.%s FROM %s as %s WHERE s.%s = ?)", pair.name, typeName, "s", pair.name);
		} else {
			field1 =  String.format("(SELECT s.%s FROM %s as %s WHERE s.%s = ?)", pair.name, typeName, "s", pair.name);
			field2 = "?";
		}

		sc.o(" VALUES(%s,%s)", field1, field2);
		sc.o(" ON CONFLICT (leftv,rightv) DO UPDATE SET leftv = ?,rightv=?");
		
		RawFragment rawFrag = new RawFragment(sc.str);
		mergeIntoFrag.rawFrag = rawFrag;
		return mergeIntoFrag;
	}
	

}