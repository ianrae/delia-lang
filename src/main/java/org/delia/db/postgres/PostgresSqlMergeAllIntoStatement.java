package org.delia.db.postgres;

import java.util.List;
import java.util.stream.Collectors;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sqlgen.SqlFieldListClause;
import org.delia.db.sqlgen.SqlMergeAllIntoStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.hld.HLDField;
import org.delia.hld.cud.HLDUpdate;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class PostgresSqlMergeAllIntoStatement extends SqlMergeAllIntoStatement {

	private SqlFieldListClause fieldListClause;

	public PostgresSqlMergeAllIntoStatement(SqlTableNameClause tblClause, SqlValueListClause valueClause) {
		super(tblClause, valueClause);
		this.fieldListClause = new SqlFieldListClause();
	}


	@Override
	public void init(HLDUpdate hld) {
		super.init(hld);
		fieldListClause.init(hld.fieldL);
	}
	
	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO");
		sc.o(tblClause.render(stm));
		
		sc.o("(%s,%s)", hld.mergeKey,hld.mergeKeyOther);
		
		DValue first = valueClause.getValueL().remove(0);
		stm.paramL.add(valueClause.renderValue(first));
		String tblName = hld.assocRelInfo.nearType.getName();
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.assocRelInfo.nearType);
		sc.o(" SELECT %s,? from %s", pkpair.name, tblName);
		
		stm.sql = sc.toString();
		return stm;
	}

//	@Override  OLD
//	public SqlStatement render() {
//		SqlStatement stm = new SqlStatement(hld);
//		StrCreator sc = new StrCreator();
//		sc.o("INSERT INTO");
//		sc.o(tblClause.render(stm));
//		
//		DValue first = valueClause.getValueL().remove(0);
//		valueClause.getValueL().clear();
//		valueClause.getValueL().add(first);
//		sc.o(valueClause.render(stm));
//
//		sc.o(" ON CONFLICT");
//		sc.o("(%s,%s)", hld.mergeKey,hld.mergeKeyOther);
//		sc.o(" DO UPDATE");
//		sc.o(" SET ");
//		int index = 0;
//		
//		List<HLDField> oneFieldL = hld.fieldL.stream().filter(x -> x.fieldName.equals(hld.mergeKeyOther)).collect(Collectors.toList());
//		ListWalker<HLDField> walker = new ListWalker<>(oneFieldL);
//		String conditionStr = null;
//		while(walker.hasNext()) {
//			HLDField ff = walker.next();
//			DValue inner = hld.valueL.get(index);
//			stm.paramL.add(valueClause.renderValue(inner));
//			
//			conditionStr = String.format("%s = %s", ff.render(), "?");
//			sc.o(conditionStr);
//			walker.addIfNotLast(sc, ", ");
//			index++;
//		}
//
//		
//		stm.sql = sc.toString();
//		return stm;
//	}
	
	
	
}
