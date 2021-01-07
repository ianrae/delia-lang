package org.delia.db.newhls;

/**
 * Represents a fetch() or fks()
 * @author ian
 *
 */
public class FetchSpec {
	public RelationField structField;
	public boolean isFK; //if true then fks, else fetch

	@Override
	public String toString() {
		String s = String.format("%s:%b", structField.toString(), isFK);
		return s;
	}
}