package org.delia.db.sql.fragment;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.relation.RelationInfo;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

//single use!!!
public class AssocTableReplacer extends SelectFragmentParser {

	protected boolean useAliases = true;
	protected boolean isPostgres = false; //hack

	public AssocTableReplacer(FactoryService factorySvc, FragmentParserService fpSvc) {
		super(factorySvc, fpSvc);
	}
	
	public void buildUpdateAll(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
			String assocField2, String mainUpdateAlias, SqlStatement statement) {
//		  scenario 1 all:
//			  update Customer[true] {wid: 333, addr: [100]}
//			  has sql:
//			    update Customer set wid=333
//			    delete CustomerAddressAssoc
		updateFrag.assocUpdateFrag = null; //remove
		int startingNumParams = statement.paramL.size();
		
		//part 1. delete CustomerAddressAssoc
		DeleteStatementFragment deleteFrag = new DeleteStatementFragment();
		deleteFrag.paramStartIndex = statement.paramL.size();
		deleteFrag.tblFrag = initTblFrag(assocUpdateFrag);
		updateFrag.assocDeleteFrag = deleteFrag;

		if (isForeignKeyIdNull(mmMap, fieldName)) {
			return;
		}

		
		//part 2. 
//	    MERGE INTO CustomerAddressAssoc as T USING (SELECT id FROM CUSTOMER) AS S
//	    ON T.leftv = s.id WHEN MATCHED THEN UPDATE SET T.rightv = ?
//	    WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
		MergeIntoStatementFragment mergeIntoFrag = generateMergeUsingAll(assocUpdateFrag, info, assocFieldName, assocField2, mainUpdateAlias, "");

		updateFrag.assocMergeIntoFrag = mergeIntoFrag;
		
		List<OpFragment> clonedL = WhereListHelper.cloneWhereList(updateFrag.whereL);
		int extra = statement.paramL.size() - startingNumParams;
		int k = cloneParams(statement, clonedL, extra);
		addForeignKeyId(mmMap, fieldName, statement);
		//and again for mergeInto
		mergeIntoFrag.paramStartIndex = statement.paramL.size() - 1;
		cloneParams(statement, 1, 0);
//		cloneParams(statement, clonedL, 1, 0);
		if (isPostgres) {
			int n = statement.paramL.size();
			statement.paramL.remove(n - 1);
//			statement.paramL.remove(n - 2);
//			cloneParams(statement, clonedL, 1, 0);
		}
	}
	
	protected MergeIntoStatementFragment generateMergeUsingAll(UpdateStatementFragment assocUpdateFrag, 
			RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, String subSelectWhere) {
		return generateMergeUsing(assocUpdateFrag, info, assocFieldName, assocField2, mainUpdateAlias, subSelectWhere);
	}	
	protected MergeIntoStatementFragment generateMergeUsing(UpdateStatementFragment assocUpdateFrag, 
			RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, String subSelectWhere) {
		
		//part 2. 
//	    MERGE INTO CustomerAddressAssoc as T USING (SELECT id FROM CUSTOMER) AS S
//	    ON T.leftv = s.id WHEN MATCHED THEN UPDATE SET T.rightv = ?
//	    WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
		MergeIntoStatementFragment mergeIntoFrag = new MergeIntoStatementFragment();
		mergeIntoFrag.tblFrag = initTblFrag(assocUpdateFrag);
		mergeIntoFrag.tblFrag.alias = "t";
		
		StrCreator sc = new StrCreator();
		String typeName = info.nearType.getName();
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		sc.o(" USING (SELECT %s FROM %s as %s%s) as s", pair.name, typeName, mainUpdateAlias, subSelectWhere);
		sc.o(" ON t.%s = s.%s", assocField2, pair.name);
		sc.o("\n WHEN MATCHED THEN UPDATE SET t.%s = ?", assocFieldName);
		
		String fields;
		if (assocFieldName.equals("leftv")) {
			fields = String.format("(?,s.%s)", pair.name);
		} else {
			fields = String.format("(s.%s,?)", pair.name);
		}
		
		sc.o("\n WHEN NOT MATCHED THEN INSERT (leftv,rightv) VALUES %s", fields);
		RawFragment rawFrag = new RawFragment(sc.toString());
		mergeIntoFrag.rawFrag = rawFrag;
		return mergeIntoFrag;
	}


