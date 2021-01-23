package org.delia.hld.simple;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.HLDQuery;
import org.delia.hld.SqlParamGenerator;
import org.delia.hld.StructField;
import org.delia.hld.cond.CustomFilterValueRenderer;
import org.delia.hld.cond.OpFilterCond;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class HavingOneSubSelectRenderer extends CustomFilterValueRendererBase implements CustomFilterValueRenderer {

	private RelationInfo relinfo;
	private boolean flipped;
	private String alias1;
	private String alias2;
	private SimpleSelect simple;
	private SimpleSqlGenerator sqlgen;
	private boolean isParent;
	
	public HavingOneSubSelectRenderer(FactoryService factorySvc, DTypeRegistry registry, SimpleSelect simpleSel, RelationInfo relinfo, boolean flipped, boolean isParent) {
		this.relinfo = relinfo;
		this.flipped = flipped;
		this.simple = simpleSel;
		this.sqlgen = new SimpleSqlGenerator(registry, factorySvc);
		this.isParent = isParent;
	}

	@Override
	public String render(Object obj, SqlParamGenerator paramGen, SqlStatement stm) {
		OpFilterCond ofc = (OpFilterCond) obj;
		StrCreator sc = new StrCreator();
		
		if (flipped) {
			if (isParent) return doParentRender(obj, paramGen, stm, sc);
			
			String s1 = String.format("%s.%s", ofc.val1.alias, ofc.val1.structField.fieldName);
			sc.o("%s IN ", s1);
			//" t1.cust IN (SELECT t2.cid FROM Customer as t2 INNER JOIN Address as t3 ON t2.cid=t3.cust WHERE t2.cid=? GROUP BY t2.cid HAVING COUNT(t2.cid)=1)", "1");
			DStructType structType = relinfo.nearType;
			String field1 = DValueHelper.findPrimaryKeyFieldPair(structType).name;
			String tbl1 = structType.getName();
			sc.o("(SELECT %s.%s FROM %s as %s", alias1, field1, tbl1, alias1);
			
			String field2 = relinfo.otherSide.fieldName; //DValueHelper.findPrimaryKeyFieldPair(relinfo.nearType).name;
			String tbl2 = relinfo.farType.getName();
			
			sc.o(" INNER JOIN %s as %s ON %s.%s=%s.%s", tbl2, alias2, alias1, field1, alias2, field2);
			String tmp = sqlgen.genAny(simple, stm);
			String clause = StringUtils.substringAfter(tmp, " WHERE ");
			sc.o(" WHERE %s", clause);
			
			sc.o(" GROUP BY %s.%s HAVING COUNT(%s.%s)=1)", alias1, field1, alias1, field1);
		} else {
			DeliaExceptionHelper.throwNotImplementedError("fl %b parent %b", flipped, isParent);
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

	private String doParentRender(Object obj, SqlParamGenerator paramGen, SqlStatement stm, StrCreator sc) {
		//" t1.cust IN (SELECT t2.cid FROM Customer as t2 INNER JOIN Address as t3 ON t2.cid=t3.cust WHERE t2.cid=? GROUP BY t2.cid HAVING COUNT(t2.cid)=1)", "1");
		DStructType structType = relinfo.nearType;
		String field1 = DValueHelper.findPrimaryKeyFieldPair(structType).name;
		String tbl1 = structType.getName();
		
		OpFilterCond ofc = (OpFilterCond) obj;
		String s1 = String.format("%s.%s", ofc.val1.alias, field1);
		sc.o("%s IN ", s1);
		
		sc.o("(SELECT %s.%s FROM %s as %s", alias1, field1, tbl1, alias1);
		
		String field2 = relinfo.otherSide.fieldName; //DValueHelper.findPrimaryKeyFieldPair(relinfo.nearType).name;
		String tbl2 = relinfo.farType.getName();
		
		sc.o(" INNER JOIN %s as %s ON %s.%s=%s.%s", tbl2, alias2, alias1, field1, alias2, field2);
		String tmp = sqlgen.genAny(simple, stm);
		String clause = StringUtils.substringAfter(tmp, " WHERE ");
		sc.o(" WHERE %s", clause);
		
		sc.o(" GROUP BY %s.%s HAVING COUNT(%s.%s)=1)", alias1, field1, alias1, field1);
		return sc.toString();
	}

	@Override
	public void assignAliases(Object obj, HLDQuery hld, HLDAliasBuilderAdapter aliasBuilder) {
		OpFilterCond ofc = (OpFilterCond) obj;
		String fieldName = ofc.val1.asSymbol();
		ofc.val1.structField = new StructField(null, fieldName, null);

		assignAliasesToFilter(simple, aliasBuilder);
		this.alias1 = simple.tblFrag.alias;
		this.alias2 = aliasBuilder.createAlias();
	}

}
