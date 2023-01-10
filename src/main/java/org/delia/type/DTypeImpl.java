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
public class DTypeImpl implements DType, DTypeInternal {
    private Shape shape;
    private EffectiveShape effectiveShape; //null unless shape is INTEGER
    protected String schema;
    private String name;
    private String packageName;
    private String completeName;
    protected DType baseType; //can be null
    private List<DRule> rules = new ArrayList<>();
    private int bitIndex;
    private DTypeName dtypeName; //set lazily

    public DTypeImpl(Shape shape, String schema, String name, DType baseType) {
        this.shape = shape;
        this.schema = schema;
        this.name = name;
        this.completeName = name;
        this.baseType = baseType;
        if (Shape.INTEGER.equals(shape)) {
            effectiveShape = EffectiveShape.EFFECTIVE_INT;
        }
    }

    @Override
    public boolean isShape(Shape target) {
        return (target != null && target.equals(shape));
    }
    @Override
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
    @Override
    public boolean isNumericShape() {
        switch(shape) {
            case INTEGER:
//        case LONG:
            case NUMBER:
                return true;
            default:
                return false;
        }
    }
    @Override
    public boolean isRelationShape() {
        switch(shape) {
            case RELATION:
                return true;
            default:
                return false;
        }
    }
    @Override
    public boolean isBlobShape() {
        return shape.equals(Shape.BLOB);
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public DTypeName getTypeName() {
        if (dtypeName == null) {
            dtypeName = new DTypeName(schema, name);
        }
        return dtypeName;
    }


    @Override
    public DType getBaseType() {
        return baseType;
    }

    @Override
    public EffectiveShape getEffectiveShape() { return effectiveShape; }

    /**
     * Can type2 be used where this is expected. eg type1 is vehicle. type2 is car. can say let xVehicle = xCar;
     * @param type2  derived class
     * @return true if type2 is assignment compatible to this object
     */
    @Override
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

        //hack. due to testing we may have created value with another registry
        //TODO: is this safe?
        DTypeName typeName = type2.getTypeName();
        if (typeName.equals(this.getTypeName())) {
            return true;
        }

        return false;
    }

    @Override
    public List<DRule> getRules() {
        List<DRule> copy = new ArrayList<>(rules);
        return copy;
    }
    @Override
    public List<DRule> getRawRules() {
        return rules;
    }
    @Override
    public boolean hasRules() {
        return rules.size() > 0;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
        this.completeName = NameUtils.completeName(packageName, name);
    }

    @Override
    public String getCompleteName() {
        return completeName;
    }

    @Override
    public int getBitIndex() {
        return bitIndex;
    }

    @Override
    public void setBitIndex(int bitIndex) {
        this.bitIndex = bitIndex;
    }

    @Override
    public boolean isStructShape() {
        return Shape.STRUCT.equals(shape);
    }

    //helps see typename in debugger
    @Override
    public String toString() {
        return name;
    }

    //-- from DTypeInternal --
    @Override
    public void finishScalarInitialization(Shape shape, String typeName, DType baseType) {
        this.shape = shape;
        this.name = typeName;
        this.completeName = name;
        this.baseType = baseType;
        if (Shape.INTEGER.equals(shape)) {
            effectiveShape = EffectiveShape.EFFECTIVE_INT;
        }
    }

    @Override
    public void setEffectiveShape(EffectiveShape effectiveShape) {
        this.effectiveShape = effectiveShape;
    }

}