	public void buildUpdateByIdOnly(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
			String assocField2, SqlStatement statement) {
//		  scenario 2 id:
//			  update Customer[55] {wid: 333, addr: [100]}
//			  has sql:
//			    update Customer set wid=333 where id=55
//			    delete CustomerAddressAssoc where leftv=55 and rightv <> 100
//			    merge into CustomerAddressAssoc key(leftv) values(55,100) //only works if 1 record updated/inserted
		updateFrag.assocUpdateFrag = null; //remove
		int startingNumParams = statement.paramL.size();

		//part 1. delete CustomerAddressAssoc where leftv=55 and rightv <> 100
		DeleteStatementFragment deleteFrag = new DeleteStatementFragment();
		deleteFrag.tblFrag = initTblFrag(assocUpdateFrag);
		deleteFrag.paramStartIndex = statement.paramL.size();
		
		updateFrag.assocDeleteFrag = deleteFrag;
		if (isForeignKeyIdNull(mmMap, fieldName)) {
			StrCreator sc = new StrCreator();
			sc.o("%s = ?", assocField2); 
			RawFragment rawFrag = new RawFragment(sc.toString());
			deleteFrag.whereL.add(rawFrag);
			
			List<OpFragment> clonedL = WhereListHelper.cloneWhereList(updateFrag.whereL);
			int extra = statement.paramL.size() - startingNumParams;
			cloneParams(statement, clonedL, extra);
			return;
		}
		
		StrCreator sc = new StrCreator();
		sc.o("%s = ? and %s <> ?", assocField2, assocFieldName); 
		RawFragment rawFrag = new RawFragment(sc.toString());
		deleteFrag.whereL.add(rawFrag);
		
		//part 2. merge into CustomerAddressAssoc key(leftv) values(55,100) //only works if 1 record updated/inserted
		MergeIntoStatementFragment mergeIntoFrag = generateMergeForIdOnly(assocUpdateFrag, info, assocFieldName, assocField2, sc.toString());
		
		updateFrag.assocMergeIntoFrag = mergeIntoFrag;
		
		List<OpFragment> clonedL = WhereListHelper.cloneWhereList(updateFrag.whereL);
		if (!clonedL.isEmpty()) {
			int extra = statement.paramL.size() - startingNumParams;
			cloneParams(statement, clonedL, extra);
		} else {
			List<SqlFragment> hldclonedL = WhereListHelper.cloneWhereListHLD(updateFrag.whereL);
			int extra = statement.paramL.size() - startingNumParams;
			cloneParamsEeex(statement, hldclonedL, extra);
		}
		addForeignKeyId(mmMap, fieldName, statement);
		//and again for mergeInto
		mergeIntoFrag.paramStartIndex = statement.paramL.size();
		cloneParams(statement, 2, 0);
		if (assocFieldName.equals("leftv")) {
			swapLastTwo(statement);
		}

		if (isPostgres) {
			int n = statement.paramL.size();
			cloneParams(statement, 2, 0);
		}
	}
	
	protected MergeIntoStatementFragment generateMergeForIdOnly(UpdateStatementFragment assocUpdateFrag, RelationInfo info, String assocFieldName, String assocField2, String subSelectWhere) {
		MergeIntoStatementFragment mergeIntoFrag = new MergeIntoStatementFragment();
		mergeIntoFrag.tblFrag = initTblFrag(assocUpdateFrag);
		
		StrCreator sc = new StrCreator();
		sc.o(" KEY(%s) VALUES(?,?)", assocField2);
		RawFragment rawFrag = new RawFragment(sc.toString());
		mergeIntoFrag.rawFrag = rawFrag;
		return mergeIntoFrag;
	}

