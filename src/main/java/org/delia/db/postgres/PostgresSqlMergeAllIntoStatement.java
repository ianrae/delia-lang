package org.delia.db.postgres;

import java.util.List;
import java.util.Optional;

import org.delia.db.SqlStatement;
import org.delia.db.sql.StrCreator;
import org.delia.db.sqlgen.SqlMergeAllIntoStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.hld.SqlColumn;
import org.delia.type.DValue;

public class PostgresSqlMergeAllIntoStatement extends SqlMergeAllIntoStatement {

	public PostgresSqlMergeAllIntoStatement(SqlTableNameClause tblClause, SqlValueListClause valueClause) {
		super(tblClause, valueClause);
	}

	@Override
	public SqlStatement render() {
		SqlStatement stm = new SqlStatement(hld);
		StrCreator sc = new StrCreator();
		sc.o("xxxxxxxMERGE INTO");
		sc.o(tblClause.render(stm));
		
//		sc.o(" KEY(%s)", hld.mergeKey);
//		sc.o(valueClause.render(stm));
		
		String alias = hld.typeOrTbl.alias;
		SqlColumn col = new SqlColumn(alias, hld.mergeKey);
		
		//USING (SELECT id FROM CUSTOMER) AS S ON T.leftv = s.id
		sc.o(" USING (SELECT %s FROM %s) AS S", hld.mergePKField, hld.mergeType);
		sc.o(" ON %s = s.%s", col.render(), hld.mergePKField);
		
		//WHEN MATCHED THEN UPDATE SET T.rightv = ?
		col = new SqlColumn(alias, hld.mergeKeyOther);
		sc.o(" WHEN MATCHED THEN UPDATE SET %s = ?", col.render());
		DValue inner = findFirstNonNullValue(hld.valueL); 
		stm.paramL.add(inner);
//		stm.paramL.add(inner);
		
//		//WHEN NOT MATCHED THEN INSERT (leftv, rightv) VALUES(s.id, ?)
//		sc.o(" WHEN NOT MATCHED THEN INSERT (leftv, rightv)", hld.mergeKey, hld.mergeKeyOther);
//		sc.o(" VALUES(s.%s, ?)", hld.mergePKField);
		
		stm.sql = sc.toString();
		return stm;
	}
	
	private DValue findFirstNonNullValue(List<DValue> valueL) {
		Optional<DValue> opt = valueL.stream().filter(x -> x != null).findFirst();
		return opt.orElse(null);
	}

}
