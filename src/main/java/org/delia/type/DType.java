package org.delia.type;

import java.util.ArrayList;
import java.util.List;

import org.delia.rule.DRule;
import org.delia.util.NameUtils;

/**
 * A delia type.  In addition to the  * built-in types (int, boolean, etc),
 * any custom scalar or struct types will have a corresponding DType objects.
 * 
 * Types are registered in DTypeRegistry.
 * 
 * @author Ian Rae
 *
 */
public class DType implements DTypeInternal {
	private Shape shape;
	private String name;
	private String packageName;
	private String completeName;
	protected DType baseType; //can be null
	private List<DRule> rules = new ArrayList<>();
	private int bitIndex;
	public boolean invalidFlag; //used to verify type-replacement worked. TODO remove

	public DType(Shape shape, String name, DType baseType) {
		this.shape = shape;
		this.name = name;
		this.completeName = name;
		this.baseType = baseType;
	}
	@Override
	public void finishScalarInitialization(Shape shape, String typeName, DType baseType) {
		this.shape = shape;
		this.name = typeName;
		this.completeName = name;
		this.baseType = baseType;
	}

	public boolean isShape(Shape target) {
		return (target != null && target.equals(shape));
	}
	public boolean isScalarShape() {
		switch(shape) {
//		case LIST:
		case STRUCT:
//		case MAP:
			return false;
		default:
			return true;
		}
	}
    public boolean isNumericShape() {
        switch(shape) {
        case INTEGER:
        case LONG:
        case NUMBER:
            return true;
        default:
            return false;
        }
    }
    public boolean isRelationShape() {
        switch(shape) {
        case RELATION:
            return true;
        default:
            return false;
        }
    }

	public Shape getShape() {
		return shape;
	}

	public String getName() {
		return name;
	}

	public DType getBaseType() {
		return baseType;
	}
	
	/**
	 * Can type2 be used where this is expected.
	 * @param type2  derived class
	 * @return true if type2 is assignment compatible to this object
	 */
	public boolean isAssignmentCompatible(DType type2) {
		if (this == type2) {
			return true;
		}
		DType current = type2.getBaseType();
		
		//!!add runaway check
		while(current != null) {
			if (current == this) {
				return true;
			}
			current = current.getBaseType();
		}
		return false;
	}
	
	public List<DRule> getRules() {
		List<DRule> copy = new ArrayList<>(rules);
		return copy;
	}
	public List<DRule> getRawRules() {
		return rules;
	}
	public boolean hasRules() {
		return rules.size() > 0;
	}

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        this.completeName = NameUtils.completeName(packageName, name);
    }
    
    public String getCompleteName() {
        return completeName;
    }

    public int getBitIndex() {
        return bitIndex;
    }

    public void setBitIndex(int bitIndex) {
        this.bitIndex = bitIndex;
    }
    
    public boolean isStructShape() {
    	return Shape.STRUCT.equals(shape);
    }

    //helps see typename in debugger
	@Override
	public String toString() {
		return name;
	}
	
//	//FUTURE: make this into an 'internal' api with DStructTypeInternal interface
//	public void internalAdjustType(DType baseType) {
//		this.baseType = baseType;
//	}

	public void performTypeReplacement(TypeReplaceSpec spec) {
		if (baseType != null && spec.needsReplacement(this, baseType)) {
			baseType = spec.newType;
		}
		
		for(DRule rule: this.rules) {
			rule.performTypeReplacement(spec);
		}
	}
}