	public void buildUpdateOther(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
			String assocField2, List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
//		  scenario 3 id:
//			  update Customer[55] {wid: 333, addr: [100]}
//			  has sql:
//			    update Customer set wid=333 where wid>20
//	    	    delete CustomerAddressAssoc where rightv <> 100 and leftv in (SELECT id FROM Address as a WHERE a.z > ?)
//	    	    WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1
		updateFrag.assocUpdateFrag = null; //remove
		int startingNumParams = statement.paramL.size();

		//part 1. delete CustomerAddressAssoc where leftv=55 and rightv <> 100
		DeleteStatementFragment deleteFrag = new DeleteStatementFragment();
		deleteFrag.tblFrag = initTblFrag(assocUpdateFrag);
		deleteFrag.paramStartIndex = statement.paramL.size();

		updateFrag.assocDeleteFrag = deleteFrag;
		
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		StrCreator sc = new StrCreator();
		sc.o(" %s IN", assocField2);
		sc.o(" (SELECT %s FROM %s as %s WHERE", keyPair.name, info.nearType.getName(), mainUpdateAlias);

		List<OpFragment> clonedL = WhereListHelper.cloneWhereList(existingWhereL);
		for(OpFragment opff: clonedL) {
			sc.o(opff.render());
		}
		sc.o(")");
		RawFragment rawFrag = new RawFragment(sc.toString());
		deleteFrag.whereL.add(rawFrag);
		
		int pos = sc.toString().indexOf(" WHERE ");
		String subSelectWhere = sc.toString().substring(pos);
		subSelectWhere = StringUtils.substringBeforeLast(subSelectWhere, ")");
		
		if (this.isForeignKeyIdNull(mmMap, fieldName)) {
			List<OpFragment> clonedL2 = WhereListHelper.cloneWhereList(updateFrag.whereL);
			int extra = statement.paramL.size() - startingNumParams;
			cloneParams(statement, clonedL2, extra);
			return;
		}
		
		//part 2. 
//	    WITH cte1 AS (SELECT ? as leftv, id as rightv FROM Customer) INSERT INTO AddressCustomerAssoc as t SELECT * from cte1
		MergeIntoStatementFragment mergeIntoFrag = generateMergeUsing(assocUpdateFrag, info, assocFieldName, assocField2, mainUpdateAlias, subSelectWhere);

		updateFrag.assocMergeIntoFrag = mergeIntoFrag;
		
		mergeIntoFrag.paramStartIndex = statement.paramL.size();
		List<OpFragment> clonedL2 = WhereListHelper.cloneWhereList(updateFrag.whereL);
		int extra = statement.paramL.size() - startingNumParams;
		int k = cloneParams(statement, clonedL2, extra);
		//and again for mergeInto
		if (isPostgres) {
			mergeIntoFrag.paramStartIndex += k;
			cloneParams(statement, clonedL2, extra);
			addForeignKeyId(mmMap, fieldName, statement);
			int n = statement.paramL.size();
			DValue last = statement.paramL.remove(n - 1);
			statement.paramL.add(n - k - 1, last);
		} else {
			mergeIntoFrag.paramStartIndex += k;
			cloneParams(statement, clonedL2, extra);
			addForeignKeyId(mmMap, fieldName, statement);
			addForeignKeyId(mmMap, fieldName, statement);
		}
	}
	
	protected TableFragment initTblFrag(StatementFragmentBase assocUpdateFrag) {
		TableFragment tblFrag = new TableFragment();
		tblFrag.alias = null;
		tblFrag.name = assocUpdateFrag.tblFrag.name;
		return tblFrag;
	}


