package org.delia.db.schema;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;

public class SchemaFingerprintGenerator {
	
	public String createFingerprint(DTypeRegistry registry) {
		String s = "";
		
		//because of re-executing with forward decls some types are in registry.orderedList twice
		//use a map to ensure only do each type once
		Map<DType,DType> dupMap = new ConcurrentHashMap<>();
		
		List<DType> list = registry.getOrderedList();
		for(DType type: list) {
			if (isBuiltInType(type) || !type.isStructShape()) {
				continue;
			}
			
			type = registry.getType(type.getName()); //get the real one (avoid earlier ones form re-execution
			if (dupMap.containsKey(type)) {
				continue;
			}
			dupMap.put(type, type);
			
			s += String.format("%s", type.getName());
			String parent = type.getBaseType() == null ? "" : type.getBaseType().getName();
			s += String.format(":struct:%s{", parent);
			
			int i = 0;
			if (type.isStructShape()) {
				DStructType dtype = (DStructType) type;
				for(TypePair pair: dtype.getAllFields()) {
					if (i > 0) {
						s += ",";
					}
					s += genField(dtype, pair);
					i++;
				}
			}
			s += "}\n";
		}
		
		return s;
	}

	private String genField(DStructType dtype, TypePair pair) {
		String flags = "";
		if (dtype.fieldIsOptional(pair.name)) {
			flags += "O";
		}
		if (dtype.fieldIsPrimaryKey(pair.name)) {
			flags += "P";
		}
		if (dtype.fieldIsUnique(pair.name)) {
			flags += "U";
		}
		if (dtype.fieldIsSerial(pair.name)) {
			flags += "S";
		}
		
		String fldName = getTypeAsString(pair);
		String s = String.format("%s:%s:%s", pair.name, fldName, flags);
		return s;
	}
	private String getTypeAsString(TypePair pair) {
		try {
			BuiltInTypes fieldType = BuiltInTypes.valueOf(pair.type.getName());
			return BuiltInTypes.getDeliaTypeName(fieldType);
		} catch (Exception e) {
		}
		return pair.type.getName();
	}

	private boolean isBuiltInType(DType type) {
		for(BuiltInTypes bintype: BuiltInTypes.values()) {
			if (type.getName().equals(bintype.name())) {
				return true;
			}
		}
		return false;
	}
}