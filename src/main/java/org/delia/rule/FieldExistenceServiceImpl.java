package org.delia.rule;

import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class FieldExistenceServiceImpl implements FieldExistenceService {

	private DTypeRegistry registry;
	private DType dtype;
	
	public FieldExistenceServiceImpl(DTypeRegistry registry, DType dtype) {
		this.registry = registry;
		this.dtype = dtype;
	}
	@Override
	public boolean existField(String fieldName) {
		DStructType structType = (DStructType) dtype;
		return structType.hasField(fieldName);
	}

}