	protected void addForeignKeyId(Map<String, DRelation> mmMap, String fieldName, SqlStatement statement) {
		DRelation drel = mmMap.get(fieldName); //100
		//hack hack hack
		DValue dvalToUse;
		if (drel.isMultipleKey()) {
			dvalToUse = drel.getMultipleKeys().get(0); //probably not always correct 
		} else {
			dvalToUse = drel.getForeignKey(); 
		}
		statement.paramL.add(dvalToUse);
	}
	protected boolean isForeignKeyIdNull(Map<String, DRelation> mmMap, String fieldName) {
		DRelation drel = mmMap.get(fieldName); //100
		return drel == null;
	}
	
	
	protected DValue getLastParam(SqlStatement statement) {
		int n = statement.paramL.size();
		DValue dval = statement.paramL.get(n - 1);
		return dval;
	}
	protected int cloneLastParam(SqlStatement statement) {
		int n = statement.paramL.size();
		DValue dval = statement.paramL.get(n - 1);
		statement.paramL.add(dval);
		return 1;
	}
	protected int cloneParams(SqlStatement statement, List<OpFragment> clonedL, int extra) {
		//clone params 
		int numToAdd = 0;
		for(SqlFragment ff: clonedL) {
			numToAdd += ff.getNumSqlParams();
		}
		cloneParams(statement, numToAdd, extra);
		return numToAdd;
	}
	//hack hack hack
	protected int cloneParamsEeex(SqlStatement statement, List<SqlFragment> clonedL, int extra) {
		//clone params 
		int numToAdd = 0;
		for(SqlFragment ff: clonedL) {
			numToAdd += ff.getNumSqlParams();
		}
		cloneParams(statement, numToAdd, extra);
		return numToAdd;
	}
	protected void cloneParams(SqlStatement statement, int numToAdd, int extra) {
		//clone params 
		int n = statement.paramL.size();
		log.logDebug("cloneParams %d %d", numToAdd, n);
		for(int i = 0; i < numToAdd; i++) {
			int k = n - (numToAdd - i) - extra;
			DValue previous = statement.paramL.get(k);
			statement.paramL.add(previous); //add copy
		}
	}
	protected void swapLastTwo(SqlStatement statement) {
		int n = statement.paramL.size();
		log.logDebug("swapLastTwoParams %d", n);
		DValue dval1 = statement.paramL.get(n - 2);
		DValue dval2 = statement.paramL.get(n - 1);
		statement.paramL.set(n - 2, dval2);
		statement.paramL.set(n - 1, dval1);
	}
	protected void buildAssocTblUpdate(UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, SqlStatement statement) {
		DRelation drel = mmMap.get(fieldName); //100
		DValue dvalToUse  = drel.getForeignKey(); 

		RelationInfo farInfo = info.otherSide;// DRuleHelper.findOtherSideMany(info.farType, structType);
		TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
		TypePair rightPair = new TypePair(assocFieldName, pair2.type);
		FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
		statement.paramL.add(dvalToUse);
		assocUpdateFrag.setValuesL.add("?");
		assocUpdateFrag.fieldL.add(ff);
	}
	public void useAliases(boolean b) {
		this.useAliases = b;
	}

	public void assocCrudInsert(UpdateStatementFragment updateFrag, 
			InsertStatementFragment insertFrag, DStructType structType, DValue mainKeyVal, DValue keyVal, RelationInfo info,
			String mainUpdateAlias, SqlStatement statement, boolean reversed) {
		
//	    INSERT INTO CustomerAddressAssoc as T (leftv, rightv) VALUES(s.id, ?)
//		int startingNumParams = statement.paramL.size();

		//struct is Address AddressCustomerAssoc
		if (!reversed) {
			genAssocTblInsertRows(insertFrag, true, mainKeyVal, info.nearType, info.farType, keyVal, info);
		} else {
			genAssocTblInsertRows(insertFrag, false, mainKeyVal, info.farType, info.nearType, keyVal, info);
		}
		
		insertFrag.paramStartIndex = statement.paramL.size();
		statement.paramL.addAll(insertFrag.statement.paramL);
		insertFrag.statement.paramL.clear();
	}
	private void genAssocTblInsertRows(InsertStatementFragment assocInsertFrag, boolean mainDValFirst, 
			DValue mainDVal, DStructType farType, DStructType nearType, DValue xdval, RelationInfo info) {
		TypePair keyPair1 = DValueHelper.findPrimaryKeyFieldPair(info.farType);
		TypePair keyPair2 = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		if (mainDValFirst) {
			genxrow(assocInsertFrag, "leftv", keyPair1, mainDVal);
			genxrow(assocInsertFrag, "rightv", keyPair2, xdval);
		} else {
			genxrow(assocInsertFrag, "leftv", keyPair1, xdval);
			genxrow(assocInsertFrag, "rightv", keyPair2, mainDVal);
		}
	}

