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
	}
	
	protected MergeIntoStatementFragment generateMergeUsing(UpdateStatementFragment assocUpdateFrag, 
			RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, String subSelectWhere) {
		
		//part 2. 
//		 INSERT INTO AddressCustomerAssoc as a (leftv, rightv)
//		  SELECT 100,b.id FROM Customer as b
//		  ON CONFLICT (leftv,rightv) 
//		  DO UPDATE SET leftv = 100
		MergeIntoStatementFragment mergeIntoFrag = new MergeIntoStatementFragment();
		mergeIntoFrag.prefix = "INSERT INTO";
		mergeIntoFrag.tblFrag = initTblFrag(assocUpdateFrag);
		mergeIntoFrag.tblFrag.alias = "t";
		
		StrCreator sc = new StrCreator();
		sc.o(" (leftv,rightv)");
		String typeName = info.nearType.getName();
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		String fields;
		if (assocFieldName.equals("leftv")) {
			fields = String.format("(?,s.%s)", pair.name);
		} else {
			fields = String.format("(s.%s,?)", pair.name);
		}
		
		sc.o(" SELECT %s FROM %s as %s%s as s", fields, typeName, "s", subSelectWhere);
		if (assocFieldName.equals("leftv")) {
			fields = String.format("leftv = ?", pair.name);
		} else {
			fields = String.format("rightv = ?", pair.name);
		}
		sc.o(" ON CONFLICT (left,rightv) DO UPDATE SET %s", fields);
		
		RawFragment rawFrag = new RawFragment(sc.str);
		mergeIntoFrag.rawFrag = rawFrag;
		return mergeIntoFrag;
	}

}