package org.delia.type;

/**
 * For internal use only.
 * @author Ian Rae
 *
 */
public interface DTypeInternal {
	void finishScalarInitialization(Shape shape, String typeName, DType baseType);
	void setEffectiveShape(EffectiveShape effectiveShape);
}
