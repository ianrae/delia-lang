package org.delia.zdb;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.table.AssocInfo;
import org.delia.db.sql.table.AssocTableCreator;
import org.delia.db.sql.table.ConstraintGen;
import org.delia.db.sql.table.FieldGen;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.ListWalker;
import org.delia.db.sql.table.SqlElement;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class TableCreator extends ServiceBase {
	protected DTypeRegistry registry;
	public List<TableInfo> alreadyCreatedL = new ArrayList<>();
	protected FieldGenFactory fieldgenFactory;
	protected SqlNameFormatter nameFormatter;
	private AssocTableCreator assocTblCreator;
	public DatIdMap datIdMap;
	
	public TableCreator(FactoryService factorySvc, DTypeRegistry registry, FieldGenFactory fieldgenFactory, 
				SqlNameFormatter nameFormatter, DatIdMap datIdMap, DBExecutor zexec) {
		super(factorySvc);
		this.registry = registry;
		this.fieldgenFactory = fieldgenFactory;
		this.nameFormatter = nameFormatter;
		this.datIdMap = datIdMap;
		
		TableExistenceService existSvc = new ZTableExistenceService();
		this.assocTblCreator = createAssocTblCreator(existSvc); 
	}

	protected AssocTableCreator createAssocTblCreator(TableExistenceService existSvc) {
		return new AssocTableCreator(factorySvc, registry, fieldgenFactory, nameFormatter, existSvc, alreadyCreatedL, datIdMap);
	}

	public String generateCreateTable(String typeName, DStructType dtype) {
		if (dtype == null) {
			dtype = (DStructType) registry.getType(typeName);
		}
		
		alreadyCreatedL.add(new TableInfo(typeName, null));
		StrCreator sc = new StrCreator();
		sc.o("CREATE TABLE %s (", tblName(typeName));
		sc.nl();
		int index = 0;
		List<SqlElement> fieldL = new ArrayList<>();
		int manyToManyFieldCount = 0;
		for(TypePair pair: dtype.getAllFields()) {
			if (isManyToManyRelation(pair, dtype)) {
				manyToManyFieldCount++;
				continue;
			}
			
			//key goes in child only
			if (DRuleHelper.isParentRelation(dtype, pair)) {
				continue;
			}
			
			int sizeof = calcSizeof(dtype, pair);
			FieldGen field = fieldgenFactory.createFieldGen(registry, pair, dtype, false, sizeof);
			fieldL.add(field);
			index++;
		}
		
		//add constraints
		List<ConstraintGen> constraints = new ArrayList<>();
		for(TypePair pair: dtype.getAllFields()) {
			if (pair.type.isStructShape() && !isManyToManyRelation(pair, dtype)) {
				ConstraintGen constraint = generateFKConstraint(sc, pair, dtype, false);
				if (constraint != null) {
					fieldL.add(constraint);
					constraints.add(constraint);
				}
			}
		}
		
		haveFieldsVisitTheirConstrainsts(fieldL, constraints);
		
		index = 0;
		for(SqlElement field: fieldL) {
			field.generateField(sc);
			if (index + 1 < fieldL.size()) {
				sc.o(",");
				sc.nl();
			}
			index++;
		}
		
		sc.nl();
		sc.o(");");
		sc.nl();
		if (manyToManyFieldCount > 0) {
			sc.nl();
			List<String> createdTables = new ArrayList<>();
			for(TypePair pair: dtype.getAllFields()) {
				if (isManyToManyRelation(pair, dtype)) {
					
					StrCreator tmpSc = new StrCreator();
					String tblName = generateAssocTable(tmpSc, pair, dtype);
					if (tblName != null) {
						if (createdTables.contains(tblName)) {
							//skip self-join
						} else {
							sc.o(tmpSc.toString());
						}
						createdTables.add(tblName);
					}
				}
			}
		}
		return sc.toString();
	}
	

	private int calcSizeof(DStructType dtype, TypePair pair) {
		return DRuleHelper.getSizeofField(dtype, pair.name);
	}

	protected void haveFieldsVisitTheirConstrainsts(List<SqlElement> fieldL, List<ConstraintGen> constraints) {
		//have field see all its contraints
		for(SqlElement el: fieldL) {
			if (el instanceof FieldGen) {
				FieldGen field = (FieldGen) el;
				field.visitConstraints(constraints); //wire them up
			}
		}
	}

	protected boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && !info.isParent) {
			return true;
		}
		return false;
	}
	protected boolean isManyToManyRelation(TypePair pair, DStructType dtype) {
		return DRuleHelper.isManyToManyRelation(pair, dtype);
	}

	protected ConstraintGen generateFKConstraint(StrCreator sc, TypePair pair, DStructType dtype, boolean isAlter) {
		//key goes in child only
		if (!shouldGenerateFKConstraint(pair, dtype)) {
			return null;
		}
		return fieldgenFactory.generateFKConstraint(registry, pair, dtype, isAlter);
	}
	
	protected void alterGenerateAssocTable(StrCreator sc, TypePair pair, DStructType dtype, boolean canCreateAssocTable) {
		assocTblCreator.alterGenerateAssocTable(sc, pair, dtype, canCreateAssocTable);
	}
	
	protected String generateAssocTable(StrCreator sc, TypePair xpair, DStructType dtype) {
		return assocTblCreator.generateAssocTable(sc, xpair, dtype);
	}

	public String generateCreateField(String typeName, DStructType dtype, String fieldName, int sizeof, boolean canCreateAssocTable) {
		if (dtype == null) {
			dtype = (DStructType) registry.getType(typeName);
		}
		
		StrCreator sc = new StrCreator();
		sc.o("ALTER TABLE %s ADD COLUMN ", tblName(typeName));
		sc.nl();
		List<SqlElement> fieldL = new ArrayList<>();
		int manyToManyFieldCount = 0;
		
		TypePair pair = DValueHelper.findField(dtype, fieldName);
		if (isManyToManyRelation(pair, dtype)) {
			manyToManyFieldCount++;
		} else {
			FieldGen field = fieldgenFactory.createFieldGen(registry, pair, dtype, true, sizeof);
			fieldL.add(field);
		}
		
		//add constraints
		if (pair.type.isStructShape() && !isManyToManyRelation(pair, dtype)) {
			ConstraintGen constraint = generateFKConstraint(sc, pair, dtype, true);
			if (constraint != null) {
				fieldL.add(constraint);
			}
		}
		
		List<ConstraintGen> constraints = getConstraintsOnly(fieldL);
		haveFieldsVisitTheirConstrainsts(fieldL, constraints);
		
		
		ListWalker<FieldGen> walker1 = new ListWalker<>(getFieldsOnly(fieldL));
		while(walker1.hasNext()) {
			FieldGen ff = walker1.next();
			ff.generateField(sc);
			walker1.addIfNotLast(sc, ",", nl());
		}
		
		sc.o(";");
		sc.nl();
		ListWalker<ConstraintGen> walker = new ListWalker<>(constraints);
		while(walker.hasNext()) {
			ConstraintGen con = walker.next();
			sc.o("ALTER TABLE %s ADD  ", tblName(typeName));
			con.generateField(sc);
			walker.addIfNotLast(sc, ",", nl());
		}
		
		sc.nl();
		if (manyToManyFieldCount > 0) {
			if (fieldL.isEmpty()) {
				sc = new StrCreator(); //reset
			} else {
				sc.nl();
			}
			if (isManyToManyRelation(pair, dtype)) {
				alterGenerateAssocTable(sc, pair, dtype, canCreateAssocTable);
			}
		}
		return sc.toString();
	}

	private String nl() {
		return "\n";
	}

	private List<FieldGen> getFieldsOnly(List<SqlElement> fieldL) {
		List<FieldGen> list = new ArrayList<>();
		for(SqlElement el: fieldL) {
			if (el instanceof FieldGen) {
				list.add((FieldGen) el);
			}
		}
		return list;
	}
	private List<ConstraintGen> getConstraintsOnly(List<SqlElement> fieldL) {
		List<ConstraintGen> list = new ArrayList<>();
		for(SqlElement el: fieldL) {
			if (el instanceof ConstraintGen) {
				list.add((ConstraintGen) el);
			}
		}
		return list;
	}

	public String generateRenameField(String tableName, String fieldName, String newName) {
		StrCreator sc = new StrCreator();
		sc.o("ALTER TABLE %s ALTER COLUMN %s", tblName(tableName), fieldName);
		sc.o(" RENAME TO %s", newName); 
		return sc.toString();
	}
	
	public String tblName(String tableName) {
		return nameFormatter.convert(tableName);
	}

	public String generateAlterFieldType(String tableName, String fieldName, String newFieldType, int sizeof) {
		StrCreator sc = new StrCreator();
		doAlterColumnPrefix(sc, tableName, fieldName);

		DStructType dtype = (DStructType) registry.getType(tableName);
		TypePair pair = DValueHelper.findField(dtype, fieldName);
		
		FieldGen fieldGen = fieldgenFactory.createFieldGen(registry, pair, dtype, true, sizeof);
		String sqlType = fieldGen.deliaToSql(pair);
		
		sc.o(" SET DATA TYPE %s", sqlType); 
		return sc.toString();
	}
	public String generateAlterField(String tableName, String fieldName, String deltaFlags, String constraintName) {
		StrCreator sc = new StrCreator();
		String[] ar = deltaFlags.split(",");
		//  deltaFlags: +O,+U,+P,+S
		
		AssocInfo ainfo = assocTblCreator.createAssocInfoIfIsManyToMany(tableName, fieldName);
		if (ainfo != null) {
			return assocTblCreator.generateAlterField(tableName, fieldName, deltaFlags, constraintName, ainfo);
		}
		
		for(String delta: ar) {
			switch(delta) {
			case "+O":
				doAlterColumnOptional(sc, tableName, fieldName, true);
				break;
			case "-O":
				doAlterColumnOptional(sc, tableName, fieldName, false);
				break;
			case "+U":
				doAlterColumnUnique(sc, tableName, fieldName, true, constraintName);
				break;
			case "-U":
				doAlterColumnUnique(sc, tableName, fieldName, false, constraintName);
				break;
			default:
			{
				//most databases don't support changing serial or primaryKey
				String msg = String.format("Field '%s.%s' - field change '%s' not supported", tableName, fieldName, delta);
				DeliaExceptionHelper.throwError("unsupported-alter-field-change", msg);
			}
				break;
			}
		}
			
		return sc.toString();
	}

	protected void doAlterColumnUnique(StrCreator sc, String tableName, String fieldName, boolean b, String constraintName) {
		doAlterTablePrefix(sc, tableName);
		if (b) {
			//public.customer_height_key
			//ALTER TABLE TEST ADD CONSTRAINT NAME_UNIQUE UNIQUE(NAME)
			sc.o(" ADD CONSTRAINT %S UNIQUE(%s)", constraintName, fieldName);  
		} else {
			sc.o(" DROP CONSTRAINT %S", constraintName);  
		}
		sc.o(";\n");
	}

	protected void doAlterColumnOptional(StrCreator sc, String tableName, String fieldName, boolean b) {
		doAlterColumnPrefix(sc, tableName, fieldName);
		
		
		if (b) {
			sc.o(" DROP NOT NULL"); //set null 
		} else {
			sc.o(" SET NOT NULL"); 
		}
		sc.o(";\n");
	}

	protected void doAlterColumnPrefix(StrCreator sc, String tableName, String fieldName) {
		sc.o("ALTER TABLE %s ALTER COLUMN %s", tblName(tableName), fieldName);
	}
	protected void doAlterTablePrefix(StrCreator sc, String tableName) {
		sc.o("ALTER TABLE %s ", tblName(tableName));
	}
	
	
	public String generateDeleteField(String typeName, DStructType dtype, String fieldName, int datId) {
		if (dtype == null) {
			dtype = (DStructType) registry.getType(typeName);
		}
		
		boolean isManyToMany = datId != 0;
		StrCreator sc = new StrCreator();
		if (! isManyToMany) {
			String sql = String.format("ALTER TABLE %s DROP COLUMN %s", tblName(typeName), fieldName);
			sc.o(sql);
			sc.nl();
		}
		List<SqlElement> fieldL = new ArrayList<>();
		
//		TypePair pair = DValueHelper.findField(dtype, fieldName);
//		if (isManyToManyRelation(pair, dtype)) {
//		} else {
//			FieldGen field = fieldgenFactory.createFieldGen(registry, pair, dtype, true);
//			fieldL.add(field);
//		}
//		
//		//delete constraints
//		if (pair.type.isStructShape() && !isManyToManyRelation(pair, dtype)) {
//			ConstraintGen constraint = generateFKConstraint(sc, pair, dtype, true);
//			if (constraint != null) {
//				fieldL.add(constraint);
//			}
//		}
//		
//		List<ConstraintGen> constraints = getConstraintsOnly(fieldL);
//		haveFieldsVisitTheirConstrainsts(fieldL, constraints);
		
		
//		ListWalker<FieldGen> walker1 = new ListWalker<>(getFieldsOnly(fieldL));
//		while(walker1.hasNext()) {
//			FieldGen ff = walker1.next();
//			ff.generateField(sc);
//			walker1.addIfNotLast(sc, ",", nl());
//		}
		
		sc.o(";");
//		sc.nl();
//		ListWalker<ConstraintGen> walker = new ListWalker<>(constraints);
//		while(walker.hasNext()) {
//			ConstraintGen con = walker.next();
//			sc.o("ALTER TABLE %s DROP  ", typeName);
//			con.generateField(sc);
//			walker.addIfNotLast(sc, ",", nl());
//		}
//		sc.o(";");
		
		sc.nl();
		if (isManyToMany) {
			String tblName = datIdMap.getAssocTblName(datId);
			sc.o("DROP TABLE IF EXISTS %s;", tblName(tblName));
		}
		return sc.toString();
	}
	
}