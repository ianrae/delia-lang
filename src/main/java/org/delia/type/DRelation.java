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
	private DTypeName typeName;

	public DRelation(DTypeName typeName, DValue key) {
		this.typeName = typeName;
		this.foreignKeyL.add(key);
	}
	public DRelation(DTypeName typeName, List<DValue> keyL) {
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
	public void clearKeys() {
		foreignKeyL.clear();
	}
	
	public DValue getForeignKey() {
//		if (foreignKeyL.size() == 0) {
//			return null; //TODO: is this ok?
//		}
		if (foreignKeyL.size() != 1) {
			throw new IllegalArgumentException(String.format("DRelation has unexpected size %d", foreignKeyL.size()));
		}
		return foreignKeyL.get(0);
	}

	public DTypeName getTypeName() {
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
	@Override
	public String toString() {
		String key = foreignKeyL.isEmpty() ? "" : foreignKeyL.get(0).asString();
		String s = String.format("%s %s (%d keys)", typeName, key, foreignKeyL.size());
		return s;
	}

    public void removeKeyIfPresent(DValue sourcePK) {
		if (foreignKeyL.isEmpty()) return;
		String srcStr = sourcePK.asString(); //TODO: use compare svc later
		for(DValue fk: foreignKeyL) {
			String fkStr = fk.asString();
			if (srcStr.equals(fkStr)) {
				foreignKeyL.remove(fk);
				return;
			}
		}
    }
}
