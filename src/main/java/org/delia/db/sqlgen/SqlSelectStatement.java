package org.delia.db.sqlgen;

import org.delia.assoc.DatIdMap;
import org.delia.db.newhls.ConversionHelper;
import org.delia.db.newhls.HLDQuery;
import org.delia.db.newhls.cud.TypeOrTable;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;

public class SqlSelectStatement implements SqlStatementGenerator {

	private HLDQuery hld;
	private SqlTableNameClause tblClause;
	private ConversionHelper conversionHelper;
	private DatIdMap datIdMap;
	
	public SqlSelectStatement(DatIdMap datIdMap, SqlTableNameClause tblClause, ConversionHelper conversionHelper) {
		this.datIdMap = datIdMap;
		this.tblClause = tblClause;
		this.conversionHelper = conversionHelper;
//		this.fieldClause = fieldClause;
//		this.valueClause = valueClause;
	}
	
	public void init(HLDQuery hld) {
		this.hld = hld;
		
		TypeOrTable typeOrTbl = new TypeOrTable(hld.fromType);
		typeOrTbl.alias = hld.fromAlias;
		tblClause.init(typeOrTbl);
//		fieldClause.init(hld.fieldL);
//		valueClause.init(hld.valueL);
	}
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("xxxwerwerINSERT INTO");
		sc.o(tblClause.render(stm));
		
		stm.sql = sc.toString();
		
//		//convert strings to dates where needed NOT NEEDED
//		for(DValue dval: stm.paramL) {
//			if (dval != null) {
//				DValue xx = conversionHelper.convertDValToActual(dval.getType(), dval);
//			}
//		}
		
		return stm;
	}

}
