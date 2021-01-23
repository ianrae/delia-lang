package org.delia.hld;

public class FinalField {
	public StructField structField;
	public RelationField rf; //can be null if [].addr (ie. if no throughchain)
	
	@Override
	public String toString() {
		return structField.toString();
	}
	
	public boolean isScalarField()  {
		return !structField.fieldType.isStructShape();
	}
}
