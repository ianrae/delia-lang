package org.delia.hld.results;

import java.util.HashMap;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

class ObjectPool {
	private Map<String,DValue> map = new HashMap<>(); //Customer.55 is key
	private ScalarValueBuilder scalarBuilder;
	private int nextSyntheticId = Integer.MIN_VALUE + 10; //want big range of negative numbers so won't class with actual pk values
	private Map<DValue,DValue> syntheticMap = new HashMap<>(); //<realvalue,synth>
	
	public ObjectPool(FactoryService factorySvc, DTypeRegistry registry) {
		scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}

	public void add(DValue dval) {
		String key = makeKey(dval);
		if (map.containsKey(key)) {
			//harvest the fks
			DStructType dtype = (DStructType) dval.getType();
			for(TypePair pair: dtype.getAllFields()) {
				if (pair.type.isStructShape()) {
					DValue inner = dval.asStruct().getField(pair.name);
					if (inner != null) {
						DRelation drel = inner.asRelation();
						addForeignKeys(key, pair, drel);
					}
				}
			}
		} else {
			map.put(key, dval);
		}
	}

	private void addForeignKeys(String key, TypePair pair, DRelation drelSrc) {
		DValue current = map.get(key);
		DValue inner = current.asStruct().getField(pair.name);
		
		//when doing fks() and are multiple relations then we load each one at a time, and one might be missing
		if (inner != null) {
			DRelation drelTarget = inner.asRelation();
			
			for(DValue srcval: drelSrc.getMultipleKeys()) {
				if (drelTarget.findMatchingKey(srcval) == null) { //avoid duplicates
					drelTarget.addKey(srcval);
				}
			}
		}
	}

	private String makeKey(DValue dval) {
		DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
		if (pkval == null) { //either type has no PK or db column was null
			DValue synth;
			if (syntheticMap.containsKey(dval)) {
				synth = syntheticMap.get(dval);
			} else {
				synth = scalarBuilder.buildInt(nextSyntheticId++);
				syntheticMap.put(dval, synth);
			}
			return makeKey(dval.getType(), synth);
		}
		return makeKey(dval.getType(), pkval);
	}
	private String makeKey(DType dtype, DValue pkval) {
		String key = String.format("%s.%s", dtype.getName(), pkval.asString());
		return key;
	}

	public boolean contains(DValue dval) {
		String key = makeKey(dval);
		DValue current = map.get(key);
		return current == dval;
	}

	//should always be a match
	public DValue findMatch(DType dtype, DValue pkval) {
		String key = makeKey(dtype, pkval);
		return map.get(key);
	}
}