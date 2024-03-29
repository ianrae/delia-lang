package org.delia.dval;

import org.delia.dval.compare.DValueCompareService;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	public static DValue findInFetchedItems(DRelation drel, DValue fkval) {
		if (! drel.haveFetched()) {
			return null;
		}

		for(DValue dval: drel.getFetchedItems()) {
			DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
			if (pkval != null && fkval != null && isPKMatch(pkval, fkval)) {
				return dval;
			}
		}
		return null;
	}
	//TODO: rewrite to be faster
	private static boolean isPKMatch(DValue pk1, DValue pk2) {
		String s1 = pk1.asString();
		String s2 = pk2.asString();
		return s1.equals(s2);
	}

	public static void addIfNotExist(DRelation drel, DValue pkval, DValueCompareService compareSvc) {
		for(DValue fkval: drel.getMultipleKeys()) {
			if (compareSvc.compare(fkval, pkval) == 0) {
				return; //already in fks
			}
		}
		drel.addKey(pkval);
	}
}
