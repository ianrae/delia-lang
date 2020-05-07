package org.delia.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.PrimaryKey;
import org.delia.type.PrimaryKeyValue;
import org.delia.type.TypePair;

public class DValueHelper {

//	public static String findUniqueField(DType inner) {
//		if (! inner.isStructShape()) {
//			return null;
//		}
//		TypePair pair = findPrimaryKeyFieldPair(inner); 
//		if (pair == null) {
//			return null;
//		} else {
//			return pair.name;
//		}
//	}
	
	public static DValue findPrimaryKeyValue(DValue dval) {
		if (! dval.getType().isStructShape()) {
			return null;
		}
		DStructType structType = (DStructType) dval.getType();
		PrimaryKey primaryKey = structType.getPrimaryKey();
		if (primaryKey == null) {
			return null;
		}
		
		PrimaryKeyValue pkv = new PrimaryKeyValue(dval);
		return pkv.getKeyValue();
//		
////		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
//		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
//		DValue inner = dval.asStruct().getField(keyPair.name);
//		return inner;
	}
	
	//TODO: support composite keys later
	public static TypePair findPrimaryKeyFieldPair(DType inner) {
		if (! inner.isStructShape()) {
			return null;
		}
		DStructType dtype = (DStructType) inner;
		PrimaryKey prikey = dtype.getPrimaryKey();
		return prikey == null ? null : prikey.getKey();
	}
	public static List<TypePair> findAllUniqueFieldPair(DType inner) {
		if (! inner.isStructShape()) {
			return null;
		}
		
		List<TypePair> resultL = new ArrayList<>();
		
		//first, look for primaryKey fields
		DStructType dtype = (DStructType) inner;
		for(TypePair pair: dtype.getAllFields()) {
			if (dtype.fieldIsPrimaryKey(pair.name)) {
				resultL.add(pair);
			}
		}
		
		//otherwise, look for unique fields (and possibly unique and optional)
		for(TypePair pair: dtype.getAllFields()) {
			if (dtype.fieldIsUnique(pair.name)) {
				resultL.add(pair);
			}
		}
		return resultL;
	}
	public static DType findFieldType(DType dtype, String fieldName) {
		if (! dtype.isStructShape()) {
			return null;
		}
		
		DStructType structType = (DStructType) dtype;
		for(TypePair pair: structType.getAllFields()) {
			if (pair.name.equals(fieldName)) {
				return pair.type;
			}
		}
		return null;
	}
	public static TypePair findField(DType dtype, String fieldName) {
		if (! dtype.isStructShape()) {
			return null;
		}
		
		DStructType structType = (DStructType) dtype;
		for(TypePair pair: structType.getAllFields()) {
			if (pair.name.equals(fieldName)) {
				return pair;
			}
		}
		return null;
	}
	public static boolean fieldExists(DType dtype, String fieldName) {
		if (! dtype.isStructShape()) {
			return false;
		}
		
		DStructType structType = (DStructType) dtype;
		for(TypePair pair: structType.getAllFields()) {
			if (pair.name.equals(fieldName)) {
				return true;
			}
		}
		return false;
	}
//	public static DType findUniqueFieldType(DType inner) {
//		if (! inner.isStructShape()) {
//			return null;
//		}
//		
//		TypePair pair = findPrimaryKeyFieldPair(inner); 
//		if (pair == null) {
//			return null;
//		} else {
//			return pair.type;
//		}
//	}
	
	public static DValue getFieldValue(DValue dval, String fieldName) {
		if (dval == null || ! dval.getType().isStructShape()) {
			return null;
		}
		
		return dval.asStruct().getField(fieldName);
	}
	
	/**
	 * Return a new DValue that combines dvalPartial into existingDVal
	 * @param dvalPartial  value contains some of the type's fields
	 * @param existingDVal DValue that we are merging into
	 * @return new value
	 */
	public static DValue mergeOne(DValue dvalPartial, DValue existingDVal) {
		return mergeOne(dvalPartial, existingDVal, null);
	}	
	
