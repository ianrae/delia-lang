package org.delia.db.sql.table;

import java.util.ArrayList;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
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
	protected TableExistenceService existSvc;
	
	public TableCreator(FactoryService factorySvc, DTypeRegistry registry, FieldGenFactory fieldgenFactory, 
				SqlNameFormatter nameFormatter, TableExistenceService existSvc) {
		super(factorySvc);
		this.registry = registry;
		this.fieldgenFactory = fieldgenFactory;
		this.nameFormatter = nameFormatter;
		this.existSvc = existSvc;
	}

	public String generateCreateTable(String typeName, DStructType dtype) {
		if (dtype == null) {
			dtype = (DStructType) registry.getType(typeName);
		}
		
		alreadyCreatedL.add(new TableInfo(typeName, null));
		StrCreator sc = new StrCreator();
		sc.o("CREATE TABLE %s (", typeName);
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
			
			FieldGen field = fieldgenFactory.createFieldGen(registry, pair, dtype, false);
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
			for(TypePair pair: dtype.getAllFields()) {
				if (isManyToManyRelation(pair, dtype)) {
					generateAssocTable(sc, pair, dtype);
				}
			}
		}
		return sc.str;
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
	
	protected void alterGenerateAssocTable(StrCreator sc, TypePair pair, DStructType dtype) {
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		String tbl1 = info.nearType.getName();
		String tbl2 = info.farType.getName();
		
		if (! haveCreatedTable(tbl1)) {
			TableInfo tblinfo = new TableInfo(tbl1, null);
			this.alreadyCreatedL.add(tblinfo);
		}
		if (! haveCreatedTable(tbl2)) {
			TableInfo tblinfo = new TableInfo(tbl2, null);
			this.alreadyCreatedL.add(tblinfo);
		}
		
		generateAssocTable(sc, pair, dtype);
	}
	
	public static String createAssocTableName(String tbl1, String tbl2) {
		String assocTableName = String.format("%s%sAssoc", tbl1, tbl2);
		return assocTableName;
	}
	
	protected void generateAssocTable(StrCreator sc, TypePair xpair, DStructType dtype) {
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, xpair);
		String tbl1 = info.nearType.getName();
		String tbl2 = info.farType.getName();
		if (!(haveCreatedTable(tbl1) && haveCreatedTable(tbl2))) {
			return;
		}

		String assocTableName = createAssocTableName(tbl1, tbl2);
		TableInfo tblinfo = alreadyCreatedL.get(alreadyCreatedL.size() - 1);
		tblinfo.assocTblName = assocTableName;
		tblinfo.tbl1 = tbl1;
		tblinfo.tbl2 = tbl2;
		tblinfo.fieldName = xpair.name;
		
		sc.o("CREATE TABLE %s (", assocTableName);
		sc.nl();
		int index = 0;
		List<SqlElement> fieldL = new ArrayList<>();
		int n = dtype.getAllFields().size();
		for(TypePair pair: dtype.getAllFields()) {
			if (isManyToManyRelation(pair, dtype)) {
				//pair is cust when dtype is Address. so firstcol is addr id called 'cust'
				TypePair copy = new TypePair("leftv", pair.type);
				FieldGen field = fieldgenFactory.createFieldGen(registry, copy, dtype, false);
				RelationInfo otherSide = DRuleHelper.findOtherSideMany(info.farType, dtype);
				field.setIsAssocTblField(info.farType.fieldIsOptional(otherSide.fieldName));
				fieldL.add(field);

				TypePair xx = DValueHelper.findPrimaryKeyFieldPair(info.farType);
				copy = new TypePair("rightv", xx.type);
				field = fieldgenFactory.createFieldGen(registry, copy, info.farType, false);
				field.setIsAssocTblField(dtype.fieldIsOptional(pair.name));
				fieldL.add(field);

				index++;
			}
		}
		
		for(TypePair pair: dtype.getAllFields()) {
			if (isManyToManyRelation(pair, dtype)) {
				TypePair copy = new TypePair("leftv", info.nearType); //address
				ConstraintGen constraint = this.fieldgenFactory.generateFKConstraint(registry, copy, info.nearType, false);
				if (constraint != null) {
					fieldL.add(constraint);
				}
				
				copy = new TypePair("rightv", info.farType);
				constraint = this.fieldgenFactory.generateFKConstraint(registry, copy, info.farType, false);
				if (constraint != null) {
					fieldL.add(constraint);
				}
				index++;
			}
		}
		
		index = 0;
		for(SqlElement field: fieldL) {
			field.generateField(sc);
			if (index + 1 < fieldL.size()) {
				sc.o(",");
				sc.nl();
			}
			index++;
		}
		
		sc.o(");");
		sc.nl();
		
	}

	protected boolean haveCreatedTable(String tbl1) {
		for(TableInfo info: alreadyCreatedL) {
			if (info.tblName.equals(tbl1)) {
				return true;
			}
		}
		return false;
	}

	public String generateCreateField(String typeName, DStructType dtype, String fieldName) {
		if (dtype == null) {
			dtype = (DStructType) registry.getType(typeName);
		}
		
		StrCreator sc = new StrCreator();
		sc.o("ALTER TABLE %s ADD COLUMN ", typeName);
		sc.nl();
		List<SqlElement> fieldL = new ArrayList<>();
		int manyToManyFieldCount = 0;
		
		TypePair pair = DValueHelper.findField(dtype, fieldName);
		if (isManyToManyRelation(pair, dtype)) {
			manyToManyFieldCount++;
		} else {
			FieldGen field = fieldgenFactory.createFieldGen(registry, pair, dtype, true);
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
			sc.o("ALTER TABLE %s ADD  ", typeName);
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
				alterGenerateAssocTable(sc, pair, dtype);
			}
		}
		return sc.str;
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
		return sc.str;
	}
	
	public String tblName(String tableName) {
		return nameFormatter.convert(tableName);
	}

	public String generateAlterFieldType(String tableName, String fieldName, String newFieldType) {
		StrCreator sc = new StrCreator();
		doAlterColumnPrefix(sc, tableName, fieldName);

		DStructType dtype = (DStructType) registry.getType(tableName);
		TypePair pair = DValueHelper.findField(dtype, fieldName);
		
		FieldGen fieldGen = fieldgenFactory.createFieldGen(registry, pair, dtype, true);
		String sqlType = fieldGen.deliaToSql(pair);
		
		sc.o(" SET DATA TYPE %s", sqlType); 
		return sc.str;
	}
	public String generateAlterField(String tableName, String fieldName, String deltaFlags, String constraintName) {
		StrCreator sc = new StrCreator();
		String[] ar = deltaFlags.split(",");
		//  deltaFlags: +O,+U,+P,+S
		
		AssocInfo ainfo = createAssocInfoIfIsManyToMany(tableName, fieldName);
		
		for(String delta: ar) {
			switch(delta) {
			case "+O":
				doAlterColumnOptional(sc, tableName, fieldName, true, ainfo);
				break;
			case "-O":
				doAlterColumnOptional(sc, tableName, fieldName, false, ainfo);
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
			
		return sc.str;
	}

	private AssocInfo createAssocInfoIfIsManyToMany(String tableName, String fieldName) {
		DStructType structType = (DStructType) registry.getType(tableName);
		TypePair pair = DValueHelper.findField(structType, fieldName);
		boolean isAssoc = this.isManyToManyRelation(pair, structType);
		if (isAssoc) {
			AssocInfo ainfo = new AssocInfo();
			ainfo.structType = structType;
			ainfo.pair = pair;
			return ainfo;
		}
		return null;
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

	protected void doAlterColumnOptional(StrCreator sc, String tableName, String fieldName, boolean b, AssocInfo ainfo) {
		if (ainfo != null) {
			List<TableInfo> tblInfoL = new ArrayList<>();
			RelationInfo relinfo = DRuleHelper.findManyToManyRelation(ainfo.pair, ainfo.structType);
			existSvc.fillTableInfoIfNeeded(tblInfoL, relinfo);
			TableInfo tblinfo = tblInfoL.get(0);
			if (tblinfo.tbl2.equals(tableName)) {
				doAlterColumnPrefix(sc, tblinfo.assocTblName, "leftv");
			} else {
				doAlterColumnPrefix(sc, tblinfo.assocTblName, "rightv");
			}
		} else {
			doAlterColumnPrefix(sc, tableName, fieldName);
		}
		
		
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
	
}