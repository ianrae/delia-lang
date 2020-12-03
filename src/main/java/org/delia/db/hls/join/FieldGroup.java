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
	
	@Override
	public String toString() {
		return "FieldGroup [isMainGroup=" + isMainGroup + ", el=" + el + "]";
	}
}
