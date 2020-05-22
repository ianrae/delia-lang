package org.delia.typebuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.type.DType;

public class PreTypeRegistry {
	private Map<String,MentionContext> mentionMap = new HashMap<>(); 
	private Map<String,String> definedMap = new HashMap<>(); 

	public boolean existsType(String typeName) {
		return mentionMap.containsKey(typeName);
	}
	public DType getType(String typeName) {
		MentionContext mention = mentionMap.get(typeName);
		return mention == null ? null : mention.dtype;
	}
	public void addMentionedType(DType dtype, String parentTypeName) {
		MentionContext mention = new MentionContext();
		mention.dtype = dtype;
		mention.parentType = parentTypeName;
		mentionMap.put(dtype.getName(), mention);
	}
	public void addTypeDefinition(DType dtype) {
		MentionContext mention = new MentionContext();
		mention.dtype = dtype;
		mention.parentType = null;
		mentionMap.put(dtype.getName(), mention);
		definedMap.put(dtype.getName(), "");
	}
	public int size() {
		return mentionMap.size();
	}
	public List<String> getUndefinedTypes() {
		List<String> list = new ArrayList<>();
		for(String typeName: mentionMap.keySet()) {
			if (!definedMap.containsKey(typeName)) {
				list.add(typeName);
			}
		}
		return list;
	}
	public Map<String, MentionContext> getMap() {
		return mentionMap;
	}
	public List<DType> getAllDefinedTypes() {
		List<DType> list = new ArrayList<>();
		for(String typeName: mentionMap.keySet()) {
			MentionContext mention = mentionMap.get(typeName);
			list.add(mention.dtype);
		}
		return list;
	}
}