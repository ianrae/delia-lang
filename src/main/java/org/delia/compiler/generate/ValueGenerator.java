package org.delia.compiler.generate;

import org.delia.type.DStructType;
import org.delia.type.DValue;

/**
 * Visitor used by DeliaGeneratorPhase. Used to convert a DValue into some other format, such as JSON.
 * @author Ian Rae
 *
 */
public interface ValueGenerator {

	void startStruct(ValuePlacement placement, DValue dval, DStructType structType, GeneratorContext genctx, int index);
	void endStruct(ValuePlacement placement, DValue dval, DStructType structType, GeneratorContext genctx);

	/**
	 * 
	 * @param fieldName never-null
	 * @param dval
	 * @param genctx
	 * @param index
	 */
	void structMemberValue(String fieldName, DValue dval, GeneratorContext genctx, int index);
	
	/**
	 * 
	 * @param varName   never-null
	 * @param dval
	 * @param genctx
	 */
	void scalarValue(String varName, DValue dval, GeneratorContext genctx);
	
	/**
	 * 
	 * @param genctx
	 */
	void endSubValue(GeneratorContext genctx);
	
	boolean finish();
}