package org.delia.valuebuilder;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class RelationValueBuilder extends DValueBuilder {
	private String foreignTypeName;
	private DTypeRegistry registry;
	private DType idType;

	public RelationValueBuilder(DType type, String foreignTypeName, DTypeRegistry registry) {
		if (!type.isShape(Shape.RELATION)) {
			addWrongTypeError("expecting relation");
			return;
		}
		this.type = type;
		this.foreignTypeName = foreignTypeName;
		this.registry = registry;
		
		DType idType = registry.getType(foreignTypeName);
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(idType);
		this.idType = pair.type;
	}

	public void buildFromString(String input) {
		if (input == null) {
			addNoDataError("no data");
			return;
		}
		
		if (idType.isShape(Shape.INTEGER)) {
			doInt(input);
		} else if (idType.isShape(Shape.LONG)) {
			doLong(input);
		} else if (idType.isShape(Shape.STRING)) {
			doString(input);
		} else {
			DeliaExceptionHelper.throwError("unsupported-relation-key-type", "Relations with primary key '%s' not supporte", idType.getShape().name());
		}
	}
	private void doInt(String input) {
		Integer nval = null;
		try {
			nval = Integer.parseInt(input);
			
			//use .valueOf to save memory. it re-uses the same instances for common values.
			nval = Integer.valueOf(nval.intValue());
			
			DType keyType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
			setNewDVal(keyType, nval);
		} catch (NumberFormatException e) {
			addParsingError(String.format("'%s' is not an integer", input), input);
		}
	}
	private void doLong(String input) {
		Long nval = null;
		try {
			nval = Long.parseLong(input);
			
			//use .valueOf to save memory. it re-uses the same instances for common values.
			nval = Long.valueOf(nval.longValue());
			
			DType keyType = registry.getType(BuiltInTypes.LONG_SHAPE);
			setNewDVal(keyType, nval);
		} catch (NumberFormatException e) {
			addParsingError(String.format("'%s' is not an long", input), input);
		}
	}
	private void doString(String input) {
		try {
			DType keyType = registry.getType(BuiltInTypes.STRING_SHAPE);
			setNewDVal(keyType, input);
		} catch (NumberFormatException e) {
			addParsingError(String.format("'%s' is not an string", input), input);
		}
	}

	private void setNewDVal(DType keyType, Object nval) {
		DValue keyVal  = new DValueImpl(keyType, nval);
		DRelation dvalrel = new DRelation(foreignTypeName, keyVal);
		this.newDVal = new DValueImpl(type, dvalrel);
	}

	public void buildFromList(List<DValue> elementL) {
		if (CollectionUtils.isEmpty(elementL)) {
			addNoDataError("no data");
			return;
		}

		DRelation dvalrel = new DRelation(foreignTypeName, elementL);
		this.newDVal = new DValueImpl(type, dvalrel);
	}

	@Override
	protected void onFinish() {
	}
}