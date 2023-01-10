package org.delia.typebuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.type.DType;
import org.delia.type.DTypeName;

public class PreTypeRegistry {
	private Map<DTypeName,MentionContext> mentionMap = new HashMap<>();
	private Map<DTypeName,String> definedMap = new HashMap<>();

//	public boolean existsType(String typeName) {
//		return mentionMap.containsKey(typeName);
//	}
	public DType getType(DTypeName typeName) {
		MentionContext mention = mentionMap.get(typeName);
		return mention == null ? null : mention.dtype;
	}
	public void addMentionedType(DType dtype, DTypeName parentTypeName) {
		MentionContext mention = new MentionContext();
		mention.dtype = dtype;
		mention.parentType = parentTypeName;
		mentionMap.put(dtype.getTypeName(), mention);
	}
	public void addTypeDefinition(DType dtype) {
		MentionContext mention = new MentionContext();
		mention.dtype = dtype;
		mention.parentType = null;
		mentionMap.put(dtype.getTypeName(), mention);
		definedMap.put(dtype.getTypeName(), "");
	}
	public int size() {
		return mentionMap.size();
	}
	public List<DTypeName> getUndefinedTypes() {
		List<DTypeName> list = new ArrayList<>();
		for(DTypeName typeName: mentionMap.keySet()) {
			if (!definedMap.containsKey(typeName)) {
				list.add(typeName);
			}
		}
		return list;
	}
	public Map<DTypeName, MentionContext> getMap() {
		return mentionMap;
	}
//	public List<DType> getAllDefinedTypes() {
//		List<DType> list = new ArrayList<>();
//		for(String typeName: mentionMap.keySet()) {
//			MentionContext mention = mentionMap.get(typeName);
//			list.add(mention.dtype);
//		}
//		return list;
//	}
}