package org.delia.db.sql.table;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
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

public class AssocTableCreator extends ServiceBase {
	private DTypeRegistry registry;
	public List<TableInfo> alreadyCreatedL;
	private FieldGenFactory fieldgenFactory;
	private SqlNameFormatter nameFormatter;
	private TableExistenceService existSvc;
	private DatIdMap datIdMap;
	
	public AssocTableCreator(FactoryService factorySvc, DTypeRegistry registry, FieldGenFactory fieldgenFactory, 
				SqlNameFormatter nameFormatter, TableExistenceService existSvc, List<TableInfo> alreadyCreatedL, DatIdMap datIdMap) {
		super(factorySvc);
		this.registry = registry;
		this.fieldgenFactory = fieldgenFactory;
		this.nameFormatter = nameFormatter;
		this.existSvc = existSvc;
		this.alreadyCreatedL = alreadyCreatedL;
		this.datIdMap = datIdMap;
	}

	

	private boolean isManyToManyRelation(TypePair pair, DStructType dtype) {
		return DRuleHelper.isManyToManyRelation(pair, dtype);
	}

	public void alterGenerateAssocTable(StrCreator sc, TypePair pair, DStructType dtype) {
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
	
	//TODO delete this!
	public static String createAssocTableName(String tbl1, String tbl2) {
		String assocTableName = String.format("%s%sAssoc", tbl1, tbl2);
		return assocTableName;
	}
	
	public void generateAssocTable(StrCreator sc, TypePair xpair, DStructType dtype) {
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, xpair);
		String tbl1 = info.nearType.getName();
		String tbl2 = info.farType.getName();
		if (!(haveCreatedTable(tbl1) && haveCreatedTable(tbl2))) {
			return;
		}
		
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(dtype, xpair);

		String assocTableName = datIdMap.getAssocTblName(relinfo.getDatId());
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
		
		if (index > 0) {
			sc.o(",");
		}
		sc.o("UNIQUE(leftv,rightv)");
		sc.o(");");
		sc.nl();
		
	}

	private boolean haveCreatedTable(String tbl1) {
		for(TableInfo info: alreadyCreatedL) {
			if (info.tblName.equals(tbl1)) {
				return true;
			}
		}
		return false;
	}


	public String generateAlterField(String tableName, String fieldName, String deltaFlags, String constraintName, AssocInfo ainfo) {
		StrCreator sc = new StrCreator();
		String[] ar = deltaFlags.split(",");
		//  deltaFlags: +O,+U,+P,+S
		
		for(String delta: ar) {
			switch(delta) {
			case "+O":
				doAlterColumnOptional(sc, tableName, fieldName, true, ainfo);
				break;
			case "-O":
				doAlterColumnOptional(sc, tableName, fieldName, false, ainfo);
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

	public AssocInfo createAssocInfoIfIsManyToMany(String tableName, String fieldName) {
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

	private void doAlterColumnOptional(StrCreator sc, String tableName, String fieldName, boolean b, AssocInfo ainfo) {
		List<TableInfo> tblInfoL = new ArrayList<>();
		RelationInfo relinfo = DRuleHelper.findManyToManyRelation(ainfo.pair, ainfo.structType);
		existSvc.fillTableInfoIfNeeded(tblInfoL, relinfo);
		TableInfo tblinfo = tblInfoL.get(0);
		if (tblinfo.tbl2.equals(tableName)) {
			doAlterColumnPrefix(sc, tblinfo.assocTblName, "leftv");
		} else {
			doAlterColumnPrefix(sc, tblinfo.assocTblName, "rightv");
		}
		
		
		if (b) {
			sc.o(" DROP NOT NULL"); //set null 
		} else {
			sc.o(" SET NOT NULL"); 
		}
		sc.o(";\n");
	}

	private void doAlterColumnPrefix(StrCreator sc, String tableName, String fieldName) {
		sc.o("ALTER TABLE %s ALTER COLUMN %s", tblName(tableName), fieldName);
	}
	public String tblName(String tableName) {
		return nameFormatter.convert(tableName);
	}
	
}