package org.delia.type;

import org.delia.rule.DRule;

import java.util.List;

/**
 * A delia type.  In addition to the  * built-in types (int, boolean, etc),
 * any custom scalar or struct types will have a corresponding DType objects.
 * <p>
 * Types are registered in DTypeRegistry.
 *
 * @author Ian Rae
 */
public interface DType {

    boolean isShape(Shape target);

    boolean isScalarShape();

    boolean isNumericShape();

    boolean isRelationShape();

    boolean isBlobShape();

    Shape getShape();

    String getName(); //TODO probably remove all uses of this because of schema

    String getSchema();

    DTypeName getTypeName();

    DType getBaseType();

    EffectiveShape getEffectiveShape();

    boolean isAssignmentCompatible(DType type2);

    List<DRule> getRules();

    List<DRule> getRawRules();

    boolean hasRules();

    String getPackageName();

    void setPackageName(String packageName);

    String getCompleteName();

    int getBitIndex();

    void setBitIndex(int bitIndex);

    boolean isStructShape();


}

