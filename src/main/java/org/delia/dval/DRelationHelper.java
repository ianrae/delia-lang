package org.delia.dval;

import java.util.ArrayList;
import java.util.Collections;
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
	
}
