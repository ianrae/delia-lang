package org.delia.db.schema;

public class SchemaType {
	public String line;
	public String typeName;
	public String action; //I/U/D
	public String field;
	public String newName;
	public int datId; //used for delete-field
	public int sizeof; //only for add (for alter action has old/new)
	
	public boolean isTblInsert() {
		return (action.equals("I") && field == null);
	}
	public boolean isFieldInsert() {
		return (action.equals("I") && field != null);
	}
	public boolean isFieldRename() {
		return (action.equals("R") && field != null);
	}
	public boolean isTblDelete() {
		return (action.equals("D") && field == null);
	}
	public boolean isTblRename() {
		return (action.equals("R") && field == null);
	}
	public boolean isFieldDelete() {
		return (action.equals("D") && field != null);
	}
	public boolean isFieldAlter() {
		return (action.equals("A") && field != null);
	}
	public boolean isFieldAlterType() {
		return (action.equals("AT") && field != null);
	}
	public boolean isFieldAlterSizeInt() {
		return (action.equals("ASN") && field != null);
	}
	public boolean isFieldAlterSizeString() {
		return (action.equals("ASZ") && field != null);
	}
	
	public String getSummary() {
		String ss = field == null ? "" : "." + field;
		return String.format("%s: %s%s  line: %s. ", action, typeName, ss, line);
		
	}
	
	@Override
	public String toString() {
		if (field == null) {
			return typeName;
		}
		return String.format("%s.%s", typeName, field);
	}

}