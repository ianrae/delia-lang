package org.delia.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a relation between one object and another.
 * Can be one-to-one, one-to-many, or many-to-many.
 * 
 * @author Ian Rae
 *
 */
public class DRelation {
	private List<DValue> foreignKeyL = new ArrayList<>();
	private List<DValue> fetchL = null;
	private String typeName;

	public DRelation(String typeName, DValue key) {
		this.typeName = typeName;
		this.foreignKeyL.add(key);
	}
	public DRelation(String typeName, List<DValue> keyL) {
		this.typeName = typeName;
		this.foreignKeyL.addAll(keyL);
	}
	public void addKey(DValue additionalKey) {
		this.foreignKeyL.add(additionalKey);
	}
	
	public boolean isMultipleKey() {
		return foreignKeyL.size() > 1;
	}
	public List<DValue> getMultipleKeys() {
		return foreignKeyL;
	}
	public DValue findMatchingKey(DValue keyval) {
		String s2 = keyval.asString();
		for(DValue dval: foreignKeyL) {
			String s1 = dval.asString();
			if (s1 != null && s1.equals(s2)) {
				return dval;
			}
		}
		return null;
	}
	
	public DValue getForeignKey() {
		if (foreignKeyL.size() != 1) {
			throw new IllegalArgumentException(String.format("DRelation has unexpected size %d", foreignKeyL.size()));
		}
		return foreignKeyL.get(0);
	}

	public String getTypeName() {
		return typeName;
	}
	
	public void setFetchedItems(List<DValue> list) {
		fetchL = new ArrayList<>(list);
	}
	public List<DValue> getFetchedItems() {
		return fetchL;
	}
	
	public boolean haveFetched() {
		return fetchL != null;
	}
}
