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
		RawFragment rawFrag = new RawFragment(sc.str);
		mergeIntoFrag.rawFrag = rawFrag;
		return mergeIntoFrag;
	}

}