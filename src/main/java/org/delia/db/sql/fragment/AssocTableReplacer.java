package org.delia.db.sql.fragment;

import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.SqlHelperFactory;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

//single use!!!
public class AssocTableReplacer extends SelectFragmentParser {

	private boolean useAliases = true;

	public AssocTableReplacer(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
			SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
		super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
	}


	public void buildUpdateByIdOnly(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
			String assocField2, List<OpFragment> oplist, SqlStatement statement) {
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
		deleteFrag.tblFrag = new TableFragment();
		deleteFrag.tblFrag.alias = null;
		deleteFrag.tblFrag.name = assocUpdateFrag.tblFrag.name;
		
		StrCreator sc = new StrCreator();
		sc.o("%s = ? and %s <> ?", assocField2, assocFieldName); //TODO should be rightv NOT IN (100) so can handle list
		RawFragment rawFrag = new RawFragment(sc.str);
		deleteFrag.whereL.add(rawFrag);
		
		//part 2. merge into CustomerAddressAssoc key(leftv) values(55,100) //only works if 1 record updated/inserted
		MergeIntoStatementFragment mergeIntoFrag = new MergeIntoStatementFragment();
		mergeIntoFrag.tblFrag = new TableFragment();
		mergeIntoFrag.tblFrag.alias = null;
		mergeIntoFrag.tblFrag.name = assocUpdateFrag.tblFrag.name;
		
		sc = new StrCreator();
		sc.o(" KEY(%s) VALUES(?,?)", assocField2);
		rawFrag = new RawFragment(sc.str);
		mergeIntoFrag.rawFrag = rawFrag;
		
		updateFrag.assocDeleteFrag = deleteFrag;
		updateFrag.assocMergeInfoFrag = mergeIntoFrag;
		
		List<OpFragment> clonedL = WhereListHelper.cloneWhereList(updateFrag.whereL);
		int extra = statement.paramL.size() - startingNumParams;
		cloneParams(statement, clonedL, extra);
		addForeignKeyId(mmMap, fieldName, statement);
		//and again for mergeInto
		cloneParams(statement, clonedL, 2, 0);
		if (assocFieldName.equals("leftv")) {
			swapLastTwo(statement);
		}
	}
	
	protected void addForeignKeyId(Map<String, DRelation> mmMap, String fieldName, SqlStatement statement) {
		DRelation drel = mmMap.get(fieldName); //100
		DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
		statement.paramL.add(dvalToUse);
	}
	
	
	private void cloneParams(SqlStatement statement, List<OpFragment> clonedL, int extra) {
		//clone params 
		int numToAdd = 0;
		for(SqlFragment ff: clonedL) {
			numToAdd += ff.getNumSqlParams();
		}
		cloneParams(statement, clonedL, numToAdd, extra);
	}
	private void cloneParams(SqlStatement statement, List<OpFragment> clonedL, int numToAdd, int extra) {
		//clone params 
		int n = statement.paramL.size();
		log.logDebug("cloneParams %d %d", numToAdd, n);
		for(int i = 0; i < numToAdd; i++) {
			int k = n - (numToAdd - i) - extra;
			DValue previous = statement.paramL.get(k);
			statement.paramL.add(previous); //add copy
		}
	}
	private void swapLastTwo(SqlStatement statement) {
		int n = statement.paramL.size();
		log.logDebug("swapLastTwoParams %d", n);
		DValue dval1 = statement.paramL.get(n - 2);
		DValue dval2 = statement.paramL.get(n - 1);
		statement.paramL.set(n - 2, dval2);
		statement.paramL.set(n - 1, dval1);
	}
	protected void buildAssocTblUpdate(UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, SqlStatement statement) {
		DRelation drel = mmMap.get(fieldName); //100
		DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later

		RelationInfo farInfo = DRuleHelper.findOtherSideMany(info.farType, structType);
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
}