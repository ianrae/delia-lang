package org.delia.type;


public class TypePair {
    public String name;
    public DType type;
    
    public TypePair(String name, DType type) {
        this.name = name;
        this.type = type;
    }

	@Override
	public String toString() {
		return String.format("%s.%s", type, name);
	}
}
