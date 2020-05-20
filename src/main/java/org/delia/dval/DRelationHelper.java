package org.delia.dval;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DValue;

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
	
}
