package org.delia.type;

/**
 * For internal use only.
 * @author Ian Rae
 *
 */
public interface DStructTypeInternal {
	void finishStructInitialization(DType baseType, OrderedMap orderedMap, PrimaryKey primaryKey);
}
