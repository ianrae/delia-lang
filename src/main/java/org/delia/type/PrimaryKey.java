package org.delia.type;

import java.util.List;

public class PrimaryKey {
	private TypePair keyPair;
	private List<TypePair> keyPairs;
	
	public PrimaryKey(TypePair keyPair) {
		this.keyPair = keyPair;
	}
	public PrimaryKey(List<TypePair> keyPairs) {
		this.keyPairs = keyPairs;
	}
	
	public boolean isMultiple() {
		return keyPairs != null;
	}
	public TypePair getKey() {
		return keyPair;
	}
	public List<TypePair> getKeys() {
		return keyPairs;
	}
	
	//only works for single key
	public String getFieldName() {
		return keyPair.name;
	}
	//only works for single key
	public DType getKeyType() {
		return keyPair.type;
	}
}
