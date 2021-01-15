package org.delia.db.newhls.simple;

import org.apache.commons.lang3.StringUtils;
import org.delia.core.FactoryService;
import org.delia.db.newhls.HLDAliasBuilderAdapter;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.SqlParamGenerator;
import org.delia.db.newhls.cond.CustomFilterValueRenderer;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.relation.RelationInfo;
import org.delia.type.DTypeRegistry;
import org.delia.util.DValueHelper;

public class HavingOneSubSelectRenderer extends CustomFilterValueRendererBase implements CustomFilterValueRenderer {

	private RelationInfo relinfo;
	private boolean flipped;
	private String alias1;
	private String alias2;
	private SimpleSelect simple;
	private SimpleSqlGenerator sqlgen;
	
	public HavingOneSubSelectRenderer(FactoryService factorySvc, DTypeRegistry registry, SimpleSelect simpleSel, RelationInfo relinfo, boolean flipped) {
		this.relinfo = relinfo;
		this.flipped = flipped;
		this.simple = simpleSel;
		this.sqlgen = new SimpleSqlGenerator(registry, factorySvc);
	}

	@Override
	public String render(Object obj, SqlParamGenerator paramGen, SqlStatement stm) {
		//TODO: do flipped later!!
		
		//(select a.cid from customer as a inner join address as b on a.cid=b.cust where a.cid=? group by a.cid having count(a.cid)=1);
		String field1 = DValueHelper.findPrimaryKeyFieldPair(relinfo.nearType).name;
		String tbl1 = relinfo.nearType.getName();
		StrCreator sc = new StrCreator();
		sc.o("(SELECT %s.%s FROM %s as %s", alias1, field1, tbl1, alias1);
		
		String field2 = relinfo.otherSide.fieldName;
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
		assignAliasesToFilter(simple, aliasBuilder);
		this.alias1 = simple.tblFrag.alias;
		this.alias2 = aliasBuilder.createAlias();
	}

}
