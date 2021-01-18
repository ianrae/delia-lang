package org.delia.db.newhls;

import org.delia.core.FactoryService;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.valuebuilder.ScalarValueBuilder;

public class ConversionHelper {

	private ScalarValueBuilder scalarBuilder; 

	public ConversionHelper(DTypeRegistry registry, FactoryService factorySvc) {
		this.scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}

	public DValue convertDValToActual(DType fieldType, DValue inner) {
		if (inner == null) return null;
		if (fieldType.isShape(Shape.DATE) && !inner.getType().isShape(Shape.DATE)) {
			String s = inner.asString();
			return scalarBuilder.buildDate(s);
		}
		
		return inner;
	}

	public DValue convertDValToActual(DType fieldType, String str) {
		if (str == null) return null;
		return scalarBuilder.buildDate(str);
	}
}
