package org.delia.sql;

import org.delia.core.FactoryService;
import org.delia.type.*;
import org.delia.util.DTypeNameUtil;
import org.delia.util.DValueHelper;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.HashMap;
import java.util.Map;

class ObjectPool {
	private Map<String,DValue> map = new HashMap<>(); //Customer.55 is key
	private ScalarValueBuilder scalarBuilder;
	private int nextSyntheticId = Integer.MIN_VALUE + 10; //want big range of negative numbers so won't class with actual pk values
	private Map<DValue,DValue> syntheticMap = new HashMap<>(); //<realvalue,synth>
	
	public ObjectPool(FactoryService factorySvc, DTypeRegistry registry) {
		scalarBuilder = factorySvc.createScalarValueBuilder(registry);
	}

	public void add(DValue dval, String relationName) {
		String key = makeKey(dval, relationName);
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

	private String makeKey(DValue dval, String relationName) {
		PrimaryKey primaryKey = DValueHelper.findPrimaryKeyField(dval.getType());
		//TODO we need a test for db column null (ie the primary key column null). Won't postgres throw an erorr in that case?
		if (primaryKey == null) { //either type has no PK or db column was null
			DValue synth;
			if (syntheticMap.containsKey(dval)) {
				synth = syntheticMap.get(dval);
			} else {
				synth = scalarBuilder.buildInt(nextSyntheticId++);
				syntheticMap.put(dval, synth);
			}
			return makeKey(dval.getType(), synth, relationName);
		}

		if (primaryKey.isMultiple()) {
			return makeCompositeKey(dval, dval.getType(), primaryKey, relationName);
		}
		DValue pkval = DValueHelper.findPrimaryKeyValue(dval); //single pk
		return makeKey(dval.getType(), pkval, relationName);
	}

	private String makeCompositeKey(DValue dval, DType dtype, PrimaryKey primaryKey, String relationName) {
		String srel = relationName == null ? "" : relationName;
		String tbl1 = DTypeNameUtil.formatLowerCaseTableName(dtype.getTypeName());

		StrCreator sc = new StrCreator();
		ListWalker<TypePair> walker1 = new ListWalker<>(primaryKey.getKeys());
		sc.addStr("{");
		while (walker1.hasNext()) {
			TypePair pair = walker1.next();
			DValue pkval = DValueHelper.getFieldValue(dval, pair.name);
			sc.o("%s", pkval.asString());
			walker1.addIfNotLast(sc, ",");
		}
		sc.addStr("}");

		String key = String.format("%s.%s:%s", tbl1, sc.toString(), srel);
		return key;
	}

	private String makeKey(DType dtype, DValue pkval, String relationName) {
		String srel = relationName == null ? "" : relationName;
		String tbl1 = DTypeNameUtil.formatLowerCaseTableName(dtype.getTypeName());
		String key = String.format("%s.%s:%s", tbl1, pkval.asString(), srel);
		return key;
	}

	//find dval (without a relationName)
	public boolean contains(DValue dval) {
		String key = makeKey(dval, null);
		DValue current = map.get(key);
		return current == dval;
	}

	//should always be a match
	public DValue findMatch(DType dtype, DValue pkval, String relationName) {
		String key = makeKey(dtype, pkval, relationName);
		return map.get(key);
	}
}