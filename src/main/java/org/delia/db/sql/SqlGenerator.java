//package org.delia.db.sql;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.delia.compiler.ast.FilterExp;
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.db.QuerySpec;
//import org.delia.db.sql.table.TableInfo;
//import org.delia.relation.RelationCardinality;
//import org.delia.relation.RelationInfo;
//import org.delia.type.DRelation;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.DValue;
//import org.delia.type.Shape;
//import org.delia.type.TypePair;
//import org.delia.util.DRuleHelper;
//import org.delia.util.DValueHelper;
//import org.delia.util.DeliaExceptionHelper;
//
//public class SqlGenerator extends ServiceBase {
//	private DTypeRegistry registry;
//	private SqlQueryGenerator sqlGenerator;
//	private SqlDateGenerator dateGenerator;
//	private SqlNameFormatter nameFormatter;
//
//	public SqlGenerator(FactoryService factorySvc, DTypeRegistry registry, SqlNameFormatter nameFormatter) {
//		super(factorySvc);
//		this.registry = registry;
//		this.sqlGenerator = new SqlQueryGenerator(factorySvc, registry);
//		this.dateGenerator = new SqlDateGenerator(factorySvc, registry);
//		this.nameFormatter = nameFormatter;
//	}
//
//	private String tblName(String typeName) {
//		return nameFormatter.convert(typeName);
//	}
//	private String tblName(DType dtype) {
//		return nameFormatter.convert(dtype);
//	}
//	
//	public String generateCreateTable(String typeName, DStructType dtype) {
//		if (dtype == null) {
//			dtype = (DStructType) registry.getType(typeName);
//		}
//
//		StrCreator sc = new StrCreator();
//		sc.o("CREATE TABLE %s (", tblName(typeName));
//		sc.nl();
//		int index = 0;
//		int n = dtype.getAllFields().size();
//		boolean lastWasSkipped = true;
//		int manyToManyFieldCount = 0;
//		for(TypePair pair: dtype.getAllFields()) {
//			if (isManyToManyRelation(pair, dtype)) {
//				lastWasSkipped = true;
//				manyToManyFieldCount++;
//				continue;
//			}
//			if (!lastWasSkipped) {
//				sc.o(",");
//				sc.nl();
//			}
//
//			generateField(sc, pair, dtype, index);
//			lastWasSkipped = false;
//			index++;
//		}
//
//		if (index >= n) {
//			sc.nl();
//		}
//
//		//add constraints
//		for(TypePair pair: dtype.getAllFields()) {
//			if (pair.type.isStructShape() && !isManyToManyRelation(pair, dtype)) {
//				generateFKConstraint(sc, pair, dtype);
//			}
//		}
//
//		sc.o(");");
//		sc.nl();
//
//		if (manyToManyFieldCount > 0) {
//			sc.nl();
//			for(TypePair pair: dtype.getAllFields()) {
//				if (pair.type.isStructShape() && isManyToManyRelation(pair, dtype)) {
//					generateAssocTable(sc, pair, dtype);
//				}
//			}
//		}
//		return sc.str;
//	}
//
//	private boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
//		//key goes in child only
//		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
//		if (info != null && !info.isParent) {
//			return true;
//		}
//		return false;
//	}
//	private boolean isManyToManyRelation(TypePair pair, DStructType dtype) {
//		//key goes in child only
//		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
//		if (info != null && info.cardinality.equals(RelationCardinality.MANY_TO_MANY)) {
//			return true;
//		}
//		return false;
//	}
//
//	private void generateFKConstraint(StrCreator sc, TypePair pair, DStructType dtype) {
//		//key goes in child only
//		if (!shouldGenerateFKConstraint(pair, dtype)) {
//			return;
//		}
//		doGenerateFKConstraint(sc, pair, dtype);
//	}
//	private void doGenerateFKConstraint(StrCreator sc, TypePair pair, DStructType dtype) {
//		//FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)
//		String fieldName = pair.name;
//		sc.o(",");
//
//		DStructType targetType = (DStructType) pair.type;
//		TypePair keyField =  DValueHelper.findPrimaryKeyFieldPair(targetType);
//		String s = String.format("FOREIGN KEY (%s) REFERENCES %s(%s)", fieldName, tblName(targetType), keyField.name);
//		sc.o(s);
//		sc.nl();
//	}
//	private void generateAssocTable(StrCreator sc, TypePair xpair, DStructType dtype) {
//		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, xpair);
//		String assocTableName = String.format("%s%sAssoc", info.nearType.getName(), info.farType.getName());
//		assocTableName = tblName(assocTableName);
//		sc.o("CREATE TABLE %s (", assocTableName);
//		sc.nl();
//		int index = 0;
//		int n = dtype.getAllFields().size();
//		for(TypePair pair: dtype.getAllFields()) {
//			if (isManyToManyRelation(pair, dtype)) {
//				generateField(sc, pair, dtype, index);
//				sc.o(",");
//				sc.nl();
//				TypePair xx = DValueHelper.findPrimaryKeyFieldPair(info.farType);
//				TypePair copy = new TypePair("far", xx.type);
//				generateField(sc, copy, info.farType, index);
//				//TODO: should probably be optional (NULL). todo fix!1
//
//				if (index > 0) {
//					sc.o(",");
//					sc.nl();
//				}
//				index++;
//			}
//		}
//
//		sc.nl();
//		for(TypePair pair: dtype.getAllFields()) {
//			if (isManyToManyRelation(pair, dtype)) {
//				doGenerateFKConstraint(sc, pair, dtype);
//				sc.nl();
//
//				//					TypePair xx = DValueHelper.findPrimaryKeyFieldPair(info.farType);
//				TypePair copy = new TypePair("far", info.farType);
//				doGenerateFKConstraint(sc, copy, info.farType);
//				index++;
//			}
//		}
//
//		sc.o(");");
//		sc.nl();
//
//	}
//
//
//	private void generateField(StrCreator sc, TypePair pair, DStructType dtype, int index) {
//		String name = pair.name;
//		String type = deliaToSql(pair);
//		//	Department		Char(35)		NOT NULL,
//		String suffix1 = dtype.fieldIsUnique(name) || dtype.fieldIsPrimaryKey(name) ? " UNIQUE" : "";
//		String suffix2 = dtype.fieldIsOptional(name) ? " NULL" : "";
//		sc.o("  %s %s%s%s", name, type, suffix1, suffix2);
//	}
//
//	private String deliaToSql(TypePair pair) {
//		switch(pair.type.getShape()) {
//		case INTEGER:
//			return "Int";
//		case LONG:
//			return "BIGINT";
//		case NUMBER:
//			return "DOUBLE";
//		case DATE:
//			return "TIMESTAMP";
//		case STRING:
//			return "VARCHAR(255)";
//		case BOOLEAN:
//			return "BOOLEAN";
//		case STRUCT:
//		{
//			TypePair innerPair = DValueHelper.findPrimaryKeyFieldPair(pair.type); //TODO: support multiple keys later
//			return deliaToSql(innerPair);
//		}
//		default:
//			return null;
//		}
//	}
//
//
//	public String generateQuery(QuerySpec spec) {
//		return sqlGenerator.generateQuery(spec);
//	}
//
//	private void addWhereClauseIfNeeded(StrCreator sc, FilterExp filter, String typeName) {
//		if (filter != null) {
//			DStructType type = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
//			String keyField = DValueHelper.findUniqueField(type);
//			if (keyField == null) {
//				//err!!
//				return;
//			}
//
//			DType inner = findFieldType(type, keyField);
//			String val = valueInSql(inner.getShape(), filter.cond.strValue());
//
//			sc.o(" WHERE %s=%s", keyField, val);
//			//				sc.o(" WHERE %s", val);
//		}
//	}
//
//	public String generateInsert(DValue dval, List<TableInfo> tblInfoL) {
//		Map<String,DRelation> map = new HashMap<>(); //local var. ok to not use ConcurrentHashMap here
//		String sql = doGenerateInsert(dval, map);
//		sql += doGenerateAssocInsertIfNeeded(dval, tblInfoL, map);
//		return sql;
//	}		
//	public String doGenerateInsert(DValue dval, Map<String, DRelation> map) {
//		//			INSERT INTO Customers (CustomerName, City, Country)
//		//			VALUES ('Cardinal', 'Stavanger', 'Norway');
//		DStructType dtype = (DStructType) dval.getType();
//		StrCreator sc = new StrCreator();
//		sc.o("INSERT INTO %s (", tblName(dtype));
//		int index = 0;
//		for(TypePair pair: dtype.getAllFields()) {
//			if (pair.type.isStructShape()) {
//				if (! shouldGenerateFKConstraint(pair, dtype)) {
//					continue;
//				}
//				if (DRuleHelper.isManyToManyRelation(pair, dtype)) {
//					DValue inner = dval.asStruct().getField(pair.name);
//					if (inner != null) {
//						map.put(pair.name, inner.asRelation());
//					}
//					continue;
//				}
//			}
//
//			DValue inner = dval.asStruct().getField(pair.name);
//			if (inner == null) {
//				continue;
//			} else if (dtype.fieldIsSerial(pair.name)) {
//				DeliaExceptionHelper.throwError("serial-value-cannot-be-provided", "Type %s, field %s - do not specify a value for a serial field", dtype.getName(), pair.name);
//			}
//
//			if (index > 0) {
//				sc.o(", ");
//			}
//			sc.o(pair.name);
//			index++;
//		}
//		sc.o(")");
//		sc.nl();
//
//		//			VALUES ('Cardinal', 'Stavanger', 'Norway');
//		sc.o("VALUES (");
//		index = 0;
//		for(TypePair pair: dtype.getAllFields()) {
//			if (DRuleHelper.isManyToManyRelation(pair, dtype)) {
//				continue;
//			}
//			if (generateInsertField(sc, dval, pair, dtype, index)) {
//				index++;
//			}
//		}
//		sc.o(");");
//		sc.nl();
//		return sc.str;
//	}
//	private String doGenerateAssocInsertIfNeeded(DValue dval, List<TableInfo> tblInfoL, Map<String, DRelation> map) {
//		String sql = "";
//		if (map.isEmpty()) {
//			return sql;
//		}
//
//		DStructType dtype = (DStructType) dval.getType();
//		for(TypePair pair: dtype.getAllFields()) {
//			RelationInfo info = DRuleHelper.findManyToManyRelation(pair, dtype);
//			if (info != null) {
//				TableInfo tblinfo = findTableInfo(tblInfoL, pair, info);
//				sql += genAssocInsert(dval, pair, tblinfo, map);
//			}
//		}
//		return sql;
//	}
//	private String genAssocInsert(DValue dval, TypePair pair, TableInfo tblinfo, Map<String, DRelation> map) {
//		DStructType dtype = (DStructType) dval.getType();
//		StrCreator sc = new StrCreator();
//		sc.o("INSERT INTO %s (", tblName(tblinfo.assocTblName));
//
//		RelationInfo info = DRuleHelper.findOtherSideOneOrMany(pair.type, dtype);
//
//		sc.o("leftv");
//		sc.o(",");
//		sc.nl();
//		sc.o("rightv");
//		sc.o(")");
//		sc.nl();
//
//		sc.o("VALUES (");
//
//		//assume normal order. TODO impl reverse order
//		TypePair xpair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
//		DRelation drel = map.get(tblinfo.fieldName); //cust
//		if (drel == null) {
//			RelationInfo info2 = DRuleHelper.findManyToManyRelation(pair, dtype);
//			xpair = DValueHelper.findPrimaryKeyFieldPair(info2.nearType);
//
//			DValue zz = dval.asStruct().getField(info2.fieldName);
//			DValue id = zz.asRelation().getForeignKey();
//
//			TypePair main = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
//			DValue mainId = dval.asStruct().getField(main.name);
//			DRelation drelMain = new DRelation(dval.getType().getName(), mainId);
//
//			genAssocValues(sc, dval, drelMain, info2, xpair, id);
//		} else {
//			xpair = DValueHelper.findPrimaryKeyFieldPair(info.farType); //Customer
//			DValue id = dval.asStruct().getField(xpair.name);
//			genAssocValues(sc, dval, drel, info, xpair, id);
//		}
//		return sc.str;
//	}
//
//	private void genAssocValues(StrCreator sc, DValue dval, DRelation drel, RelationInfo info, TypePair xpair, DValue id) {
//		if (drel.isMultipleKey()) {
//			int index = 0;
//			for(DValue keyVal: drel.getMultipleKeys()) {
//				//					if (index > 0) {
//				//						sc.o(",");
//				//					}
//				String s = valueInSql(id.getType().getShape(), id.getObject());
//				sc.o(s);
//
//				sc.o(",");
//				if (keyVal == null) {
//					sc.o("null");
//				} else {
//					s = valueInSql(keyVal.getType().getShape(), keyVal.getObject());
//					sc.o(s);
//				}
//				if (index < drel.getMultipleKeys().size() - 1) {
//					sc.o("),(");
//				}
//				index++;
//			}
//		} else {
//			String s = valueInSql(id.getType().getShape(), id.getObject());
//			sc.o(s);
//
//			sc.o(",");
//			genRelationValue(sc, drel, 0);
//		}
//
//		sc.o(");");
//		sc.nl();
//
//	}
//
//	private TableInfo findTableInfo(List<TableInfo> tblInfoL, TypePair pair, RelationInfo info) {
//		for(TableInfo tblinfo: tblInfoL) {
//			if (tblinfo.tbl1 != null && tblinfo.tbl2 != null) {
//				if (tblinfo.tbl1.equals(info.nearType.getName())) {
//					if (tblinfo.tbl2.equals(info.farType.getName())) {
//						return tblinfo;
//					}
//				}
//
//				if (tblinfo.tbl2.equals(info.nearType.getName())) {
//					if (tblinfo.tbl1.equals(info.farType.getName())) {
//						return tblinfo;
//					}
//				}
//			}
//		}
//		return null;
//	}
//
//	private boolean generateInsertField(StrCreator sc, DValue dval, TypePair pair, DStructType dtype, int index) {
//		DType innerType = pair.type; //findFieldType(dtype, pair.name);
//		DValue inner = dval.asStruct().getField(pair.name);
//		if (inner == null) {
//			return false;
//		}
//
//		if (innerType.isStructShape()) {
//			if (! shouldGenerateFKConstraint(pair, dtype)) {
//				return false;
//			}
//			DRelation drel = inner.asRelation();
//			genRelationValue(sc, drel, index);
//			return true;
//		}
//
//		if (index > 0) {
//			sc.o(",");
//		}
//		//			if (inner == null) {
//		//				sc.o("null");
//		//				return;
//		//			} else {
//		String s = valueInSql(innerType.getShape(), inner.getObject());
//		sc.o(s);
//		//			}
//		return true;
//	}
//
//	private void genRelationValue(StrCreator sc, DRelation drel, int index) {
//		//			String keyField = DValueHelper.findUniqueField(innerType);
//		DValue keyVal = drel.getForeignKey(); //TODO; handle composite keys later
//		if (index > 0) {
//			sc.o(",");
//		}
//		if (keyVal == null) {
//			sc.o("null");
//			return;
//		} else {
//			String s = valueInSql(keyVal.getType().getShape(), keyVal.getObject());
//			sc.o(s);
//		}
//	}
//
//	public String valueInSql(Shape shape, Object value) {
//		switch(shape) {
//		case INTEGER:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%d", (Integer)value);
//		case LONG:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%d", (Long)value);
//		case NUMBER:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%g", (Double)value);
//		case BOOLEAN:
//			if (value instanceof String) {
//				return (String)value;
//			}
//			return String.format("%b", (Boolean)value);
//		case STRING:
//			return String.format("'%s'", value.toString());
//		case DATE:
//			return this.dateGenerator.dateValueInSql(value);
//		default:
//			return "";
//		}
//	}
//
//	public DType findFieldType(DStructType dtype, String fieldName) {
//		for(TypePair pair: dtype.getAllFields()) {
//			if (pair.name.equals(fieldName)) {
//				return pair.type;
//			}
//		}
//		return null;
//	}
//
//	public String generateDelete(QuerySpec spec) {
//		StrCreator sc = new StrCreator();
//		sc.o("DELETE FROM %s", tblName(spec.queryExp.getTypeName()));
//		addWhereClauseIfNeeded(sc, spec.queryExp.filter, spec.queryExp.getTypeName());
//		sc.o(";");
//		return sc.str;
//	}
//	
//	public String generateUpdate(DValue dval, List<TableInfo> tblInfoL, QuerySpec spec) {
//		Map<String,DRelation> map = new HashMap<>(); //ok to not use ConcurrentHashMap here
//		String sql = doGenerateUpdate(dval, map);
//		if (sql.isEmpty()) {
//			return sql;
//		}
////		sql += doGenerateAssocInsertIfNeeded(dval, tblInfoL, map);
//		
//		String query = generateQuery(spec);
//		int pos = query.indexOf("WHERE ");
//		if (pos > 0) {
//			query = query.substring(pos);
//			return String.format("%s %s", sql, query);
//		} else {
//			return sql;
//		}
//	}		
//	private String doGenerateUpdate(DValue dval, Map<String, DRelation> map) {
////		UPDATE table_name
////		SET column1 = value1, column2 = value2, ...
////		WHERE condition;		
//		
//		DStructType dtype = (DStructType) dval.getType();
//		StrCreator sc = new StrCreator();
//		sc.o("UPDATE %s SET ", tblName(dtype));
//		int index = 0;
//		for(TypePair pair: dtype.getAllFields()) {
//			if (pair.type.isStructShape()) {
//				if (! shouldGenerateFKConstraint(pair, dtype)) {
//					continue;
//				}
//				if (DRuleHelper.isManyToManyRelation(pair, dtype)) {
//					DValue inner = dval.asStruct().getField(pair.name);
//					if (inner != null) {
//						map.put(pair.name, inner.asRelation());
//					}
//					continue;
//				}
//			}
//
//			DValue inner = dval.asStruct().getField(pair.name);
//			if (inner == null) {
//				continue;
//			}
//
//			if (index > 0) {
//				sc.o(", ");
//			}
//			sc.o(pair.name);
//			sc.o("=");
//			generateInsertField(sc, dval, pair, dtype, 0);
//			index++;
//		}
//		sc.nl();
//
//		if (index == 0) {
//			return ""; //nothing to update
//		}
//		return sc.str;
//	}
//
//	public String generateTableDetect(String tableName) {
//		StrCreator sc = new StrCreator();
//		sc.o("SELECT EXISTS ( ");
//		sc.o(" SELECT FROM information_schema.tables"); 
//		boolean b = false;
//		if (b) {
//			sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
//			sc.o(" AND    table_name   = '%s' )", tblName(tableName));
//		} else {
////			sc.o(" WHERE  table_schema = '%s'", "PUBLIC");
////			sc.o(" WHERE    table_name   = '%s' )", tableName.toLowerCase());
//			sc.o(" WHERE    table_name   = '%s' )", tblName(tableName));
//		}
//		return sc.str;
//	}
//	
//}