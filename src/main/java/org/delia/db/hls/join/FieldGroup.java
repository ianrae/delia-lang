package org.delia.db.hls.join;

/*
 * In HLS query, each field is part of a field group. The first set of the fields are the main group, then there are JOINs, additional
 * field groups are added, one per join.
 */
public class FieldGroup {
	public boolean isMainGroup;
	public JTElement el; //null if isMainGroup is true
	
	public FieldGroup(boolean isMain, JTElement el) {
		this.isMainGroup = isMain;
		this.el = el;
	}
	
	public String getUniqueKey() {
		if (isMainGroup) {
			return "_MAINGROUP_";
		} else {
			String key = String.format("%s.%s.%s", el.dtype.getName(), el.fieldName, el.fieldType.getName());
			return key;
		}
	}
	
	@Override
	public String toString() {
		return "FieldGroup [isMainGroup=" + isMainGroup + ", el=" + el + "]";
	}
}