	/**
	 * Return a new DValue that combines dvalPartial into existingDVal
	 * @param dvalPartial  value contains some of the type's fields
	 * @param existingDVal DValue that we are merging into
	 * @param skipMap fields to skip. ie. to not merge
	 * @return new value
	 */
	public static DValue mergeOne(DValue dvalPartial, DValue existingDVal, Map<String,String> skipMap) {
	    Map<String,DValue> srcMap = new HashMap<>(dvalPartial.asMap()); //make a copy
	    Map<String,DValue> existingMap = existingDVal.asMap();
	    
	    Map<String,DValue> newMap = existingDVal.asMap();
	    
	    //merge or copy fields in existingDVal
	    for(String fieldName: existingMap.keySet()) {
	    	boolean skip = skipMap != null && skipMap.containsKey(fieldName);
	    	
			DValue inner;
			if (skip) {
				inner = existingMap.get(fieldName);
			} else if (srcMap.containsKey(fieldName)) {
				inner = srcMap.get(fieldName);
				srcMap.remove(fieldName);
			} else {
				inner = existingMap.get(fieldName);
			}
			DValue clone = cloneField(inner);
			newMap.put(fieldName, clone);
	    }
	    
	    //add fields in dvalPartial but not in existingDVal (ie. are null in existingDVal)
	    for(String fieldName: srcMap.keySet()) {
	    	boolean skip = skipMap != null && skipMap.containsKey(fieldName);
	    	if (skip) {
	    		continue;
	    	}
			DValue inner = srcMap.get(fieldName);
			DValue clone = cloneField(inner);
			newMap.put(fieldName, clone);
	    }
	    
	    DValue newVal = new DValueImpl(existingDVal.getType(), newMap);
		return newVal;
	}
	public static DValue cloneField(DValue inner) {
		if (inner == null) {
			return null;
		}
		DValueImpl clone;
		if (inner.getObject() instanceof DRelation) {
			DRelation rel = (DRelation) inner.getObject();
			DRelation copy = new DRelation(rel.getTypeName(), rel.getMultipleKeys());
			clone = new DValueImpl(inner.getType(), copy);
		} else {
			Object obj = inner.getObject();
			clone = new DValueImpl(inner.getType(), obj);
		}

		clone.setValidationState(inner.getValidationState());
		clone.setPersistenceId(inner.getPersistenceId());
		return clone;
	}
	public static String findMatchingRelation(DStructType dtype, DStructType relType) {
		//TODO: later support named relations
		DStructType structType = (DStructType) dtype;
		for(TypePair pair: structType.getAllFields()) {
			//hmm why doesn't this work. are 2 Customer type object???
//			if (pair.type.equals(relType)) {
//				return pair.name;
//			}
			if (pair.type.getName().equals(relType.getName())) {
				return pair.name;
			}
		}
		return null;
	}

	public static void throwIfFieldNotExist(String msgPrefix, String fieldName, DValue dval) {
		if (dval != null) {
			throwIfFieldNotExist(msgPrefix, fieldName, dval.getType());
		}
	}
	public static void throwIfFieldNotExist(String msgPrefix, String fieldName, DType dtype) {
		if (dtype == null || !dtype.isStructShape()) {
			return;
		}
		DStructType structType = (DStructType) dtype;
		if (! fieldExists(structType, fieldName)) {
			DeliaExceptionHelper.throwError("unknown-field", "%s - can't find field '%s' in type '%s'", msgPrefix, fieldName, dtype.getName());
		}
	}
	public static boolean typeHasSerialPrimaryKey(DType dtype) {
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dtype);
		if (pair == null) {
			return false;
		}
		DStructType structType = (DStructType) dtype;
		return structType.fieldIsSerial(pair.name);
	}
}