	private void genxrow(InsertStatementFragment assocInsertFrag, String assocFieldName, TypePair keyPair1, DValue dval) {
		TypePair tmpPair = new TypePair(assocFieldName, keyPair1.type);
		FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocInsertFrag.tblFrag, assocInsertFrag, tmpPair);
		assocInsertFrag.setValuesL.add("?");
		assocInsertFrag.fieldL.add(ff);
		assocInsertFrag.statement.paramL.add(dval);
	}
	private void genxrow(UpdateStatementFragment assocInsertFrag, String assocFieldName, TypePair keyPair1, DValue dval) {
		TypePair tmpPair = new TypePair(assocFieldName, keyPair1.type);
		FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocInsertFrag.tblFrag, assocInsertFrag, tmpPair);
		assocInsertFrag.setValuesL.add("?");
		assocInsertFrag.fieldL.add(ff);
		assocInsertFrag.statement.paramL.add(dval);
	}
	
	public void assocCrudDelete(UpdateStatementFragment updateFrag, 
			DeleteStatementFragment deleteFrag, DStructType structType, DValue mainKeyVal, DValue keyVal, RelationInfo info,
			String mainUpdateAlias, SqlStatement statement, boolean reversed) {
		
		//part 1. delete CustomerAddressAssoc where leftv=55 and rightv <> 100
		deleteFrag.paramStartIndex = statement.paramL.size();
		
		StrCreator sc = new StrCreator();
		sc.o("%s = ? and %s = ?", "leftv", "rightv"); 
		RawFragment rawFrag = new RawFragment(sc.toString());
		deleteFrag.whereL.add(rawFrag);
		
		if (reversed) {
			statement.paramL.add(keyVal);
			statement.paramL.add(mainKeyVal);
		} else {
			statement.paramL.add(mainKeyVal);
			statement.paramL.add(keyVal);
		}
	}
	
	public void assocCrudUpdate(UpdateStatementFragment updateFrag, 
			UpdateStatementFragment innerUpdateFrag, DStructType structType, DValue mainKeyVal, DValue oldVal, DValue newVal, RelationInfo info,
			String mainUpdateAlias, SqlStatement statement, boolean reversed) {
		
		//part 1. delete CustomerAddressAssoc where leftv=55 and rightv <> 100
		innerUpdateFrag.paramStartIndex = statement.paramL.size();
		
		TypePair keyPair1 = DValueHelper.findPrimaryKeyFieldPair(info.farType);
		TypePair keyPair2 = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		if (reversed) {
			genxrow(innerUpdateFrag, "leftv", keyPair1, newVal);
			genxrow(innerUpdateFrag, "rightv", keyPair2, mainKeyVal);
		} else {
			genxrow(innerUpdateFrag, "leftv", keyPair1, mainKeyVal);
			genxrow(innerUpdateFrag, "rightv", keyPair2, newVal);
		}
		
		StrCreator sc = new StrCreator();
		sc.o(" %s = ? and %s = ?", "leftv", "rightv"); 
		RawFragment rawFrag = new RawFragment(sc.toString());
		innerUpdateFrag.whereL.add(rawFrag);
		
		innerUpdateFrag.paramStartIndex = statement.paramL.size();
		statement.paramL.addAll(innerUpdateFrag.statement.paramL);
		innerUpdateFrag.statement.paramL.clear();
		
		if (reversed) {
			statement.paramL.add(oldVal);
			statement.paramL.add(mainKeyVal);
		} else {
			statement.paramL.add(mainKeyVal);
			statement.paramL.add(oldVal);
		}
	}
	
}