package org.delia.compiler.ast;


public class StructFieldExp extends ExpBase {
	public String fieldName;
	public String typeName;
	public boolean isOptional;
	public boolean isPrimaryKey; //implies unique
	public boolean isUnique;
	public boolean isRelation;
	public boolean isOne;
	public boolean isMany;
	public boolean isParent;
	public boolean isSerial;
	public String relationName;

	public StructFieldExp(StructFieldPrefix structFieldPrefix, FieldQualifierExp qual1, FieldQualifierExp qual2, FieldQualifierExp qual3, FieldQualifierExp qual4) {
		super(structFieldPrefix.pos);
		this.fieldName = structFieldPrefix.nameExp.name();
		this.typeName = structFieldPrefix.exp.name();
		this.isRelation = structFieldPrefix.isRelation;
		this.relationName = structFieldPrefix.relationName;
		
		if (qual1 != null) {
			setFlags(qual1);
		}
		if (qual2 != null) {
			setFlags(qual2);
		}
		if (qual3 != null) {
			setFlags(qual3);
		}
		if (qual4 != null) {
			setFlags(qual4);
		}
	}
	
	private void setFlags(FieldQualifierExp qual) {
		if (qual.strValue().equals("optional")) {
			this.isOptional = true;
		} else if (qual.strValue().equals("unique")) {
			this.isUnique = true;
		} else if (qual.strValue().equals("one")) {
			this.isOne = true;
		} else if (qual.strValue().equals("many")) {
			this.isMany = true;
		} else if (qual.strValue().equals("primaryKey")) {
			this.isPrimaryKey = true;
		} else if (qual.strValue().equals("parent")) {
			this.isParent = true;
		} else if (qual.strValue().equals("serial")) {
			this.isSerial = true;
		}
	}

	public String getFieldName() {
		return fieldName;
	}
	
	@Override
	public String strValue() {
//		String ss = String.format("%s %s", nameExp.strValue(), formatValue(typeName));
		String srel = isRelation ? "relation " : "";
		String ss = String.format("%s%s %s", srel, fieldName, typeName);
		if (isOptional) {
			ss += " optional";
		}
		
		if (isPrimaryKey) {
			ss += " primaryKey";
		}
		if (isUnique) {
			ss += " unique";
		}
		if (isOne) {
			ss += " one";
		}
		if (isMany) {
			ss += " many";
		}
		if (isParent) {
			ss += " parent";
		}
		if (isSerial) {
			ss += " serial";
		}
		return ss;
	}

	@Override
	public String toString() {
		return strValue();
	}
}