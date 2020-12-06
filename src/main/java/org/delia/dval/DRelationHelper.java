package org.delia.dval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;

public class DRelationHelper {

	public static void addToFetchedItems(DRelation drel, List<DValue> subObjL) {
		if (subObjL.isEmpty()) {
			return;
		}
		List<DValue> fetchedL = drel.haveFetched() ? drel.getFetchedItems() : new ArrayList<>();
		fetchedL.addAll(subObjL);
		drel.setFetchedItems(fetchedL);
	}
	
	public static void addToFetchedItems(DRelation drel, DValue subObj) {
		if (drel.haveFetched()) {
			if (drel.getFetchedItems().contains(subObj)) {
				return; //already in list
			}
		}
		
		List<DValue> fetchedL = drel.haveFetched() ? drel.getFetchedItems() : new ArrayList<>();
		fetchedL.add(subObj);
		drel.setFetchedItems(fetchedL);
	}

	public static void addToFetchedItemsFromRelation(DValue inner1, DRelation drel2) {
		if (! drel2.haveFetched()) {
			return;
		}
		DRelation drel1 = inner1.asRelation();
		addToFetchedItems(drel1, drel2.getFetchedItems());
	}

	public static void sortFKs(DRelation drel) {
		List<DValue> fks = drel.getMultipleKeys();
		if (fks.size() < 2) {
			return;
		}
		
		FKSorter sorter = new FKSorter(true);
		Collections.sort(fks, sorter);
	}

	public static boolean isRelation(DStructType structType, String fieldName) {
		TypePair pair = DValueHelper.findField(structType, fieldName);
		return pair != null && pair.type.isStructShape();
	}
	
	public static DValue createEmptyRelation(DStructType structType, String fieldName, DTypeRegistry registry) {
		DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
		TypePair pair = DValueHelper.findField(structType, fieldName);
		RelationValueBuilder builder = new RelationValueBuilder(relType, pair.type, registry);
		builder.buildEmptyRelation();
		boolean b = builder.finish();
		if (!b) {
			DeliaExceptionHelper.throwError("relation-create-failed", "Type '%s': Failed to create empty relation for field '%s'", structType.getName(), fieldName);
		}
		return builder.getDValue();
	}
	public static void addFK(DValue relValue, DValue entityValue) {
		DValue pkval = DValueHelper.findPrimaryKeyValue(entityValue);
		DRelation drel = relValue.asRelation();
		drel.addKey(pkval);
	}
}
