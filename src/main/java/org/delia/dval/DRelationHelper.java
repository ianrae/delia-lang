package org.delia.dval;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DValue;

public class DRelationHelper {

	public static void addToFetchedItems(DRelation drel, List<DValue> subObjL) {
		List<DValue> fetchedL = drel.haveFetched() ? drel.getFetchedItems() : new ArrayList<>();
		fetchedL.addAll(subObjL);
		drel.setFetchedItems(fetchedL);
	}
	
	public static void addToFetchedItems(DRelation drel, DValue subObj) {
		List<DValue> fetchedL = drel.haveFetched() ? drel.getFetchedItems() : new ArrayList<>();
		fetchedL.add(subObj);
		drel.setFetchedItems(fetchedL);
	}
	
}
