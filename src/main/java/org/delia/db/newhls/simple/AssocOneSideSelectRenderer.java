package org.delia.db.newhls.simple;

import org.apache.commons.lang3.StringUtils;
import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.newhls.HLDAliasBuilderAdapter;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.newhls.StructField;
import org.delia.db.newhls.cond.CustomFilterValueRenderer;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.relation.RelationInfo;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

public class AssocOneSideSelectRenderer extends CustomFilterValueRendererBase implements CustomFilterValueRenderer {

	private RelationInfo relinfo;
	private boolean flipped;
	private String alias1;
	private SimpleSelect simple;
	private SimpleSqlGenerator sqlgen;
	private DatIdMap datIdMap;
	
	public AssocOneSideSelectRenderer(FactoryService factorySvc, DTypeRegistry registry, SimpleSelect simpleSel, RelationInfo relinfo, boolean flipped, DatIdMap datIdMap) {
		this.relinfo = relinfo;
		this.flipped = flipped;
		this.simple = simpleSel;
		this.sqlgen = new SimpleSqlGenerator(registry, factorySvc);
		this.datIdMap = datIdMap;
	}

	@Override
	public String render(Object obj, SqlParamGenerator paramGen, SqlStatement stm) {
		OpFilterCond ofc = (OpFilterCond) obj;
		StrCreator sc = new StrCreator();
		
		if (flipped) {
			//where t1.rightv IN (select t2.rightv from DAT as t2 where t2.leftv=55)
			String field1 = datIdMap.getAssocOtherField(relinfo);
			String tbl1 = datIdMap.getAssocTblName(relinfo.getDatId());
			String s1 = String.format("%s.%s", ofc.val1.alias, field1);
			sc.o("%s IN ", s1);
			sc.o("(SELECT %s.%s FROM %s as %s", alias1, field1, tbl1, alias1);
			
			String tmp = sqlgen.genAny(simple, stm);
			String clause = StringUtils.substringAfter(tmp, " WHERE ");
			sc.o(" WHERE %s)", clause);
		} else {
			DeliaExceptionHelper.throwNotImplementedError("fl %b", flipped);
//			//DELETE FROM Customer as t0 WHERE t0.cid IN (SELECT t1.cust FROM Address as t1 INNER JOIN Customer as t2 ON t1.cust=t2.cid WHERE t1.id=? GROUP BY t1.cust HAVING COUNT(t1.cid)=1)
//			String field1 = DValueHelper.findPrimaryKeyFieldPair(relinfo.nearType).name;
//			String tbl1 = relinfo.nearType.getName();
//			sc.o("(SELECT %s.%s FROM %s as %s", alias1, field1, tbl1, alias1);
//			
//			String field2 = relinfo.otherSide.fieldName;
//			String tbl2 = relinfo.farType.getName();
//			
//			sc.o(" INNER JOIN %s as %s ON %s.%s=%s.%s", tbl2, alias2, alias1, field1, alias2, field2);
//			String tmp = sqlgen.genAny(simple, stm);
//			String clause = StringUtils.substringAfter(tmp, " WHERE ");
//			sc.o(" WHERE %s", clause);
//			
//			sc.o(" GROUP BY %s.%s HAVING COUNT(%s.%s)=1)", alias1, field1, alias1, field1);
		}
		return sc.toString();
	}

	@Override
	public void assignAliases(Object obj, HLDQuery hld, HLDAliasBuilderAdapter aliasBuilder) {
		OpFilterCond ofc = (OpFilterCond) obj;
		String fieldName = ofc.val1.asSymbol();
		ofc.val1.structField = new StructField(null, fieldName, null);

		assignAliasesToFilter(simple, aliasBuilder);
		this.alias1 = simple.tblFrag.alias;
	}

}
