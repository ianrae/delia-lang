package org.delia.db.sql.prepared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.TableCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class InsertStatementGenerator extends ServiceBase {
	private DTypeRegistry registry;
	private SqlNameFormatter nameFormatter;
	protected boolean specialHandlingForEmptyInsertFlag = false; //insert into Participant values (default);
	private TableExistenceService existSvc;

	public InsertStatementGenerator(FactoryService factorySvc, DTypeRegistry registry, SqlNameFormatter nameFormatter, 
				TableExistenceService existSvc) {
		super(factorySvc);
		this.registry = registry;
		this.nameFormatter = nameFormatter;
		this.existSvc = existSvc;
	}

	private String tblName(String typeName) {
		return nameFormatter.convert(typeName);
	}
	private String tblName(DType dtype) {
		return nameFormatter.convert(dtype);
	}
	

	public SqlStatement generateInsert(DValue dval, List<TableInfo> tblInfoL) {
		Map<String,DRelation> map = new HashMap<>(); //local var. ok to not use ConcurrentHashMap here
		SqlStatement statement = new SqlStatement();
		String sql = doGenerateInsert(dval, map, statement);
		sql += doGenerateAssocInsertIfNeeded(dval, tblInfoL, map, statement);
		statement.sql = sql;
		return statement;
	}		
	private String doGenerateInsert(DValue dval, Map<String, DRelation> map, SqlStatement statement) {
		//			INSERT INTO Customers (CustomerName, City, Country)
		//			VALUES ('Cardinal', 'Stavanger', 'Norway');
		DStructType dtype = (DStructType) dval.getType();
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO %s (", tblName(dtype));
		int index = 0;
		for(TypePair pair: dtype.getAllFields()) {
			if (pair.type.isStructShape()) {
				if (! shouldGenerateFKConstraint(pair, dtype)) {
					continue;
				}
				if (DRuleHelper.isManyToManyRelation(pair, dtype)) {
					DValue inner = dval.asStruct().getField(pair.name);
					if (inner != null) {
						map.put(pair.name, inner.asRelation());
					}
					continue;
				}
			}

			DValue inner = dval.asStruct().getField(pair.name);
			if (inner == null) {
				continue;
			} else if (dtype.fieldIsSerial(pair.name)) {
				DeliaExceptionHelper.throwError("serial-value-cannot-be-provided", "Type %s, field %s - do not specify a value for a serial field", dtype.getName(), pair.name);
			}

			if (index > 0) {
				sc.o(", ");
			}
			sc.o(pair.name);
			index++;
		}
		sc.o(")");
		sc.nl();
		
		if (index == 0 && this.specialHandlingForEmptyInsertFlag) {
			//insert into Participant values (default);
			String s = sc.str.substring(0, sc.str.indexOf('('));
			sc.str = s;
			sc.o(" values (default);");
			return sc.str;
		}

		//			VALUES ('Cardinal', 'Stavanger', 'Norway');
		sc.o("VALUES (");
		index = 0;
		for(TypePair pair: dtype.getAllFields()) {
			if (DRuleHelper.isManyToManyRelation(pair, dtype)) {
				continue;
			}
			if (generateInsertField(sc, dval, pair, dtype, index, statement)) {
				index++;
			}
		}
		sc.o(");");
		sc.nl();
		return sc.str;
	}
	private boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && !info.isParent) {
			return true;
		}
		return false;
	}
	private String doGenerateAssocInsertIfNeeded(DValue dval, List<TableInfo> tblInfoL, Map<String, DRelation> map, SqlStatement statement) {
		String sql = "";
		if (map.isEmpty()) {
			return sql;
		}

		DStructType dtype = (DStructType) dval.getType();
		for(TypePair pair: dtype.getAllFields()) {
			RelationInfo info = DRuleHelper.findManyToManyRelation(pair, dtype);
			if (info != null) {
				fillTableInfoIfNeeded(tblInfoL, info);
				TableInfo tblinfo = TableInfoHelper.findTableInfo(tblInfoL, pair, info);
				sql += genAssocInsert(dval, pair, tblinfo, map, statement);
			}
		}
		return sql;
	}
	private void fillTableInfoIfNeeded(List<TableInfo> tblInfoL, RelationInfo info) {
		String tbl1 = info.nearType.getName();
		String tbl2 = info.farType.getName();
		
		//try tbl1 tbl2 Assoc
		String assocTblName = TableCreator.createAssocTableName(tbl1, tbl2);
		if (existSvc.doesTableExist(assocTblName)) {
			TableInfo tblinfo = new TableInfo(tbl1, assocTblName);
			tblinfo.tbl1 = tbl1;
			tblinfo.tbl2 = tbl2;
			tblInfoL.add(tblinfo);
			return;
		}
		
		//try other way around
		assocTblName = TableCreator.createAssocTableName(tbl2, tbl1);
		if (existSvc.doesTableExist(assocTblName)) {
			TableInfo tblinfo = new TableInfo(tbl2, assocTblName);
			tblinfo.tbl1 = tbl2;
			tblinfo.tbl2 = tbl1;
			tblInfoL.add(tblinfo);
		}
	}

	private String genAssocInsert(DValue dval, TypePair pair, TableInfo tblinfo, Map<String, DRelation> map, SqlStatement statement) {
		DStructType dtype = (DStructType) dval.getType();
		StrCreator sc = new StrCreator();
		sc.o("INSERT INTO %s (", tblName(tblinfo.assocTblName));

		RelationInfo info = DRuleHelper.findOtherSideOneOrMany(pair.type, dtype);

		sc.o("leftv");
		sc.o(",");
		sc.nl();
		sc.o("rightv");
		sc.o(")");
		sc.nl();

		sc.o("VALUES (");

		//assume normal order. TODO impl reverse order
		TypePair xpair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		DRelation drel = map.get(tblinfo.fieldName); //cust
		if (drel == null) {
			RelationInfo info2 = DRuleHelper.findManyToManyRelation(pair, dtype);
			xpair = DValueHelper.findPrimaryKeyFieldPair(info2.nearType);

			DValue zz = dval.asStruct().getField(info2.fieldName);
			DValue id = zz.asRelation().getForeignKey();

			TypePair main = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
			DValue mainId = dval.asStruct().getField(main.name);
			DRelation drelMain = new DRelation(dval.getType().getName(), mainId);

			genAssocValues(sc, dval, drelMain, info2, xpair, id, statement);
		} else {
			xpair = DValueHelper.findPrimaryKeyFieldPair(info.farType); //Customer
			DValue id = dval.asStruct().getField(xpair.name);
			genAssocValues(sc, dval, drel, info, xpair, id, statement);
		}
		return sc.str;
	}

	private void genAssocValues(StrCreator sc, DValue dval, DRelation drel, RelationInfo info, TypePair xpair, DValue id, SqlStatement statement) {
		if (drel.isMultipleKey()) {
			int index = 0;
			for(DValue keyVal: drel.getMultipleKeys()) {
				
//				String s = valueInSql(id.getType().getShape(), id.getObject());
				statement.paramL.add(id);
				sc.o("?");

				sc.o(",");
				if (keyVal == null) {
					statement.paramL.add(null);
					sc.o("?");
				} else {
					statement.paramL.add(keyVal);
					sc.o("?");
				}
				if (index < drel.getMultipleKeys().size() - 1) {
					sc.o("),(");
				}
				index++;
			}
		} else {
//			String s = valueInSql(id.getType().getShape(), id.getObject());
			statement.paramL.add(id);
			sc.o("?");

			sc.o(",");
			genRelationValue(sc, drel, 0, statement);
		}

		sc.o(");");
		sc.nl();

	}


	private boolean generateInsertField(StrCreator sc, DValue dval, TypePair pair, DStructType dtype, int index, SqlStatement statement) {
		DType innerType = pair.type; //findFieldType(dtype, pair.name);
		DValue inner = dval.asStruct().getField(pair.name);
		if (inner == null) {
			return false;
		}

		if (innerType.isStructShape()) {
			if (! shouldGenerateFKConstraint(pair, dtype)) {
				return false;
			}
			DRelation drel = inner.asRelation();
			genRelationValue(sc, drel, index, statement);
			return true;
		}

		if (index > 0) {
			sc.o(",");
		}
//		String s = valueInSql(innerType.getShape(), inner.getObject());
		statement.paramL.add(inner);
		sc.o("?");
		//			}
		return true;
	}

	private void genRelationValue(StrCreator sc, DRelation drel, int index, SqlStatement statement) {
		//			String keyField = DValueHelper.findUniqueField(innerType);
		DValue keyVal = drel.getForeignKey(); //TODO; handle composite keys later
		if (index > 0) {
			sc.o(",");
		}
		if (keyVal == null) {
			statement.paramL.add(null);
			sc.o("null");
			return;
		} else {
//			String s = valueInSql(keyVal.getType().getShape(), keyVal.getObject());
			statement.paramL.add(keyVal);
			sc.o("?");
		}
	}
	
	
	//=================
	public String generateUpdateBody(StrCreator sc, DValue dval, Map<String, DRelation> map, SqlStatement statement) {
//		UPDATE table_name
//		SET column1 = value1, column2 = value2, ...
//		WHERE condition;		
		
		DStructType dtype = (DStructType) dval.getType();
		int index = 0;
		for(TypePair pair: dtype.getAllFields()) {
			if (pair.type.isStructShape()) {
				if (! shouldGenerateFKConstraint(pair, dtype)) {
					continue;
				}
				if (DRuleHelper.isManyToManyRelation(pair, dtype)) {
					DValue inner = dval.asStruct().getField(pair.name);
					if (inner != null) {
						map.put(pair.name, inner.asRelation());
					}
					continue;
				}
			}

			DValue inner = dval.asStruct().getField(pair.name);
			if (inner == null) {
				continue;
			}

			if (index > 0) {
				sc.o(", ");
			}
			sc.o(pair.name);
			sc.o("=");
			generateInsertField(sc, dval, pair, dtype, 0, statement);
			index++;
		}

		if (index == 0) {
			return ""; //nothing to update
		}
		return sc.str;
	}
	
}