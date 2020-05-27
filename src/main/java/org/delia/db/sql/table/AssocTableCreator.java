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
	
	public void generateAssocTable(StrCreator sc, TypePair xpair, DStructType dtype) {
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(dtype, xpair);
		String tbl1 = relinfo.nearType.getName();
		String tbl2 = relinfo.farType.getName();
		if (!(haveCreatedTable(tbl1) && haveCreatedTable(tbl2))) {
			return;
		}
		
		String assocTableName = datIdMap.getAssocTblName(relinfo.getDatId());
		TableInfo tblinfo = alreadyCreatedL.get(alreadyCreatedL.size() - 1);
		tblinfo.assocTblName = assocTableName;
		
		if (datIdMap.isLeftType(assocTableName, relinfo)) {
			tblinfo.tbl1 = tbl1;
			tblinfo.tbl2 = tbl2;
			tblinfo.fieldName = xpair.name;
			TypePair relpair = new TypePair(xpair.name, relinfo.nearType);
			doGenerateAssocTable(sc, assocTableName, xpair, relinfo.nearType, relinfo.farType, relpair);
		} else {
			tblinfo.tbl1 = tbl2;
			tblinfo.tbl2 = tbl1;
			tblinfo.fieldName = xpair.name;
			TypePair relpair = new TypePair(xpair.name, relinfo.nearType);
			doGenerateAssocTable(sc, assocTableName, xpair, relinfo.farType, relinfo.nearType, relpair);
		}
	}

	private void doGenerateAssocTable(StrCreator sc, String assocTableName, TypePair xpair, DStructType leftType, DStructType rightType, TypePair relpair) {
		sc.o("CREATE TABLE %s (", assocTableName);
		sc.nl();
		List<SqlElement> fieldL = new ArrayList<>();
		//RelationInfo relinfo = DRuleHelper.findManyToManyRelation(relpair, (DStructType) relpair.type);
		TypePair copy = new TypePair("leftv", leftType);
		FieldGen field = fieldgenFactory.createFieldGen(registry, copy, leftType, false);
		field.setIsAssocTblField(); //rightType.fieldIsOptional(otherSide.fieldName));
		fieldL.add(field);

		copy = new TypePair("rightv", rightType);
		field = fieldgenFactory.createFieldGen(registry, copy, rightType, false);
		field.setIsAssocTblField(); //leftType.fieldIsOptional(pair.name));
		fieldL.add(field);

		
		copy = new TypePair("leftv", leftType); //address
		ConstraintGen constraint = this.fieldgenFactory.generateFKConstraint(registry, copy, leftType, false);
		if (constraint != null) {
			fieldL.add(constraint);
		}

		copy = new TypePair("rightv", rightType);
		constraint = this.fieldgenFactory.generateFKConstraint(registry, copy, rightType, false);
		if (constraint != null) {
			fieldL.add(constraint);
		}
		
		int index = 0;
		for(SqlElement xfield: fieldL) {
			xfield.generateField(sc);
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
			
		return sc.toString();
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
		existSvc.fillTableInfoIfNeeded(tblInfoL, relinfo, datIdMap);
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